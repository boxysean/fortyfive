// Coordinate bag strategies — port of RandomBag, OrderedBag, CentreBag

export function shuffle(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

export function randomBag(coords) {
  return shuffle(coords);
}

export function orderedBag(coords, leftFirst = 2, topFirst = 1) {
  if (topFirst === 0 || leftFirst === 0) shuffle(coords);
  coords.sort((a, b) => {
    if (Math.abs(leftFirst) >= Math.abs(topFirst) && leftFirst !== 0) {
      if (a.c !== b.c) return leftFirst > 0 ? a.c - b.c : b.c - a.c;
      if (topFirst === 0) return 0;
      return topFirst > 0 ? a.r - b.r : b.r - a.r;
    } else {
      if (a.r !== b.r) return topFirst > 0 ? a.r - b.r : b.r - a.r;
      if (leftFirst === 0) return 0;
      return leftFirst > 0 ? a.c - b.c : b.c - a.c;
    }
  });
  return coords;
}

export function centreBag(coords, rows, cols) {
  shuffle(coords);
  const hr = rows / 2;
  const hc = cols / 2;
  coords.sort((a, b) => {
    const da = Math.sqrt((hr - a.r) ** 2 + (hc - a.c) ** 2);
    const db = Math.sqrt((hr - b.r) ** 2 + (hc - b.c) ** 2);
    return da - db;
  });
  return coords;
}

export function applyBag(type, coords, rows, cols) {
  switch (type) {
    case 'ordered': return orderedBag(coords);
    case 'centre':
    case 'center': return centreBag(coords, rows, cols);
    case 'random':
    default: return randomBag(coords);
  }
}
