// Render the banners in banners/manifest.json to PNG with headless Chrome, and record how.
//
//   node tools/render-banner.mjs                       render every banner, update metadata
//   node tools/render-banner.mjs --only a.png,b.png    render just these
//   node tools/render-banner.mjs --verify              also fail if a PNG's sha256 changed
//   node tools/render-banner.mjs --check               determinism test: render each twice
//
// One Chrome per banner loads render.html, which draws the scene synchronously from its
// seed, composites the logos and POSTs the canvas PNG back to this server's /save.
// Node stdlib only. Chrome: $CHROME, else the Playwright Chromium below.

import http from 'node:http';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import crypto from 'node:crypto';
import { spawn, execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const BANNERS = path.join(ROOT, 'banners');
const MANIFEST = path.join(BANNERS, 'manifest.json');
const CHROME = process.env.CHROME || '/home/hermes/.cache/ms-playwright/chromium-1243/chrome-linux64/chrome';
const TIMEOUT_MS = 180_000;
const EXIT_GRACE_MS = 5_000;

const AUTHORED = ['file', 'scene', 'seed', 'canvas', 'grid', 'palette', 'movement', 'strokeWidth', 'logos', 'description'];
const GENERATED = ['generator', 'generatedAt', 'png'];

// --- args ---

const args = process.argv.slice(2);
const flag = (name) => args.includes(name);
const onlyIdx = args.indexOf('--only');
const only = onlyIdx === -1 ? null : (args[onlyIdx + 1] || '').split(',').filter(Boolean);
const CHECK = flag('--check');
const VERIFY = flag('--verify');
for (const a of args) {
  if (a.startsWith('--') && !['--only', '--check', '--verify'].includes(a)) die(`unknown option ${a}`);
}

function die(msg) {
  console.error(`render-banner: ${msg}`);
  process.exit(2);
}

// --- helpers ---

const sha256 = (buf) => crypto.createHash('sha256').update(buf).digest('hex');
const writeJson = (file, value) => fs.writeFileSync(file, `${JSON.stringify(value, null, 2)}\n`);
const sidecarPath = (file) => path.join(BANNERS, `${file.replace(/\.png$/i, '')}.json`);

/** Parse the PNG signature and IHDR chunk (first 33 bytes). */
function readPngHeader(buf) {
  const SIG = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a];
  if (buf.length < 33) throw new Error(`PNG is only ${buf.length} bytes`);
  if (!SIG.every((b, i) => buf[i] === b)) throw new Error('bad PNG signature');
  if (buf.readUInt32BE(8) !== 13 || buf.toString('latin1', 12, 16) !== 'IHDR') throw new Error('first chunk is not IHDR');
  return { width: buf.readUInt32BE(16), height: buf.readUInt32BE(20) };
}

function git(...a) {
  return execFileSync('git', a, { cwd: ROOT, encoding: 'utf8' }).trim();
}

// --- manifest validation ---

const manifest = JSON.parse(fs.readFileSync(MANIFEST, 'utf8'));
if (!Array.isArray(manifest)) die('banners/manifest.json must be an array');
const { scenes } = await import('../js/scenes/index.js');

function validate(entry) {
  const missing = AUTHORED.filter((k) => !(k in entry));
  if (missing.length) throw new Error(`missing authored keys: ${missing.join(', ')}`);
  const config = scenes.find((s) => s.slug === entry.scene) || scenes.find((s) => s.name === entry.scene);
  if (!config) throw new Error(`no scene ${entry.scene} in js/scenes/index.js`);

  // The manifest describes the scene; make sure it still describes it truthfully
  const problems = [];
  if (config.canvasWidth !== entry.canvas.width || config.canvasHeight !== entry.canvas.height) {
    problems.push(`canvas ${entry.canvas.width}×${entry.canvas.height} vs scene ${config.canvasWidth}×${config.canvasHeight}`);
  }
  if (config.widthSpacing !== entry.grid.widthSpacing || config.heightSpacing !== entry.grid.heightSpacing) {
    problems.push(`grid ${JSON.stringify(entry.grid)} vs scene ${config.widthSpacing}×${config.heightSpacing}`);
  }
  const strokes = new Set((config.lineDraws || []).map((d) => d.strokeWidth || 1));
  if (!strokes.has(entry.strokeWidth)) problems.push(`strokeWidth ${entry.strokeWidth} not in scene's [${[...strokes]}]`);
  const norm = (l) => [l.name, l.file, l.x, l.y, l.height, l.chip || false, l.monochrome || false, l.opacity ?? 1].join('|');
  const want = (config.logos || []).map(norm).join('\n');
  if ((entry.logos || []).map(norm).join('\n') !== want) problems.push('logos differ from the scene config');
  for (const logo of entry.logos || []) {
    const actual = sha256(fs.readFileSync(path.join(BANNERS, logo.file)));
    if (actual !== logo.sha256) problems.push(`${logo.file} sha256 is ${actual}, manifest says ${logo.sha256}`);
  }
  if (problems.length) throw new Error(problems.join('; '));
  return config;
}

