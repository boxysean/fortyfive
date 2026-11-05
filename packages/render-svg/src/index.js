// SVG backend — serialises a core display list (packages/core/src/display.js) to the text of one
// self-contained .svg. Each primitive becomes one element meaning exactly what
// packages/render-canvas/src/replay.js paints for it; images are inlined as data URIs so the file
// stands alone. The output is a pure function of the list and options — no ids, dates or comments
// that vary — so two runs over the same frame are byte-identical and can be diffed or committed.
//
//   import { frameToSvg } from '@fortyfive/render-svg';
//   const svg = await frameToSvg(scene.frame(), { width: 1200, height: 600, flipY: true });

/**
 * Serialise `primitives` to SVG text, `width` × `height` output pixels. `flipY` mirrors the frame
 * top-to-bottom with a transform on the root group rather than by rewriting coordinates, so the
 * primitives serialise identically either way. `resolve(src)` → data URI string supplies image
 * hrefs (see assets.js); it is only called, and only awaited, when the frame has images.
 */
export async function frameToSvg(primitives, { width, height, flipY = false, resolve } = {}) {
  if (!Number.isInteger(width) || !Number.isInteger(height) || width <= 0 || height <= 0) {
    throw new Error(`render-svg: width and height must be positive integers, got ${width}×${height}`);
  }

  // Validate and resolve everything before writing anything, as drawFrame does: a bad primitive
  // or a failed fetch fails the whole frame rather than yielding a truncated file.
  const hrefs = new Map();
  for (const p of primitives) {
    if (!WRITERS.has(p.type)) throw new Error(`render-svg: unknown primitive type ${p.type}`);
    if (p.type !== 'image' || hrefs.has(p.src)) continue;
    if (!resolve) throw new Error(`render-svg: image ${p.src} but no resolve() given`);
    hrefs.set(p.src, resolve(p.src));
  }
  for (const [src, pending] of hrefs) {
    const href = await pending;
    if (typeof href !== 'string') throw new Error(`render-svg: resolve(${src}) must return a data URI string`);
    hrefs.set(src, href);
  }

  const filters = [];
  const body = primitives.map((p, i) => WRITERS.get(p.type)(p, { index: i, href: hrefs.get(p.src), filters }));

  const out = [
    '<?xml version="1.0" encoding="UTF-8"?>',
    `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">`,
  ];
  if (filters.length) out.push('  <defs>', ...filters, '  </defs>');
  out.push(flipY ? `  <g transform="translate(0 ${height}) scale(1 -1)">` : '  <g>');
  for (const element of body) out.push(`    ${element}`);
  out.push('  </g>', '</svg>');
  return out.join('\n') + '\n';
}

/**
 * The one way a number reaches the output. Three decimals is far below a pixel, and rounding here
 * keeps float noise (0.30000000000000004, 1e-7, -0) out of the file so equal frames stay equal
 * text. NaN or Infinity would serialise to something no renderer agrees on, so they throw.
 */
export function num(v) {
  if (!Number.isFinite(v)) throw new Error(`render-svg: not a finite number: ${v}`);
  const r = Math.round(v * 1000) / 1000;
  return String(r === 0 ? 0 : r);
}

/** Escape a value from the display list for use inside a double-quoted attribute. */
function esc(v) {
  return String(v).replace(/[&<>"]/g, (c) => ENTITIES[c]);
}

const ENTITIES = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' };

function writePath({ d, stroke, width, cap = 'butt' }) {
  if (!Array.isArray(d) || d.length < 2) throw new Error('render-svg: path needs at least two points');
  const points = d.map(([x, y], i) => `${i ? 'L' : 'M'} ${num(x)} ${num(y)}`).join(' ');
  // Canvas's default miter limit is 10 and SVG's is 4; without it, sharp corners are bevelled
  // here and mitred on canvas.
  return `<path d="${points}" fill="none" stroke="${esc(stroke)}" stroke-width="${num(width)}" stroke-linecap="${esc(cap)}" stroke-miterlimit="10"/>`;
}

function writeRect({ x, y, w, h, fill, radius }) {
  const rx = radius === undefined ? '' : ` rx="${num(radius)}"`;
  return `<rect x="${num(x)}" y="${num(y)}" width="${num(w)}" height="${num(h)}" fill="${esc(fill)}"${rx}/>`;
}

function writeImage({ x, y, w, h, opacity, monochrome }, { index, href, filters }) {
  // preserveAspectRatio="none": canvas drawImage(image, x, y, w, h) stretches to the box
  const alpha = (opacity ?? 1) === 1 ? '' : ` opacity="${num(opacity)}"`;
  const image = `<image x="${num(x)}" y="${num(y)}" width="${num(w)}" height="${num(h)}" href="${esc(href)}" preserveAspectRatio="none"${alpha}/>`;
  if (!monochrome) return image;

  // Recolour keeping alpha — replay's `source-in` fill: flood the colour, keep it where the image
  // is opaque. sRGB so the flood colour is the CSS colour, not a linearRGB reinterpretation of it.
  const id = `mono${index}`;
  filters.push(
    `    <filter id="${id}" color-interpolation-filters="sRGB">`,
    `      <feFlood flood-color="${esc(monochrome)}"/><feComposite in2="SourceAlpha" operator="in"/>`,
    '    </filter>',
  );
  return `<g filter="url(#${id})">${image}</g>`;
}

const WRITERS = new Map([['rect', writeRect], ['path', writePath], ['image', writeImage]]);
