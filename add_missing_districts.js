const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Buildings.json';

let content = fs.readFileSync(path, 'utf8');
content = content.replace(/\/\/.*$/gm, '');
const buildings = JSON.parse(content);

// Add missing unique district replacement buildings
const newBuildings = [
  // Greece - Acropolis (Theater Square replacement)
  { "name": "Acropolis", "district": "Theater Square", "cost": 36, "requiredCivic": "Drama and Poetry", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Theater Square] district on a specific tile", "Must be on a Hill", "Provides +1 Culture for each adjacent district"] },
  
  // Russia - Lavra (Holy Site replacement)
  { "name": "Lavra", "district": "Holy Site", "cost": 36, "requiredTech": "Astrology", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Holy Site] district on a specific tile", "Provides +1 Great Prophet point per turn", "+1 Culture for each adjacent district"] },
  
  // Zulu - Stronghold (Encampment replacement)
  { "name": "Stronghold", "district": "Encampment", "cost": 36, "requiredTech": "Bronze Working", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Encampment] district on a specific tile", "Provides +2 Housing", "+2 Production"] },
  
  // Korea - Seowon (Campus replacement)
  { "name": "Seowon", "district": "Campus", "cost": 36, "requiredTech": "Writing", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Campus] district on a specific tile", "Must be on a Hill", "Base Science: +4", "Adjacency bonus: +2 Science for each adjacent Mine, +1 for each adjacent Farm/Plantation/Lumber mill"] },
  
  // Rome - Oppidum (Encampment replacement)
  { "name": "Oppidum", "district": "Encampment", "cost": 36, "requiredTech": "Bronze Working", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Encampment] district on a specific tile", "Provides +2 Housing", "+1 Great General point per turn"] },
  
  // Germany - Hansa (Industrial Zone replacement)
  { "name": "Hansa", "district": "Industrial Zone", "cost": 36, "requiredTech": "Apprenticeship", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Industrial Zone] district on a specific tile", "+2 Production per adjacent Commercial Hub/Market/Bank/Stock Exchange", "+1 Production per adjacent resource", "+2 Production per adjacent Aqueduct/Dam/Canal"] },
  
  // England - Royal Navy Dockyard (Harbor replacement)
  { "name": "Royal Navy Dockyard", "district": "Harbor", "cost": 36, "requiredTech": "Celestial Navigation", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Harbor] district on a specific tile", "Must be on Coast", "+2 Gold per adjacent district", "+1 Trade Route capacity", "Naval units get +1 Movement"] },
  
  // Phoenicia - Cothon (Harbor replacement)
  { "name": "Cothon", "district": "Harbor", "cost": 36, "requiredTech": "Celestial Navigation", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Harbor] district on a specific tile", "Must be on Coast", "+50% Production towards Settlers", "Naval units heal +20 HP/turn"] },
  
  // Mali - Suguba (Commercial Hub replacement)
  { "name": "Suguba", "district": "Commercial Hub", "cost": 36, "requiredTech": "Currency", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Commercial Hub] district on a specific tile", "+2 Gold per adjacent Holy Site/Shrine/Temple", "+1 Gold per adjacent River tile", "-20% Production cost for Commercial Hub buildings"] },
  
  // Aerodrome (district placeholder - already exists as district, this is the building)
  { "name": "Aerodrome", "district": "Aerodrome", "cost": 36, "requiredTech": "Flight", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Aerodrome] district on a specific tile"] },
  
  // Preserve (district placeholder)
  { "name": "Preserve", "district": "Preserve", "cost": 36, "requiredCivic": "Conservation", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Preserve] district on a specific tile"] },
  
  // Diplomatic Quarter (district placeholder - GS)
  { "name": "Diplomatic Quarter", "district": "Diplomatic Quarter", "cost": 36, "requiredCivic": "Diplomatic Service", "maintenance": 0, "uniques": ["Unbuildable", "Creates a [Diplomatic Quarter] district on a specific tile"] },
  
  // Vietnam - Thành (Encampment replacement) - note: already exists as "Thành" in the data
];

for (const b of newBuildings) {
  if (!buildings.some(x => x.name === b.name)) {
    buildings.push(b);
  }
}

// Fix existing district placeholders to have correct format
const districtFixes = {
  'Campus': { cost: 36, requiredTech: 'Writing', uniques: ['Unbuildable', 'Creates a [Campus] district on a specific tile'] },
  'Theater Square': { cost: 36, requiredCivic: 'Drama and Poetry', uniques: ['Unbuildable', 'Creates a [Theater Square] district on a specific tile'] },
  'Holy Site': { cost: 36, requiredTech: 'Astrology', uniques: ['Unbuildable', 'Creates a [Holy Site] district on a specific tile'] },
  'Commercial Hub': { cost: 36, requiredTech: 'Currency', uniques: ['Unbuildable', 'Creates a [Commercial Hub] district on a specific tile'] },
  'Industrial Zone': { cost: 36, requiredTech: 'Apprenticeship', uniques: ['Unbuildable', 'Creates a [Industrial Zone] district on a specific tile'] },
  'Neighborhood': { cost: 36, requiredTech: 'Sanitation', uniques: ['Unbuildable', 'Creates a [Neighborhood] district on a specific tile', 'Housing based on tile appeal'] },
  'Encampment': { cost: 36, requiredTech: 'Bronze Working', uniques: ['Unbuildable', 'Creates a [Encampment] district on a specific tile'] },
  'Harbor': { cost: 36, requiredTech: 'Celestial Navigation', uniques: ['Unbuildable', 'Creates a [Harbor] district on a specific tile', 'Must be next to [Coast]'] },
  'Aqueduct': { cost: 36, requiredTech: 'Engineering', uniques: ['Unbuildable', 'Creates a [Aqueduct] district on a specific tile'] },
  'Entertainment Complex': { cost: 36, requiredCivic: 'Games and Recreation', uniques: ['Unbuildable', 'Creates a [Entertainment Complex] district on a specific tile'] },
  'Water Entertainment Complex': { cost: 36, requiredCivic: 'Games and Recreation', uniques: ['Unbuildable', 'Creates a [Water Entertainment Complex] district on a specific tile', 'Requires [Coastal]'] },
  'Spaceport': { cost: 2000, requiredTech: 'Rocketry', uniques: ['Unbuildable', 'Creates a [Spaceport] district on a specific tile'] },
  'Government Plaza': { cost: 36, requiredCivic: 'Political Philosophy', uniques: ['Unbuildable', 'Creates a [Government Plaza] district on a specific tile'] },
};

for (const b of buildings) {
  const fix = districtFixes[b.name];
  if (fix) {
    b.cost = fix.cost;
    if (fix.requiredTech) b.requiredTech = fix.requiredTech;
    else delete b.requiredTech;
    if (fix.requiredCivic) b.requiredCivic = fix.requiredCivic;
    else delete b.requiredCivic;
    b.uniques = fix.uniques;
    b.maintenance = 0;
    b.district = b.name;
  }
}

fs.writeFileSync(path, JSON.stringify(buildings, null, 2) + '\n', { encoding: 'utf8' });
console.log('Done! Total buildings:', buildings.length);