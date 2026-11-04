// Browser entry — keep this free of Node-only imports: apps/demo loads it straight into a page.
// The Node backend lives behind its own entry (`@fortyfive/render-canvas/node`), which pulls in
// the native @napi-rs/canvas module and must never be bundled for the browser.
export { Engine } from './engine.js';
export { createAssetResolver } from './assets.js';
export { drawFrame, drawMirroredFrame, browserCreateCanvas } from './replay.js';
