// Engine — animation loop and scene lifecycle

import { Scene } from './scene.js';

export class Engine {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.scene = null;
    this.paused = false;
    this.speedMultiplier = 1;
    this.frameId = null;
    this.onFinished = null; // callback when scene finishes
  }

  loadScene(config) {
    this.stop();
    if (config.canvasWidth && config.canvasHeight) {
      this.canvas.classList.add('fixed-size');
      this.canvas.width = config.canvasWidth;
      this.canvas.height = config.canvasHeight;
    } else {
      this.canvas.classList.remove('fixed-size');
      this.canvas.width = this.canvas.clientWidth * devicePixelRatio;
      this.canvas.height = this.canvas.clientHeight * devicePixelRatio;
    }
    this.scene = new Scene(config, this.canvas.width, this.canvas.height);
    this.start();
  }

  start() {
    if (this.frameId) return;
    this.paused = false;
    this._loop();
  }

  stop() {
    if (this.frameId) {
      cancelAnimationFrame(this.frameId);
      this.frameId = null;
    }
  }

  pause() {
    this.paused = true;
  }

  resume() {
    this.paused = false;
  }

  togglePause() {
    this.paused = !this.paused;
  }

  setSpeed(multiplier) {
    this.speedMultiplier = multiplier;
  }

  _loop() {
    this.frameId = requestAnimationFrame(() => this._loop());

    if (this.paused || !this.scene) return;

    const finished = this.scene.draw(this.speedMultiplier);
    this.scene.render(this.ctx);

    if (finished && this.onFinished) {
      this.onFinished();
    }
  }
}
