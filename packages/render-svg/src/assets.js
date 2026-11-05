// Asset resolver — turns an image primitive's `src` into a data URI, so the .svg that frameToSvg
// writes carries its images inside it and renders the same wherever the file is opened. Uses only
// fetch and btoa, which a browser and Node both have; no Buffer, so it runs in either. Node's fetch
// refuses file: URLs, so there (and only there) a file: asset is read from disk instead, through a
// dynamic import of node:fs that a browser bundle never sees.

const EXTENSION_TYPES = {
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
};

/**
 * Returns an async `resolve(src)` → `data:<mime>;base64,…`. Relative `src` values resolve against
 * `base`: the document in a browser, the working directory in Node. Each asset is fetched once,
 * cached by absolute URL, so a frame that draws the same logo many times fetches it once.
 */
export function createSvgAssetResolver(base = defaultBase()) {
  const cache = new Map();

  return function resolve(src) {
    const key = String(new URL(src, base));
    if (!cache.has(key)) {
      cache.set(key, (async () => {
        if (key.startsWith('file:') && isNode()) {
          return `data:${mimeType(key, null)};base64,${toBase64(await readLocal(key))}`;
        }
        const res = await fetch(key);
        if (!res.ok) throw new Error(`fetch ${key}: HTTP ${res.status}`);
        const mime = mimeType(key, res.headers.get('content-type'));
        return `data:${mime};base64,${toBase64(new Uint8Array(await res.arrayBuffer()))}`;
      })());
      cache.get(key).catch(() => cache.delete(key));
    }
    return cache.get(key);
  };
}

/** Node, whose fetch has no file: scheme. A browser loads file: URLs (if at all) through fetch. */
function isNode() {
  return typeof process !== 'undefined' && typeof process.versions?.node === 'string';
}

/** Read a file: URL from disk. Imported on demand so the module has no static Node import. */
async function readLocal(url) {
  const { readFile } = await import('node:fs/promises');
  const { fileURLToPath } = await import('node:url');
  const file = fileURLToPath(url);
  let buffer;
  try {
    buffer = await readFile(file);
  } catch (err) {
    throw new Error(`read ${file}: ${err.code ?? err.message}`);
  }
  return new Uint8Array(buffer.buffer, buffer.byteOffset, buffer.byteLength);
}

function defaultBase() {
  if (typeof document !== 'undefined' && document.baseURI) return document.baseURI;
  if (typeof process !== 'undefined' && typeof process.cwd === 'function') return 'file://' + process.cwd() + '/';
  throw new Error('createSvgAssetResolver: no base given, and neither a document nor a process.cwd() to default to');
}

/**
 * The server's word wins; the extension is the fallback for responses that carry none. An unknown
 * type throws rather than guessing, since a wrong mime type makes the image silently not render.
 */
function mimeType(url, header) {
  const declared = header?.split(';')[0].trim().toLowerCase();
  if (declared) return declared;
  const path = new URL(url).pathname.toLowerCase();
  const ext = path.slice(path.lastIndexOf('.'));
  const type = path.includes('.') ? EXTENSION_TYPES[ext] : undefined;
  if (!type) throw new Error(`${url}: no Content-Type and no known image extension`);
  return type;
}

/** btoa takes a binary string; build it in chunks so a large image does not overflow the stack. */
function toBase64(bytes) {
  let binary = '';
  for (let i = 0; i < bytes.length; i += 0x8000) {
    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + 0x8000));
  }
  return btoa(binary);
}
