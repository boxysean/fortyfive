// Node backend — replays a core display list onto @napi-rs/canvas and encodes a PNG, with no
// browser anywhere. The paint is the same code the browser path runs (replay.js); only the canvas
// implementation differs, so the two rasterisers agree everywhere except anti-aliasing.
//
//   import { renderToPng } from '@fortyfive/render-canvas/node';
//   const png = await renderToPng(scene.frame(), { width: 1200, height: 600, flipY: true });

import { createCanvas, loadImage } from '@napi-rs/canvas';
import { drawFrame, drawMirroredFrame } from './replay.js';

/**
 * Resolves an image primitive's `src` to a decoded @napi-rs/canvas Image, mirroring
 * assets.js (the browser resolver): relative srcs resolve against `base`, each URL is fetched once
 * and cached, and SVGs are decoded at their authored size. Returns `resolve(src)` → { image, aspect }.
 */
export function createNodeAssetResolver(base = 'file://' + process.cwd() + '/') {
  const cache = new Map();

  return function resolve(src) {
    const key = String(new URL(src, base));
    if (!cache.has(key)) {
      cache.set(key, (async () => {
        const res = await fetch(key);
        if (!res.ok) throw new Error(`fetch ${key}: HTTP ${res.status}`);
        const image = await loadImage(Buffer.from(await res.arrayBuffer()));
        if (!image.width || !image.height) throw new Error(`${key}: cannot determine aspect ratio`);
        return { image, aspect: image.width / image.height };
      })());
      cache.get(key).catch(() => cache.delete(key));
    }
    return cache.get(key);
  };
}

/**
 * Replay `primitives` into a fresh canvas of `width` × `height` and return it. `flipY` mirrors the
 * finished raster top-to-bottom (not the coordinates), exactly as the browser backend does.
 */
export async function renderToCanvas(primitives, { width, height, flipY = false, resolve, createCanvas: allocate = createCanvas } = {}) {
  if (!Number.isInteger(width) || !Number.isInteger(height) || width <= 0 || height <= 0) {
    throw new Error(`renderToCanvas: width and height must be positive integers, got ${width}×${height}`);
  }
  const canvas = allocate(width, height);
  const ctx = canvas.getContext('2d');
  const replay = { resolve: resolve ?? createNodeAssetResolver(), createCanvas: allocate };
  if (flipY) await drawMirroredFrame(ctx, primitives, replay);
  else await drawFrame(ctx, primitives, replay);
  return canvas;
}

/** Replay `primitives` and return the frame as PNG bytes. */
export async function renderToPng(primitives, options) {
  const canvas = await renderToCanvas(primitives, options);
  return canvas.toBuffer('image/png');
}
