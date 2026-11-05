# @fortyfive/cli

`fortyfive` renders a scene to a PNG or SVG file from the command line, with no browser. A PNG is
the same bytes the node backend (`@fortyfive/render-canvas/node`) produces at the same seed; an SVG
is `@fortyfive/render-svg`'s self-contained file.

## Install

From the repo root:

```sh
pnpm install
node packages/cli/bin/fortyfive.js --help
```

In a project that depends on `@fortyfive/cli`, the `fortyfive` bin is on the path: `npx fortyfive --help`.

## Render a banner

```sh
node packages/cli/bin/fortyfive.js scenes --scenes apps/demo/scenes/index.js
# simple     1200x600  Simple
# ...
# banner     1600x30  Banner

node packages/cli/bin/fortyfive.js render --scenes apps/demo/scenes/index.js \
  --scene neon --seed 42 --out neon.png --manifest neon.json
```

```
fortyfive render --scene <slug> --seed <int> --out <path> [--format png|svg]
                 [--width N] [--height N] [--scenes <path>] [--manifest <path>]
fortyfive scenes  --scenes <path> [--json]
```

- `--scene` is the scene's name lowercased (`Neon` → `neon`), matched case-insensitively.
- `--format` defaults to the `--out` extension: `.svg` is SVG, anything else PNG.
- `--width`/`--height` default to the scene's `canvasWidth`/`canvasHeight`, else 1200×600.
- `--out -` writes the image to stdout: `fortyfive render ... --format svg --out - | less`.
- `scenes --json` prints `[{ slug, name, width, height }, ...]`.

Exit codes: `0` success, `1` usage error (bad flags, missing or broken registry, unknown scene),
`2` render failure (the scene threw, or the output could not be written). Errors go to stderr;
a successful render prints nothing to stdout.

## `--scenes`: bring your own art

The CLI ships no scenes and has no default registry. Scenes are content, not engine: `--scenes` is
the path to any ES module, resolved from your working directory, that exports a `scenes` array (or
a default-exported array) of scene configs. This repo's art lives in `apps/demo/scenes/index.js`;
another repo points the same tool at its own.

## Seeds

The same scene, seed and size always render the same picture. Leave `--seed` off and the CLI picks
a random 32-bit seed and tells you, on stderr, so the render can be made again:

```
fortyfive: no --seed given — using 1234567890 (pass --seed 1234567890 to reproduce)
```

## The manifest

`--manifest <path>` writes a JSON sidecar recording what was rendered and the sha256 of the exact
bytes written (to the file, or to stdout with `--out -`). Rerun with the same values and the hash
matches:

```json
{
  "scene": "Neon",
  "seed": 42,
  "engine": "fortyfive",
  "version": "0.1.0",
  "format": "png",
  "width": 1200,
  "height": 600,
  "sha256": "43525ed19064c1a800b8421bba8404cb4d18970ecaa18e58434adcc86dce75f4"
}
```
