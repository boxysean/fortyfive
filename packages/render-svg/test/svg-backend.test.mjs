// SVG backend test — the same frame, serialised by frameToSvg and rasterised with @resvg/resvg-js,
// measured against the PNGs the browser path produced (packages/render-canvas/test/fixtures, made by
// tools/render-reference.mjs with headless Chrome).
//
// resvg and Skia anti-alias differently, so unlike the node backend the numbers are not zero. The
// gate is the same measured difference: mean absolute per-channel difference, 99th-percentile
// per-pixel difference, and the share of pixels differing by more than 8. Anti-aliasing lives on
// line edges; anything bigger than that is a structural difference (shifted geometry, missing
// primitive, wrong colour, misplaced image) and fails the gate.
//
// Run with: node --test packages/render-svg/test/

import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { createCanvas, loadImage } from '@napi-rs/canvas';
import { Resvg } from '@resvg/resvg-js';

import { Scene } from '../../core/src/scene.js';
import { frameToSvg } from '../src/index.js';
import { createSvgAssetResolver } from '../src/assets.js';
import { scenes } from '../../../apps/demo/scenes/index.js';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const FIXTURES = path.join(HERE, '../../render-canvas/test/fixtures');
const MANIFEST = JSON.parse(fs.readFileSync(path.join(FIXTURES, 'manifest.json'), 'utf8'));

// Fixed by the fixtures: 1200 × 600, seed 42, stepped at 8× speed until every line is done
const WIDTH = 1200;
const HEIGHT = 600;
const SEED = 42;
const SPEED = 8;
const MAX_FRAMES = 20000;

// The gate. Unlike slice 3 this compares two different anti-aliasing engines, so the numbers are a
// band, not zeros. Measured across the five scenes: mean 1.05–1.52, p99 16–24, max 21–45, 7.3–15.7%
// of pixels past 8 — all of it on line edges. A whole-frame one-pixel shift measures mean 14–45 with
// 30–40% past 8, so these limits have an order of magnitude of headroom over a structural break.
const MAX_MEAN = 2.5;
const MAX_P99 = 32;
const MAX_PIXEL = 64;        // no legitimate AA difference reaches this; a wrong colour does
const MAX_SHARE_OVER8 = 0.20;

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

/** Rasterise SVG text with resvg at its own width; no system fonts, so the host cannot leak in. */
function rasterise(svg, width = WIDTH) {
  return new Resvg(svg, { fitTo: { mode: 'width', value: width }, font: { loadSystemFonts: false } }).render().asPng();
}

/** The RGBA of pixel (x, y). */
function pixel(image, x, y) {
  const i = (y * image.width + x) * 4;
  return Array.from(image.data.subarray(i, i + 4));
}

const fixtures = new Map(MANIFEST.renders.map((r) => [r.scene, r]));
const neon = () => displayList(scenes.find((s) => s.name === 'Neon')).primitives;

test('every demo scene rasterises to its fixture', async (t) => {
  assert.equal(fixtures.size, scenes.length, `manifest lists ${fixtures.size} renders, ${scenes.length} scenes exist`);
  for (const config of scenes) {
    const fixture = fixtures.get(config.name);
    assert.ok(fixture, `${config.name}: no fixture in manifest.json`);
    const { primitives, frames } = displayList(config);
    // Same seed, same code: the display list must still be the one the fixtures were painted from
    assert.equal(primitives.length, fixture.primitives, `${config.name}: display list changed (${primitives.length} vs ${fixture.primitives} primitives)`);
    assert.equal(frames, fixture.frames, `${config.name}: frame count changed (${frames} vs ${fixture.frames})`);

    const svg = await frameToSvg(primitives, { width: WIDTH, height: HEIGHT });
    const got = await decode(rasterise(svg));
    const want = await decode(fs.readFileSync(path.join(FIXTURES, fixture.file)));
    const diff = compare(got, want);

    t.diagnostic(`${config.name}: mean ${diff.mean.toFixed(4)}, p99 ${diff.p99}, max ${diff.maxDiff}, `
      + `over-8 ${(diff.shareOver8 * 100).toFixed(4)}%, ${diff.pixels} px`);

    assert.ok(diff.mean <= MAX_MEAN, `${config.name}: mean absolute difference ${diff.mean.toFixed(4)} > ${MAX_MEAN}`);
    assert.ok(diff.p99 <= MAX_P99, `${config.name}: 99th-percentile difference ${diff.p99} > ${MAX_P99}`);
    assert.ok(diff.maxDiff <= MAX_PIXEL, `${config.name}: a pixel differs by ${diff.maxDiff} > ${MAX_PIXEL}`);
    assert.ok(diff.shareOver8 <= MAX_SHARE_OVER8,
      `${config.name}: ${(diff.shareOver8 * 100).toFixed(3)}% of pixels differ by more than 8 (> ${MAX_SHARE_OVER8 * 100}%)`);
  }
});

