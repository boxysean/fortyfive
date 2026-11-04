// Rectangle areas — port of RectangleArea.java

export class RectangleArea {
  constructor(name, x, y, width, height) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.areaWidth = width;
    this.areaHeight = height;
  }

  _action(grid, geometry, value) {
    const sr = geometry.yToRow(this.y);
    const er = geometry.yToRow(this.y + this.areaHeight);
    const sc = geometry.xToColumn(this.x);
    const ec = geometry.xToColumn(this.x + this.areaWidth);
    for (let r = sr; r < er && r < grid.length; r++) {
      for (let c = sc; c < ec && c < grid[0].length; c++) {
        if (r >= 0 && c >= 0) grid[r][c] = value;
      }
    }
  }

  add(grid, geometry) { this._action(grid, geometry, true); }
  subtract(grid, geometry) { this._action(grid, geometry, false); }
  set(grid, geometry) { this._action(grid, geometry, true); }
  unset(grid, geometry) { this._action(grid, geometry, false); }
}

export function createArea(config, screenWidth, screenHeight) {
  const x = parseSize(config.x, 0, screenWidth);
  const y = parseSize(config.y, 0, screenHeight);
  const w = parseSize(config.width, screenWidth, screenWidth);
  const h = parseSize(config.height, screenHeight, screenHeight);
  return new RectangleArea(config.name, x, y, w, h);
}

function parseSize(val, defaultVal, maxVal) {
  if (val === undefined || val === null) return defaultVal;
  if (val === 'width' || val === 'height') return maxVal;
  if (typeof val === 'string' && val.includes('/')) {
    const [num, den] = val.split('/').map(Number);
    return Math.floor((num / den) * maxVal);
  }
  return Number(val);
}
