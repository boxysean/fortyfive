// Render the demo scenes to reference PNGs with headless Chrome, through the browser replay path.
//
//   node tools/render-reference.mjs                       write test/fixtures/*.png
//   node tools/render-reference.mjs --out /some/dir        write somewhere else
//   node tools/render-reference.mjs --width 1200 --height 600 --seed 42
//
// One Chrome load draws every scene synchronously (packages/core + the browser replay) and POSTs
// the canvases back here. The PNGs are committed so the Node-backend test never needs Chrome.
// Node stdlib only. Chrome: $CHROME, else the Playwright Chromium below.

import http from 'node:http';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import crypto from 'node:crypto';
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const CHROME = process.env.CHROME || '/home/hermes/.cache/ms-playwright/chromium-1243/chrome-linux64/chrome';
const TIMEOUT_MS = 180_000;

const args = process.argv.slice(2);
function option(name, fallback) {
  const i = args.indexOf(name);
  return i === -1 ? fallback : args[i + 1];
}
const OUT = path.resolve(ROOT, option('--out', 'packages/render-canvas/test/fixtures'));
const WIDTH = Number(option('--width', 1200));
const HEIGHT = Number(option('--height', 600));
const SEED = Number(option('--seed', 42));

const TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
};

let posted = null;
const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://127.0.0.1');
  const send = (status, body, type = 'text/plain; charset=utf-8') => {
    res.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-store' });
    res.end(body);
  };
  try {
    if (req.method === 'POST' && (url.pathname === '/save' || url.pathname === '/error')) {
      const chunks = [];
      req.on('data', (c) => chunks.push(c));
      req.on('end', () => {
        const body = Buffer.concat(chunks).toString('utf8');
        if (url.pathname === '/error') posted = { error: body.split('\n')[0] };
        else posted = { body };
        send(200, 'ok');
      });
      return;
    }
    if (req.method !== 'GET' && req.method !== 'HEAD') return send(405, 'method not allowed');
    const file = path.resolve(ROOT, `.${decodeURIComponent(url.pathname)}`);
    if (file !== ROOT && !file.startsWith(ROOT + path.sep)) return send(403, 'forbidden');
    const stat = fs.statSync(file, { throwIfNoEntry: false });
    if (!stat || !stat.isFile()) return send(404, 'not found');
    send(200, fs.readFileSync(file), TYPES[path.extname(file)] || 'application/octet-stream');
  } catch (err) {
    send(500, String(err.message));
  }
});

await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
const PORT = server.address().port;
const TMP = fs.mkdtempSync(path.join(os.tmpdir(), 'fortyfive-reference-'));

function waitExit(child, ms) {
  if (child.exitCode !== null || child.signalCode !== null) return Promise.resolve();
  return new Promise((resolve) => {
    const t = setTimeout(resolve, ms);
    child.once('exit', () => { clearTimeout(t); resolve(); });
  });
}

const page = `http://127.0.0.1:${PORT}/tools/render-reference.html?width=${WIDTH}&height=${HEIGHT}&seed=${SEED}`;
const shot = path.join(TMP, 'screenshot.png');
const profile = fs.mkdtempSync(path.join(TMP, 'profile-'));
const chrome = spawn(CHROME, [
  '--headless=new', '--disable-gpu', '--no-sandbox', '--hide-scrollbars',
  '--disable-dev-shm-usage', `--user-data-dir=${profile}`,
  `--window-size=${WIDTH},${HEIGHT}`, '--virtual-time-budget=120000',
  `--screenshot=${shot}`, page,
], { stdio: ['ignore', 'ignore', 'pipe'] });
let stderr = '';
chrome.stderr.on('data', (d) => { stderr = (stderr + d).slice(-4000); });
chrome.on('error', (err) => { posted = { error: `cannot start Chrome at ${CHROME}: ${err.message}` }; });

const deadline = Date.now() + TIMEOUT_MS;
try {
  while (!posted && Date.now() < deadline) await new Promise((r) => setTimeout(r, 200));
  if (!posted) throw new Error(`timed out after ${TIMEOUT_MS / 1000} s waiting for the page\n${stderr.trim().split('\n').slice(-8).join('\n')}`);
  if (posted.error) throw new Error(`page error: ${posted.error}`);

  const renders = JSON.parse(posted.body);
  fs.mkdirSync(OUT, { recursive: true });
  const manifest = [];
  for (const render of renders) {
    const PREFIX = 'data:image/png;base64,';
    if (!render.png.startsWith(PREFIX)) throw new Error(`${render.name}: not a PNG data URL`);
    const png = Buffer.from(render.png.slice(PREFIX.length), 'base64');
    const file = `${render.name.toLowerCase()}.png`;
    fs.writeFileSync(path.join(OUT, file), png);
    manifest.push({
      file,
      scene: render.name,
      seed: render.seed,
      width: render.width,
      height: render.height,
      frames: render.frames,
      primitives: render.primitives,
      bytes: png.length,
      sha256: crypto.createHash('sha256').update(png).digest('hex'),
    });
    console.log(`render ${file} ${render.width}×${render.height} seed ${render.seed}  ${render.frames} frames  ${render.primitives} primitives  ${png.length} bytes`);
  }
  fs.writeFileSync(path.join(OUT, 'manifest.json'), `${JSON.stringify({ renderer: 'browser', tool: 'tools/render-reference.mjs', renders: manifest }, null, 2)}\n`);
  console.log(`\nwrote ${manifest.length} fixture(s) + manifest.json to ${path.relative(ROOT, OUT)}`);
} finally {
  server.close();
  if (chrome.exitCode === null && chrome.signalCode === null) chrome.kill('SIGTERM');
  await waitExit(chrome, 5000);
  if (chrome.exitCode === null && chrome.signalCode === null) chrome.kill('SIGKILL');
  await waitExit(chrome, 5000);
  // Chrome's helpers can still be writing into the profile; the OS reaps TMP if this misses
  try {
    fs.rmSync(TMP, { recursive: true, force: true, maxRetries: 10, retryDelay: 200 });
  } catch (err) {
    console.warn(`warning: could not remove ${TMP}: ${err.message}`);
  }
}