test('the comparison would catch a one-pixel change', async (t) => {
  // The non-zero numbers above must be a real reading, not a comparison that cannot fail. First,
  // resvg is deterministic: the same SVG rasterised twice measures exactly zero.
  const primitives = neon();
  const svg = await frameToSvg(primitives, { width: WIDTH, height: HEIGHT });
  const upright = await decode(rasterise(svg));
  const again = compare(upright, await decode(rasterise(svg)));
  assert.equal(again.mean, 0, `the same SVG rasterised twice measured mean ${again.mean}`);
  assert.equal(again.maxDiff, 0, `the same SVG rasterised twice measured max ${again.maxDiff}`);
  assert.equal(again.shareOver8, 0, 'the same SVG rasterised twice has pixels past 8');

  // Then nudge one vertex of the longest path by exactly one pixel
  let longest = -1;
  primitives.forEach((p, i) => {
    if (p.type === 'path' && (longest < 0 || p.d.length > primitives[longest].d.length)) longest = i;
  });
  assert.ok(longest >= 0, 'Neon has no path to perturb');
  const nudged = primitives.slice();
  const d = primitives[longest].d.map(([x, y]) => [x, y]);
  d[1][0] += 1;
  nudged[longest] = { ...primitives[longest], d };

  const diff = compare(upright, await decode(rasterise(await frameToSvg(nudged, { width: WIDTH, height: HEIGHT }))));
  t.diagnostic(`one vertex +1px: mean ${diff.mean.toFixed(6)}, p99 ${diff.p99}, max ${diff.maxDiff}, `
    + `over-8 ${Math.round(diff.shareOver8 * diff.pixels)} px`);
  // A one-pixel nudge of one vertex moves a few pixels around two segments: the frame mean and the
  // 99th percentile barely notice and the global gate passes it. What catches it is the per-pixel
  // reading — the maximum and the count of pixels past 8 — which the scene gate also asserts on
  // (as a share). So this proves the measurement sees a local change, not that the gate fails it.
  assert.ok(diff.maxDiff > 8, `a one-pixel nudge measured max ${diff.maxDiff}`);
  assert.ok(diff.shareOver8 > 0, 'a one-pixel nudge moved no pixel past 8');
});

test('the gate would fail a frame that moved a pixel', async (t) => {
  // The band above is wide because two AA engines differ on every edge. It must still be far
  // narrower than a structural break: shift every point of every path one pixel right — the shape a
  // broken coordinate space produces — and the same comparison against the same fixture must fail.
  const primitives = neon();
  const shifted = primitives.map((p) => (p.type === 'path' ? { ...p, d: p.d.map(([x, y]) => [x + 1, y]) } : p));
  const svg = await frameToSvg(shifted, { width: WIDTH, height: HEIGHT });
  const got = await decode(rasterise(svg));
  const want = await decode(fs.readFileSync(path.join(FIXTURES, fixtures.get('Neon').file)));
  const diff = compare(got, want);
  t.diagnostic(`every path +1px: mean ${diff.mean.toFixed(4)}, p99 ${diff.p99}, max ${diff.maxDiff}, `
    + `over-8 ${(diff.shareOver8 * 100).toFixed(4)}%`);
  assert.ok(diff.mean > MAX_MEAN, `a whole-frame shift measured mean ${diff.mean.toFixed(4)} ≤ ${MAX_MEAN}`);
  assert.ok(diff.shareOver8 > MAX_SHARE_OVER8,
    `a whole-frame shift put only ${(diff.shareOver8 * 100).toFixed(3)}% of pixels past 8`);
});

test('the same frame twice is byte-identical', async () => {
  for (const config of scenes) {
    const { primitives } = displayList(config);
    const a = await frameToSvg(primitives, { width: WIDTH, height: HEIGHT });
    const b = await frameToSvg(primitives, { width: WIDTH, height: HEIGHT });
    assert.equal(a, b, `${config.name}: two serialisations of the same frame differ`);
  }
});

