// Asset resolver — turns an image primitive's `src` into a decoded Image plus its aspect ratio.

/**
 * Returns an async `resolve(src)` → { image, aspect }. Relative `src` values resolve against
 * `base`. Each SVG is fetched once (cached by absolute URL), loaded into an Image via a data:
 * URL and decoded before it is returned.
 */
export function createAssetResolver(base = document.baseURI) {
  const cache = new Map();

  return function resolve(src) {
    const key = String(new URL(src, base));
    if (!cache.has(key)) {
      cache.set(key, (async () => {
        const res = await fetch(key);
        if (!res.ok) throw new Error(`fetch ${key}: HTTP ${res.status}`);
        const text = await res.text();
        const image = new Image();
        image.src = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(text)}`;
        await image.decode();
        let w = image.naturalWidth;
        let h = image.naturalHeight;
        if (!w || !h) {
          // Fall back to the root element's width/height attributes
          const svg = text.match(/<svg\b[^>]*>/)?.[0] || '';
          w = parseFloat(svg.match(/\swidth="([\d.]+)/)?.[1]);
          h = parseFloat(svg.match(/\sheight="([\d.]+)/)?.[1]);
        }
        if (!w || !h) throw new Error(`${key}: cannot determine SVG aspect ratio`);
        return { image, aspect: w / h };
      })());
      cache.get(key).catch(() => cache.delete(key));
    }
    return cache.get(key);
  };
}
