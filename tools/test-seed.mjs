// Determinism test: same seed → identical display list, different seed → different list.
// The trace is the scene's serialised frame (every coordinate, stroke colour and width); no DOM.
// Run with: node tools/test-seed.mjs

import { Scene } from '../packages/core/src/scene.js';
import { clearSeed } from '../packages/core/src/random.js';
import { banner } from '../apps/demo/scenes/banner.js';

const WIDTH = 1600;
const HEIGHT = 30;
const MAX_FRAMES = 100000;

function trace(seed) {
  const scene = new Scene({ ...banner, seed }, WIDTH, HEIGHT);
  let frames = 0;
  while (!scene.finished) {
    if (++frames > MAX_FRAMES) throw new Error(`Scene did not finish within ${MAX_FRAMES} frames`);
    scene.draw(8);
  }
  clearSeed();
  const primitives = scene.frame();
  const paths = primitives.filter((p) => p.type === 'path').length;
  return { text: primitives.map((p) => JSON.stringify(p)).join('\n'), ops: primitives.length, paths, frames };
}

const results = [];
function check(name, ok, detail) {
  results.push(ok);
  console.log(`${ok ? 'PASS' : 'FAIL'}  ${name}${detail ? ` — ${detail}` : ''}`);
}

const a = trace(12345);
const b = trace(12345);
const c = trace(999);

console.log(`seed 12345 run 1: ${a.ops} primitives, ${a.frames} frames`);
console.log(`seed 12345 run 2: ${b.ops} primitives, ${b.frames} frames`);
console.log(`seed 999:         ${c.ops} primitives, ${c.frames} frames`);

// The background rect is always there; non-empty means at least one stroke
check('trace is non-empty', a.paths > 0, `${a.paths} paths`);
check('same seed gives identical trace', a.text === b.text);
check('different seed gives different trace', a.text !== c.text);

const failed = results.filter((ok) => !ok).length;
console.log(failed === 0 ? `\nPASS (${results.length}/${results.length})` : `\nFAIL (${failed} of ${results.length} failed)`);
process.exit(failed === 0 ? 0 : 1);
