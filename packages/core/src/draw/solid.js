// SolidDraw — port of SolidDraw.java
// Emits solid-coloured line segments as display-list path primitives

export class SolidDraw {
  constructor(name, palette, strokeWidth) {
    this.name = name;
    this.palette = palette;
    this.strokeWidth = strokeWidth;
  }

  segment(px, py, pxx, pyy) {
    return {
      type: 'path',
      d: [[px, py], [pxx, pyy]],
      stroke: this.palette.current().toCSS(),
      width: this.strokeWidth,
      cap: 'round',
    };
  }
}
