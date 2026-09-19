const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Buildings.json';

let content = fs.readFileSync(path, 'utf8');
content = content.replace(/\/\/.*$/gm, '');
const buildings = JSON.parse(content);

const correctData = {
  'Granary': { cost: 65, requiredTech: 'Pottery', maintenance: 1, stats: { Food: 2, Housing: 1 }, uniques: [] },
  'Water Mill': { cost: 100, requiredTech: 'Wheel', maintenance: 0, stats: { Food: 2, Production: 1, Housing: 1 }, uniques: [] },
  'Monument': { cost: 60, requiredTech: 'Writing', maintenance: 1, stats: { Culture: 2 }, uniques: [] },
  'Shrine': { cost: 70, requiredTech: 'Astrology', maintenance: 1, stats: { Faith: 2, GreatProphetPoints: 1 }, uniques: ['+1 Citizen slot', 'Allows purchasing Missionaries with Faith'] },
  'Ancient Walls': { cost: 100, requiredTech: 'Masonry', maintenance: 0, stats: { Defense: 200, Health: 100, Housing: 1 }, uniques: ['Ranged Strike'] },
  'Barracks': { cost: 65, requiredTech: 'Bronze Working', maintenance: 1, stats: { Production: 1, GreatGeneralPoints: 1 }, uniques: ['+1 Citizen slot', '+25% XP for units trained here', 'Units heal +15 HP/turn in city'] },
  'Library': { cost: 100, requiredTech: 'Writing', maintenance: 1, stats: { Science: 2, GreatScientistPoints: 1 }, uniques: ['+1 Citizen slot'] },
  'Market': { cost: 100, requiredTech: 'Currency', maintenance: 0, stats: { Gold: 2, GreatMerchantPoints: 1 }, uniques: ['+1 Citizen slot', '+1 Trade Route capacity'] },
  'Lighthouse': { cost: 100, requiredTech: 'Sailing', maintenance: 0, stats: { Food: 1, Gold: 2, GreatAdmiralPoints: 1, Housing: 1 }, uniques: ['+1 Citizen slot'] },
  'Stable': { cost: 100, requiredTech: 'Horseback Riding', maintenance: 1, stats: { Production: 2, GreatGeneralPoints: 1 }, uniques: ['+1 Citizen slot', '+25% XP for cavalry units'] },
  'Temple': { cost: 210, requiredCivic: 'Political Philosophy', maintenance: 2, stats: { Faith: 4, GreatProphetPoints: 1 }, uniques: ['+1 Citizen slot', 'Allows purchasing Apostles with Faith', '+1 Relic slot'] },
  'Workshop': { cost: 145, requiredTech: 'Apprenticeship', maintenance: 1, stats: { Production: 2, GreatEngineerPoints: 1 }, uniques: ['+1 Citizen slot'] },
  'University': { cost: 210, requiredTech: 'Education', maintenance: 2, stats: { Science: 4, GreatScientistPoints: 1 }, uniques: ['+1 Citizen slot', '+1 Science per Campus adjacency'] },
  'Bank': { cost: 210, requiredTech: 'Banking', maintenance: 0, stats: { Gold: 4, GreatMerchantPoints: 1 }, uniques: ['+1 Citizen slot', '+1 Trade Route capacity'] },
  'Armory': { cost: 185, requiredTech: 'Gunpowder', maintenance: 2, stats: { Production: 2, GreatGeneralPoints: 1 }, uniques: ['+1 Citizen slot', '+25% XP for all units', 'Units heal +15 HP/turn in city'] },
  'Medieval Walls': { cost: 185, requiredTech: 'Castles', maintenance: 0, stats: { Defense: 400, Health: 200, Housing: 2 }, uniques: ['Ranged Strike'] },
  'Factory': { cost: 235, requiredTech: 'Industrialization', maintenance: 2, stats: { Production: 3, GreatEngineerPoints: 1 }, uniques: ['+1 Citizen slot', 'Regional effect: +3 Production to all cities within 6 tiles'] },
  'Stock Exchange': { cost: 335, requiredTech: 'Economics', maintenance: 0, stats: { Gold: 8, GreatMerchantPoints: 1 }, uniques: ['+1 Citizen slot', '+1 Trade Route capacity'] },
  'Renaissance Walls': { cost: 300, requiredTech: 'Siege Tactics', maintenance: 0, stats: { Defense: 600, Health: 300, Housing: 3 }, uniques: ['Ranged Strike'] },
  'Power Plant': { cost: 235, requiredTech: 'Electricity', maintenance: 3, requiredBuilding: 'Factory', stats: { Production: 4, ProductionPercent: 10 }, uniques: ['PowerProduction: 6', 'Requires Coal', 'Regional effect: +4 Production to all cities within 6 tiles'] },
  'Hydroelectric Dam': { cost: 310, requiredTech: 'Electricity', maintenance: 2, stats: { Production: 6, ProductionPercent: 10 }, uniques: ['PowerProduction: 6', 'Must be adjacent to River', 'Regional effect: +6 Production to all cities within 6 tiles'] },
  'Coal Power Plant': { cost: 235, requiredTech: 'Electricity', maintenance: 3, requiredBuilding: 'Factory', stats: { Production: 4, ProductionPercent: 10 }, uniques: ['PowerProduction: 6', 'Requires Coal', 'Regional effect: +4 Production to all cities within 6 tiles'] },
  'Oil Power Plant': { cost: 235, requiredTech: 'Combustion', maintenance: 3, requiredBuilding: 'Factory', stats: { Production: 4, ProductionPercent: 10 }, uniques: ['PowerProduction: 6', 'Requires Oil', 'Regional effect: +4 Production to all cities within 6 tiles'] },
  'Nuclear Power Plant': { cost: 310, requiredTech: 'Nuclear Fusion', maintenance: 3, requiredBuilding: 'Factory', stats: { Production: 4, ProductionPercent: 10 }, uniques: ['PowerProduction: 6', 'Requires Uranium', 'Regional effect: +6 Production to all cities within 6 tiles'] },
  'Research Lab': { cost: 310, requiredTech: 'Plastics', maintenance: 3, stats: { Science: 4, GreatScientistPoints: 1 }, uniques: ['+1 Citizen slot', '+1 Science per Campus adjacency'] },
  'Broadcast Center': { cost: 310, requiredTech: 'Radio', maintenance: 3, stats: { Culture: 4, GreatMusicianPoints: 1 }, uniques: ['+1 Citizen slot'] },
  'Airport': { cost: 310, requiredTech: 'Flight', maintenance: 2, stats: { GreatScientistPoints: 1 }, uniques: ['+1 Citizen slot', 'Allows airlifting units between cities', 'Air unit capacity: 2'] },
  'Sewer': { cost: 185, requiredTech: 'Sanitation', maintenance: 2, stats: { Housing: 2 }, uniques: [] },
  'Neighborhood': { cost: 60, requiredTech: 'Sanitation', maintenance: 0, stats: { Housing: 2 }, uniques: ['Housing based on tile appeal'] },
  'Spaceport': { cost: 2000, requiredTech: 'Rocketry', maintenance: 0, district: 'Spaceport', stats: {}, uniques: ['Creates a [Spaceport] district on a specific tile'] },
  'Aqueduct': { cost: 100, requiredTech: 'Engineering', district: 'Aqueduct', maintenance: 0, stats: { Housing: 2 }, uniques: ['Provides fresh water to city', 'Housing based on fresh water availability'] },
  'Bath': { cost: 100, requiredTech: 'Engineering', district: 'Aqueduct', maintenance: 0, stats: { Housing: 2, Amenities: 1 }, uniques: ['Provides fresh water to city', 'Housing based on fresh water availability', '+1 Amenity'] },
};

let updated = 0;
let notInMap = [];
for (const b of buildings) {
  const correct = correctData[b.name];
  if (correct) {
    if (correct.cost !== undefined) b.cost = correct.cost;
    if (correct.maintenance !== undefined) b.maintenance = correct.maintenance;
    if (correct.requiredTech !== undefined) { b.requiredTech = correct.requiredTech; delete b.requiredCivic; }
    if (correct.requiredCivic !== undefined) { b.requiredCivic = correct.requiredCivic; delete b.requiredTech; }
    if (correct.district !== undefined) b.district = correct.district;
    if (correct.requiredBuilding !== undefined) b.requiredBuilding = correct.requiredBuilding;
    if (correct.stats !== undefined) b.stats = correct.stats;
    if (correct.uniques !== undefined) b.uniques = correct.uniques;
    updated++;
  } else {
    notInMap.push(b.name);
  }
}

fs.writeFileSync(path, JSON.stringify(buildings, null, 2) + '\n', { encoding: 'utf8' });
console.log('Updated:', updated, 'buildings');
console.log('Not in map (need manual check):', notInMap.length);
console.log('Samples:', notInMap.slice(0, 30));