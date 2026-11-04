// Simple scene — two colored lines exploring the grid

export const simple = {
  name: 'Simple',
  bgcolour: 'black',
  widthSpacing: 10,
  heightSpacing: 10,

  colours: [
    { name: 'red', red: 255, green: 50, blue: 50 },
    { name: 'green', red: 50, green: 255, blue: 80 },
  ],

  colourPalettes: [
    { name: 'red', colours: ['red'] },
    { name: 'green', colours: ['green'] },
  ],

  areas: [
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
  ],

  coordBags: [
    { name: 'random', type: 'random' },
  ],

  movements: [
    { name: 'A', type: 'intelligent', intelligence: 2, straightProb: 0.8 },
  ],

  lineDraws: [
    { name: 'A', palette: 'red', strokeWidth: 5 },
    { name: 'B', palette: 'green', strokeWidth: 5 },
  ],

  lines: [
    { name: 'A', draw: 'A', movement: 'A', startArea: '+all', coordBag: 'random' },
    { name: 'B', draw: 'B', movement: 'A', startArea: '+all', coordBag: 'random' },
  ],

  deploy: ['A', 'B'],
};
