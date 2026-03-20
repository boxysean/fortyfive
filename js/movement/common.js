// Shared movement constants and utilities
// Direction indices: 0=S, 1=SE, 2=E, 3=NE, 4=N, 5=NW, 6=W, 7=SW

export const DR = [1, 1, 0, -1, -1, -1, 0, 1];
export const DC = [0, 1, 1, 1, 0, -1, -1, -1];

export const DIR_DISALLOWED = 0;
export const DIR_AVOID = 1;
export const DIR_PREFERRED = 2;

/**
 * Check if a line at (cr, cc) facing direction cd has at least one valid move.
 * Tests directions cd-1, cd, cd+1.
 */
export function hasMove(cr, cc, cd, scene, blocked) {
  for (let offset = -1; offset <= 1; offset++) {
    const nd = (cd + offset + 8) % 8;
    const nr = cr + DR[nd];
    const nc = cc + DC[nd];
    if (!scene.invalidMove(cr, cc, nr, nc, nd, blocked)) {
      return true;
    }
  }
  return false;
}
