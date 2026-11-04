// Seedable random source — passthrough to Math.random until a seed is set

let generator = Math.random;

// mulberry32: small, fast 32-bit PRNG
function mulberry32(a) {
  return function () {
    a |= 0;
    a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export function setSeed(seed) {
  generator = mulberry32(Number(seed) >>> 0);
}

export function clearSeed() {
  generator = Math.random;
}

export function random() {
  return generator();
}
