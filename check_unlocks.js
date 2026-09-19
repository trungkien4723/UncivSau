const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/';

function parseJSON(file) {
  const content = fs.readFileSync(file, 'utf8');
  // Remove single-line comments
  const cleaned = content.replace(/\/\/.*$/gm, '');
  return JSON.parse(cleaned);
}

const buildings = parseJSON(path + 'Buildings.json');
const units = parseJSON(path + 'Units.json');
const improvements = parseJSON(path + 'TileImprovements.json');
const districts = parseJSON(path + 'Districts.json');
const wonders = parseJSON(path + 'Wonders_new.json');

console.log('Buildings:', buildings.length);
console.log('Units:', units.length);
console.log('Improvements:', improvements.length);
console.log('Districts:', districts.length);
console.log('Wonders:', wonders.length);

const checks = [
  ['Building', 'Granary', buildings],
  ['Building', 'Library', buildings],
  ['Building', 'Shrine', buildings],
  ['Building', 'Barracks', buildings],
  ['Building', 'Workshop', buildings],
  ['Building', 'University', buildings],
  ['Building', 'Armory', buildings],
  ['Building', 'Bank', buildings],
  ['Building', 'Factory', buildings],
  ['Building', 'Sewer', buildings],
  ['Building', 'Stock Exchange', buildings],
  ['Building', 'Hangar', buildings],
  ['Building', 'Power Plant', buildings],
  ['Building', 'Broadcast Center', buildings],
  ['Building', 'Research Lab', buildings],
  ['Building', 'Airport', buildings],
  ['Building', 'Spaceport', buildings],
  ['Unit', 'Archer', units],
  ['Unit', 'Spearman', units],
  ['Unit', 'Heavy Chariot', units],
  ['Unit', 'Horseman', units],
  ['Unit', 'Swordsman', units],
  ['Unit', 'Quadrireme', units],
  ['Unit', 'Siege Tower', units],
  ['Unit', 'Catapult', units],
  ['Unit', 'Pikeman', units],
  ['Unit', 'Knight', units],
  ['Unit', 'Crossbowman', units],
  ['Unit', 'Musketman', units],
  ['Unit', 'Frigate', units],
  ['Unit', 'Bombard', units],
  ['Unit', 'Field Cannon', units],
  ['Unit', 'Cavalry', units],
  ['Unit', 'Ironclad', units],
  ['Unit', 'Ranger', units],
  ['Unit', 'Biplane', units],
  ['Unit', 'Infantry', units],
  ['Unit', 'Artillery', units],
  ['Unit', 'Battleship', units],
  ['Unit', 'Submarine', units],
  ['Unit', 'Tank', units],
  ['Unit', 'Bomber', units],
  ['Unit', 'Fighter', units],
  ['Unit', 'Helicopter', units],
  ['Improvement', 'Camp', improvements],
  ['Improvement', 'Pasture', improvements],
  ['Improvement', 'Mine', improvements],
  ['Improvement', 'Quarry', improvements],
  ['Improvement', 'Fishing Boats', improvements],
  ['Improvement', 'Plantation', improvements],
  ['Improvement', 'Lumber Mill', improvements],
  ['Improvement', 'Fort', improvements],
  ['Improvement', 'Airstrip', improvements],
  ['Improvement', 'Oil Well', improvements],
  ['Improvement', 'Seaside Resort', improvements],
  ['Improvement', 'Missile Silo', improvements],
  ['District', 'Holy Site', districts],
  ['District', 'Campus', districts],
  ['District', 'Encampment', districts],
  ['District', 'Commercial Hub', districts],
  ['District', 'Industrial Zone', districts],
  ['District', 'Royal Navy Dockyard', districts],
  ['District', 'Aqueduct', districts],
  ['District', 'Aerodrome', districts],
  ['District', 'Spaceport', districts],
  ['District', 'Dam', districts],
  ['Wonder', 'Stonehenge', wonders],
  ['Wonder', 'Hanging Gardens', wonders],
  ['Wonder', 'Pyramids', wonders],
  ['Wonder', 'Great Lighthouse', wonders],
  ['Wonder', 'Colossus', wonders],
  ['Wonder', 'Terracotta Army', wonders],
  ['Wonder', 'Petra', wonders],
  ['Wonder', 'Alhambra', wonders],
  ['Wonder', 'Hagia Sophia', wonders],
  ['Wonder', 'Venetian Arsenal', wonders],
  ['Wonder', 'Great Zimbabwe', wonders],
  ['Wonder', 'Forbidden City', wonders],
  ['Wonder', 'Potala Palace', wonders],
  ['Wonder', 'Ruhr Valley', wonders],
  ['Wonder', 'Oxford University', wonders],
  ['Wonder', 'Eiffel Tower', wonders],
  ['Wonder', 'Big Ben', wonders],
];

let missing = 0;
for (const [type, name, arr] of checks) {
  const found = arr.some(x => x.name === name);
  if (!found) {
    console.log('MISSING:', type, name);
    missing++;
  }
}

console.log('\nDone checking! Missing:', missing);