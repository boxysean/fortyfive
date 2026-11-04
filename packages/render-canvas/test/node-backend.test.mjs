// Node backend test — the same frame, rendered with @napi-rs/canvas, measured against PNGs the
// browser path produced (test/fixtures, made by tools/render-reference.mjs with headless Chrome).
//
// Two rasterisers will not agree byte for byte, so the gate is a measured difference: mean absolute
// per-channel difference, 99th-percentile per-pixel difference, and the share of pixels differing by
// more than 8. Anti-aliasing lives on line edges; anything bigger than that is a structural
// difference (shifted geometry, missing primitive, wrong colour) and fails the gate.
//
// Run with: node --test packages/render-canvas/test/

import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createCanvas, loadImage } from '@napi-rs/canvas';

import { Scene } from '../../core/src/scene.js';
import { renderToCanvas, renderToPng } from '../src/node.js';
import { scenes } from '../../../apps/demo/scenes/index.js';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const FIXTURES = path.join(HERE, 'fixtures');
const MANIFEST = JSON.parse(fs.readFileSync(path.join(FIXTURES, 'manifest.json'), 'utf8'));

// Fixed by the fixtures: 1200 × 600, seed 42, stepped at 8× speed until every line is done
const WIDTH = 1200;
const HEIGHT = 600;
const SEED = 42;
const SPEED = 8;
const MAX_FRAMES = 20000;

// The gate (see the task card): small AA differences pass, structural ones cannot
const MAX_MEAN = 1.0;
const MAX_P99 = 8;

/** Build the scene for `config` from a fixed seed and step it to completion, like the demo does. */
const lists = new Map();
function displayList(config) {
  if (!lists.has(config.name)) {
    const scene = new Scene({ ...config, seed: SEED }, WIDTH, HEIGHT);
    let frames = 0;
    while (!scene.finished) {
      if (++frames > MAX_FRAMES) throw new Error(`${config.name}: did not finish in ${MAX_FRAMES} frames`);
      scene.draw(SPEED);
    }
    lists.set(config.name, { primitives: scene.frame(), frames });
  }
  return lists.get(config.name);
}

/** Decode PNG bytes to { data (RGBA), width, height }. */
async function decode(png) {
  const image = await loadImage(png);
  const canvas = createCanvas(image.width, image.height);
  const ctx = canvas.getContext('2d');
  ctx.drawImage(image, 0, 0);
  return { data: ctx.getImageData(0, 0, image.width, image.height).data, width: image.width, height: image.height };
}

/** Compare two same-size images channel by channel and report how far apart they are. */
function compare(a, b) {
  assert.equal(a.width, b.width, 'width differs');
  assert.equal(a.height, b.height, 'height differs');
  const channels = [];
  const perPixel = [];
  let over8 = 0;
  let maxDiff = 0;
  for (let i = 0; i < a.data.length; i += 4) {
    let pixelMax = 0;
    // Alpha is opaque on both paths (every scene paints a full-frame background)
    for (let c = 0; c < 3; c++) {
      const d = Math.abs(a.data[i + c] - b.data[i + c]);
      channels.push(d);
      if (d > pixelMax) pixelMax = d;
    }
    perPixel.push(pixelMax);
    if (pixelMax > 8) over8++;
    if (pixelMax > maxDiff) maxDiff = pixelMax;
  }
  channels.sort((x, y) => x - y);
  perPixel.sort((x, y) => x - y);
  const sum = channels.reduce((acc, d) => acc + d, 0);
  const mean = sum / channels.length;
  const p99 = perPixel[Math.min(perPixel.length - 1, Math.floor(perPixel.length * 0.99))];
  return { mean, p99, maxDiff, shareOver8: over8 / perPixel.length, pixels: perPixel.length };
}


const fixtures = new Map(MANIFEST.renders.map((r) => [r.scene, r]));

test('every demo scene finishes deterministically from seed 42', () => {
  assert.equal(fixtures.size, scenes.length, `manifest lists ${fixtures.size} renders, ${scenes.length} scenes exist`);
  for (const config of scenes) {
    const fixture = fixtures.get(config.name);
    assert.ok(fixture, `${config.name}: no fixture in manifest.json`);
    const { primitives, frames } = displayList(config);
    // Same seed, same code: the display list must still be the one the fixtures were painted from
    assert.equal(primitives.length, fixture.primitives, `${config.name}: display list changed (${primitives.length} vs ${fixture.primitives} primitives)`);
    assert.equal(frames, fixture.frames, `${config.name}: frame count changed (${frames} vs ${fixture.frames})`);
  }
});

