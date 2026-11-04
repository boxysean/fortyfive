# @fortyfive/render-canvas

Two backends for one display list: a browser `<canvas>` and, for Node, `@napi-rs/canvas` (Skia).
Both replay the list through the **same** code — `src/replay.js` — so the only thing that differs
between them is the canvas implementation underneath.

```js
// browser (apps/demo)
import { Engine, drawFrame } from '@fortyfive/render-canvas';

// Node — no browser anywhere
import { renderToPng } from '@fortyfive/render-canvas/node';
const png = await renderToPng(scene.frame(), { width: 1200, height: 600, flipY: true });
```

`renderToPng(primitives, { width, height, flipY, resolve })` returns PNG bytes.
`renderToCanvas(...)` returns the canvas instead, if you want to keep painting. `flipY` mirrors the
**finished raster** top-to-bottom, not the coordinates, so the mirrored frame's anti-aliasing is
pixel-identical to the upright one.

Images: the browser resolves `src` with `createAssetResolver()` (fetch + `<img>`); Node resolves it
with `createNodeAssetResolver()` (fetch + `loadImage`), which is the default for `renderToPng`.

The Node entry is a separate module (`./node`) so the browser bundle never sees the native module.
`src/index.js` stays browser-only.

## The node-vs-browser gate

`@napi-rs/canvas` and a browser will not produce byte-identical **PNGs** — different encoders,
different compression. The gate is therefore a measured difference in **pixels**:

| scene | mean abs per-channel | 99th pct | max | pixels off by > 8 |
|---|---|---|---|---|
| Simple | 0.000000 | 0 | 0 | 0.0000% |
| Wanderer | 0.000000 | 0 | 0 | 0.0000% |
| Neon | 0.000000 | 0 | 0 | 0.0000% |
| Labyrinth | 0.000000 | 0 | 0 | 0.0000% |
| Banner | 0.000000 | 0 | 0 | 0.0000% |

Both rasterisers are Skia, so for these scenes the pixels agree exactly; only the PNG bytes differ.
The thresholds in `test/node-backend.test.mjs` are mean ≤ 1.0 and p99 ≤ 8, with ≤ 1% of pixels
allowed past 8 — room for anti-aliasing if a future Chrome or Skia build moves, and far too tight
for a shifted line or a missing primitive. One test deliberately shifts a frame by one pixel to
prove the measurement still catches that.

Reference PNGs live in `test/fixtures/` (1200 × 600, seed 42) and were produced by the **browser**
path, so CI never needs Chrome:

```sh
node tools/render-reference.mjs                          # headless Chrome → test/fixtures/ (Chrome: $CHROME)
node --test packages/render-canvas/test/node-backend.test.mjs   # the comparison, no Chrome
```

Regenerate the fixtures only when the art or the replay deliberately changes — the test also asserts
each scene still produces the frame count and primitive count recorded in `test/fixtures/manifest.json`,
so a fixture that no longer matches the scene fails loudly instead of quietly relaxing the gate.
