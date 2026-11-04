// Determinism test: same seed → identical draw trace, different seed → different trace.
// Run with: node tools/test-seed.mjs

function makeContext(ops) {
  return {
    fillStyle: '',
    strokeStyle: '',
    lineWidth: 1,
    lineCap: 'butt',
    fillRect() {},
    beginPath() {},
    moveTo(x, y) { ops.push(`moveTo ${x} ${y}`); },
    lineTo(x, y) { ops.push(`lineTo ${x} ${y}`); },
    stroke() { ops.push('stroke'); },
    drawImage() {},
  };
}

let currentOps = null;

globalThis.document = {
  createElement(tag) {
    if (tag !== 'canvas') throw new Error(`Unsupported element: ${tag}`);
    const ctx = makeContext(currentOps);
    return { width: 0, height: 0, getContext: () => ctx };
  },
};

const { Scene } = await import('../js/scene.js');
const { clearSeed } = await import('../js/random.js');
const { banner } = await import('../js/scenes/banner.js');

const WIDTH = 1600;
const HEIGHT = 30;
const MAX_FRAMES = 100000;

function trace(seed) {
  currentOps = [];
  const scene = new Scene({ ...banner, seed }, WIDTH, HEIGHT);
  let frames = 0;
  while (!scene.finished) {
    if (++frames > MAX_FRAMES) throw new Error(`Scene did not finish within ${MAX_FRAMES} frames`);
    scene.draw(8);
  }
  const ops = currentOps;
  currentOps = null;
  clearSeed();
  return { text: ops.join('\n'), ops: ops.length, frames };
}

const results = [];
function check(name, ok, detail) {
  results.push(ok);
  console.log(`${ok ? 'PASS' : 'FAIL'}  ${name}${detail ? ` — ${detail}` : ''}`);
}

const a = trace(12345);
const b = trace(12345);
const c = trace(999);

console.log(`seed 12345 run 1: ${a.ops} ops, ${a.frames} frames`);
console.log(`seed 12345 run 2: ${b.ops} ops, ${b.frames} frames`);
console.log(`seed 999:         ${c.ops} ops, ${c.frames} frames`);

check('trace is non-empty', a.ops > 0, `${a.ops} ops`);
check('same seed gives identical trace', a.text === b.text);
check('different seed gives different trace', a.text !== c.text);

const failed = results.filter((ok) => !ok).length;
console.log(failed === 0 ? `\nPASS (${results.length}/${results.length})` : `\nFAIL (${failed} of ${results.length} failed)`);
process.exit(failed === 0 ? 0 : 1);
