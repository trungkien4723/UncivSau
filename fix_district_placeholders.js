const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Buildings.json';

let content = fs.readFileSync(path, 'utf8');
content = content.replace(/\/\/.*$/gm, '');
const buildings = JSON.parse(content);

// Fix district placeholder buildings - they should all be "Unbuildable" with cost 36 (except Spaceport)
const districtPlaceholders = {
  'Campus': { requiredTech: 'Writing', cost: 36 },
  'Theater Square': { requiredCivic: 'Drama and Poetry', cost: 36 },
  'Holy Site': { requiredTech: 'Astrology', cost: 36 },  // Not Pottery - Astrology unlocks Holy Site district
  'Commercial Hub': { requiredTech: 'Currency', cost: 36 },
  'Industrial Zone': { requiredTech: 'Apprenticeship', cost: 36 },
  'Neighborhood': { requiredTech: 'Sanitation', cost: 36 },  // Changed from 60
  'Encampment': { requiredTech: 'Bronze Working', cost: 36 },
  'Harbor': { requiredTech: 'Celestial Navigation', cost: 36 },  // Not Sailing - Celestial Navigation unlocks Harbor district
  'Aqueduct': { requiredTech: 'Engineering', cost: 36 },
  'Entertainment Complex': { requiredCivic: 'Games and Recreation', cost: 36 },
  'Water Entertainment Complex': { requiredCivic: 'Games and Recreation', cost: 36 },
  'Spaceport': { requiredTech: 'Rocketry', cost: 2000 },
  'Government Plaza': { requiredCivic: 'Political Philosophy', cost: 36 },
  'Aerodrome': { requiredTech: 'Flight', cost: 36 },
  'Preserve': { requiredCivic: 'Conservation', cost: 36 },
  'Diplomatic Quarter': { requiredCivic: 'Diplomatic Service', cost: 36 },
};

let updated = 0;
for (const b of buildings) {
  const fix = districtPlaceholders[b.name];
  if (fix) {
    b.cost = fix.cost;
    if (fix.requiredTech) { b.requiredTech = fix.requiredTech; delete b.requiredCivic; }
    if (fix.requiredCivic) { b.requiredCivic = fix.requiredCivic; delete b.requiredTech; }
    // Add Unbuildable
    if (!b.uniques.includes('Unbuildable')) {
      b.uniques.unshift('Unbuildable');
    }
    // Ensure "Creates a [District] district" unique exists
    if (!b.uniques.some(u => u.includes('Creates a'))) {
      b.uniques.push('Creates a [' + b.name + '] district on a specific tile');
    }
    updated++;
  }
}

// Also fix Harbor and Aqueduct to not have their production uniques (those are for the actual buildings in the district)
const harbor = buildings.find(b => b.name === 'Harbor');
if (harbor) {
  harbor.uniques = ['Unbuildable', 'Creates a [Harbor] district on a specific tile', 'Must be next to [Coast]'];
}
const aqueduct = buildings.find(b => b.name === 'Aqueduct');
if (aqueduct) {
  aqueduct.uniques = ['Unbuildable', 'Creates a [Aqueduct] district on a specific tile'];
}
const neighborhood = buildings.find(b => b.name === 'Neighborhood');
if (neighborhood) {
  neighborhood.uniques = ['Unbuildable', 'Creates a [Neighborhood] district on a specific tile', 'Housing based on tile appeal'];
}

// Fix Spaceport - it already has correct unique
const spaceport = buildings.find(b => b.name === 'Spaceport');
if (spaceport) {
  spaceport.uniques = ['Unbuildable', 'Creates a [Spaceport] district on a specific tile'];
}

fs.writeFileSync(path, JSON.stringify(buildings, null, 2) + '\n', { encoding: 'utf8' });
console.log('Fixed', updated, 'district placeholder buildings');