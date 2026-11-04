// Cable Run — one heavy ribbon of parallel strands crossing the 4:1 strip left to right,
// stepping from dbt orange to Fivetran blue along the run. White ground.

const WIDTH = 1584;
const SPACING = 12;
const COLS = WIDTH / SPACING; // 132
const STRANDS = 6;
const FIRST_ROW = 10; // ribbon enters at rows 10–15 of 33 and sags right-down from there
const STEPS = 8;

// Every move is E, NE or SE, so each segment advances exactly one column. Each strand
// owns a fresh linear palette with one entry per column, so colour tracks x position.
const orange = [254, 103, 3]; // #FE6703
const blue = [48, 107, 234]; // #306BEA
const stepColours = Array.from({ length: STEPS }, (_, i) => {
  const t = i / (STEPS - 1);
  const [red, green, blue_] = orange.map((o, k) => Math.round(o + (blue[k] - o) * t));
  return { name: `step${i}`, red, green, blue: blue_ };
});
const ramp = Array.from({ length: COLS - 1 }, (_, c) => `step${Math.floor((c * STEPS) / (COLS - 1))}`);

const strands = Array.from({ length: STRANDS }, (_, i) => `S${i}`);

// Knockout: logo boxes (below) plus 12 px padding = x 1315–1555.84, y 319–368, snapped
// outward to the 12 px grid (areas floor their far edge) — lines may not enter it
const knockout = { name: 'knockout', x: 1308, y: 312, width: 252, height: 60 };

export const cableRun = {
  slug: 'cable-run',
  name: 'Cable Run',
  bgcolour: 'white',
  canvasWidth: 1584,
  canvasHeight: 396,
  widthSpacing: SPACING,
  heightSpacing: SPACING,

  colours: stepColours,

  colourPalettes: strands.map((s) => ({ name: s, colours: ramp, mode: 'linear' })),

  areas: [
    knockout,
    // S0 is the bottom strand; deploy order is bottom-up so it always moves first each round
    ...strands.map((s, i) => ({
      name: s, x: 0, y: (FIRST_ROW + STRANDS - 1 - i) * SPACING, width: SPACING, height: SPACING,
    })),
  ],

  coordBags: [
    { name: 'ordered', type: 'ordered' },
  ],

  movements: [
    // The bottom strand steers: straight east with occasional one-step drops (SE only)
    { name: 'lead', type: 'intelligent', intelligence: 3, straightProb: 0.92, direction: '01200000' },
    // Strands above cling to the one below, so they drop with it and the ribbon sags as one
    { name: 'follow', type: 'cling', direction: '02210000' },
  ],

  lineDraws: strands.map((s) => ({ name: s, palette: s, strokeWidth: 10 })),

  lines: strands.map((s, i) => ({
    name: s,
    draw: s,
    movement: i === 0 ? 'lead' : 'follow',
    startArea: `+${s}`,
    coordBag: 'ordered',
    drawSpeed: 2,
    threshold: '+knockout',
  })),

  deploy: strands,

  logos: [
    // height 25: dbt 25 × 391/90 = 108.61 wide, Fivetran 25 × 115/32 = 89.84 wide.
    // Right edge at 1584 − 40 = 1544; Fivetran x = 1544 − 89.84 → 1454;
    // dbt x = 1454 − 18 − 108.61 → 1327. Bottom at 396 − 40 = 356 → y 331.
    { name: 'dbt-labs', file: 'assets/dbt-labs.svg', x: 1327, y: 331, height: 25 },
    { name: 'fivetran', file: 'assets/fivetran-dark.svg', x: 1454, y: 331, height: 25 },
  ],
};
