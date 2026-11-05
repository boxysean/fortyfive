# @fortyfive/render-svg

A third backend for the display list: it writes one self-contained `.svg` instead of painting.
Each primitive becomes one element that means what `render-canvas/src/replay.js` paints for it,
and images are inlined as data URIs so the file needs nothing beside it. No runtime dependencies;
runs in Node and in a browser.

```js
import { frameToSvg } from '@fortyfive/render-svg';
import { createSvgAssetResolver } from '@fortyfive/render-svg/assets';

const svg = await frameToSvg(scene.frame(), { width: 1200, height: 600, flipY: true });

// a frame with images: every src becomes a data: URI inside the file
const withImages = await frameToSvg(frame, { width, height, resolve: createSvgAssetResolver() });
```

`frameToSvg(primitives, { width, height, flipY, resolve })` returns the SVG **text**. It is async only
because image `src`s are fetched and inlined; a frame without images awaits nothing, and the output is
byte-stable — the same frame always serialises to the same bytes (numbers are rounded to three
decimals, with no ids, dates or comments that vary between runs).

`createSvgAssetResolver(base)` returns `resolve(src)` → `data:<mime>;base64,…`, fetching each URL once.
`base` defaults to `document.baseURI` in a browser and the working directory in Node, and a `file:`
asset in Node is read from disk (Node's `fetch` has no `file:` scheme) — so a repo-relative `src`
works in both worlds.

Where SVG's defaults differ from canvas's, the output pins canvas's: `stroke-miterlimit="10"` on paths,
`preserveAspectRatio="none"` on images (drawImage stretches to the box), and `monochrome` is an
`feFlood` + `feComposite operator="in"` filter in sRGB — replay's `source-in` fill.

## flipY

`flipY` is a **transform**, not flipped coordinates: the root group becomes
`<g transform="translate(0 HEIGHT) scale(1 -1)">` and every primitive serialises exactly as it would
upright. The canvas backends mirror the finished raster for the same reason — the flip is applied to
the whole frame, after the primitives, and never leaks into them.

## The gate

The test rasterises each scene's SVG with `@resvg/resvg-js` **2.6.2** and measures it against the
browser-made reference PNGs in `render-canvas/test/fixtures/` (1200 × 600, seed 42). resvg is a
**devDependency only**: the shipped backend has no dependencies and pulls in no native code — it
writes text, and turning that text into pixels is the test's business, not the package's.

| scene | mean abs per-channel | 99th pct | max | pixels off by > 8 |
|---|---|---|---|---|
| Simple | 1.1824 | 24 | 45 | 12.7294% |
| Wanderer | 1.3277 | 23 | 36 | 15.6485% |
| Neon | 1.2171 | 16 | 21 | 10.0540% |
| Labyrinth | 1.0485 | 17 | 42 | 9.4864% |
| Banner | 1.5197 | 15 | 21 | 7.2875% |

These are not zero because they compare two anti-aliasing engines: resvg's coverage anti-aliasing
against Chrome's (Skia's) analytic anti-aliasing. The serialisation itself agrees exactly —
horizontal and vertical strokes are pixel-identical in both, a 1px diagonal differs by 1, a 5px
diagonal by at most 16, and shifting one raster by ±0.25 or ±0.5 px in any direction makes every
comparison worse, so there is no half-pixel offset hiding in the numbers. 98–100% of the pixels past
8 sit on an edge of the fixture; the difference is on edges, not in the geometry, and this art is
dense in diagonals.

The thresholds in `test/svg-backend.test.mjs` are therefore a band, not zeros: mean ≤ 2.5, p99 ≤ 32,
no pixel past 64, and ≤ 20% of pixels past 8. No legitimate anti-aliasing difference reaches 64; a
wrong colour does.

What the gate catches: a shifted, missing, mis-sized or wrong-coloured primitive at frame scale. One
test proves it — shifting every point of every path in Neon one pixel right (the shape a broken
coordinate space produces) measures mean 28.08 with 39.0% of pixels past 8, an order of magnitude
outside the band. What it does not catch on its own: a single vertex nudged by one pixel moves a few
dozen pixels, which is below the frame mean by construction (mean 0.0013, p99 0). That is why the
perturbation test asserts on the per-pixel **max** (194) and the count past 8 (27 px) instead — it
proves the measurement sees a local change, not that the frame gate fails it.

```sh
pnpm --filter @fortyfive/render-svg test     # resvg against the fixtures, no Chrome
```

## The file

Neon's frame (13,859 primitives) serialises to one 1.87 MB `.svg` (1,873,252 bytes), one element per
line. Images are inlined as `data:` URIs, so a frame that uses them is still a single file that
stands alone — Neon happens to have none.
