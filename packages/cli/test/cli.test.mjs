// CLI test — drives bin/fortyfive.js as a subprocess from the repo root, the way a person would, and
// holds it to its contract: exit codes, what goes to stdout vs stderr, and output that is the same
// bytes the backends produce for the same seed.
//
// Run with: node --test packages/cli/test/

import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { Resvg } from '@resvg/resvg-js';

import { Scene } from '../../core/src/scene.js';
import { renderToPng } from '../../render-canvas/src/node.js';
import { sha256 } from '../src/render.js';
import { scenes } from '../../../apps/demo/scenes/index.js';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(HERE, '../../..');
const BIN = path.join(HERE, '../bin/fortyfive.js');
const REGISTRY = 'apps/demo/scenes/index.js';
const VERSION = JSON.parse(fs.readFileSync(path.join(HERE, '../package.json'), 'utf8')).version;
const TMP = fs.mkdtempSync(path.join(os.tmpdir(), 'fortyfive-cli-'));

test.after(() => fs.rmSync(TMP, { recursive: true, force: true }));

/** Run the CLI from the repo root; stdout stays a Buffer so PNG bytes survive. */
function cli(...args) {
  const r = spawnSync(process.execPath, [BIN, ...args], { cwd: ROOT, maxBuffer: 64 << 20 });
  return { code: r.status, stdout: r.stdout, out: r.stdout.toString(), err: r.stderr.toString() };
}

const tmp = (name) => path.join(TMP, name);

test('help, a bare invocation and --version exit 0 on stdout', () => {
  for (const args of [[], ['--help'], ['-h'], ['render', '--help']]) {
    const r = cli(...args);
    assert.equal(r.code, 0, args.join(' '));
    assert.match(r.out, /fortyfive render --scene/);
    assert.equal(r.err, '');
  }
  const v = cli('--version');
  assert.equal(v.code, 0);
  assert.equal(v.out, `${VERSION}\n`);
});

test('scenes lists the registry with slugs and dimensions', () => {
  const r = cli('scenes', '--scenes', REGISTRY);
  assert.equal(r.code, 0, r.err);
  const lines = r.out.trimEnd().split('\n');
  assert.equal(lines.length, scenes.length);
  assert.match(lines.find((l) => l.startsWith('neon ')), /^neon\s+1200x600  Neon$/);
  assert.match(lines.find((l) => l.startsWith('banner')), /^banner\s+1600x30  Banner$/);

  const json = cli('scenes', '--scenes', REGISTRY, '--json');
  assert.equal(json.code, 0, json.err);
  const rows = JSON.parse(json.out);
  assert.deepEqual(rows.map((s) => s.name), scenes.map((s) => s.name));
  assert.deepEqual(rows.find((s) => s.slug === 'banner'), { slug: 'banner', name: 'Banner', width: 1600, height: 30 });
  assert.deepEqual(rows.find((s) => s.slug === 'neon'), { slug: 'neon', name: 'Neon', width: 1200, height: 600 });
});

test('no --scenes is a usage error naming the flag and the example', () => {
  for (const args of [['scenes'], ['render', '--scene', 'neon', '--seed', '1', '--out', tmp('x.png')]]) {
    const r = cli(...args);
    assert.equal(r.code, 1);
    assert.equal(r.out, '');
    assert.equal(r.err, [
      'fortyfive: no scene registry given.',
      '  pass --scenes <path> to a module that exports a `scenes` array, resolved from your',
      '  working directory, e.g.',
      '    --scenes apps/demo/scenes/index.js',
      '',
    ].join('\n'));
  }
});

test('a PNG render is the node backend\'s bytes, with a manifest that hashes them', async () => {
  const out = tmp('neon.png');
  const manifest = tmp('neon.json');
  const r = cli('render', '--scene', 'NEON', '--seed', '42', '--out', out, '--manifest', manifest, '--scenes', REGISTRY);
  assert.equal(r.code, 0, r.err);
  assert.equal(r.out, '', 'success is silent on stdout');
  assert.equal(r.err, '', 'a given seed prints nothing');

  const config = scenes.find((s) => s.name === 'Neon');
  const scene = new Scene({ ...config, seed: 42 }, 1200, 600);
  while (!scene.finished) scene.draw(8);
  const expected = await renderToPng(scene.frame(), { width: 1200, height: 600 });
  const bytes = fs.readFileSync(out);
  assert.ok(bytes.equals(expected), 'CLI PNG differs from renderToPng at the same seed');

  const text = fs.readFileSync(manifest, 'utf8');
  assert.ok(text.endsWith('}\n'));
  assert.deepEqual(JSON.parse(text), {
    scene: 'Neon', seed: 42, engine: 'fortyfive', version: VERSION,
    format: 'png', width: 1200, height: 600, sha256: sha256(bytes),
  });
});

