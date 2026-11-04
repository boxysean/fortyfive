// Scene registry — this repo's own art, not the LinkedIn banner set.
//
// Scenes are content, not engine: a consumer (another repo, another app) points the engine at
// its own registry. Nothing under packages/ imports from here.

import { simple } from './simple.js';
import { wanderer } from './wanderer.js';
import { neon } from './neon.js';
import { labyrinth } from './labyrinth.js';
import { banner } from './banner.js';

export const scenes = [simple, wanderer, neon, labyrinth, banner];
