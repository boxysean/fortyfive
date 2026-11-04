// Engine — animation loop and scene lifecycle

import { Scene } from '../../core/src/scene.js';
import { createAssetResolver } from './assets.js';
import { drawFrame } from './replay.js';

export class Engine {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.scene = null;
    this.paused = false;
    this.speedMultiplier = 1;
    this.frameId = null;
    this.onFinished = null; // callback when scene finishes
    this.resolve = createAssetResolver();
    this.painting = null; // the paint in flight, if any
    this.painted = 0; // primitives of the current scene already on the canvas
    this.repaintAll = false;
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
    this.painted = 0;
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

  /** Repaint the scene's whole display list, e.g. after appending primitives to it */
  redraw() {
    this.repaintAll = true;
    this._paint();
  }

  _loop() {
    this.frameId = requestAnimationFrame(() => this._loop());

    if (this.paused || !this.scene) return;

    const finished = this.scene.draw(this.speedMultiplier);
    this._paint();

    if (finished && this.onFinished) {
      this.onFinished();
    }
  }

  // The display list only grows, so a paint replays just the primitives added since the last
  // one. One paint at a time: a tick that finds a paint still in flight drops its own.
  _paint() {
    if (this.painting || !this.scene) return;
    const { scene } = this;
    if (this.repaintAll) {
      this.repaintAll = false;
      this.painted = 0;
    }
    const list = scene.frame();
    const from = this.painted;
    const to = list.length;
    if (from === to) return;
    this.painting = drawFrame(this.ctx, list.slice(from, to), { resolve: this.resolve })
      .catch((err) => console.error(err))
      .finally(() => {
        // A failed batch is not retried every tick; a scene swapped mid-paint keeps its reset
        if (this.scene === scene) this.painted = to;
        this.painting = null;
        if (this.repaintAll) this._paint();
      });
  }
}