test('--out - writes the SVG to stdout, and the manifest hashes what was sent', () => {
  const manifest = tmp('banner.json');
  const r = cli('render', '--scene', 'banner', '--seed', '-3', '--out', '-', '--format', 'svg', '--manifest', manifest, '--scenes', REGISTRY);
  assert.equal(r.code, 0, r.err);
  assert.match(r.out, /^<\?xml/);
  assert.match(r.out, /width="1600" height="30"/, 'the scene\'s canvas size is the default');
  const m = JSON.parse(fs.readFileSync(manifest, 'utf8'));
  assert.equal(m.seed, -3);
  assert.equal(m.format, 'svg');
  assert.equal(m.sha256, sha256(r.stdout));

  // It is a real picture: resvg rasterises it at the declared size
  const png = new Resvg(r.out).render();
  assert.equal(png.width, 1600);
  assert.equal(png.height, 30);
});

test('the format follows the extension; flags override the scene size', () => {
  const out = tmp('small.svg');
  const r = cli('render', '--scene', 'simple', '--seed', '0', '--out', out, '--width', '300', '--height', '200', '--scenes', REGISTRY);
  assert.equal(r.code, 0, r.err);
  assert.match(fs.readFileSync(out, 'utf8'), /^<\?xml[^]*width="300" height="200"/);

  const png = tmp('forced.svg');
  assert.equal(cli('render', '--scene', 'simple', '--seed', '0', '--out', png, '--format', 'png', '--width', '300', '--height', '200', '--scenes', REGISTRY).code, 0);
  assert.equal(fs.readFileSync(png).subarray(1, 4).toString(), 'PNG', 'an explicit --format wins over the extension');
});

test('an omitted seed is generated, announced on stderr, and reproduces', () => {
  const a = tmp('random-a.png');
  const r = cli('render', '--scene', 'simple', '--out', a, '--width', '300', '--height', '150', '--scenes', REGISTRY);
  assert.equal(r.code, 0, r.err);
  assert.equal(r.out, '');
  const match = r.err.match(/^fortyfive: no --seed given — using (\d+) \(pass --seed \1 to reproduce\)\n$/);
  assert.ok(match, `unexpected stderr: ${r.err}`);
  const seed = Number(match[1]);
  assert.ok(seed >= 0 && seed < 2 ** 32);

  const b = tmp('random-b.png');
  assert.equal(cli('render', '--scene', 'simple', '--seed', match[1], '--out', b, '--width', '300', '--height', '150', '--scenes', REGISTRY).code, 0);
  assert.ok(fs.readFileSync(a).equals(fs.readFileSync(b)), 'the announced seed did not reproduce the render');
});

test('usage errors exit 1 with the reason on stderr', () => {
  const noRegistry = tmp('empty.mjs');
  fs.writeFileSync(noRegistry, 'export const other = 1;\n');
  const ok = ['--scenes', REGISTRY, '--out', tmp('u.png')];
  const cases = [
    [['frob'], /unknown command frob/],
    [['render', '--scene', 'neon', '--seed', '1', '--colour', 'red', ...ok], /unknown flag --colour/],
    [['scenes', '--scenes', REGISTRY, '--seed', '1'], /unknown flag --seed/],
    [['render', '--scene', 'neon', '--seed', '1', '--format', 'gif', ...ok], /--format must be png or svg/],
    [['render', '--scene', 'neon', '--seed', '1.5', ...ok], /--seed must be an integer/],
    [['render', '--scene', 'neon', '--seed', '1', '--width', '0', ...ok], /--width must be a positive integer/],
    [['render', '--scene', 'neon', '--seed', '1', '--height', 'tall', ...ok], /--height must be a positive integer/],
    [['render', '--scene', '--seed', '1', ...ok], /--scene needs a value/],
    [['render', '--seed', '1', ...ok], /needs --scene/],
    [['render', '--scene', 'neon', '--scenes', REGISTRY], /needs --out/],
    [['render', '--scene', 'nope', '--seed', '1', ...ok], /no scene nope .*have: simple, wanderer, neon/],
    [['scenes', '--scenes', 'no/such/registry.js'], /no such file/],
    [['scenes', '--scenes', noRegistry], /does not export a `scenes` array/],
    [['render', 'neon'], /unexpected argument neon/],
  ];
  for (const [args, reason] of cases) {
    const r = cli(...args);
    assert.equal(r.code, 1, `${args.join(' ')}\n${r.err}`);
    assert.match(r.err, reason, args.join(' '));
    assert.equal(r.out, '', `${args.join(' ')} wrote to stdout`);
  }
});

test('a render that cannot be written exits 2 with the error and its stack', () => {
  const r = cli('render', '--scene', 'simple', '--seed', '1', '--width', '100', '--height', '50', '--out', tmp('missing/dir/x.png'), '--scenes', REGISTRY);
  assert.equal(r.code, 2);
  assert.match(r.err, /render failed: Error: ENOENT/);
  assert.match(r.err, /\n\s+at /, 'the stack is printed');
});

