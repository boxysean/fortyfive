// ClingMovement — port of ClingMovement.java
// Wall-hugging movement that prefers directions with more filled neighbors

import { DR, DC, DIR_DISALLOWED, DIR_AVOID, DIR_PREFERRED } from './common.js';
import { shuffle } from '../coordinatebag.js';

export class ClingMovement {
  constructor(config) {
    this.name = config.name;
    this.direction = parseDirection(config.direction || '22222222');
  }

  forwardOnce(line, scene) {
    const { cr, cc, cd } = line;
    const offsets = [-1, 0, 1];
    shuffle(offsets);

    let bestDir = -1;
    let highScore = -1;

    for (const offset of offsets) {
      const nd = (cd + offset + 8) % 8;

      if (this.direction[nd] === DIR_DISALLOWED) continue;

      const nr = cr + DR[nd];
      const nc = cc + DC[nd];

      if (scene.invalidMove(cr, cc, nr, nc, nd, line.blocked)) continue;

      if (this.direction[nd] === DIR_AVOID) {
        // Take first available if direction is avoid-class
        if (bestDir === -1) bestDir = nd;
        continue;
      }

      // Score: count filled/out-of-bounds neighbors
      let score = 0;
      for (let d = 0; d < 8; d++) {
        const ar = nr + DR[d];
        const ac = nc + DC[d];
        if (ar < 0 || ar >= scene.rows || ac < 0 || ac >= scene.cols || scene.grid[ar][ac]) {
          score++;
        }
      }

      if (score > highScore) {
        highScore = score;
        bestDir = nd;
      }
    }

    if (bestDir === -1) return false;

    const nr = cr + DR[bestDir];
    const nc = cc + DC[bestDir];
    scene.grid[cr][cc] = true;
    scene.grid[nr][nc] = true;
    line.cr = nr;
    line.cc = nc;
    line.cd = bestDir;
    return true;
  }
}

function parseDirection(str) {
  return str.split('').map(Number);
}
