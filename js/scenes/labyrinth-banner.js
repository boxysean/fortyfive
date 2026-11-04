// Labyrinth Banner — the labyrinth recipe retuned to 4:1: ordered traces in dbt orange and
// Fivetran blue route around four component blocks, which are hatched in orange. Dark ground.

const CELL = 6;

// Component blocks. Traces may not enter them; the hatch lines may not leave them.
const pads = [
  { name: 'pad0', x: 144, y: 60, width: 180, height: 96 },
  { name: 'pad1', x: 468, y: 222, width: 240, height: 108 },
  { name: 'pad2', x: 822, y: 48, width: 150, height: 150 },
  { name: 'pad3', x: 1062, y: 252, width: 198, height: 90 },
];

// Logo boxes (below) plus 12 px padding = x 1322–1555.75, y 174–222, snapped outward to the
// 6 px grid. Every line routes around it, so the chip sits in a clearing.
const plate = { name: 'plate', x: 1320, y: 174, width: 240, height: 48 };

// Scene picks a new line's first heading without looking at its threshold, and retries a
// start cell forever if that first step is refused. So traces never start in the ring
// of cells just outside a block, and hatches never start in the ring just inside one.
const grow = (a, by, suffix) => ({
  name: `${a.name}${suffix}`, x: a.x - by, y: a.y - by, width: a.width + 2 * by, height: a.height + 2 * by,
});
const halos = [...pads, plate].map((a) => grow(a, CELL, 'Halo'));
const cores = pads.map((p) => grow(p, -CELL, 'Core'));

const traceBlocked = [...pads.map((p) => `+${p.name}`), '+plate'];
const traceStart = ['+all', ...halos.map((h) => `-${h.name}`)];
const hatchBlocked = ['+all', ...pads.map((p) => `-${p.name}`)];
const hatchStart = cores.map((c) => `+${c.name}`);

export const labyrinthBanner = {
  slug: 'labyrinth-banner',
  name: 'Labyrinth Banner',
  bgcolour: 'black',
  canvasWidth: 1584,
  canvasHeight: 396,
  widthSpacing: CELL,
  heightSpacing: CELL,

  colours: [
    { name: 'orange', red: 254, green: 103, blue: 3 }, // #FE6703
    { name: 'blue', red: 48, green: 107, blue: 234 }, // #306BEA
  ],

  colourPalettes: [
    { name: 'brand', colours: ['orange', 'blue'], mode: 'random' },
  ],

  areas: [
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
    ...pads,
    plate,
    ...halos,
    ...cores,
  ],

  coordBags: [
    { name: 'ordered', type: 'ordered' },
  ],

  movements: [
    // Intelligence 2, not 3 — see the start-area note above
    { name: 'explore', type: 'intelligent', intelligence: 2, straightProb: 0.8 },
    // Almost always straight and cardinal-preferred: fills each block with long parallel runs
    { name: 'hatch', type: 'intelligent', intelligence: 2, straightProb: 0.97, direction: '21212121' },
  ],

  lineDraws: [
    { name: 'trace', palette: 'brand', strokeWidth: 2 },
    { name: 'hatch', palette: 'orange', strokeWidth: 1 },
  ],

  lines: [
    { name: 'A', draw: 'trace', movement: 'explore', startArea: traceStart, coordBag: 'ordered', drawSpeed: 2, threshold: traceBlocked },
    { name: 'B', draw: 'hatch', movement: 'hatch', startArea: hatchStart, coordBag: 'ordered', drawSpeed: 2, threshold: hatchBlocked },
    { name: 'C', draw: 'trace', movement: 'explore', startArea: traceStart, coordBag: 'ordered', drawSpeed: 2, threshold: traceBlocked },
  ],

  deploy: ['A', 'B', 'C'],

  logos: [
    // height 24: dbt 24 × 391/90 = 104.27 wide, Fivetran 24 × 115/32 = 86.25 wide.
    // Right edge at 1544; Fivetran x = 1544 − 86.25 = 1457.75 → 1457;
    // dbt x = 1457 − 18 − 104.27 = 1334.73 → 1334. Centred: y = (396 − 24) / 2 = 186.
    { name: 'dbt-labs', file: 'assets/dbt-labs.svg', x: 1334, y: 186, height: 24, chip: '#fff' },
    { name: 'fivetran', file: 'assets/fivetran-dark.svg', x: 1457, y: 186, height: 24, chip: '#fff' },
  ],
};
