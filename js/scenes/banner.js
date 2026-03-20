// Banner — black lines on white, 1600×50 horizontal strip

export const banner = {
  name: 'Banner',
  bgcolour: 'white',
  canvasWidth: 1600,
  canvasHeight: 30,
  widthSpacing: 6,
  heightSpacing: 6,

  colours: [
    { name: 'black', red: 0, green: 0, blue: 0 },
  ],

  colourPalettes: [
    { name: 'black', colours: ['black'] },
  ],

  areas: [
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
  ],

  coordBags: [
    { name: 'random', type: 'random' },
  ],

  movements: [
    { name: 'flow', type: 'intelligent', intelligence: 3, straightProb: 0.98 },
  ],

  lineDraws: [
    { name: 'A', palette: 'black', strokeWidth: 2 },
  ],

  lines: [
    { name: 'A', draw: 'A', movement: 'flow', startArea: '+all', coordBag: 'random', drawSpeed: 3 },
  ],

  deploy: ['A'],
};
