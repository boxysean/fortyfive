// Scale test: a banner scene rendered at 2× (js/banner-scale.js) must fill exactly the same
// grid cells as at 1×, and every drawn coordinate and stroke width must be exactly doubled.
// Run with: node tools/test-scale.mjs

let currentOps = null;

function makeContext() {
  const ctx = {
    fillStyle: '',
    strokeStyle: '',
    lineWidth: 1,
    lineCap: 'butt',
    fillRect() {},
    beginPath() {},
    moveTo(x, y) { currentOps.push(['M', x, y, ctx.lineWidth, ctx.strokeStyle]); },
    lineTo(x, y) { currentOps.push(['L', x, y, ctx.lineWidth, ctx.strokeStyle]); },
    stroke() {},
    drawImage() {},
  };
  return ctx;
}

globalThis.document = {
  createElement(tag) {
    if (tag !== 'canvas') throw new Error(`Unsupported element: ${tag}`);
    const ctx = makeContext();
    return { width: 0, height: 0, getContext: () => ctx };
  },
};

const { Scene } = await import('../js/scene.js');
const { clearSeed } = await import('../js/random.js');
const { scenes } = await import('../js/scenes/index.js');
const { scaleSceneConfig } = await import('../js/banner-scale.js');

const SLUGS = ['cable-run', 'mesh', 'labyrinth-banner', 'neon-banner', 'wanderer-banner'];
const SEED = 45;
const SCALE = 2;
const MAX_FRAMES = 20000;

function run(config, scale) {
  currentOps = [];
  const cfg = { ...scaleSceneConfig(config, scale), seed: SEED };
  const scene = new Scene(cfg, config.canvasWidth * scale, config.canvasHeight * scale);
  let frames = 0;
  while (!scene.finished) {
    if (++frames > MAX_FRAMES) throw new Error(`did not finish within ${MAX_FRAMES} frames`);
    scene.draw(8);
  }
  clearSeed();
  const ops = currentOps;
  currentOps = null;
  return { grid: scene.grid.map((row) => row.map(Number).join('')).join('\n'), ops, rows: scene.rows, cols: scene.cols };
}

let failed = 0;
for (const slug of SLUGS) {
  const config = scenes.find((s) => s.slug === slug);
  const a = run(config, 1);
  const b = run(config, SCALE);
  const problems = [];
  if (a.rows !== b.rows || a.cols !== b.cols) problems.push(`grid ${a.cols}×${a.rows} vs ${b.cols}×${b.rows}`);
  if (a.grid !== b.grid) problems.push('filled cells differ');
  if (a.ops.length !== b.ops.length) problems.push(`${a.ops.length} vs ${b.ops.length} draw ops`);
  const bad = a.ops.findIndex((op, i) => {
    const o = b.ops[i];
    return !o || o[0] !== op[0] || o[1] !== op[1] * SCALE || o[2] !== op[2] * SCALE
      || o[3] !== op[3] * SCALE || o[4] !== op[4];
  });
  if (bad !== -1) problems.push(`op ${bad} not an exact ${SCALE}× of 1×`);
  if (problems.length) failed++;
  console.log(`${slug.padEnd(18)}${`${a.cols} × ${a.rows}`.padEnd(11)}${String(a.ops.length).padStart(7)} ops  ${problems.length ? `FAIL: ${problems.join('; ')}` : 'ok'}`);
}

console.log(failed === 0 ? `\nPASS (${SLUGS.length}/${SLUGS.length})` : `\nFAIL (${failed} of ${SLUGS.length} failed)`);
process.exit(failed === 0 ? 0 : 1);
