# FortyFive → JavaScript Translation Plan

## Overview
Translate the Java/Processing generative art project into vanilla JavaScript + HTML5 Canvas, deployable as a GitHub Pages site. Full-screen canvas with minimal overlay controls.

## Scope (v1)
**Include:** SolidDraw, RectangleArea, IntelligentMovement, ClingMovement, ColourPalettes (random + linear), CoordinateBags (random, ordered, centre), presentation system (cycling scenes), pause/play, speed control, scene selector.

**Exclude (for now):** ImageDraw, ImageArea, ImageGrid, OSC, command line, threading/worker pools, snapshot/save, YAML parsing.

## File Structure
```
docs/                          ← GitHub Pages serves from /docs
├── index.html                 ← Single HTML page with canvas + controls overlay
├── css/
│   └── style.css              ← Minimal styling for canvas + overlay
├── js/
│   ├── main.js                ← Entry point: init canvas, start loop, wire controls
│   ├── engine.js              ← Core engine: setup/draw loop via requestAnimationFrame
│   ├── scene.js               ← Scene: grid, lines, per-frame stepping logic
│   ├── line.js                ← Line entity: position, direction, movement+draw refs
│   ├── geometry.js            ← Grid math: rows/cols, gridToPixel, pixelToGrid
│   ├── movement/
│   │   ├── intelligent.js     ← IntelligentMovement (straight prob, intelligence levels)
│   │   └── cling.js           ← ClingMovement (wall-hugging neighbor scoring)
│   ├── draw/
│   │   └── solid.js           ← SolidDraw: stroke + line on canvas
│   ├── colour.js              ← Colour, RandomColourPalette, LinearColourPalette
│   ├── areas.js               ← RectangleArea: add/subtract/set/unset on boolean grid
│   ├── coordinatebag.js       ← RandomBag, OrderedBag, CentreBag
│   ├── startarea.js           ← StartArea + coordinate management
│   └── scenes/                ← Built-in scene configs as JS objects
│       ├── index.js           ← Scene registry (exports all scenes)
│       ├── simple.js          ← Basic two-color lines
│       ├── cling.js           ← Cling movement demo
│       └── complex.js         ← Multi-line, multi-area composition
```

## Implementation Steps

### Step 1: HTML + CSS + Canvas setup (`index.html`, `style.css`)
- Full-viewport `<canvas>` element
- Floating overlay in bottom-right: play/pause button, speed slider, scene dropdown
- Dark semi-transparent overlay styling
- Load all JS as ES modules (`<script type="module">`)

### Step 2: Geometry (`geometry.js`)
- Port `SceneGeometry` grid math
- `rows(height, heightSpacing)`, `cols(width, widthSpacing)`
- `gridToPixel(gg, gmax, smax)` = `(2*gg + 1) * (smax / gmax) / 2.0`
- `rowToY()`, `columnToX()`, `yToRow()`, `xToColumn()`

### Step 3: Colour system (`colour.js`)
- `Colour` class: `{ name, r, g, b }` with `toCSS()` → `rgb(r,g,b)`
- `RandomColourPalette`: `current()` / `next()` picks random from list
- `LinearColourPalette`: `next()` indexes by global iteration counter
- Factory function: `createPalette(config, colourMap)` → palette instance

### Step 4: Areas (`areas.js`)
- `RectangleArea` class: `{ name, x, y, width, height }`
- Methods: `add(grid, geometry)`, `subtract(grid, geometry)`, `set(grid, geometry)`, `unset(grid, geometry)`
- Converts pixel bounds to grid coords, sets boolean values

### Step 5: Coordinate bags (`coordinatebag.js`)
- `randomBag(coords)`: Fisher-Yates shuffle
- `orderedBag(coords, leftFirst, topFirst)`: sort by row/col with configurable priority
- `centreBag(coords, rows, cols)`: sort by Euclidean distance from center

### Step 6: Start areas (`startarea.js`)
- `StartArea` class: holds sorted coordinate list, index pointer
- `getNextStartPoint(grid)`: skip occupied cells, return next valid coord
- Factory: given area configs with +/-/./! prefixes, build boolean grid, collect valid coords, apply coordinate bag ordering

### Step 7: Movement strategies (`movement/intelligent.js`, `movement/cling.js`)
- Shared constants: `DR = [1,1,0,-1,-1,-1,0,1]`, `DC = [0,1,1,1,0,-1,-1,-1]`
- `DIR_DISALLOWED=0, DIR_AVOID=1, DIR_PREFERRED=2`

**IntelligentMovement:**
- `forwardOnce(line, scene)`: straight probability check → attempt rotation → opposite rotation → fallback
- Intelligence levels 0-3 control retry behavior and dead-end avoidance
- `hasMove(cr, cc, cd, scene)`: check if any ±1 direction from cd is valid

