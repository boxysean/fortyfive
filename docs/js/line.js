// Line entity — port of Line.java

export class Line {
  constructor(cr, cc, cd, stepSpeed, drawSpeed, movement, draw, blocked) {
    this.cr = cr;
    this.cc = cc;
    this.cd = cd;
    this.stepSpeed = stepSpeed;
    this.drawSpeed = drawSpeed;
    this.movement = movement;
    this.draw = draw;
    this.blocked = blocked; // per-line boolean grid (or null)
    this.alive = true;
  }

  forward(scene) {
    for (let i = 0; i < this.stepSpeed; i++) {
      if (!this.movement.forwardOnce(this, scene)) {
        this.alive = false;
        return false;
      }
    }
    return true;
  }

  forwardDraw(scene) {
    const oldR = this.cr;
    const oldC = this.cc;

    if (!this.forward(scene)) {
      return false;
    }

    scene.drawLine(oldR, oldC, this.cr, this.cc, this.draw);
    this.draw.palette.next();
    return true;
  }
}
