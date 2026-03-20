// Main entry point — wire up canvas, controls, and engine

import { Engine } from './engine.js';
import { scenes } from './scenes/index.js';

const canvas = document.getElementById('canvas');
const btnPause = document.getElementById('btn-pause');
const sliderSpeed = document.getElementById('slider-speed');
const speedLabel = document.getElementById('speed-label');
const selectScene = document.getElementById('select-scene');
const btnExport = document.getElementById('btn-export');

const engine = new Engine(canvas);

let currentSceneIdx = 0;

// Populate scene selector
scenes.forEach((s, i) => {
  const opt = document.createElement('option');
  opt.value = i;
  opt.textContent = s.name;
  selectScene.appendChild(opt);
});

function loadScene(idx) {
  currentSceneIdx = idx;
  selectScene.value = idx;
  engine.loadScene(scenes[idx]);
}

// Do not auto-advance when a scene finishes; stay on the completed canvas.

// Controls
btnPause.addEventListener('click', () => {
  engine.togglePause();
  btnPause.textContent = engine.paused ? '\u25B6' : '\u23F8';
});

sliderSpeed.addEventListener('input', () => {
  const val = parseFloat(sliderSpeed.value);
  engine.setSpeed(val);
  speedLabel.textContent = `${val}\u00D7`;
});

btnExport.addEventListener('click', () => {
  const link = document.createElement('a');
  link.download = `${scenes[currentSceneIdx].name.toLowerCase()}.png`;
  link.href = canvas.toDataURL('image/png');
  link.click();
});

selectScene.addEventListener('change', () => {
  loadScene(parseInt(selectScene.value));
});

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
  if (e.code === 'Space') {
    e.preventDefault();
    btnPause.click();
  } else if (e.code === 'ArrowRight') {
    loadScene((currentSceneIdx + 1) % scenes.length);
  } else if (e.code === 'ArrowLeft') {
    loadScene((currentSceneIdx - 1 + scenes.length) % scenes.length);
  }
});

// Handle resize
window.addEventListener('resize', () => {
  loadScene(currentSceneIdx);
});

// Go!
loadScene(0);