test('images are embedded and the file stands alone', async (t) => {
  const assets = fs.mkdtempSync(path.join(os.tmpdir(), 'render-svg-assets-'));
  const alone = fs.mkdtempSync(path.join(os.tmpdir(), 'render-svg-alone-'));
  t.after(() => {
    fs.rmSync(assets, { recursive: true, force: true });
    fs.rmSync(alone, { recursive: true, force: true });
  });

  // A 40 × 40 SVG: a red 20 × 20 square in a transparent 10px margin
  fs.writeFileSync(path.join(assets, 'square.svg'),
    '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect x="10" y="10" width="20" height="20" fill="#ff0000"/></svg>');
  // A 20 × 10 solid PNG
  const solid = createCanvas(20, 10);
  const sctx = solid.getContext('2d');
  sctx.fillStyle = '#2080c0';
  sctx.fillRect(0, 0, 20, 10);
  fs.writeFileSync(path.join(assets, 'solid.png'), solid.toBuffer('image/png'));

  const W = 200;
  const H = 100;
  const frame = [
    { type: 'rect', x: 0, y: 0, w: W, h: H, fill: '#204060' },
    { type: 'image', src: 'square.svg', x: 20, y: 20, w: 40, h: 40, monochrome: '#fff' },
    { type: 'image', src: 'solid.png', x: 100, y: 20, w: 80, h: 40, opacity: 0.5 },
  ];
  const svg = await frameToSvg(frame, { width: W, height: H, resolve: createSvgAssetResolver(pathToFileURL(assets) + '/') });

  const hrefs = [...svg.matchAll(/<image\b[^>]*\bhref="([^"]*)"/g)].map((m) => m[1]);
  assert.equal(hrefs.length, 2, `expected two <image> elements, found ${hrefs.length}`);
  for (const href of hrefs) assert.ok(href.startsWith('data:'), `an <image> href is not a data URI: ${href.slice(0, 40)}`);
  assert.equal((svg.match(/<image\b/g) ?? []).length, hrefs.length, 'an <image> has no href');
  assert.doesNotMatch(svg, /href="(?!data:)/, 'a href that is not a data URI');

  // Only the .svg, alone in a fresh directory: there is nothing beside it it could load
  const file = path.join(alone, 'frame.svg');
  fs.writeFileSync(file, svg);
  assert.deepEqual(fs.readdirSync(alone), ['frame.svg']);
  const image = await decode(rasterise(fs.readFileSync(file, 'utf8'), W));

  // Monochrome: the square's centre is the mono colour; its transparent margin shows the background
  assert.deepEqual(pixel(image, 40, 40), [255, 255, 255, 255], 'monochrome image centre is not #fff');
  assert.deepEqual(pixel(image, 24, 24), [0x20, 0x40, 0x60, 255], 'background does not show through the transparent margin');
  // Opacity 0.5: the PNG's colour half over the background, ±1 for rounding
  const half = [0x20, 0x80, 0xc0].map((c, i) => (c + [0x20, 0x40, 0x60][i]) / 2);
  const centre = pixel(image, 140, 40);
  for (let c = 0; c < 3; c++) {
    assert.ok(Math.abs(centre[c] - half[c]) <= 1, `opacity 0.5 image centre ${centre} is not ${half} over the background`);
  }
});

test('flipY is a transform, not flipped coordinates', async (t) => {
  const primitives = neon();
  const upright = await frameToSvg(primitives, { width: WIDTH, height: HEIGHT });
  const flipped = await frameToSvg(primitives, { width: WIDTH, height: HEIGHT, flipY: true });

  // Every element serialises identically; only the root group's opening tag differs
  const body = (svg) => svg.split('\n').filter((line) => !/^\s*<g[ >]/.test(line)).join('\n');
  assert.equal(body(flipped), body(upright), 'flipY changed a primitive, not just the root group');
  assert.match(flipped, /\n {2}<g transform="translate\(0 600\) scale\(1 -1\)">\n/);
  assert.match(upright, /\n {2}<g>\n/);

  const a = await decode(rasterise(upright));
  const b = await decode(rasterise(flipped));
  const mirror = { width: a.width, height: a.height, data: new Uint8ClampedArray(a.data.length) };
  const row = a.width * 4;
  for (let y = 0; y < a.height; y++) mirror.data.set(a.data.subarray(y * row, (y + 1) * row), (a.height - 1 - y) * row);
  const diff = compare(mirror, b);
  t.diagnostic(`flipped vs mirrored upright: mean ${diff.mean.toFixed(6)}, p99 ${diff.p99}, max ${diff.maxDiff}`);
  assert.ok(diff.mean <= 0.1, `flipY raster is not the mirror of the upright raster (mean ${diff.mean.toFixed(4)})`);
});

test('the writer refuses what it cannot render', async () => {
  const size = { width: 10, height: 10 };
  await assert.rejects(() => frameToSvg([{ type: 'triangle' }], size), /unknown primitive type/);
  await assert.rejects(() => frameToSvg([], { width: 0, height: 10 }), /positive integers/);
  await assert.rejects(() => frameToSvg([], { width: 10, height: -1 }), /positive integers/);
  await assert.rejects(() => frameToSvg([{ type: 'path', d: [[0, 0]], stroke: '#fff', width: 1 }], size), /at least two points/);
  const image = { type: 'image', src: 'a.png', x: 0, y: 0, w: 1, h: 1 };
  await assert.rejects(() => frameToSvg([image], size), /no resolve\(\) given/);
  await assert.rejects(() => frameToSvg([image], { ...size, resolve: async () => 42 }), /must return a data URI string/);
});