**ClingMovement:**
- `forwardOnce(line, scene)`: shuffle direction offsets, score each by filled neighbor count, pick highest-scoring valid direction

### Step 8: Draw strategy (`draw/solid.js`)
- `SolidDraw` class: `{ palette, strokeWidth }`
- `drawLine(ctx, px, py, pxx, pyy)`:
  - `ctx.lineWidth = strokeWidth`
  - `ctx.strokeStyle = palette.current().toCSS()`
  - `ctx.beginPath(); ctx.moveTo(px,py); ctx.lineTo(pxx,pyy); ctx.stroke()`

### Step 9: Line entity (`line.js`)
- `Line` class: `{ cr, cc, cd, stepSpeed, drawSpeed, movement, draw, blocked }`
- `forward()`: call `movement.forwardOnce()` stepSpeed times
- `forwardDraw(scene)`: save old pos, forward(), draw segment, return success

### Step 10: Scene (`scene.js`)
- `Scene` class: manages grid[][], lines[], geometry, off-screen canvas
- `setup(config)`:
  1. Build colour map, palette map, area map, movement map, draw map
  2. Create geometry from widthSpacing/heightSpacing
  3. Build boolean grid (rows × cols)
  4. For each line config in deploy list: create StartArea, find valid start, create Line
- `draw(ctx, width, height)`:
  1. For each line: calculate speedRem = drawSpeed * speedMultiplier
  2. Round-robin: while any line has remaining steps, advance each by 1
  3. Each step: line.forwardDraw() draws onto off-screen canvas
  4. Blit off-screen canvas to main canvas
  5. Return true if all lines finished
- `drawLine(oldR, oldC, newR, newC, lineDraw)`: convert grid→pixel, call lineDraw.drawLine()

### Step 11: Engine (`engine.js`)
- `Engine` class: manages canvas, scene lifecycle, animation loop
- `start(sceneConfig)`: create scene, call scene.setup()
- `loop()`: via requestAnimationFrame
  1. If paused, skip
  2. Call scene.draw()
  3. If scene finished, wait briefly then load next scene (or repeat)
  4. Schedule next frame
- `pause()` / `resume()` / `setSpeed(multiplier)` / `loadScene(name)`

### Step 12: Demo scenes (`scenes/*.js`)
Port the "Simple" example from FortyFive.java docs + create 2-3 more interesting configs:
- **simple**: Two colors (red/green), random start, intelligent movement
- **cling**: Wall-clinging movement, warm color palette, rectangle threshold areas
- **complex**: Multiple lines with different movements, varied stroke widths, more colors

### Step 13: Main entry + controls (`main.js`)
- On DOMContentLoaded: size canvas to window, create Engine
- Wire up controls: play/pause button, speed slider (0.5x–5x), scene dropdown
- Handle window resize: resize canvas, restart current scene
- Keyboard shortcuts: space=pause, left/right=prev/next scene

### Step 14: GitHub Pages config
- Add empty `.nojekyll` file in docs/
- The repo settings should point GitHub Pages to the `/docs` folder

## Key Translation Notes

| Java/Processing | JavaScript/Canvas |
|---|---|
| `PGraphics pg = createGraphics(w, h, P2D)` | `offscreen = document.createElement('canvas'); offCtx = offscreen.getContext('2d')` |
| `pg.beginDraw()` / `pg.endDraw()` | Not needed (Canvas is always drawable) |
| `pg.stroke(r, g, b)` | `ctx.strokeStyle = \`rgb(${r},${g},${b})\`` |
| `pg.strokeWeight(w)` | `ctx.lineWidth = w` |
| `pg.line(x1, y1, x2, y2)` | `ctx.beginPath(); ctx.moveTo(x1,y1); ctx.lineTo(x2,y2); ctx.stroke()` |
| `pg.fill(0); pg.rect(0,0,w,h)` | `ctx.fillStyle='black'; ctx.fillRect(0,0,w,h)` |
| `g.image(pg.get(), 0, 0)` | `ctx.drawImage(offscreen, 0, 0)` |
| `RandomSingleton.nextInt(n)` | `Math.floor(Math.random() * n)` |
| `Collections.shuffle(list)` | Fisher-Yates shuffle |
| `synchronized` blocks | Not needed (JS is single-threaded) |
| `ExecutorService` | Not needed |
| Frame rate via `frameRate(fps)` | requestAnimationFrame with frame timing |

## Order of Implementation
1. Steps 1-2: HTML/Canvas + Geometry (foundation)
2. Steps 3-6: Colour, Areas, CoordBags, StartAreas (data layer)
3. Steps 7-9: Movement, Draw, Line (core entities)
4. Steps 10-11: Scene + Engine (orchestration)
5. Steps 12-13: Demo scenes + Controls (user-facing)
6. Step 14: GitHub Pages setup
