---
name: add-scene
description: Add a new generative art scene to the FortyFive web app
user_invocable: true
---

# Add a FortyFive Scene

The user wants to add a new scene to the FortyFive generative art web app.

## Context

FortyFive is a generative art engine that draws colored lines on a grid. Lines start from
designated areas, move according to movement strategies, and draw with colour palettes.
The web version lives at the repo root and scenes are JS config objects in
`apps/demo/scenes/`.

## Scene Config Schema

Each scene is a JS object with these sections:

```js
export const myScene = {
  name: 'Display Name',           // shown in UI dropdown
  bgcolour: 'black',              // 'black' or 'white'
  widthSpacing: 8,                // grid cell width in pixels (smaller = more cells = finer detail)
  heightSpacing: 8,               // grid cell height in pixels

  colours: [                      // define named colours (RGB 0-255)
    { name: 'cyan', red: 0, green: 255, blue: 255 },
  ],

  colourPalettes: [               // group colours into palettes
    { name: 'pal', colours: ['cyan', ...], mode: 'random' }, // mode: 'random' | 'linear'
  ],

  areas: [                        // rectangular regions on canvas
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
    // fractional sizes: x: '1/4', width: '1/2'
  ],

  coordBags: [                    // order that start points are tried
    { name: 'bag', type: 'random' }, // type: 'random' | 'ordered' | 'centre'
  ],

  movements: [                    // how lines navigate the grid
    {
      name: 'mov',
      type: 'intelligent',        // 'intelligent' | 'cling'
      intelligence: 2,            // 0=blind, 1=no-retry, 2=retry, 3=avoids dead-ends
      straightProb: 0.8,          // 0.0-1.0, probability of going straight
      // direction: '22222222',   // 8-char: indices 0=S,1=SE,2=E,3=NE,4=N,5=NW,6=W,7=SW
                                  // values: 0=disallowed, 1=avoid, 2=preferred
    },
  ],

  lineDraws: [                    // visual style for lines
    { name: 'draw', palette: 'pal', strokeWidth: 3 },
  ],

  lines: [                        // line templates combining movement + draw + area
    {
      name: 'A',
      draw: 'draw',               // references lineDraws name
      movement: 'mov',            // references movements name
      startArea: '+all',          // prefix: + (add), - (subtract), . (set), ! (unset)
      coordBag: 'bag',            // references coordBags name
      drawSpeed: 1,               // segments drawn per frame (higher = faster)
      // threshold: '+someArea',  // areas where this line CANNOT go
    },
  ],

  deploy: ['A'],                  // which line names to activate (can repeat names)
};
```

## Steps to Add a Scene

1. **Ask the user** what kind of scene they want if they haven't described it. Offer creative suggestions based on what the building blocks can produce (e.g., "dense neon maze", "slow organic growth from center", "wall-hugging coral reef", "diagonal-only crystal").

2. **Design the scene config** based on the user's description. Key creative levers:
   - More lines in `deploy` = more concurrent drawing threads
   - Lower `widthSpacing`/`heightSpacing` = finer grid = more detail
   - `cling` movement creates organic, wall-hugging patterns; `intelligent` creates more maze-like paths
   - Higher `intelligence` (2-3) makes lines avoid getting stuck
   - Lower `straightProb` (0.3-0.5) = curvy lines; higher (0.8-0.95) = straighter paths
   - Direction strings can force diagonal-only (`"02020202"`), cardinal-only (`"20202020"`), etc.
   - `threshold` areas create blocked zones that lines must navigate around
   - `centre` coord bag makes lines start from the middle; `ordered` fills systematically
   - `linear` palette mode cycles colours in order; `random` picks randomly

3. **Create the scene file** at `apps/demo/scenes/<name>.js` following the existing pattern.

4. **Register it** in `apps/demo/scenes/index.js` — add the import and append to the `scenes` array.

5. **Validate** with a headless smoke test — no browser, no DOM stub:
   ```js
   // A temp test file run with node: import Scene and the new config, loop scene.draw(8)
   // until scene.finished, then read scene.frame() (the display list)
   // Verify cells are filled > 0, primitives are non-empty, and no error is thrown
   // packages/core/src/scene.js is the working example
   ```

6. **Commit and push** the new scene.

## Existing Scenes for Reference

Read these files for inspiration and to match the code style:
- `apps/demo/scenes/simple.js` — minimal 2-line scene
- `apps/demo/scenes/wanderer.js` — 3 cling-movement lines with warm palette
- `apps/demo/scenes/neon.js` — 5 fast thin lines with neon colours
- `apps/demo/scenes/labyrinth.js` — ordered start + threshold areas
