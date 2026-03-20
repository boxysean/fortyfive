// Grid math — port of SceneGeometry.java

export class Geometry {
  constructor(width, height, widthSpacing, heightSpacing) {
    this.width = width;
    this.height = height;
    this.widthSpacing = widthSpacing;
    this.heightSpacing = heightSpacing;
    this._rows = Math.floor(height / heightSpacing);
    this._cols = Math.floor(width / widthSpacing);
  }

  get rows() { return this._rows; }
  get cols() { return this._cols; }

  gridToPixel(gg, gmax, smax) {
    return (2 * gg + 1) * (smax / gmax) / 2.0;
  }

  rowToY(r) { return this.gridToPixel(r, this._rows, this.height); }
  columnToX(c) { return this.gridToPixel(c, this._cols, this.width); }

  yToRow(y) { return Math.floor(y * this._rows / this.height); }
  xToColumn(x) { return Math.floor(x * this._cols / this.width); }
}