const selected = only ? manifest.filter((e) => only.includes(e.file)) : manifest;
if (only) {
  const unknown = only.filter((f) => !manifest.some((e) => e.file === f));
  if (unknown.length) die(`--only: not in manifest: ${unknown.join(', ')}`);
}
for (const entry of selected) {
  try {
    validate(entry);
  } catch (err) {
    die(`${entry.file}: ${err.message}`);
  }
}

// --- server ---

const TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.css': 'text/css; charset=utf-8',
};
const virtualFiles = new Map(); // url path → Buffer (scratch manifests for --check)
const pending = new Map(); // entry.file → { dest, resolve, reject }

function readBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on('data', (c) => chunks.push(c));
    req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
    req.on('error', reject);
  });
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://127.0.0.1');
  const send = (status, body, type = 'text/plain; charset=utf-8') => {
    res.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-store' });
    res.end(body);
  };
  try {
    if (req.method === 'POST' && (url.pathname === '/save' || url.pathname === '/error')) {
      const file = url.searchParams.get('file');
      const job = pending.get(file);
      const body = await readBody(req);
      if (!job) return send(404, `no pending render for ${file}`);
      if (url.pathname === '/error') {
        job.reject(new Error(`page error: ${body.split('\n')[0]}`));
        return send(200, 'noted');
      }
      const PREFIX = 'data:image/png;base64,';
      if (!body.startsWith(PREFIX)) {
        job.reject(new Error(`POST body is not a PNG data URL (${body.slice(0, 40)}…)`));
        return send(400, 'expected a PNG data URL');
      }
      const png = Buffer.from(body.slice(PREFIX.length), 'base64');
      fs.mkdirSync(path.dirname(job.dest), { recursive: true });
      fs.writeFileSync(job.dest, png);
      job.resolve(png);
      return send(200, String(png.length));
    }
    if (req.method !== 'GET' && req.method !== 'HEAD') return send(405, 'method not allowed');
    if (virtualFiles.has(url.pathname)) return send(200, virtualFiles.get(url.pathname), TYPES['.json']);
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
const TMP = fs.mkdtempSync(path.join(os.tmpdir(), 'fortyfive-render-'));

// --- one Chrome per banner ---

function waitExit(child, ms) {
  if (child.exitCode !== null || child.signalCode !== null) return Promise.resolve();
  return new Promise((resolve) => {
    const t = setTimeout(resolve, ms);
    child.once('exit', () => { clearTimeout(t); resolve(); });
  });
}

/** Render manifest entry `file` (as served at manifestPath) and write its PNG to dest. */
async function render(manifestPath, file, dest, width, height) {
  const started = Date.now();
  const shot = path.join(TMP, `screenshot-${crypto.randomUUID()}.png`);
  const profile = fs.mkdtempSync(path.join(TMP, 'profile-'));
  const page = `http://127.0.0.1:${PORT}/render.html?manifest=${encodeURIComponent(manifestPath)}&file=${encodeURIComponent(file)}`;

  let job;
  const posted = new Promise((resolve, reject) => { job = { dest, resolve, reject }; });
  pending.set(file, job);

  const chrome = spawn(CHROME, [
    '--headless=new', '--disable-gpu', '--no-sandbox', '--hide-scrollbars',
    '--disable-dev-shm-usage', `--user-data-dir=${profile}`,
    `--window-size=${width},${height}`, '--virtual-time-budget=30000',
    `--screenshot=${shot}`, page,
  ], { stdio: ['ignore', 'ignore', 'pipe'] });
  let stderr = '';
  chrome.stderr.on('data', (d) => { stderr = (stderr + d).slice(-4000); });
  chrome.on('error', (err) => job.reject(new Error(`cannot start Chrome at ${CHROME}: ${err.message}`)));
  chrome.on('exit', (code, signal) => {
    // Chrome quits after its screenshot; a POST still in flight gets a short grace period
    setTimeout(() => job.reject(new Error(
      `Chrome exited (${signal || `code ${code}`}) before ${file} was POSTed\n${stderr.trim().split('\n').slice(-5).join('\n')}`,
    )), EXIT_GRACE_MS);
  });
  const timer = setTimeout(() => job.reject(new Error(`timed out after ${TIMEOUT_MS / 1000} s waiting for ${file}`)), TIMEOUT_MS);

  try {
    const png = await posted;
    return { png, ms: Date.now() - started };
  } finally {
    clearTimeout(timer);
    pending.delete(file);
    if (chrome.exitCode === null && chrome.signalCode === null) chrome.kill('SIGTERM');
    await waitExit(chrome, 5000);
    if (chrome.exitCode === null && chrome.signalCode === null) chrome.kill('SIGKILL');
    await waitExit(chrome, 5000);
    // Chrome's helper processes can still be writing the profile; the whole TMP goes at exit
    try {
      fs.rmSync(profile, { recursive: true, force: true, maxRetries: 5, retryDelay: 200 });
      fs.rmSync(shot, { force: true });
    } catch {}
  }
}

function checkPng(png, entry) {
  const scale = entry.canvas.scale ?? 1;
  const want = { width: entry.canvas.width * scale, height: entry.canvas.height * scale };
  const got = readPngHeader(png);
  if (got.width !== want.width || got.height !== want.height) {
    throw new Error(`PNG is ${got.width}×${got.height}, expected ${want.width}×${want.height}`);
  }
  return { sha256: sha256(png), bytes: png.length, width: got.width, height: got.height };
}

const dims = (e) => [e.canvas.width * (e.canvas.scale ?? 1), e.canvas.height * (e.canvas.scale ?? 1)];
const short = (h) => (h ? h.slice(0, 16) : '(none)'.padEnd(16));

// --- modes ---

let failures = 0;
const failed = (file, err) => {
  failures++;
  console.log(`FAIL   ${file}  ${err.message}`);
};

async function runCheck() {
  // Two copies of the manifest whose files point at scratch names, served from memory
  for (const pass of ['a', 'b']) {
    const copy = manifest.map((e) => ({ ...e, file: `__check-${pass}/${e.file}` }));
    virtualFiles.set(`/__check/manifest-${pass}.json`, Buffer.from(JSON.stringify(copy)));
  }
  console.log(`check: rendering ${selected.length} banner(s) twice into ${TMP}\n`);
  for (const entry of selected) {
    const [w, h] = dims(entry);
    try {
      const out = {};
      for (const pass of ['a', 'b']) {
        const name = `__check-${pass}/${entry.file}`;
        const { png, ms } = await render(`/__check/manifest-${pass}.json`, name, path.join(TMP, name), w, h);
        out[pass] = checkPng(png, entry);
        console.log(`render ${name} ${w}×${h}  ${ms} ms  ${png.length} bytes`);
      }
      const same = out.a.sha256 === out.b.sha256;
      if (!same) failures++;
      const recorded = entry.png?.sha256;
      const info = !recorded ? 'no hash recorded' : recorded === out.a.sha256 ? 'matches manifest' : 'differs from manifest';
      console.log(`${same ? 'PASS' : 'FAIL'}   ${entry.file}  run1 ${short(out.a.sha256)}  run2 ${short(out.b.sha256)}`
        + `  [informational: manifest ${short(recorded)} — ${info}]\n`);
    } catch (err) {
      failed(entry.file, err);
    }
  }
  console.log(failures === 0
    ? `check PASS: ${selected.length}/${selected.length} banners rendered byte-identical twice in this run`
    : `check FAIL: ${failures} of ${selected.length} banners not reproducible`);
  console.log('(The manifest comparison is informational: a different Chrome build may encode PNGs differently.)');
}

async function runRender() {
  const commit = git('rev-parse', 'HEAD');
  const dirty = git('status', '--porcelain', '--', 'js', 'render.html', 'tools');
  if (dirty) console.log(`warning: uncommitted changes under js/, render.html or tools/ — generator.commit ${commit.slice(0, 7)} will not fully describe this render\n`);

  let mismatches = 0;
  let totalBytes = 0;
  const t0 = Date.now();
  for (const entry of selected) {
    const [w, h] = dims(entry);
    const previous = entry.png?.sha256;
    try {
      const { png, ms } = await render('/banners/manifest.json', entry.file, path.join(BANNERS, entry.file), w, h);
      const info = checkPng(png, entry);
      totalBytes += info.bytes;
      console.log(`render ${entry.file} ${w}×${h}  ${ms} ms  ${info.bytes} bytes  sha256 ${short(info.sha256)}`);

      const record = {};
      for (const k of AUTHORED) record[k] = entry[k];
      for (const k of Object.keys(entry)) if (!AUTHORED.includes(k) && !GENERATED.includes(k)) record[k] = entry[k];
      record.generator = { repo: 'boxysean/fortyfive', commit, tool: 'tools/render-banner.mjs' };
      record.generatedAt = new Date().toISOString();
      record.png = info;
      manifest[manifest.indexOf(entry)] = record;
      writeJson(sidecarPath(entry.file), record);

      if (VERIFY) {
        if (!previous) {
          mismatches++;
          console.log(`MISMATCH ${entry.file}: no png.sha256 recorded in the manifest to verify against`);
        } else if (previous !== info.sha256) {
          mismatches++;
          console.log(`MISMATCH ${entry.file}: rendered ${info.sha256}\n         manifest had ${previous}`);
        }
      }
    } catch (err) {
      failed(entry.file, err);
    }
  }

  writeJson(MANIFEST, manifest);
  writeContactSheet();
  const ok = selected.length - failures;
  console.log(`\n${ok}/${selected.length} rendered, ${totalBytes} bytes, ${((Date.now() - t0) / 1000).toFixed(1)} s`
    + ` — wrote banners/manifest.json, ${ok} sidecar(s), banners/index.html (commit ${commit.slice(0, 7)})`);
  if (VERIFY) {
    console.log(mismatches === 0 ? `verify OK: all ${selected.length} sha256 values match the manifest`
      : `verify FAILED: ${mismatches} MISMATCH(es) against the previously recorded sha256`);
  }
  failures += mismatches;
}

function writeContactSheet() {
  const names = Object.fromEntries(scenes.map((s) => [s.slug, s.name]));
  const data = manifest.map((e) => ({ file: e.file, scene: e.scene, name: names[e.scene] || e.scene, seed: e.seed, description: e.description }));
  const json = JSON.stringify(data, null, 2).replace(/</g, '\\u003c');
  fs.writeFileSync(path.join(BANNERS, 'index.html'), `<!DOCTYPE html>
<!-- Generated by tools/render-banner.mjs from banners/manifest.json. Do not edit by hand. -->
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FortyFive banners</title>
  <style>
    body { margin: 0; padding: 24px; background: #111; color: #ddd; font: 15px/1.5 system-ui, -apple-system, 'Segoe UI', sans-serif; }
    h1 { font-size: 18px; font-weight: 600; margin: 0 0 24px; }
    figure { margin: 0 0 40px; }
    img { display: block; width: 100%; height: auto; background: #222; }
    figcaption { margin-top: 8px; }
    .meta { color: #999; font-size: 13px; }
  </style>
</head>
<body>
  <h1>FortyFive banners</h1>
  <main id="sheet"></main>
  <script>
    const banners = ${json};
    const sheet = document.getElementById('sheet');
    for (const b of banners) {
      const fig = document.createElement('figure');
      const link = document.createElement('a');
      link.href = b.file;
      const img = document.createElement('img');
      img.src = b.file;
      img.alt = b.description;
      link.appendChild(img);
      const cap = document.createElement('figcaption');
      const title = document.createElement('strong');
      title.textContent = b.name;
      const meta = document.createElement('span');
      meta.className = 'meta';
      meta.textContent = \`  \${b.scene} · seed \${b.seed} · \${b.file}\`;
      const desc = document.createElement('div');
      desc.textContent = b.description;
      cap.append(title, meta, desc);
      fig.append(link, cap);
      sheet.appendChild(fig);
    }
  </script>
</body>
</html>
`);
}

try {
  if (CHECK) await runCheck();
  else await runRender();
} finally {
  server.close();
  fs.rmSync(TMP, { recursive: true, force: true, maxRetries: 10, retryDelay: 200 });
}
process.exit(failures === 0 ? 0 : 1);
