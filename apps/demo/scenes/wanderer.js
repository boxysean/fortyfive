// Wanderer — wall-clinging lines with warm palette

export const wanderer = {
  name: 'Wanderer',
  bgcolour: 'black',
  widthSpacing: 8,
  heightSpacing: 8,

  colours: [
    { name: 'orange', red: 255, green: 140, blue: 0 },
    { name: 'gold', red: 255, green: 200, blue: 40 },
    { name: 'coral', red: 255, green: 100, blue: 80 },
    { name: 'amber', red: 255, green: 170, blue: 50 },
  ],

  colourPalettes: [
    { name: 'warm', colours: ['orange', 'gold', 'coral', 'amber'], mode: 'random' },
  ],

  areas: [
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
  ],

  coordBags: [
    { name: 'centre', type: 'centre' },
  ],

  movements: [
    { name: 'cling', type: 'cling', direction: '22222222' },
  ],

  lineDraws: [
    { name: 'A', palette: 'warm', strokeWidth: 4 },
    { name: 'B', palette: 'warm', strokeWidth: 4 },
    { name: 'C', palette: 'warm', strokeWidth: 4 },
  ],

  lines: [
    { name: 'A', draw: 'A', movement: 'cling', startArea: '+all', coordBag: 'centre' },
    { name: 'B', draw: 'B', movement: 'cling', startArea: '+all', coordBag: 'centre' },
    { name: 'C', draw: 'C', movement: 'cling', startArea: '+all', coordBag: 'centre' },
  ],

  deploy: ['A', 'B', 'C'],
};
