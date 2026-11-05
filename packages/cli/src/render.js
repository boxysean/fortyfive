// Build a scene from a config and a seed, step it to completion, and encode the finished frame.
// The build loop is the one the backends' tests use, so a CLI render at seed N is the same frame —
// and for PNG the same bytes — as the node backend produces at seed N.

import { createHash } from 'node:crypto';
import { Scene } from '@fortyfive/core/scene';
import { renderToPng } from '@fortyfive/render-canvas/node';
import { frameToSvg } from '@fortyfive/render-svg';
import { createSvgAssetResolver } from '@fortyfive/render-svg/assets';

export const SPEED = 8;
export const MAX_FRAMES = 20000;
export const DEFAULT_WIDTH = 1200;
export const DEFAULT_HEIGHT = 600;
export const FORMATS = ['png', 'svg'];

/** Hex sha256 of the exact bytes written; a string is hashed as its UTF-8 encoding, as written. */
export function sha256(data) {
  return createHash('sha256').update(data).digest('hex');
}

/**
 * Render `config` at `seed` to `width` × `height`. Returns a PNG Buffer or SVG text. `base` is where
 * relative image srcs resolve — the caller's working directory, like every other Node resolver here.
 */
export async function renderScene({ config, seed, width, height, format, base = 'file://' + process.cwd() + '/' }) {
  if (!FORMATS.includes(format)) throw new Error(`unknown format ${format}`);

  const scene = new Scene({ ...config, seed }, width, height);
  let frames = 0;
  while (!scene.finished) {
    if (++frames > MAX_FRAMES) throw new Error(`${config.name}: did not finish in ${MAX_FRAMES} frames`);
    scene.draw(SPEED);
  }
  const primitives = scene.frame();

  // No flipY: the output must match the node backend byte for byte at the same seed
  if (format === 'png') return renderToPng(primitives, { width, height });
  return frameToSvg(primitives, { width, height, resolve: createSvgAssetResolver(base) });
}
