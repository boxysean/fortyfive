// SolidDraw — port of SolidDraw.java
// Draws solid-colored line segments on a canvas context

export class SolidDraw {
  constructor(name, palette, strokeWidth) {
    this.name = name;
    this.palette = palette;
    this.strokeWidth = strokeWidth;
  }

  drawLine(ctx, px, py, pxx, pyy) {
    const colour = this.palette.current();
    ctx.strokeStyle = colour.toCSS();
    ctx.lineWidth = this.strokeWidth;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(px, py);
    ctx.lineTo(pxx, pyy);
    ctx.stroke();
  }
}
