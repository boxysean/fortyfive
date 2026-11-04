// Wanderer Banner — one slow orange line wandering the whole strip on black, leaving most
// of the ground empty. It starts from a single cell on the left edge and sweeps up and down
// as it crosses to the right edge, where the scene ends.

// Logo boxes (below) plus 12 px padding = x 1355–1555.88, y 176–220. The knockout runs
// from the 22 px column left of that (col 61) to the right edge, full height: a wall rather
// than a box, so the line ends there instead of dying on a box corner halfway across, and
// the small unplated marks sit in a clear panel of their own.
const knockout = { name: 'knockout', x: 1342, y: 0, width: 242, height: 'height' };

export const wandererBanner = {
  slug: 'wanderer-banner',
  name: 'Wanderer Banner',
  bgcolour: 'black',
  canvasWidth: 1584,
  canvasHeight: 396,
  // 72 × 12. The line crosses the 61 columns before the wall, at least one cell each, so
  // it always fills at least 61 / (72 × 12) = 7.1% of the grid. Any finer and a run that
  // happens to cross mostly diagonally drops under the 5% floor (18 rows: 4.7%).
  widthSpacing: 22,
  heightSpacing: 33,

  colours: [
    { name: 'orange', red: 254, green: 103, blue: 3 }, // #FE6703
  ],

  colourPalettes: [
    { name: 'orange', colours: ['orange'] },
  ],

  areas: [
    { name: 'source', x: 0, y: 198, width: 22, height: 33 },
    knockout,
  ],

  coordBags: [
    { name: 'random', type: 'random' },
  ],

  movements: [
    // Long straight runs north, south and east, never west: everything east of the line is
    // still empty, so it cannot trap itself and always crosses the whole strip. Intelligence
    // 3 makes it turn before it hits an edge rather than die there. That is only safe
    // because the source is one cell and the line is alone: Scene retries a start cell
    // forever if its first step is refused, and 3 can refuse — but never on the empty grid
    // this line starts on, and after it the source is filled.
    { name: 'drift', type: 'intelligent', intelligence: 3, straightProb: 0.9, direction: '22122000' },
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
