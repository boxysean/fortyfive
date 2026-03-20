// Scene — port of Scene.java + SceneFactory.java
// Orchestrates grid, lines, colours, areas, movements, and draws

import { Geometry } from './geometry.js';
import { createColour, createPalette } from './colour.js';
import { createArea } from './areas.js';
import { Line } from './line.js';
import { SolidDraw } from './draw/solid.js';
import { IntelligentMovement } from './movement/intelligent.js';
import { ClingMovement } from './movement/cling.js';
import { shuffle, applyBag } from './coordinatebag.js';
import { StartArea, parseAreaRef } from './startarea.js';
import { DR, DC } from './movement/common.js';

export class Scene {
  constructor(config, width, height) {
    this.config = config;
    this.width = width;
    this.height = height;
    this.finished = false;
    this.iterations = 0;

    const ws = config.widthSpacing || 10;
    const hs = config.heightSpacing || 10;
    this.geometry = new Geometry(width, height, ws, hs);
    this.rows = this.geometry.rows;
    this.cols = this.geometry.cols;

    // Off-screen canvas for accumulative drawing
    this.offscreen = document.createElement('canvas');
    this.offscreen.width = width;
    this.offscreen.height = height;
    this.offCtx = this.offscreen.getContext('2d');

    // Initialize background
    const bg = (config.bgcolour || 'black').toLowerCase();
    this.offCtx.fillStyle = bg === 'white' ? '#fff' : '#000';
    this.offCtx.fillRect(0, 0, width, height);

    // Build registries from config
    this.colourMap = new Map();
    this.paletteMap = new Map();
    this.areaMap = new Map();
    this.coordBagMap = new Map();
    this.movementMap = new Map();
    this.drawMap = new Map();
    this.lineFactoryMap = new Map();

    this._buildColours(config.colours || []);
    this._buildPalettes(config.colourPalettes || []);
    this._buildAreas(config.areas || []);
    this._buildCoordBags(config.coordBags || []);
    this._buildMovements(config.movements || []);
    this._buildDraws(config.lineDraws || []);
    this._buildLineFactories(config.lines || []);

    // Create grid and lines
    this.grid = Array.from({ length: this.rows }, () => new Array(this.cols).fill(false));
    this.dlist = [0, 1, 2, 3, 4, 5, 6, 7];
    shuffle(this.dlist);

    const deploy = config.deploy || [];
    this.lineNames = deploy;
    this.lines = new Array(deploy.length).fill(null);
    this.speedRem = new Array(deploy.length).fill(0);

    for (let i = 0; i < deploy.length; i++) {
      const factory = this.lineFactoryMap.get(deploy[i]);
      if (factory) {
        this.lines[i] = this._newLine(factory);
      }
    }
  }

  // --- Registry builders ---

  _buildColours(list) {
    for (const cfg of list) {
      this.colourMap.set(cfg.name, createColour(cfg));
    }
  }

  _buildPalettes(list) {
    for (const cfg of list) {
      this.paletteMap.set(cfg.name, createPalette(cfg, this.colourMap));
    }
  }

  _buildAreas(list) {
    for (const cfg of list) {
      this.areaMap.set(cfg.name, createArea(cfg, this.width, this.height));
    }
  }

  _buildCoordBags(list) {
    for (const cfg of list) {
      this.coordBagMap.set(cfg.name, cfg.type || 'random');
    }
  }

  _buildMovements(list) {
    for (const cfg of list) {
      const type = (cfg.type || 'intelligent').toLowerCase();
      if (type.startsWith('cling')) {
        this.movementMap.set(cfg.name, { type: 'cling', config: cfg });
      } else {
        this.movementMap.set(cfg.name, { type: 'intelligent', config: cfg });
      }
    }
  }

  _buildDraws(list) {
    for (const cfg of list) {
      const paletteName = cfg.palette || cfg.paletteName || cfg.name;
      const palette = this.paletteMap.get(paletteName) || this.colourMap.get(paletteName);
      if (!palette) {
        console.warn(`Unknown palette: ${paletteName}`);
        continue;
      }
      this.drawMap.set(cfg.name, new SolidDraw(cfg.name, palette, cfg.strokeWidth || 1));
    }
  }

  _buildLineFactories(list) {
    for (const cfg of list) {
      const name = cfg.name;
      const movementName = cfg.movement || name;
      const drawName = cfg.draw || cfg.linedraw || name;
      const coordBagName = cfg.coordBag || name;
      const stepSpeed = cfg.stepSpeed || 1;
      const drawSpeed = cfg.drawSpeed || 1;

      // Build blocked grid from thresholds
      let blocked = null;
      if (cfg.threshold) {
        const thresholdNames = Array.isArray(cfg.threshold) ? cfg.threshold : [cfg.threshold];
        blocked = Array.from({ length: this.rows }, () => new Array(this.cols).fill(false));
        for (const ref of thresholdNames) {
          const { prefix, name: areaName } = parseAreaRef(ref);
          const area = this.areaMap.get(areaName);
          if (area) {
            switch (prefix) {
              case '+': area.add(blocked, this.geometry); break;
              case '-': area.subtract(blocked, this.geometry); break;
              case '.': area.set(blocked, this.geometry); break;
              case '!': area.unset(blocked, this.geometry); break;
            }
          }
        }
      }

      // Build start area
      const startAreaNames = cfg.startArea
        ? (Array.isArray(cfg.startArea) ? cfg.startArea : [cfg.startArea])
        : [];
      const startAreaRefs = startAreaNames.map(parseAreaRef);

      const coordBagType = this.coordBagMap.get(coordBagName) || 'random';

      this.lineFactoryMap.set(name, {
        name,
        movementName,
        drawName,
        stepSpeed,
        drawSpeed,
        blocked,
        startAreaRefs,
        coordBagType,
      });
    }
  }

