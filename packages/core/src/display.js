// Display list — the primitives a Scene emits, in paint order (first is painted first).
//
// A backend (packages/render-* or anything else) replays the list; core only records
// what to paint, never how. Coordinates are output pixels, y down.
//
//   { type: 'path',  d: [[x0, y0], [x1, y1], ...], stroke: '#FE6703', width: 10, cap: 'round' }
//       An open polyline, stroked with colour `stroke` at line width `width`.
//       `cap` is 'butt' | 'round' | 'square', default 'butt'.
//
//   { type: 'rect',  x, y, w, h, fill: '#ffffff', radius: 8 }
//       A filled rectangle. `radius` optional: when present the corners are rounded.
//
//   { type: 'image', src: 'https://…/assets/dbt-labs.svg', x, y, w, h, opacity: 1, monochrome: '#fff' }
//       An image drawn into the box (x, y, w, h). `src` is a URL the backend resolves.
//       `opacity` optional, default 1. `monochrome` optional: a CSS colour; when present the
//       backend recolours every opaque pixel of the image to it, keeping its alpha.
//
// Any primitive may carry an optional `z` (depth, for a future 3D backend). Nothing reads it
// yet; 2D backends ignore it and paint in list order.
//
// Options a backend accepts alongside a list:
//
//   { flipY: true }
//       Mirror top-to-bottom. A raster backend paints the list, then mirrors the finished
//       raster - not the coordinates, so anti-aliasing matches the unflipped paint exactly.
//       Anything painted afterwards stays upright. SVG (slice 4) will use a transform.
