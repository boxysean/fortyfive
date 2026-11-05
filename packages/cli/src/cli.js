// fortyfive — render a scene from a registry to a PNG or SVG file.
//
// run(argv) parses, dispatches and returns the exit code; it never exits itself. The exit codes are
// the contract: 0 success, 1 usage error (the fault is in the command line or the registry it
// names), 2 render failure (the command was fine, building or writing the picture was not).

import fs from 'node:fs';
import path from 'node:path';
import { createRequire } from 'node:module';
import { randomInt } from 'node:crypto';
import { pathToFileURL } from 'node:url';
import { renderScene, sha256, DEFAULT_WIDTH, DEFAULT_HEIGHT, FORMATS } from './render.js';

const { version: VERSION } = createRequire(import.meta.url)('../package.json');

const USAGE = `fortyfive ${VERSION} — render fortyfive scenes without a browser

usage:
  fortyfive render --scene <slug> --seed <int> --out <path> [--format png|svg]
                   [--width N] [--height N] [--scenes <path>] [--manifest <path>]
  fortyfive scenes --scenes <path> [--json]

  --scenes <path>    module exporting a \`scenes\` array, resolved from the working directory
  --scene <slug>     scene name, lowercased (e.g. neon)
  --seed <int>       any integer; omitted, a random one is chosen and printed to stderr
  --out <path>       output file, or - for stdout
  --format png|svg   default: from the --out extension (.svg → svg, else png)
  --width/--height   default: the scene's canvasWidth/canvasHeight, else ${DEFAULT_WIDTH}×${DEFAULT_HEIGHT}
  --manifest <path>  also write a JSON sidecar with the seed and the output's sha256

exit codes: 0 success, 1 usage error, 2 render failure
`;

const NO_REGISTRY = `fortyfive: no scene registry given.
  pass --scenes <path> to a module that exports a \`scenes\` array, resolved from your
  working directory, e.g.
    --scenes apps/demo/scenes/index.js
`;

// Flags each command accepts; `true` marks a switch that takes no value
const COMMANDS = {
  render: { scene: false, seed: false, out: false, format: false, width: false, height: false, scenes: false, manifest: false },
  scenes: { scenes: false, json: true },
};

/** Thrown for anything that is the caller's fault; run() turns it into exit 1. */
class UsageError extends Error {}

export async function run(argv) {
  try {
    if (argv.length === 0 || argv.includes('--help') || argv.includes('-h')) {
      process.stdout.write(USAGE);
      return 0;
    }
    if (argv.includes('--version')) {
      process.stdout.write(`${VERSION}\n`);
      return 0;
    }
    const [command, ...rest] = argv;
    if (!Object.hasOwn(COMMANDS, command)) throw new UsageError(`unknown command ${command} (try --help)`);
    const flags = parseFlags(rest, COMMANDS[command]);
    return command === 'render' ? await render(flags) : await list(flags);
  } catch (err) {
    if (!(err instanceof UsageError)) throw err;
    process.stderr.write(err.message.startsWith('fortyfive:') ? err.message : `fortyfive: ${err.message}\n`);
    return 1;
  }
}

/**
 * `--name value`, `--name=value`, or a bare switch. A following token is a value unless it starts
 * with `--`, so `--seed -7` and `--out -` work while `--scene --seed 3` reports the missing value.
 */
function parseFlags(args, allowed) {
  const flags = {};
  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (!arg.startsWith('--')) throw new UsageError(`unexpected argument ${arg}`);
    let [name, value] = splitOnce(arg.slice(2), '=');
    if (!Object.hasOwn(allowed, name)) throw new UsageError(`unknown flag --${name}`);
    if (Object.hasOwn(flags, name)) throw new UsageError(`--${name} given more than once`);
    if (allowed[name]) {
      if (value !== undefined) throw new UsageError(`--${name} takes no value`);
      value = true;
    } else if (value === undefined) {
      const next = args[i + 1];
      if (next === undefined || next.startsWith('--')) throw new UsageError(`--${name} needs a value`);
      value = next;
      i++;
    }
    flags[name] = value;
  }
  return flags;
}

function splitOnce(s, sep) {
  const at = s.indexOf(sep);
  return at === -1 ? [s, undefined] : [s.slice(0, at), s.slice(at + 1)];
}

