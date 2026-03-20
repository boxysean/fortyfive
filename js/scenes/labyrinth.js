// Labyrinth — ordered start with threshold areas creating corridors

export const labyrinth = {
  name: 'Labyrinth',
  bgcolour: 'black',
  widthSpacing: 8,
  heightSpacing: 8,

  colours: [
    { name: 'teal', red: 0, green: 200, blue: 180 },
    { name: 'sea', red: 0, green: 160, blue: 200 },
    { name: 'mint', red: 100, green: 220, blue: 160 },
    { name: 'sky', red: 100, green: 180, blue: 255 },
  ],

  colourPalettes: [
    { name: 'cool', colours: ['teal', 'sea', 'mint', 'sky'], mode: 'random' },
  ],

  areas: [
    { name: 'all', x: 0, y: 0, width: 'width', height: 'height' },
    { name: 'center', x: '1/4', y: '1/4', width: '1/2', height: '1/2' },
  ],

  coordBags: [
    { name: 'ordered', type: 'ordered' },
  ],

  movements: [
    { name: 'explore', type: 'intelligent', intelligence: 3, straightProb: 0.7 },
  ],

  lineDraws: [
    { name: 'A', palette: 'cool', strokeWidth: 3 },
    { name: 'B', palette: 'cool', strokeWidth: 3 },
    { name: 'C', palette: 'cool', strokeWidth: 3 },
  ],

  lines: [
    { name: 'A', draw: 'A', movement: 'explore', startArea: '+all', coordBag: 'ordered' },
    { name: 'B', draw: 'B', movement: 'explore', startArea: '+center', coordBag: 'ordered' },
    { name: 'C', draw: 'C', movement: 'explore', startArea: '+all', coordBag: 'ordered', threshold: '+center' },
  ],

  deploy: ['A', 'B', 'C'],
};
