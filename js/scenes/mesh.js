// Mesh — 31 fine lines entering from the middle of the left edge and fanning right: each
// runs east with occasional single-row steps, outer lines stepping more often, so the
// bundle opens like a fan. Graded dbt orange (top) to Fivetran blue (bottom). White ground.

const SPACING = 4; // 396 × 99 grid
const LINES = 31;
const FIRST_ROW = 19; // sources on every second row of the left column, rows 19–79 of 99
const MID = (LINES - 1) / 2;
const MAX_STEP = 0.12; // outermost lines step off level on ~12% of moves

// Each line may only go E (preferred) and, if it is above the middle, NE — below, SE —
// as 'avoid' headings, which the movement takes for a single step and then turns out of.
// So a line's slope is set by how often it skips straight: 1 − straightProb.
//
// Termination: every heading that could reach column 0, or move vertically, is disallowed,
// so cell (r, 1) can only ever be entered from the source (r, 0) beside it — sources sit two
// rows apart, so no other line can take it. A source's E step is therefore always open and
// its line always gets away. (Scene rebuilds the start area for each line, so a source
// whose first step was refused would otherwise be retried forever.) Intelligence 2, not 3,
// for the same reason: 3 refuses a first step into a dead end.
const direction = (i) => (i < MID ? '00210000' : i > MID ? '01200000' : '00200000');
const straightProb = (i) => 1 - (MAX_STEP * Math.abs(i - MID)) / MID;

// LINES samples from dbt orange #FE6703 to Fivetran blue #306BEA, one per line, top to
// bottom. Lines can't cross, so the order holds all the way across.
const orange = [254, 103, 3];
const blue = [48, 107, 234];
const tones = Array.from({ length: LINES }, (_, i) => {
  const t = i / (LINES - 1);
  const [red, green, b] = orange.map((o, k) => Math.round(o + (blue[k] - o) * t));
  return { name: `tone${i}`, red, green, blue: b };
});

const names = Array.from({ length: LINES }, (_, i) => `L${i}`);

export const mesh = {
  slug: 'mesh',
  name: 'Mesh',
  bgcolour: 'white',
  canvasWidth: 1584,
  canvasHeight: 396,
  widthSpacing: SPACING,
  heightSpacing: SPACING,

  colours: tones,

  colourPalettes: [],

  areas: names.map((n, i) => ({
    name: n, x: 0, y: (FIRST_ROW + 2 * i) * SPACING, width: SPACING, height: SPACING,
  })),

  coordBags: [
    { name: 'ordered', type: 'ordered' },
  ],

  movements: names.map((n, i) => ({
    name: n, type: 'intelligent', intelligence: 2, straightProb: straightProb(i), direction: direction(i),
  })),

  lineDraws: names.map((n, i) => ({ name: n, palette: `tone${i}`, strokeWidth: 1 })),

  lines: names.map((n) => ({
    name: n, draw: n, movement: n, startArea: `+${n}`, coordBag: 'ordered', drawSpeed: 4,
  })),

  deploy: names,

  logos: [
    // height 24: dbt 24 × 391/90 = 104.27 wide, Fivetran 24 × 115/32 = 86.25 wide.
    // Right edge at 1544; Fivetran x = 1544 − 86.25 = 1457.75 → 1457;
    // dbt x = 1457 − 18 − 104.27 = 1334.73 → 1334 (round down to keep the gap ≥ 18).
    // Top at the 40 px margin.
    { name: 'dbt-labs', file: 'assets/dbt-labs.svg', x: 1334, y: 40, height: 24, chip: '#fff' },
    { name: 'fivetran', file: 'assets/fivetran-dark.svg', x: 1457, y: 40, height: 24, chip: '#fff' },
  ],
};