/** The registry the caller points at. There is deliberately no default: the tool ships no art. */
async function loadRegistry(value) {
  if (value === undefined) throw new UsageError(NO_REGISTRY);
  const file = path.resolve(process.cwd(), value);
  if (!fs.existsSync(file)) throw new UsageError(`--scenes ${value}: no such file (${file})`);
  let mod;
  try {
    mod = await import(pathToFileURL(file).href);
  } catch (err) {
    throw new UsageError(`--scenes ${value}: could not load: ${err.message}`);
  }
  const scenes = Array.isArray(mod.scenes) ? mod.scenes : Array.isArray(mod.default) ? mod.default : null;
  if (!scenes) throw new UsageError(`--scenes ${value}: does not export a \`scenes\` array`);
  for (const config of scenes) {
    if (typeof config?.name !== 'string' || !config.name) {
      throw new UsageError(`--scenes ${value}: every entry needs a string \`name\``);
    }
  }
  return scenes;
}

const slug = (config) => config.name.toLowerCase();

/** Flag overrides scene, scene overrides default. */
function dimensions(config, flags = {}) {
  return {
    width: flags.width ?? config.canvasWidth ?? DEFAULT_WIDTH,
    height: flags.height ?? config.canvasHeight ?? DEFAULT_HEIGHT,
  };
}

async function list(flags) {
  const scenes = await loadRegistry(flags.scenes);
  const rows = scenes.map((config) => ({ slug: slug(config), name: config.name, ...dimensions(config) }));
  if (flags.json) {
    process.stdout.write(JSON.stringify(rows, null, 2) + '\n');
  } else {
    const pad = Math.max(...rows.map((r) => r.slug.length), 0);
    for (const r of rows) process.stdout.write(`${r.slug.padEnd(pad)}  ${r.width}x${r.height}  ${r.name}\n`);
  }
  return 0;
}

async function render(flags) {
  // Validate the whole command line before loading anything, so a typo costs no import
  if (flags.out === undefined) throw new UsageError('render needs --out <path> (or --out - for stdout)');
  if (flags.scene === undefined) throw new UsageError('render needs --scene <slug>');
  const format = flags.format ?? (flags.out.toLowerCase().endsWith('.svg') ? 'svg' : 'png');
  if (!FORMATS.includes(format)) throw new UsageError(`--format must be png or svg, got ${format}`);
  const sizes = {};
  for (const name of ['width', 'height']) {
    if (flags[name] !== undefined) sizes[name] = positiveInt(name, flags[name]);
  }
  let seed;
  if (flags.seed !== undefined) {
    seed = integer('seed', flags.seed);
  }

  const scenes = await loadRegistry(flags.scenes);
  const config = scenes.find((c) => slug(c) === flags.scene.toLowerCase());
  if (!config) {
    throw new UsageError(`no scene ${flags.scene} in ${flags.scenes} (have: ${scenes.map(slug).join(', ')})`);
  }

  if (seed === undefined) {
    // Never silently: an unrepeatable render is a picture nobody can make again
    seed = randomInt(0, 2 ** 32);
    process.stderr.write(`fortyfive: no --seed given — using ${seed} (pass --seed ${seed} to reproduce)\n`);
  }
  const { width, height } = dimensions(config, sizes);

  try {
    const output = await renderScene({ config, seed, width, height, format });
    if (flags.out === '-') await writeStdout(output);
    else await fs.promises.writeFile(flags.out, output);
    if (flags.manifest !== undefined) {
      const manifest = {
        scene: config.name,
        seed,
        engine: 'fortyfive',
        version: VERSION,
        format,
        width,
        height,
        sha256: sha256(output),
      };
      await fs.promises.writeFile(flags.manifest, JSON.stringify(manifest, null, 2) + '\n');
    }
  } catch (err) {
    process.stderr.write(`fortyfive: render failed: ${err.stack ?? err}\n`);
    return 2;
  }
  return 0;
}

/** Resolve once the bytes are handed off, so exiting straight after cannot truncate a pipe. */
function writeStdout(data) {
  return new Promise((resolve, reject) => {
    process.stdout.write(data, (err) => (err ? reject(err) : resolve()));
  });
}

function integer(name, value) {
  if (!/^-?\d+$/.test(value) || !Number.isSafeInteger(Number(value))) {
    throw new UsageError(`--${name} must be an integer, got ${value}`);
  }
  return Number(value);
}

function positiveInt(name, value) {
  const n = /^\d+$/.test(value) ? Number(value) : NaN;
  if (!Number.isSafeInteger(n) || n <= 0) throw new UsageError(`--${name} must be a positive integer, got ${value}`);
  return n;
}
