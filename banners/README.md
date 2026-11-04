# Banners

Five 4:1 LinkedIn banners (1584 × 396 logical, rendered at 2× = 3168 × 792) drawn by the
FortyFive scenes in `js/scenes/` with the dbt Labs and Fivetran marks composited on top.
Open `index.html` (works from `file://`) for the contact sheet.

## Regenerate

    node tools/render-banner.mjs

Needs only Node 22 and Chrome (`$CHROME`, else the Playwright Chromium at
`~/.cache/ms-playwright/chromium-1243/chrome-linux64/chrome`). No npm, no build step.

The tool serves the repo on `127.0.0.1`, opens one headless Chrome per banner on
`render.html?file=<png>`, which draws the scene synchronously from its seed, composites
the logos (`js/logos.js`) and POSTs the canvas PNG back to the tool. The PNG is the canvas
bytes, not a screenshot. Each PNG's IHDR is checked against the manifest's size.

Options: `--only 01-cable-run.png,02-mesh.png` renders a subset; `--verify` fails loudly if
any PNG's sha256 differs from the one already recorded; `--check` renders every banner twice
into a scratch directory and fails unless both runs are byte-identical (it writes nothing here).

At 2× the scene's spacing, stroke widths and pixel-unit areas are all doubled
(`js/banner-scale.js`), so the grid is the same as at 1× and the output is true 2× pixels.
`node tools/test-scale.mjs` proves every banner fills the same cells at both scales.

## Metadata

- `manifest.json` is the **input**: `file`, `scene`, `seed`, `canvas`, `grid`, `palette`,
  `movement`, `strokeWidth`, `logos` (with the sha256 of each asset) and `description` are
  authored and preserved verbatim. The tool checks them against the scene config and the asset
  files before rendering, and refuses to run if they have drifted.
- After a render the tool adds `generator` (repo, `git rev-parse HEAD`, tool), `generatedAt`
  and `png` (`sha256`, `bytes`, `width`, `height`), and writes the same object to
  `<name>.json` beside each PNG: the sidecar is the **record** of how that file was made.
- The sha256s let you prove a PNG came from the record next to it: `shasum -a 256 *.png`
  must match `png.sha256`, and the logo hashes match `assets/` (see `assets/PROVENANCE.md`).
  `generator.commit` describes the code only if the tree was clean; the tool warns otherwise.

## Determinism caveat

A seed fixes the drawing exactly, and the same Chrome build produces byte-identical PNGs
(`--check`). A different Chrome version may rasterise or PNG-encode differently, so a fresh
render can have a different sha256 without anything being wrong. `--check` only compares
two runs made in the same session; its comparison with the manifest hash is informational.

## Adding a banner

1. Add a scene in `js/scenes/` with `slug`, `canvasWidth: 1584`, `canvasHeight: 396` and a
   `logos` array, and register it in `js/scenes/index.js`.
2. Add an entry to `manifest.json` with the authored keys above (copy an existing one; logo
   positions, `grid` and `strokeWidth` must match the scene).
3. Run `node tools/render-banner.mjs --only <new>.png`, then `node tools/render-banner.mjs --check`.
