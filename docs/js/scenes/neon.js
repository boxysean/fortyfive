// Neon — many thin fast lines in neon colors

export const neon = {
  name: 'Neon',
  bgcolour: 'black',
  widthSpacing: 6,
  heightSpacing: 6,

  colours: [
    { name: 'cyan', red: 0, green: 255, blue: 255 },
    { name: 'magenta', red: 255, green: 0, blue: 255 },
    { name: 'lime', red: 0, green: 255, blue: 100 },
    { name: 'pink', red: 255, green: 50, blue: 150 },
    { name: 'blue', red: 80, green: 120, blue: 255 },
  ],

  colourPalettes: [
    { name: 'neon', colours: ['cyan', 'magenta', 'lime', 'pink', 'blue'], mode: 'random' },
  ],

  areas: [
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
  ],

  coordBags: [
    { name: 'random', type: 'random' },
  ],

  movements: [
    { name: 'fast', type: 'intelligent', intelligence: 3, straightProb: 0.85 },
  ],

  lineDraws: [
    { name: 'A', palette: 'neon', strokeWidth: 2 },
    { name: 'B', palette: 'neon', strokeWidth: 2 },
    { name: 'C', palette: 'neon', strokeWidth: 2 },
    { name: 'D', palette: 'neon', strokeWidth: 2 },
    { name: 'E', palette: 'neon', strokeWidth: 2 },
  ],

  lines: [
    { name: 'A', draw: 'A', movement: 'fast', startArea: '+all', coordBag: 'random', drawSpeed: 2 },
    { name: 'B', draw: 'B', movement: 'fast', startArea: '+all', coordBag: 'random', drawSpeed: 2 },
    { name: 'C', draw: 'C', movement: 'fast', startArea: '+all', coordBag: 'random', drawSpeed: 2 },
    { name: 'D', draw: 'D', movement: 'fast', startArea: '+all', coordBag: 'random', drawSpeed: 2 },
    { name: 'E', draw: 'E', movement: 'fast', startArea: '+all', coordBag: 'random', drawSpeed: 2 },
  ],

  deploy: ['A', 'B', 'C', 'D', 'E'],
};
