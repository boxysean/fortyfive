// Wanderer Banner — one slow orange line wandering the whole strip on black, leaving most
// of the ground empty. It starts from a single cell on the left edge and meanders, in small
// steps, across to the right edge, where the scene ends.

// Logo boxes (below) plus 12 px padding = x 1355–1555.88, y 176–220. The knockout runs
// from the column boundary left of that (x 1344, col 168) to the right edge, full height:
// a wall rather than a box, so the line ends there instead of dying on a box corner
// halfway across, and the small unplated marks sit in a clear panel of their own.
const knockout = { name: 'knockout', x: 1344, y: 0, width: 240, height: 'height' };

export const wandererBanner = {
  slug: 'wanderer-banner',
  name: 'Wanderer Banner',
  bgcolour: 'black',
  canvasWidth: 1584,
  canvasHeight: 396,
  // 198 × 49, 8 px square cells: fine enough that a diagonal step is a small kink rather
  // than a 33 px cliff. The line crosses the 168 columns before the wall, at least one cell
  // each, so it fills at least 168 / (198 × 49) = 1.7% of the grid — deliberately sparse.
  widthSpacing: 8,
  heightSpacing: 8,

  colours: [
    { name: 'orange', red: 254, green: 103, blue: 3 }, // #FE6703
  ],

  colourPalettes: [
    { name: 'orange', colours: ['orange'] },
  ],

  areas: [
    { name: 'source', x: 0, y: 198, width: 8, height: 8 },
    knockout,
  ],

  coordBags: [
    { name: 'random', type: 'random' },
  ],

  movements: [
    // East, north-east and south-east only — no straight north or south, so there are no
    // tall zigzags, and at the wall every way forward is blocked and the line just ends
    // (with N/S allowed it turns and climbs the wall face to the edge). straightProb 0.75:
    // a diagonal can only turn into east or the other diagonal, so diagonals average about
    // 4 steps; from east, half the turn rolls land on a disallowed N/S and fall back to
    // straight, so the flat runs average about 8. The line never goes west, so it cannot
    // trap itself and always reaches the wall. Intelligence 3 makes it turn before it hits
    // the top or bottom edge rather than die there. That is only safe because the source
    // is one cell and the line is alone: Scene retries a start cell forever if its first
    // step is refused, and 3 can refuse — but never on the empty grid this line starts on,
    // and after it the source is filled.
    { name: 'drift', type: 'intelligent', intelligence: 3, straightProb: 0.75, direction: '02220000' },
  ],

  lineDraws: [
    { name: 'A', palette: 'orange', strokeWidth: 2 },
  ],

  lines: [
    { name: 'A', draw: 'A', movement: 'drift', startArea: '+source', coordBag: 'random', drawSpeed: 1, threshold: '+knockout' },
  ],

  deploy: ['A'],

  logos: [
    // height 20: dbt 20 × 391/90 = 86.89 wide, Fivetran 20 × 115/32 = 71.88 wide.
    // Right edge at 1544; Fivetran x = 1544 − 71.88 = 1472.12 → 1472;
    // dbt x = 1472 − 18 − 86.89 = 1367.11 → 1367. Centred: y = (396 − 20) / 2 = 188.
    { name: 'dbt-labs', file: 'assets/dbt-labs.svg', x: 1367, y: 188, height: 20, monochrome: true, opacity: 0.9 },
    { name: 'fivetran', file: 'assets/fivetran-white.svg', x: 1472, y: 188, height: 20, monochrome: true, opacity: 0.9 },
  ],
};
