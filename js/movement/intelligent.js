// IntelligentMovement — port of IntelligentMovement.java
// Goal-directed pathfinding with straight-probability and intelligence levels

import { DR, DC, DIR_DISALLOWED, DIR_AVOID, hasMove } from './common.js';

const INTELLIGENCE_HIT = 0;
const INTELLIGENCE_NONE = 1;
const INTELLIGENCE_AVOID = 2;
const INTELLIGENCE_AVOID_ADV = 3;

export class IntelligentMovement {
  constructor(config) {
    this.name = config.name;
    this.straightProb = config.straightProb ?? 0.8;
    this.intelligence = config.intelligence ?? 2;
    this.direction = parseDirection(config.direction || '22222222');
  }

  forwardOnce(line, scene) {
    const { cr, cc, cd } = line;
    let nAttempts = this.intelligence <= INTELLIGENCE_NONE ? 1 : 3;

    // Random rotation direction: +1 or -1
    const rotSign = Math.random() < 0.5 ? 1 : -1;
    // Random rotation amount: 1 or 2
    const rotAmount = Math.random() < 0.5 ? 1 : 2;

    let skippedStraight = false;

    for (let attempt = 0; attempt <= nAttempts; attempt++) {
      let nd;

      if (attempt === 0) {
        // Attempt straight
        if (Math.random() >= this.straightProb || this.direction[cd] === DIR_AVOID) {
          skippedStraight = true;
          if (this.intelligence >= INTELLIGENCE_AVOID) nAttempts = Math.min(nAttempts + 1, 3);
          continue;
        }
        nd = cd;
      } else if (attempt === 1) {
        nd = (cd + rotSign * rotAmount + 8) % 8;
      } else if (attempt === 2) {
        nd = (cd - rotSign * rotAmount + 8) % 8;
      } else {
        // Fallback: try straight if we skipped it
        if (skippedStraight) {
          nd = cd;
        } else {
          continue;
        }
      }

      if (this.direction[nd] === DIR_DISALLOWED) continue;

      const nr = cr + DR[nd];
      const nc = cc + DC[nd];

      if (scene.invalidMove(cr, cc, nr, nc, nd, line.blocked)) {
        if (this.intelligence >= INTELLIGENCE_AVOID) continue;
        break;
      }

      // Advanced intelligence: avoid dead ends (unless this is the last attempt)
      if (attempt < nAttempts && this.intelligence >= INTELLIGENCE_AVOID_ADV) {
        if (!hasMove(nr, nc, nd, scene, line.blocked)) continue;
      }

      // Success: update grid and line position
      scene.grid[cr][cc] = true;
      scene.grid[nr][nc] = true;
      line.cr = nr;
      line.cc = nc;
      line.cd = nd;
      return true;
    }

    return false;
  }
}

function parseDirection(str) {
  return str.split('').map(Number);
}
