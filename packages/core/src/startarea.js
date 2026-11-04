// Start area management — port of StartArea, StartAreaFactory

import { applyBag } from './coordinatebag.js';

export class StartArea {
  constructor(coords) {
    this.coords = coords;
    this.idx = 0;
    this.gr = -1;
    this.gc = -1;
  }

  getNextStartPoint(grid) {
    while (this.idx < this.coords.length) {
      const { r, c } = this.coords[this.idx];
      this.idx++;
      if (!grid[r][c]) {
        this.gr = r;
        this.gc = c;
        return true;
      }
    }
    return false;
  }
}

/**
 * Build a StartArea from area configs and the grid.
 * areaRefs is an array of { prefix, name } where prefix is +, -, ., or !
 */
export function buildStartArea(areaRefs, areaMap, geometry, blocked, coordBagType) {
  const rows = geometry.rows;
  const cols = geometry.cols;

  // Build valid grid from area operations
  const valid = Array.from({ length: rows }, () => new Array(cols).fill(false));

  for (const { prefix, name } of areaRefs) {
    const area = areaMap.get(name);
    if (!area) throw new Error(`Unknown area: ${name}`);
    switch (prefix) {
      case '+': area.add(valid, geometry); break;
      case '-': area.subtract(valid, geometry); break;
      case '.': area.set(valid, geometry); break;
      case '!': area.unset(valid, geometry); break;
      default: area.add(valid, geometry); break;
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

/** Parse area ref string like "+all" or "!border" into { prefix, name } */
export function parseAreaRef(ref) {
  const prefixes = ['+', '-', '.', '!'];
  if (prefixes.includes(ref[0])) {
    return { prefix: ref[0], name: ref.slice(1) };
  }
  return { prefix: '+', name: ref };
}
