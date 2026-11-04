// Logo compositor — draws vendor wordmarks (SVG) onto a finished banner canvas.
//
// Each logo is { name, file, x, y, height, chip?, monochrome?, opacity? }. `file` is relative
// to banners/; x, y and height are logical 1584 × 396 pixels and are multiplied by `scale`.

const BANNERS = new URL('../banners/', import.meta.url);
const MIN_HEIGHT = 20; // logical px
const CHIP_RADIUS = 8;
const CHIP_PADDING = 10;

const cache = new Map();

/** Fetch an SVG and load it into an Image via a data: URL. Resolves once decoded. */
export function loadSvgImage(url) {
  const key = String(url);
  if (!cache.has(key)) {
    cache.set(key, (async () => {
      const res = await fetch(key);
      if (!res.ok) throw new Error(`fetch ${key}: HTTP ${res.status}`);
      const text = await res.text();
      const img = new Image();
      img.src = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(text)}`;
      await img.decode();
      let w = img.naturalWidth;
      let h = img.naturalHeight;
      if (!w || !h) {
        // Fall back to the root element's width/height attributes
        const svg = text.match(/<svg\b[^>]*>/)?.[0] || '';
        w = parseFloat(svg.match(/\swidth="([\d.]+)/)?.[1]);
        h = parseFloat(svg.match(/\sheight="([\d.]+)/)?.[1]);
      }
      if (!w || !h) throw new Error(`${key}: cannot determine SVG aspect ratio`);
      return { img, aspect: w / h };
    })());
    cache.get(key).catch(() => cache.delete(key));
  }
  return cache.get(key);
}

function monochromeColour(logo) {
  if (typeof logo.monochrome === 'string') return logo.monochrome;
  return logo.chip || '#fff';
}

/** Composite `logos` onto ctx. Loads every image first, then draws synchronously. */
export async function drawLogos(ctx, logos, scale = 1) {
  if (!logos || logos.length === 0) return;
  for (const logo of logos) {
    if (!(logo.height * scale >= MIN_HEIGHT * scale)) {
      throw new Error(`logo ${logo.name}: height ${logo.height} is under the ${MIN_HEIGHT} px minimum`);
    }
  }
  const loaded = await Promise.all(logos.map((l) => loadSvgImage(new URL(l.file, BANNERS))));

  logos.forEach((logo, i) => {
    const { img, aspect } = loaded[i];
    const x = logo.x * scale;
    const y = logo.y * scale;
    const h = logo.height * scale;
    const w = h * aspect;

    ctx.save();
    if (logo.chip) {
      const pad = CHIP_PADDING * scale;
      ctx.fillStyle = logo.chip;
      ctx.beginPath();
      ctx.roundRect(x - pad, y - pad, w + 2 * pad, h + 2 * pad, CHIP_RADIUS * scale);
      ctx.fill();
    }

    let source = img;
    if (logo.monochrome) {
      const scratch = document.createElement('canvas');
      scratch.width = Math.ceil(w);
      scratch.height = Math.ceil(h);
      const sctx = scratch.getContext('2d');
      sctx.drawImage(img, 0, 0, w, h);
      sctx.globalCompositeOperation = 'source-in';
      sctx.fillStyle = monochromeColour(logo);
      sctx.fillRect(0, 0, scratch.width, scratch.height);
      source = scratch;
    }

    ctx.globalAlpha = logo.opacity ?? 1;
    if (source === img) ctx.drawImage(img, x, y, w, h);
    else ctx.drawImage(source, x, y);
    ctx.restore();
  });
}
