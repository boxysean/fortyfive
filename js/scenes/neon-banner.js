// Neon Banner — the neon recipe at 4:1: eight thin fast lines in hot brand tints plus cyan
// and magenta, scattered from random starts over black.

// Logo boxes (below) plus 12 px padding = x 28–268.84, y 319–368, snapped outward to the
// 6 px grid. The monochrome marks sit on clear ground instead of on top of strokes.
const knockout = { name: 'knockout', x: 24, y: 318, width: 246, height: 54 };
// Lines never start in the ring of cells around the knockout: Scene picks a first heading
// without looking at the threshold, and retries a refused start cell forever
const halo = { name: 'halo', x: 18, y: 312, width: 258, height: 66 };

const names = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

export const neonBanner = {
  slug: 'neon-banner',
  name: 'Neon Banner',
  bgcolour: 'black',
  canvasWidth: 1584,
  canvasHeight: 396,
  widthSpacing: 6,
  heightSpacing: 6,

  colours: [
    { name: 'orange', red: 254, green: 103, blue: 3 }, // dbt #FE6703
    { name: 'electric', red: 92, green: 155, blue: 255 }, // Fivetran #306BEA, lifted for black
    { name: 'cyan', red: 0, green: 229, blue: 255 },
    { name: 'magenta', red: 255, green: 46, blue: 196 },
  ],

  colourPalettes: [
    { name: 'neon', colours: ['orange', 'electric', 'cyan', 'magenta'], mode: 'random' },
  ],

  areas: [
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
    knockout,
    halo,
  ],

  coordBags: [
    { name: 'random', type: 'random' },
  ],

  movements: [
    // Intelligence 2, not neon's 3 — 3 refuses dead-end first steps, which livelocks here
    { name: 'fast', type: 'intelligent', intelligence: 2, straightProb: 0.85 },
  ],

  lineDraws: names.map((n) => ({ name: n, palette: 'neon', strokeWidth: 2 })),

  lines: names.map((n) => ({
    name: n, draw: n, movement: 'fast', startArea: ['+all', '-halo'], coordBag: 'random', drawSpeed: 3, threshold: '+knockout',
  })),

  deploy: names,

  logos: [
    // height 25: dbt 25 × 391/90 = 108.61 wide, Fivetran 25 × 115/32 = 89.84 wide.
    // Left edge at the 40 px margin; Fivetran x = 40 + 108.61 + 18 = 166.61 → 167.
    // Bottom at 396 − 40 = 356 → y 331.
    { name: 'dbt-labs', file: 'assets/dbt-labs.svg', x: 40, y: 331, height: 25, monochrome: true, opacity: 0.85 },
    { name: 'fivetran', file: 'assets/fivetran-white.svg', x: 167, y: 331, height: 25, monochrome: true, opacity: 0.85 },
  ],
};
