// Headless stats for the 4:1 banner scenes: grid size, frames to finish, grid fill.
// Fails if any scene throws, does not finish within the frame cap, or fills under its
// minimum (5%, except the single-line wanderer-banner, which is meant to be sparse).
// Run with: node tools/scene-stats.mjs [--seed N]

function makeContext() {
  return {
    fillStyle: '',
    strokeStyle: '',
    lineWidth: 1,
    lineCap: 'butt',
    fillRect() {},
    beginPath() {},
    moveTo() {},
    lineTo() {},
    stroke() {},
    drawImage() {},
  };
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

const SLUGS = ['cable-run', 'mesh', 'labyrinth-banner', 'neon-banner', 'wanderer-banner'];
const WIDTH = 1584;
const HEIGHT = 396;
const MAX_FRAMES = 4000;
// Minimum grid fill, percent. wanderer-banner is one line on a 198 × 49 grid: crossing to
// the wall fills ~1.7%, so 0.4% only catches a line that died early.
const MIN_FILL = {
  'cable-run': 5,
  mesh: 5,
  'labyrinth-banner': 5,
  'neon-banner': 5,
  'wanderer-banner': 0.4,
};

const seedArg = process.argv.indexOf('--seed');
const seed = seedArg === -1 ? 45 : Number(process.argv[seedArg + 1]);

function run(config) {
  const scene = new Scene({ ...config, seed }, WIDTH, HEIGHT);
  let frames = 0;
  while (!scene.finished && frames < MAX_FRAMES) {
    scene.draw(8);
    frames++;
  }
  const filled = scene.grid.reduce((n, row) => n + row.filter(Boolean).length, 0);
  clearSeed();
  return {
    cols: scene.cols,
    rows: scene.rows,
    frames,
    finished: scene.finished,
    fill: (100 * filled) / (scene.rows * scene.cols),
  };
}

console.log(`seed ${seed}, ${WIDTH}×${HEIGHT}, draw(8), cap ${MAX_FRAMES} frames\n`);
console.log(`${'slug'.padEnd(18)}${'grid'.padEnd(11)}${'frames'.padStart(7)}${'fill'.padStart(8)}  result`);

let failed = 0;
for (const slug of SLUGS) {
  const config = scenes.find((s) => s.slug === slug);
  let line;
  try {
    if (!config) throw new Error('not registered in js/scenes/index.js');
    if (config.canvasWidth !== WIDTH || config.canvasHeight !== HEIGHT) {
      throw new Error(`canvas is ${config.canvasWidth}×${config.canvasHeight}, expected ${WIDTH}×${HEIGHT}`);
    }
    const r = run(config);
    const problems = [];
    if (!r.finished) problems.push(`did not finish within ${MAX_FRAMES} frames`);
    if (r.fill < MIN_FILL[slug]) problems.push(`fill under ${MIN_FILL[slug]}%`);
    if (problems.length) failed++;
    line = `${`${r.cols} × ${r.rows}`.padEnd(11)}${String(r.frames).padStart(7)}${`${r.fill.toFixed(1)}%`.padStart(8)}  ${problems.length ? `FAIL: ${problems.join('; ')}` : 'ok'}`;
  } catch (err) {
    failed++;
    line = `FAIL: threw ${err.message}`;
  }
  console.log(`${slug.padEnd(18)}${line}`);
}

console.log(failed === 0 ? `\nPASS (${SLUGS.length}/${SLUGS.length})` : `\nFAIL (${failed} of ${SLUGS.length} failed)`);
process.exit(failed === 0 ? 0 : 1);
