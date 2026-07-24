const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Techs.json';
const content = fs.readFileSync(path, 'utf8');
const techGroups = JSON.parse(content);

const descriptions = {
  'Pottery': 'Developed the ability to cultivate wheat and rice, enabling the rise of agriculture and permanent settlements.',
  'Animal Husbandry': 'Tamed cattle and sheep, providing reliable sources of food, wool, and labor for early civilizations.',
  'Archery': 'Mastered the bow and arrow, giving ranged combat capability against enemy units.',
  'Mining': 'Learned to chop forests and extract copper, revealing vital ore deposits for tool-making.',
  'Sailing': 'Enabled builders to embark on water and allowed coastal city founding with fishing boats and galley units.',
  'Astrology': 'Studied the stars to understand celestial patterns, often guiding the placement of religious sites and wonders like Stonehenge.',
  'Irrigation': 'Learned to clear marshes and farm resources efficiently, supporting agricultural expansion and the Hanging Gardens.',
  'Writing': 'Developed systems of record-keeping and communication, enabling libraries, campuses, and diplomacy with other civilizations.',
  'Masonry': 'Mastered stone-cutting and quarrying, enabling the construction of ancient walls, pyramids, and battering rams.',
  'Bronze Working': 'Introduced the Bronze Age. Chopping rainforests reveals iron deposits and unlocks military units like the Spearman.',
  'Wheel': 'One of humanity\'s greatest inventions — unlocked water mills, heavy chariots, and resource mining.',
  'Celestial Navigation': 'Enabled traders to embark on sea voyages, harvesting fish from ocean resources. Also unlocked the Royal Navy Dockyard.',
  'Currency': 'Standardized trade with minted money — the foundation of commerce, banks, and the Commercial Hub district.',
  'Horseback Riding': 'Tamed the horse for transportation and warfare — unlocked the Stable, Knights, and Pastures.',
  'Iron Working': 'Mastered iron smelting — revealed iron deposits and unlocked the Swordsman and Iron Mine.',
  'Shipbuilding': 'Advanced naval architecture allowing all land units to embark across oceans. Unlocked the Colossus and Quadrireme.',
  'Mathematics': 'Advanced mathematical knowledge that boosts naval movement speed and enables specialty district construction.',
  'Construction': 'Engineering fundamentals enabling the construction of water mills, siege towers, and the Terracotta Army.',
  'Engineering': 'Foundational engineering — enabled aqueducts, catapults, and ancient wall construction.',
  'Military Tactics': 'Developed military formations and strategies for organized warfare, unlocking the Pikeman.',
  'Apprenticeship': 'Systematic training of craftsmen and workers increased mine production and enabled the Industrial Zone.',
  'Stirrups': 'The invention of the stirrup revolutionized cavalry warfare in feudal times, unlocking the Knight.',
  'Machinery': 'Mechanical innovation powered the crossbowman and lumber mill — key medieval advancements.',
  'Education': 'Formalized learning and scholarship — earned Great Scientists, Hagia Sophia, and universities.',
  'Military Engineering': 'Applied engineering to warfare — enabled aqueducts, the Armory, and the Military Engineer.',
  'Castles': 'Medieval fortification knowledge provided culture bonuses from the Great Wall and expanded spy capacity under Catherine.',
  'Cartography': 'Advanced map-making that boosted fishing boat gold, enabled ocean navigation for all naval units, and unlocked the Caravel.',
  'Mass Production': 'Industrial-scale production techniques enabled shipyard construction and the Venetian Arsenal.',
  'Banking': 'Financial innovation that boosted quarry production and enabled banks and the Great Zimbabwe.',
  'Gunpowder': 'Revolutionary discovery of niter and gunpowder — unlocked the Armory and the Musketman.',
  'Printing': 'The printing press expanded diplomatic visibility and doubled tourism from Great Works of Writing — unlocked the Forbidden City.',
  'Square Rigging': 'Advanced sailing technology enabling ocean-going Frigates — a key naval combat unit.',
  'Astronomy': 'Scientific study of celestial bodies — enabled Potala Palace and university placement advantages.',
  'Metalcasting': 'Advanced metallurgy for manufacturing weapons — unlocked the Bombard.',
  'Siege Tactics': 'Specialized knowledge for constructing and operating fortifications and siege weapons.',
  'Industrialization': 'The Industrial Revolution — boosted mine production and enabled Factories and the Ruhr Valley wonder.',
  'Scientific Theory': 'The scientific method enabled research agreements and boosted plantation food — unlocked Oxford University.',
  'Ballistics': 'Study of projectile motion — enabled fortified positions and the Field Cannon.',
  'Military Science': 'Advanced military academy practices — unlocked Cavalry, Redcoats, and the Military Academy.',
  'Steam Power': 'Harnessed steam power — granted increased movement to embarked units, revealed coal, and unlocked Ironclads.',
  'Sanitation': 'Public health infrastructure — stepwells gained extra housing, and the sewer and medic units were unlocked.',
  'Economics': 'Economic theory and finance enabled stock exchanges, Big Ben, and enhanced trade networks.',
  'Rifling': 'Grooved barrel technology — enabled niter mines and the Ranger skirmisher unit.',
  'Flight': 'Pioneer aviation — boosted tourism from cultural improvements and unlocked hangars, airfields, and aircraft.',
  'Replaceable Parts': 'Mass production of standardized components upgraded farms to mechanized agriculture and unlocked Infantry.',
  'Steel': 'Advanced metallurgy for stronger weapons and infrastructure — unlocked the Eiffel Tower, Artillery, and Battleships.',
  'Electricity': 'Harnessing electrical power — enabled power plants, seaports, submarines, and privateer naval units.',
  'Radio': 'Wireless communication revolution — enabled national parks, broadcast centers, and seaside resorts.',
  'Chemistry': 'Understood chemical processes — unlocked research laboratories and anti-tank crews.',
  'Combustion': 'Internal combustion engine — enabled oil extraction, tank production, and revealed oil deposits.',
  'Advanced Flight': 'Modern aviation technology — revealed aluminum and unlocked bombers and fighters with airports.',
  'Rocketry': 'Space-age propulsion — boosted quarry production and enabled spaceports, missile silos, and satellite launches.',
  'Advanced Ballistics': 'Precision-guided weaponry — enabled anti-air guns and machine guns with power plant infrastructure.',
  'Combined Arms': 'Integrated military doctrine — enabled aircraft carriers and destroyers from airstrips.',
  'Plastics': 'Synthetic polymer chemistry — boosted fishing boat output and enabled offshore oil rigs.',
  'Computers': 'Digital computing revolution — granted additional spy capacity and doubled all tourism yields.',
  'Nuclear Fission': 'Atomic energy — revealed uranium and enabled nuclear devices and the Manhattan Project.',
  'Synthetic Materials': 'Man-made materials — boosted camp gold and enabled helicopter production and aerodromes.',
  'Telecommunications': 'Global communication networks — enabled nuclear submarines and dual broadcast center construction.',
  'Satellites': 'Orbital technology — boosted space program progress enabling moon landings and mechanized infantry.',
  'Guidance Systems': 'Precision targeting technology — enabled mobile SAMs and rocket artillery.',
  'Lasers': 'Light amplification technology — unlocked jet fighters and missile cruisers.',
  'Composites': 'Advanced material science for modern armor — unlocked modern armor and anti-tank units.',
  'Stealth Technology': 'Cutting-edge radar evasion and stealth design — unlocked the Jet Bomber.',
  'Robotics': 'Automated manufacturing — boosted pasture production and enabled Mars habitation projects.',
  'Nuclear Fusion': 'Controlled fusion energy — enabled thermonuclear devices, Mars reactors, and the Operation Ivy project.',
  'Nanotechnology': 'Molecular-level manufacturing — enabled aluminum mines and Mars hydroponics projects.',
  'Future Tech': 'Theoretical science beyond current understanding — can be researched repeatedly for score victory benefits.',
};

let count = 0;
let skipped = [];
for (const group of techGroups) {
  for (const tech of group.techs) {
    if (!tech.civilopediaText) {
      if (descriptions[tech.name]) {
        tech.civilopediaText = [{ text: descriptions[tech.name] }];
        count++;
      } else {
        skipped.push(tech.name);
      }
    }
  }
}

const output = JSON.stringify(techGroups, null, 2);
fs.writeFileSync(path, output + '\n', { encoding: 'utf8' });
console.log('Added civilopediaText to', count, 'techs');
if (skipped.length > 0) {
  console.log('Skipped (no description):', skipped);
}
