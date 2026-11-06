// Three motif studies for the homepage journey, drawn on the fortyfive grid and
// serialised with @fortyfive/render-svg (the same display list the engine emits).
//   node gen.mjs   -> hat.svg empire.svg stephansdom.svg
import { frameToSvg } from '../../../packages/render-svg/src/index.js';
import { writeFileSync } from 'node:fs';

const W = 1000, H = 700, STEP = 4;
const BG = '#0B0F16', INK = '#E9EEF7', ACCENT = '#FE6703';
const INK_THIN = '#7C8BA1';

const snap = (v) => Math.round(v / STEP) * STEP;
const P = (pts, colour = INK, width = 3, cap = 'round') => ({
  type: 'path', d: pts.map(([x, y]) => [snap(x), snap(y)]), stroke: colour, width, cap,
});

function bez(p0, p1, p2, p3, n = 28) {
  const out = [];
  for (let i = 0; i <= n; i++) {
    const t = i / n, u = 1 - t;
    out.push([
      u * u * u * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t * t * t * p3[0],
      u * u * u * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t * t * t * p3[1],
    ]);
  }
  return out;
}

const rect = () => ({ type: 'rect', x: 0, y: 0, w: W, h: H, fill: BG });

// ---------------------------------------------------------------- cowboy hat
function hat() {
  const p = [rect()];
  const line = (pts, w = 3, c = INK) => p.push(P(pts, c, w));

  // brim: bottom sweep, then the top sweep meeting it at both tips
  line(bez([140, 470], [230, 575], [770, 575], [860, 470]), 3);
  line(bez([140, 470], [250, 512], [750, 512], [860, 470]), 3);
  // the tips turn up
  line([[140, 470], [120, 440]], 3);
  line([[860, 470], [880, 440]], 3);

  // crown sits on the brim: tall, tapering, with a centre dent
  line([[340, 500], [382, 268]], 3);
  line([[660, 500], [618, 268]], 3);
  line(bez([382, 268], [420, 314], [460, 330], [500, 324]), 3);
  line(bez([618, 268], [580, 314], [540, 330], [500, 324]), 3);
  // the pinched front dents, thin so the silhouette still rules
  line(bez([368, 292], [400, 326], [430, 352], [470, 360]), 1.5, INK_THIN);
  line(bez([632, 292], [600, 326], [570, 352], [530, 360]), 1.5, INK_THIN);

  // hatband
  line([[343, 452], [657, 452]], 2);
  line([[341, 472], [659, 472]], 2);

  // brim stitching: dashes hanging off the outer curve
  for (let i = 1; i < 26; i++) {
    const t = i / 26;
    const x = 150 + t * 700;
    const y = 512 + Math.sin(Math.PI * t) * 46;
    line([[x, y], [x, y + 14]], 1.2, INK_THIN);
  }
  return p;
}

// ------------------------------------------------------- empire state building
function empire() {
  const p = [rect()];
  const line = (pts, w = 3, c = INK) => p.push(P(pts, c, w));

  // silhouette: three broad setbacks, then a long slim shaft, then the mast
  const tiers = [
    { x0: 380, x1: 620, y0: 640, y1: 566 },
    { x0: 402, x1: 598, y0: 566, y1: 520 },
    { x0: 424, x1: 576, y0: 520, y1: 476 },
  ];
  for (const t of tiers) {
    line([[t.x0, t.y1], [t.x0, t.y0]], 3);
    line([[t.x1, t.y1], [t.x1, t.y0]], 3);
    line([[t.x0, t.y1], [t.x1, t.y1]], 2);
  }
  // shaft: continuous verticals, the thing that makes it read as one tower
  line([[444, 476], [444, 214]], 4);
  line([[556, 476], [556, 214]], 4);
  // upper setbacks
  line([[444, 214], [466, 214]], 2); line([[534, 214], [556, 214]], 2);
  line([[466, 214], [466, 186]], 3); line([[534, 214], [534, 186]], 3);
  line([[466, 186], [534, 186]], 2);
  line([[478, 186], [478, 160]], 2); line([[522, 186], [522, 160]], 2);
  line([[478, 160], [522, 160]], 2);

  // mast and antenna, the accent
  line([[492, 160], [492, 92]], 3, ACCENT);
  line([[508, 160], [508, 92]], 3, ACCENT);
  line([[492, 92], [500, 56]], 2, ACCENT);
  line([[508, 92], [500, 56]], 2, ACCENT);

  // window ticks: short, low contrast, only inside the mass
  for (let y = 240; y <= 460; y += 26) {
    line([[460, y], [486, y]], 1.2, INK_THIN);
    line([[514, y], [540, y]], 1.2, INK_THIN);
  }
  for (let y = 500; y <= 620; y += 24) {
    line([[442, y], [470, y]], 1.2, INK_THIN);
    line([[530, y], [558, y]], 1.2, INK_THIN);
  }
  line([[200, 640], [800, 640]], 1.5, INK_THIN);
  return p;
}

