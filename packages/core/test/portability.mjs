// Portability test: nothing under packages/core/src may reference a browser/DOM global, so the
// engine runs unchanged in Node, a worker, or behind any rendering backend. Scans raw file text
// (comments included) with word-boundary matching: `canvas` is a hit, `canvasWidth` is not.
// Run with: node test/portability.mjs   (from packages/core)

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const SRC = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../src');
const FORBIDDEN = [
  'document', 'window', 'Image', 'fetch', 'requestAnimationFrame', 'cancelAnimationFrame',
  'getContext', 'canvas', 'Canvas', 'ImageData', 'Path2D', 'OffscreenCanvas', 'devicePixelRatio',
  'navigator', 'localStorage', 'XMLHttpRequest', 'WebSocket',
];
const PATTERN = new RegExp(`\\b(${FORBIDDEN.join('|')})\\b`, 'g');

/** Every forbidden name in `text`, as { line, name }. */
function scan(text) {
  const hits = [];
  text.split('\n').forEach((line, i) => {
    for (const m of line.matchAll(PATTERN)) hits.push({ line: i + 1, name: m[1] });
  });
  return hits;
}

// Prove the detector in the same run: it must flag a DOM call and pass clean code
const dirty = scan("const c = document.createElement('canvas');");
const clean = scan('const canvasWidth = config.canvasWidth; const images = [];');
if (!dirty.some((h) => h.name === 'document') || clean.length !== 0) {
  console.log(`FAIL: detector self-check (dirty sample: ${JSON.stringify(dirty)}, clean sample: ${JSON.stringify(clean)})`);
  process.exit(1);
}

function jsFiles(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((e) => {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) return jsFiles(p);
    return e.isFile() && e.name.endsWith('.js') ? [p] : [];
  });
}

const files = jsFiles(SRC);
let hits = 0;
for (const file of files) {
  for (const h of scan(fs.readFileSync(file, 'utf8'))) {
    hits++;
    console.log(`${path.relative(process.cwd(), file)}:${h.line}: ${h.name}`);
  }
}

if (files.length === 0) {
  console.log(`FAIL: no .js files found under ${SRC}`);
  process.exit(1);
}
if (hits > 0) {
  console.log(`FAIL: ${hits} DOM reference(s) in ${files.length} files`);
  process.exit(1);
}
console.log(`PASS: ${files.length} files scanned, no DOM references`);
