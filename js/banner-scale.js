// Scale a scene config for an N× render that keeps the same grid.
//
// Spacing and stroke widths scale with the canvas, so the grid has the same rows and
// columns and lines are the same weight relative to the strip. Areas are in pixels too
// (RectangleArea maps x → column as floor(x · cols / width)), so their numeric geometry
// scales as well — otherwise knockouts and start cells would land on the wrong cells.
// Relative sizes ('width', 'height', 'a/b') already follow the canvas and are left alone.

function scaleSize(val, scale) {
  if (val === undefined || val === null) return val;
  if (val === 'width' || val === 'height') return val;
  if (typeof val === 'string' && val.includes('/')) return val;
  return Number(val) * scale;
}

export function scaleSceneConfig(config, scale = 1) {
  const cfg = structuredClone(config);
  if (scale === 1) return cfg;
  cfg.widthSpacing = (cfg.widthSpacing || 10) * scale;
  cfg.heightSpacing = (cfg.heightSpacing || 10) * scale;
  if (cfg.canvasWidth) cfg.canvasWidth *= scale;
  if (cfg.canvasHeight) cfg.canvasHeight *= scale;
  for (const d of cfg.lineDraws || []) d.strokeWidth = (d.strokeWidth || 1) * scale;
  for (const a of cfg.areas || []) {
    for (const k of ['x', 'y', 'width', 'height']) {
      if (k in a) a[k] = scaleSize(a[k], scale);
    }
  }
  return cfg;
}
