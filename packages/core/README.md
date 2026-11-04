# @fortyfive/core

The engine: seeded PRNG, grid geometry, palettes, areas, movement strategies, lines, and `Scene`.

A `Scene` **does not draw**. It accumulates a **display list** — an ordered array of plain objects
describing what to paint — and hands it to a backend. That is what makes the engine portable: it
touches no browser API, so the same seed produces the same list in Node, in a worker, or in a
browser tab.

## The display list

`scene.frame()` returns the list in paint order (first painted first). The first primitive is
always the background rect. `scene.append(primitives)` pushes more on top — useful for a consumer
that wants to composite something onto a finished scene (a wordmark, a caption) without teaching
the scene about it.

```js
{ type: 'path',  d: [[x0, y0], [x1, y1], ...], stroke: '#FE6703', width: 10, cap: 'round' }
//   an open polyline, stroked at `width`; `cap` is 'butt' (default) | 'round' | 'square'

{ type: 'rect',  x, y, w, h, fill: '#ffffff', radius: 8 }
//   a filled rectangle; `radius` optional, rounds the corners when present

{ type: 'image', src: 'https://…/assets/dbt-labs.svg', x, y, w, h, opacity: 1, monochrome: '#fff' }
//   an image box; `src` is a URL the backend resolves, `opacity` defaults to 1, and
//   `monochrome` (a CSS colour) asks the backend to recolour the image, keeping its alpha
```

Coordinates are output pixels, y down. Any primitive may carry an optional `z` (depth). Nothing
reads it yet: 2D backends ignore it and paint in list order, and a future projector will flatten
3D primitives through a camera into the same 2D list before any backend sees them. That is the
whole 3D seam — no renderer ever has to know about 3D.

A backend is free to add rendering *options* of its own. `flipY: true` is one: the raster
backend mirrors the finished image, so anything painted afterwards stays upright.

## The portability rule

**Nothing under `packages/core/src` may reference `document`, `window`, `Image`, `fetch`,
`requestAnimationFrame`, a context or any other browser / canvas global.** Image loading is a
backend capability: a scene names an asset, the active backend resolves it.

`node test/portability.mjs` enforces it — it scans the source text (comments included) for those
names and exits non-zero on a hit. If core runs in bare Node with no DOM, the package is
portable; if it doesn't, it isn't.

## Determinism

`seed → display list` is bit-exact and pure JavaScript. `display list → pixels` is exact within a
backend but **not across backends**: font fallback, anti-aliasing and path rasterisation differ.
So a recorded hash always belongs to a named backend and version.

## Running

```sh
node --version                       # 22 or newer
node test/portability.mjs            # the portability guarantee
```