// ---------------------------------------------------------------- stephansdom
function stephansdom() {
  const p = [rect()];
  const line = (pts, w = 3, c = INK) => p.push(P(pts, c, w));
  const G = 640, EAVE = 470, RIDGE = 300;

  // nave walls
  line([[350, G], [350, EAVE]], 3);
  line([[650, G], [650, EAVE]], 3);
  // steep gable roof
  line([[336, EAVE], [500, RIDGE], [664, EAVE]], 3);
  // tiles: chevrons inside the triangle only
  for (let y = EAVE - 14; y > RIDGE + 12; y -= 20) {
    const half = ((y - RIDGE) / (EAVE - RIDGE)) * 164;
    for (let x = 500 - half + 14; x < 500 + half - 14; x += 30) {
      line([[x, y], [x + 15, y - 15]], 1.2, INK_THIN);
      line([[x + 15, y], [x, y - 15]], 1.2, INK_THIN);
    }
  }

  // south tower, right: the tall spire
  line([[664, G], [664, 320]], 3);
  line([[756, G], [756, 320]], 3);
  line([[664, 320], [710, 150]], 3);
  line([[756, 320], [710, 150]], 3);
  line([[710, 150], [710, 118]], 2);
  line([[696, 130], [724, 130]], 2);
  for (let y = 350; y < G - 24; y += 34) line([[678, y], [742, y]], 1.2, INK_THIN);

  // north tower, left: shorter, pyramid cap
  line([[258, G], [258, 360]], 3);
  line([[346, G], [346, 360]], 3);
  line([[258, 360], [302, 300]], 3);
  line([[346, 360], [302, 300]], 3);
  line([[258, 338], [346, 338]], 1.5, INK_THIN);
  line([[280, 320], [324, 320]], 1.5, INK_THIN);
  for (let y = 370; y < G - 24; y += 34) line([[272, y], [332, y]], 1.2, INK_THIN);

  // portal, base of the nave
  line([[452, G], [452, 566]], 3);
  line([[548, G], [548, 566]], 3);
  p.push(P([[452, 566],
            ...bez([452, 566], [452, 540], [476, 522], [500, 518]),
            ...bez([500, 518], [524, 522], [548, 540], [548, 566])], INK, 3));

  // rose window, on the wall, the accent
  const cx = 500, cy = 500, r = 18;
  const ring = [];
  for (let a = 0; a <= 36; a++) {
    const th = (a / 36) * Math.PI * 2;
    ring.push([cx + Math.cos(th) * r, cy + Math.sin(th) * r]);
  }
  p.push(P(ring, ACCENT, 3));
  for (let a = 0; a < 8; a++) {
    const th = (a / 8) * Math.PI * 2;
    line([[cx + Math.cos(th) * 5, cy + Math.sin(th) * 5],
          [cx + Math.cos(th) * (r - 3), cy + Math.sin(th) * (r - 3)]], 1.4, ACCENT);
  }

  line([[200, G], [820, G]], 1.5, INK_THIN);
  return p;
}

const figures = { hat, empire, stephansdom };
for (const [name, build] of Object.entries(figures)) {
  const svg = await frameToSvg(build(), { width: W, height: H });
  writeFileSync(`/opt/data/cache/scratch/motifs/${name}.svg`, svg);
  console.log(name, svg.length, 'bytes');
}