test('a scene that cannot be built exits 2', () => {
  const broken = tmp('broken.mjs');
  // A valid registry entry (it has a name) whose config throws when Scene reads it — and a default
  // export, the registry's other accepted shape
  fs.writeFileSync(broken, 'export default [{ name: "Broken", get lines() { throw new Error("bad scene"); } }];\n');
  const r = cli('render', '--scene', 'broken', '--seed', '1', '--out', tmp('b.png'), '--scenes', broken);
  assert.equal(r.code, 2, r.err);
  assert.match(r.err, /bad scene/);
});

// The registry by absolute path, for runs whose cwd is not the repo root
const REGISTRY_ABS = path.join(ROOT, REGISTRY);
const BROWSERS = ['chromium', 'google-chrome', 'google-chrome-stable', 'chrome'];

/** A fresh, empty directory under TMP — nothing from the repo, nothing from another test. */
const freshDir = (name) => fs.mkdtempSync(path.join(TMP, `${name}-`));

/** Width and height from a PNG's IHDR chunk, which always sits at bytes 16..24. */
function pngSize(bytes) {
  return { width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}

const PNG_MAGIC = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);

test('no browser on PATH — a PNG is still produced', () => {
  const sandbox = freshDir('sandbox');
  const cwd = freshDir('cwd');
  const markers = tmp(`poison-${path.basename(sandbox)}`);
  fs.mkdirSync(markers);

  // Every name a headless-chrome lookup would try is a trap: if one runs, it leaves a marker
  for (const name of BROWSERS) {
    const exe = path.join(sandbox, name);
    fs.writeFileSync(exe, `#!/bin/sh\ntouch '${path.join(markers, name)}'\nexit 1\n`);
    fs.chmodSync(exe, 0o755);
  }
  const env = { ...process.env, PATH: sandbox };

  // The poison proves chrome is never *called*; this proves no real one could be *found* either —
  // every PATH entry is checked, so the gate holds even if the sandbox were not the only entry
  const real = env.PATH.split(path.delimiter).filter(Boolean)
    .flatMap((dir) => BROWSERS.map((name) => path.join(dir, name)))
    .filter((file) => fs.existsSync(file) && path.dirname(file) !== sandbox);
  assert.deepEqual(real, [], `a real browser is reachable on the child PATH: ${real.join(', ')}`);

  const out = path.join(cwd, 'out.png');
  const r = spawnSync(process.execPath, [BIN, 'render', '--scene', 'simple', '--seed', '7', '--out', out,
    '--width', '320', '--height', '200', '--scenes', REGISTRY_ABS], { cwd, env, maxBuffer: 64 << 20 });
  assert.equal(r.status, 0, `render failed without a browser:\n${r.stderr}`);
  assert.equal(r.stdout.toString(), '', 'success is silent on stdout');

  const bytes = fs.readFileSync(out);
  assert.ok(bytes.subarray(0, 8).equals(PNG_MAGIC), 'the output does not start with the PNG signature');
  assert.deepEqual(pngSize(bytes), { width: 320, height: 200 }, 'IHDR does not carry the requested size');

  const invoked = BROWSERS.filter((name) => fs.existsSync(path.join(markers, name)));
  assert.deepEqual(invoked, [], `the render tried to launch a browser: ${invoked.join(', ')}`);
});

test('the same seed produces the same bytes in two separate processes', () => {
  // Reproducibility must survive a process boundary, not just a second call in the same process
  const a = tmp('proc-a.png');
  const b = tmp('proc-b.png');
  for (const out of [a, b]) {
    const r = cli('render', '--scene', 'wanderer', '--seed', '1234', '--out', out, '--width', '320', '--height', '200', '--scenes', REGISTRY);
    assert.equal(r.code, 0, r.err);
  }
  assert.ok(fs.readFileSync(a).equals(fs.readFileSync(b)), 'two processes rendered the same seed differently');
});

test('--format svg writes one self-contained file', () => {
  const dir = freshDir('svg');
  const out = path.join(dir, 'neon.svg');
  const r = cli('render', '--scene', 'neon', '--seed', '42', '--out', out, '--scenes', REGISTRY_ABS);
  assert.equal(r.code, 0, r.err);
  assert.deepEqual(fs.readdirSync(dir), ['neon.svg'], 'the SVG must be the only file written — no sidecar assets');

  const text = fs.readFileSync(out, 'utf8');
  assert.match(text, /^<\?xml/, 'missing the XML declaration');
  assert.match(text, /<svg/, 'no <svg> element');
  assert.doesNotMatch(text, /href="https?:/, 'the SVG references the network');
  // Anything it does reference must be inlined; a relative href would break once the file is moved
  const hrefs = [...text.matchAll(/href="([^"]*)"/g)].map((m) => m[1]);
  for (const href of hrefs) assert.ok(href.startsWith('data:'), `external reference: href="${href.slice(0, 60)}"`);

  const png = new Resvg(text).render();
  assert.equal(png.width, 1200, 'rasterised width');
  assert.equal(png.height, 600, 'rasterised height');
});
