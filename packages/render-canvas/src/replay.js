// Replay — paints a core display list (packages/core/src/display.js) onto a canvas 2D context.

/**
 * Paint `primitives` onto ctx in list order. `resolve(src)` → { image } supplies decoded images
 * (see assets.js); every image is resolved before anything is painted, so the paint itself is
 * synchronous and a frame is never left half-drawn by a failed fetch.
 */
export async function drawFrame(ctx, primitives, { resolve } = {}) {
  const images = new Map();
  for (const p of primitives) {
    if (!PAINTERS.has(p.type)) throw new Error(`drawFrame: unknown primitive type ${p.type}`);
    if (p.type !== 'image' || images.has(p.src)) continue;
    if (!resolve) throw new Error(`drawFrame: image ${p.src} but no resolve() given`);
    images.set(p.src, resolve(p.src));
  }
  for (const [src, pending] of images) images.set(src, (await pending).image);

  for (const p of primitives) PAINTERS.get(p.type)(ctx, p, images.get(p.src));
}

/**
 * Paint `primitives` mirrored top-to-bottom, replacing whatever ctx held. The list is replayed
 * into a scratch canvas the size of ctx.canvas and that finished raster is drawn flipped, so
 * anti-aliasing is pixel-identical to the unflipped paint (a geometric flip of the list is not).
 */
export async function drawMirroredFrame(ctx, primitives, { resolve } = {}) {
  const { width, height } = ctx.canvas;
  const scratch = document.createElement('canvas');
  scratch.width = width;
  scratch.height = height;
  await drawFrame(scratch.getContext('2d'), primitives, { resolve });

  ctx.save();
  ctx.setTransform(1, 0, 0, 1, 0, 0);
  ctx.clearRect(0, 0, width, height);
  ctx.translate(0, height);
  ctx.scale(1, -1);
  ctx.drawImage(scratch, 0, 0);
  ctx.restore();
}

function drawRect(ctx, { x, y, w, h, fill, radius }) {
  ctx.fillStyle = fill;
  if (radius === undefined) {
    ctx.fillRect(x, y, w, h);
  } else {
    ctx.beginPath();
    ctx.roundRect(x, y, w, h, radius);
    ctx.fill();
  }
}

function drawPath(ctx, { d, stroke, width, cap = 'butt' }) {
  ctx.strokeStyle = stroke;
  ctx.lineWidth = width;
  ctx.lineCap = cap;
  ctx.beginPath();
  ctx.moveTo(d[0][0], d[0][1]);
  for (let i = 1; i < d.length; i++) ctx.lineTo(d[i][0], d[i][1]);
  ctx.stroke();
}

function drawImage(ctx, { x, y, w, h, opacity, monochrome }, image) {
  let source = image;
  if (monochrome) {
    // Recolour: paint the image into a scratch canvas, then keep its alpha with `source-in`
    const scratch = document.createElement('canvas');
    scratch.width = Math.ceil(w);
    scratch.height = Math.ceil(h);
    const sctx = scratch.getContext('2d');
    sctx.drawImage(image, 0, 0, w, h);
    sctx.globalCompositeOperation = 'source-in';
    sctx.fillStyle = monochrome;
    sctx.fillRect(0, 0, scratch.width, scratch.height);
    source = scratch;
  }

  ctx.save();
  ctx.globalAlpha = opacity ?? 1;
  if (source === image) ctx.drawImage(image, x, y, w, h);
  else ctx.drawImage(source, x, y);
  ctx.restore();
}

const PAINTERS = new Map([['rect', drawRect], ['path', drawPath], ['image', drawImage]]);