  // --- Line creation ---

  _createStartArea(factory) {
    const { startAreaRefs, coordBagType, blocked } = factory;
    const rows = this.rows;
    const cols = this.cols;

    // Build valid grid from area operations
    const valid = Array.from({ length: rows }, () => new Array(cols).fill(false));
    for (const { prefix, name } of startAreaRefs) {
      const area = this.areaMap.get(name);
      if (!area) continue;
      switch (prefix) {
        case '+': area.add(valid, this.geometry); break;
        case '-': area.subtract(valid, this.geometry); break;
        case '.': area.set(valid, this.geometry); break;
        case '!': area.unset(valid, this.geometry); break;
      }
    }

    // Collect valid, unblocked coordinates
    const coords = [];
    for (let r = 0; r < rows; r++) {
      for (let c = 0; c < cols; c++) {
        if (valid[r][c] && (!blocked || !blocked[r][c])) {
          coords.push({ r, c });
        }
      }
    }

    applyBag(coordBagType, coords, rows, cols);
    return new StartArea(coords);
  }

  _newLine(factory) {
    const startArea = this._createStartArea(factory);
    let gd = -1;
    let gr, gc;

    while (gd === -1) {
      if (!startArea.getNextStartPoint(this.grid)) {
        return null; // No more valid start points
      }

      gr = startArea.gr;
      gc = startArea.gc;

      // Pick best direction from shuffled list
      let highestValue = -1;
      let highestDirection = -1;

      const movementEntry = this.movementMap.get(factory.movementName);
      const dirArray = movementEntry
        ? parseDirection(movementEntry.config.direction || '22222222')
        : [2, 2, 2, 2, 2, 2, 2, 2];

      for (const d of this.dlist) {
        if (dirArray[d] > highestValue) {
          const nr = gr + DR[d];
          const nc = gc + DC[d];
          if (!this.invalidMove(gr, gc, nr, nc)) {
            highestValue = dirArray[d];
            highestDirection = d;
          }
        }
      }

      gd = highestDirection;
      shuffle(this.dlist);
    }

    // Create movement instance
    const movementEntry = this.movementMap.get(factory.movementName);
    let movement;
    if (movementEntry && movementEntry.type === 'cling') {
      movement = new ClingMovement(movementEntry.config);
    } else if (movementEntry) {
      movement = new IntelligentMovement(movementEntry.config);
    } else {
      movement = new IntelligentMovement({ name: factory.name });
    }

    const draw = this.drawMap.get(factory.drawName);
    if (!draw) {
      console.warn(`Unknown draw: ${factory.drawName}`);
      return null;
    }

    return new Line(gr, gc, gd, factory.stepSpeed, factory.drawSpeed, movement, draw, factory.blocked);
  }

  // --- Per-frame rendering ---

  /** Returns true if all lines are finished */
  draw(speedMultiplier) {
    let finished = true;

    // Calculate speed for each line
    for (let i = 0; i < this.lines.length; i++) {
      if (this.lines[i] && this.lines[i].alive) {
        this.speedRem[i] = this.lines[i].drawSpeed * speedMultiplier;
      } else {
        this.speedRem[i] = 0;
      }
    }

    // Round-robin stepping
    let complete = false;
    while (!complete) {
      complete = true;
      for (let i = 0; i < this.lines.length; i++) {
        const line = this.lines[i];
        if (line && line.alive && this.speedRem[i] > 0) {
          if (!line.forwardDraw(this)) {
            // Try to create a new line
            const factory = this.lineFactoryMap.get(this.lineNames[i]);
            if (factory) {
              this.lines[i] = this._newLine(factory);
            }
          }
          this.speedRem[i]--;
          complete = false;
          finished = false;
        }
      }
    }

    if (finished) {
      this.finished = true;
      this.iterations++;
    }

    return finished;
  }

  /** Blit offscreen to main canvas */
  render(ctx) {
    ctx.drawImage(this.offscreen, 0, 0, this.width, this.height);
  }

  /** Convert grid coords to pixels and draw a line segment */
  drawLine(gr, gc, grr, gcc, lineDraw) {
    const px = this.geometry.columnToX(gc);
    const py = this.geometry.rowToY(gr);
    const pxx = this.geometry.columnToX(gcc);
    const pyy = this.geometry.rowToY(grr);
    lineDraw.drawLine(this.offCtx, px, py, pxx, pyy);
  }

  /** Check if a move is invalid (out of bounds, occupied, or crosses diagonal) */
  invalidMove(cr, cc, nr, nc, nd, blocked) {
    if (nr < 0 || nc < 0 || nr >= this.rows || nc >= this.cols) return true;
    if (this.grid[nr][nc]) return true;
    if (blocked && blocked[nr][nc]) return true;

    // Check diagonal crossing
    if (cr !== nr && cc !== nc) {
      const cornerA = this.grid[nr][cc] || (blocked && blocked[nr][cc]);
      const cornerB = this.grid[cr][nc] || (blocked && blocked[cr][nc]);
      if (cornerA && cornerB) return true;
    }

    return false;
  }
}

function parseDirection(str) {
  if (!str) return [2, 2, 2, 2, 2, 2, 2, 2];
  return str.split('').map(Number);
}
