// Scene registry

import { simple } from './simple.js';
import { wanderer } from './wanderer.js';
import { neon } from './neon.js';
import { labyrinth } from './labyrinth.js';
import { banner } from './banner.js';
import { cableRun } from './cable-run.js';
import { mesh } from './mesh.js';
import { labyrinthBanner } from './labyrinth-banner.js';
import { neonBanner } from './neon-banner.js';
import { wandererBanner } from './wanderer-banner.js';

export const scenes = [
  simple, wanderer, neon, labyrinth, banner,
  cableRun, mesh, labyrinthBanner, neonBanner, wandererBanner,
];
