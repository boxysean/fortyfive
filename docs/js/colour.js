// Colour system — port of Colour, RandomColourPalette, LinearColourPalette

export class Colour {
  constructor(name, r, g, b) {
    this.name = name;
    this.r = r;
    this.g = g;
    this.b = b;
  }

  toCSS() {
    return `rgb(${this.r},${this.g},${this.b})`;
  }

  // Colour also acts as a single-element palette
  current() { return this; }
  next() { return this; }
}

export class RandomColourPalette {
  constructor(name, colours) {
    this.name = name;
    this.colours = colours;
    this._current = colours[Math.floor(Math.random() * colours.length)];
  }

  current() { return this._current; }

  next() {
    this._current = this.colours[Math.floor(Math.random() * this.colours.length)];
    return this._current;
  }
}

export class LinearColourPalette {
  constructor(name, colours) {
    this.name = name;
    this.colours = colours;
    this._idx = 0;
    this._current = colours[0];
  }

  current() { return this._current; }

  next() {
    this._idx = (this._idx + 1) % this.colours.length;
    this._current = this.colours[this._idx];
    return this._current;
  }
}

export function createColour(config) {
  return new Colour(config.name, config.red || 0, config.green || 0, config.blue || 0);
}

export function createPalette(config, colourMap) {
  const colours = config.colours.map(name => {
    const c = colourMap.get(name);
    if (!c) throw new Error(`Unknown colour: ${name}`);
    return c;
  });
  const mode = config.mode || 'random';
  if (mode === 'linear') {
    return new LinearColourPalette(config.name, colours);
  }
  return new RandomColourPalette(config.name, colours);
}