test('the node backend renders the same picture as the browser', async (t) => {
  for (const config of scenes) {
    const fixture = fixtures.get(config.name);
    const { primitives } = displayList(config);
    const png = await renderToPng(primitives, { width: WIDTH, height: HEIGHT });
    const got = await decode(png);
    const want = await decode(fs.readFileSync(path.join(FIXTURES, fixture.file)));
    const diff = compare(got, want);


    t.diagnostic(`${config.name}: mean ${diff.mean.toFixed(4)}, p99 ${diff.p99}, max ${diff.maxDiff}, `
      + `over-8 ${(diff.shareOver8 * 100).toFixed(4)}%, ${diff.pixels} px`);

    assert.ok(diff.mean <= MAX_MEAN, `${config.name}: mean absolute difference ${diff.mean.toFixed(4)} > ${MAX_MEAN}`);
    assert.ok(diff.p99 <= MAX_P99, `${config.name}: 99th-percentile difference ${diff.p99} > ${MAX_P99}`);
    // A structural difference (a whole line shifted or missing) moves far more than 1% of pixels
    assert.ok(diff.shareOver8 <= 0.01, `${config.name}: ${(diff.shareOver8 * 100).toFixed(3)}% of pixels differ by more than 8`);
  }
});

test('rendering the same frame twice is byte-identical', async () => {
  for (const config of scenes) {
    const { primitives } = displayList(config);
    const a = await renderToPng(primitives, { width: WIDTH, height: HEIGHT });
    const b = await renderToPng(primitives, { width: WIDTH, height: HEIGHT });
    assert.ok(a.equals(b), `${config.name}: two renders of the same frame differ`);
  }
});

test('flipY mirrors the finished raster, not the coordinates', async () => {
  const config = scenes.find((s) => s.name === 'Neon');
  const { primitives } = displayList(config);
  const upright = await decode(await renderToPng(primitives, { width: WIDTH, height: HEIGHT }));
  const flipped = await decode(await renderToPng(primitives, { width: WIDTH, height: HEIGHT, flipY: true }));

  let differing = 0;
  for (let y = 0; y < upright.height; y++) {
    const top = (y * upright.width) * 4;
    const bottom = ((upright.height - 1 - y) * upright.width) * 4;
    for (let x = 0; x < upright.width * 4; x++) {
      if (upright.data[top + x] !== flipped.data[bottom + x]) differing++;
    }
  }
  const share = differing / upright.data.length;
  assert.equal(differing, 0, `${differing} channel values differ between the mirror of the upright frame and flipY (${(share * 100).toFixed(3)}%)`);
});

test('the node backend refuses a frame it cannot paint', async () => {
  await assert.rejects(() => renderToPng([{ type: 'triangle' }], { width: 10, height: 10 }), /unknown primitive type/);
  await assert.rejects(() => renderToPng([], { width: 0, height: 10 }), /positive integers/);
});

test('the comparison would catch a structural difference', async () => {
  // A shifted picture is what a broken backend looks like, so prove the measurement sees it:
  // the zeros the gate reports must be a real reading, not a comparison that cannot fail.
  const config = scenes.find((s) => s.name === 'Neon');
  const { primitives } = displayList(config);
  const upright = await decode(await renderToPng(primitives, { width: WIDTH, height: HEIGHT }));
  const shifted = { width: upright.width, height: upright.height, data: new Uint8ClampedArray(upright.data) };
  for (let y = 0; y < shifted.height; y++) {
    const row = y * shifted.width * 4;
    shifted.data.copyWithin(row, row + 4, row + shifted.width * 4);
  }
  const diff = compare(upright, shifted);
  assert.ok(diff.mean > MAX_MEAN, `a one-pixel shift measured mean ${diff.mean.toFixed(3)}, the gate is ${MAX_MEAN}`);
  assert.ok(diff.shareOver8 > 0.01, `a one-pixel shift moved only ${(diff.shareOver8 * 100).toFixed(2)}% of pixels past 8`);
});

