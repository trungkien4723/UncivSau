const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Techs.json';
const data = JSON.parse(fs.readFileSync(path, 'utf8'));

// Comprehensive tech descriptions with unlocks from Civ 6 Civilopedia/IGN guide
const techDetails = {
  // Ancient Era
  'Pottery': {
    desc: 'Developed the ability to cultivate wheat and rice, enabling the rise of agriculture and permanent settlements.',
    unlocks: 'Unlocks: Granary (building), enables Irrigation and Writing technologies.'
  },
  'Animal Husbandry': {
    desc: 'Tamed cattle and sheep, providing reliable sources of food, wool, and labor for early civilizations.',
    unlocks: 'Unlocks: Camp, Pasture (improvements); reveals Horses resource; enables Archery and Horseback Riding.'
  },
  'Archery': {
    desc: 'Mastered the bow and arrow, giving ranged combat capability against enemy units.',
    unlocks: 'Unlocks: Archer (unit); boosted by killing a unit with a Slinger.'
  },
  'Mining': {
    desc: 'Learned to chop forests and extract copper, revealing vital ore deposits for tool-making.',
    unlocks: 'Unlocks: Mine, Quarry (improvements); reveals Iron resource; enables Bronze Working, Masonry, Wheel.'
  },
  'Sailing': {
    desc: 'Enabled builders to embark on water and allowed coastal city founding with fishing boats and galley units.',
    unlocks: 'Unlocks: Fishing Boats (improvement), Galley (unit); enables Shipbuilding and Celestial Navigation; builders can embark.'
  },
  'Astrology': {
    desc: 'Studied the stars to understand celestial patterns, often guiding the placement of religious sites and wonders like Stonehenge.',
    unlocks: 'Unlocks: Shrine (building), Stonehenge (wonder), Holy Site (district); boosted by finding a Natural Wonder.'
  },
  'Irrigation': {
    desc: 'Learned to clear marshes and farm resources efficiently, supporting agricultural expansion and the Hanging Gardens.',
    unlocks: 'Unlocks: Plantation (improvement), Hanging Gardens (wonder); enables Feudalism (civic); boosted by improving a resource with a Farm.'
  },
  'Writing': {
    desc: 'Developed systems of record-keeping and communication, enabling libraries, campuses, and diplomacy with other civilizations.',
    unlocks: 'Unlocks: Library (building), Campus (district); enables Currency, Education, and Recorded History (civic); boosted by meeting another civilization.'
  },
  'Masonry': {
    desc: 'Mastered stone-cutting and quarrying, enabling the construction of ancient walls, pyramids, and battering rams.',
    unlocks: 'Unlocks: Ancient Walls (building), Pyramids (wonder), Battering Ram (unit); enables Construction; boosted by building a Quarry.'
  },
  'Bronze Working': {
    desc: 'Introduced the Bronze Age — chopping rainforests reveals iron deposits and unlocks military units like the Spearman.',
    unlocks: 'Unlocks: Barracks (building), Encampment (district), Spearman (unit); reveals Iron resource; enables Iron Working and Machinery; boosted by killing 3 Barbarians.'
  },
  'Wheel': {
    desc: 'One of humanity\'s greatest inventions — unlocked water mills, heavy chariots, and resource mining.',
    unlocks: 'Unlocks: Water Mill (building), Heavy Chariot (unit); enables Engineering; boosted by mining a resource.'
  },

  // Classical Era
  'Celestial Navigation': {
    desc: 'Enabled traders to embark on sea voyages, harvesting fish from ocean resources. Also unlocked the Royal Navy Dockyard.',
    unlocks: 'Unlocks: Lighthouse (building), Great Lighthouse (wonder), Royal Navy Dockyard (district); Traders can embark; boosted by improving 2 sea resources.'
  },
  'Currency': {
    desc: 'Standardized trade with minted money — the foundation of commerce, banks, and the Commercial Hub district.',
    unlocks: 'Unlocks: Market (building), Commercial Hub (district); enables Banking, Economics, and Guilds (civic); boosted by making a Trade Route.'
  },
  'Horseback Riding': {
    desc: 'Tamed the horse for transportation and warfare — unlocked the Stable, Knights, and Pastures.',
    unlocks: 'Unlocks: Stable (building), Horseman (unit); enables Stirrups, Apprenticeship, and Construction; boosted by building a Pasture.'
  },
  'Iron Working': {
    desc: 'Mastered iron smelting — revealed iron deposits and unlocked the Swordsman and Iron Mine.',
    unlocks: 'Unlocks: Swordsman (unit); reveals Niter resource (later era); enables Machinery and Military Engineering; boosted by building an Iron Mine.'
  },
  'Shipbuilding': {
    desc: 'Advanced naval architecture allowing all land units to embark across oceans. Unlocked the Colossus and Quadrireme.',
    unlocks: 'Unlocks: Colossus (wonder), Quadrireme (unit); all land units can embark across ocean; enables Buttress (GS); boosted by owning 2 Galleys.'
  },
  'Mathematics': {
    desc: 'Advanced mathematical knowledge that boosts naval movement speed and enables specialty district construction.',
    unlocks: 'Unlocks: Petra (wonder); +1 Movement for all naval units; enables Military Tactics, Buttress (GS), Education; boosted by building 3 different specialty districts.'
  },
  'Construction': {
    desc: 'Engineering fundamentals enabling the construction of water mills, siege towers, and the Terracotta Army.',
    unlocks: 'Unlocks: Terracotta Army (wonder), Siege Tower (unit); enables Castles, Military Engineering; boosted by building a Water Mill.'
  },
  'Engineering': {
    desc: 'Foundational engineering — enabled aqueducts, catapults, and ancient wall construction.',
    unlocks: 'Unlocks: Aqueduct (district), Catapult (unit); enables Machinery, Apprenticeship; boosted by building Ancient Walls.'
  },

  // Medieval Era
  'Military Tactics': {
    desc: 'Developed military formations and strategies for organized warfare, unlocking the Pikeman.',
    unlocks: 'Unlocks: Pikeman (unit); enables Siege Tactics, Mass Production; boosted by killing a unit with a Spearman.'
  },
  'Apprenticeship': {
    desc: 'Systematic training of craftsmen and workers increased mine production and enabled the Industrial Zone.',
    unlocks: 'Unlocks: Workshop (building), Industrial Zone (district); +1 Production to Mine improvements; enables Military Science, Industrialization; boosted by building 3 Mines.'
  },
  'Stirrups': {
    desc: 'The invention of the stirrup revolutionized cavalry warfare in feudal times, unlocking the Knight.',
    unlocks: 'Unlocks: Knight (unit); enables Military Science, Gunpowder; boosted by having the Feudalism civic.'
  },
  'Machinery': {
    desc: 'Mechanical innovation powered the crossbowman and lumber mill — key medieval advancements.',
    unlocks: 'Unlocks: Crossbowman (unit), Lumber Mill (improvement); enables Printing, Ballistics; boosted by owning 3 Archers.'
  },
  'Education': {
    desc: 'Formalized learning and scholarship — earned Great Scientists, Hagia Sophia, and universities.',
    unlocks: 'Unlocks: University (building), Hagia Sophia (wonder); enables Banking, Astronomy, Scientific Theory; boosted by earning a Great Scientist.'
  },
  'Military Engineering': {
    desc: 'Applied engineering to warfare — enabled aqueducts, the Armory, and the Military Engineer.',
    unlocks: 'Unlocks: Armory (building), Military Engineer (unit); enables Siege Tactics, Steel; boosted by building an Aqueduct.'
  },
  'Castles': {
    desc: 'Medieval fortification knowledge provided culture bonuses from the Great Wall and expanded spy capacity under Catherine.',
    unlocks: 'Unlocks: Medieval Walls (building), Alhambra (wonder); enables Siege Tactics; boosted by adopting a government with 6 policy slots.'
  },
  'Buttress': {
    desc: 'Architectural supports reinforcing walls, enabling the construction of taller structures, dams, and grand cathedrals with stained glass.',
    unlocks: 'Unlocks: Dam (district), Hagia Sophia (wonder); enables Cartography, Mass Production; boosted by building a Classical era or later wonder. (Gathering Storm)'
  },

  // Renaissance Era
  'Cartography': {
    desc: 'Advanced map-making that boosted fishing boat gold, enabled ocean navigation for all naval units, and unlocked the Caravel.',
    unlocks: 'Unlocks: Caravel (unit); +1 Gold from Fishing Boats; all naval/embarked units can enter Ocean tiles; +1 Movement for embarked units; enables Square Rigging, Astronomy; boosted by building 2 Harbors.'
  },
  'Mass Production': {
    desc: 'Industrial-scale production techniques enabled shipyard construction and the Venetian Arsenal.',
    unlocks: 'Unlocks: Shipyard (building), Venetian Arsenal (wonder); enables Steel, Industrialization; boosted by building a Lumber Mill.'
  },
  'Banking': {
    desc: 'Financial innovation that boosted quarry production and enabled banks and the Great Zimbabwe.',
    unlocks: 'Unlocks: Bank (building), Great Zimbabwe (wonder); +1 Gold from Quarry improvements; enables Economics, Chemistry; boosted by having the Guilds civic.'
  },
  'Gunpowder': {
    desc: 'Revolutionary discovery of niter and gunpowder — unlocked the Armory and the Musketman.',
    unlocks: 'Unlocks: Musketman (unit), Armory (building); reveals Niter resource; enables Metal Casting, Military Science; boosted by building an Armory.'
  },
  'Printing': {
    desc: 'The printing press expanded diplomatic visibility and doubled tourism from Great Works of Writing — unlocked the Forbidden City.',
    unlocks: 'Unlocks: Forbidden City (wonder); +1 Diplomatic Visibility; Tourism from Great Works of Writing doubled; enables Military Science, Industrialization; boosted by building 2 Universities.'
  },
  'Square Rigging': {
    desc: 'Advanced sailing technology enabling ocean-going Frigates — a key naval combat unit.',
    unlocks: 'Unlocks: Frigate (unit); enables Rifling, Steel; boosted by killing a unit with a Musketman.'
  },
  'Astronomy': {
    desc: 'Scientific study of celestial bodies — enabled Potala Palace and university placement advantages.',
    unlocks: 'Unlocks: Potala Palace (wonder); enables Scientific Theory, Flight; boosted by building a University adjacent to a Mountain.'
  },
  'Metal Casting': {
    desc: 'Pouring molten metal into molds — a transformative technique enabling mass production of identical metal objects, weapons, and tools.',
    unlocks: 'Unlocks: Bombard (unit); enables Ballistics, Economics; boosted by owning 2 Crossbowmen.'
  },
  'Siege Tactics': {
    desc: 'Specialized knowledge for constructing and operating fortifications and siege weapons.',
    unlocks: 'Unlocks: Renaissance Walls (building), Fort (improvement); enables Field Cannon, Advanced Ballistics; boosted by owning 2 Bombards.'
  },

  // Industrial Era
  'Industrialization': {
    desc: 'The Industrial Revolution — boosted mine production and enabled Factories and the Ruhr Valley wonder.',
    unlocks: 'Unlocks: Factory (building), Ruhr Valley (wonder); +1 Production to Mine improvements; enables Steel, Flight, Advanced Flight; boosted by building 3 Workshops.'
  },
  'Scientific Theory': {
    desc: 'The scientific method enabled research agreements and boosted plantation food — unlocked Oxford University.',
    unlocks: 'Unlocks: Oxford University (wonder); enables Research Agreement diplomatic action; +1 Food from Plantation improvements; enables Sanitation, Electricity, Chemistry; boosted by having The Enlightenment civic.'
  },
  'Ballistics': {
    desc: 'Study of projectile motion — enabled fortified positions and the Field Cannon.',
    unlocks: 'Unlocks: Field Cannon (unit); enables Advanced Ballistics, Rifling; boosted by having 2 Forts in territory.'
  },
  'Military Science': {
    desc: 'Advanced military academy practices — unlocked Cavalry, Redcoats, and the Military Academy.',
    unlocks: 'Unlocks: Cavalry (unit), Redcoat (unit), Military Academy (building); enables Steel, Combustion; boosted by killing a unit with a Knight.'
  },
  'Steam Power': {
    desc: 'Harnessed steam power — granted increased movement to embarked units, revealed coal, and unlocked Ironclads.',
    unlocks: 'Unlocks: Ironclad (unit); +2 Movement for embarked units; reveals Coal resource; enables Sanitation, Advanced Flight, Combustion; boosted by building 2 Shipyards.'
  },
  'Sanitation': {
    desc: 'Public health infrastructure — stepwells gained extra housing, and the sewer and medic units were unlocked.',
    unlocks: 'Unlocks: Sewer (building), Medic (unit); Stepwell improvements +1 Housing; enables Neighborhood (district); boosted by building 2 Neighborhoods.'
  },
  'Economics': {
    desc: 'Economic theory and finance enabled stock exchanges, Big Ben, and enhanced trade networks.',
    unlocks: 'Unlocks: Stock Exchange (building), Big Ben (wonder); enables Replaceable Parts, Radio; boosted by building 2 Banks.'
  },
  'Rifling': {
    desc: 'Grooved barrel technology — enabled niter mines and the Ranger skirmisher unit.',
    unlocks: 'Unlocks: Ranger (unit); reveals Niter resource (if not revealed); enables Steel, Refining (GS); boosted by building a Niter Mine.'
  },

  // Modern Era
  'Flight': {
    desc: 'Pioneer aviation — boosted tourism from cultural improvements and unlocked hangars, airfields, and aircraft.',
    unlocks: 'Unlocks: Biplane (unit), Observation Balloon (unit), Hangar (building), Aerodrome (district), Airstrip (improvement); Tourism bonus to improvements with Culture; enables Advanced Flight, Radar (GS); boosted by building an Industrial era or later wonder.'
  },
  'Replaceable Parts': {
    desc: 'Mass production of standardized components upgraded farms to mechanized agriculture and unlocked Infantry.',
    unlocks: 'Unlocks: Infantry (unit); Farms upgraded to Mechanized Agriculture; +1 Food adjacency for adjacent Farms; enables Advanced Ballistics, Computers, Combustion; boosted by owning 3 Musketmen.'
  },
  'Steel': {
    desc: 'Advanced metallurgy for stronger weapons and infrastructure — unlocked the Eiffel Tower, Artillery, and Battleships.',
    unlocks: 'Unlocks: Artillery (unit), Battleship (unit), Eiffel Tower (wonder); +1 Production from Lumber Mills; enables Advanced Ballistics, Combined Arms, Combustion, Nuclear Fission; boosted by building a Coal Mine.'
  },
  'Electricity': {
    desc: 'Harnessing electrical power — enabled power plants, seaports, submarines, and privateer naval units.',
    unlocks: 'Unlocks: Power Plant (building), Seaport (building), Submarine (unit); enables Radio, Nuclear Fission; boosted by owning 3 Privateers.'
  },
  'Radio': {
    desc: 'Wireless communication revolution — enabled national parks, broadcast centers, and seaside resorts.',
    unlocks: 'Unlocks: Broadcast Center (building), Seaside Resort (improvement); enables National Park; enables Advanced Flight, Computers; boosted by building a National Park.'
  },
  'Chemistry': {
    desc: 'Understood chemical processes — unlocked research laboratories and anti-tank crews.',
    unlocks: 'Unlocks: Research Lab (building), AT Crew (unit); enables Plastics, Nuclear Fission, Guidance Systems; boosted by completing a Research Agreement.'
  },
  'Combustion': {
    desc: 'Internal combustion engine — enabled oil extraction, tank production, and revealed oil deposits.',
    unlocks: 'Unlocks: Tank (unit), Oil Well (improvement); +1 Movement for embarked units; reveals Oil resource; enables Advanced Flight, Plastics, Synthetic Materials; boosted by extracting an Artifact.'
  },

  // Atomic Era
  'Advanced Flight': {
    desc: 'Modern aviation technology — revealed aluminum and unlocked bombers and fighters with airports.',
    unlocks: 'Unlocks: Bomber (unit), Fighter (unit), Airport (building); reveals Aluminum resource; enables Rocketry, Satellites, Nuclear Fission; boosted by building 3 Biplanes.'
  },
  'Rocketry': {
    desc: 'Space-age propulsion — boosted quarry production and enabled spaceports, missile silos, and satellite launches.',
    unlocks: 'Unlocks: Spaceport (district), Missile Silo (improvement), Launch Earth Satellite (project); +1 Production from Quarries; enables Guidance Systems, Nuclear Fusion; boosted by Great Scientist or Spy.'
  },
  'Advanced Ballistics': {
    desc: 'Precision-guided weaponry — enabled anti-air guns and machine guns with power plant infrastructure.',
    unlocks: 'Unlocks: Anti-Air Gun (unit), Machine Gun (unit); enables Robotics, Lasers; boosted by building 2 Power Plants.'
  },
  'Combined Arms': {
    desc: 'Integrated military doctrine — enabled aircraft carriers and destroyers from airstrips.',
    unlocks: 'Unlocks: Aircraft Carrier (unit), Destroyer (unit); enables Robotics, Nanotechnology; boosted by building an Airstrip.'
  },
  'Plastics': {
    desc: 'Synthetic polymer chemistry — boosted fishing boat output and enabled offshore oil rigs.',
    unlocks: 'Unlocks: Offshore Oil Rig (improvement); +1 Food to Fishing Boats; enables Composites, Nanotechnology, Satellites; boosted by building an Oil Well.'
  },
  'Computers': {
    desc: 'Digital computing revolution — granted additional spy capacity and doubled all tourism yields.',
    unlocks: 'Unlocks: Additional Spy capacity; All Tourism yields doubled; enables Robotics, Telecommunications, Nuclear Fusion; boosted by having a government with 8 policy slots.'
  },
  'Nuclear Fission': {
    desc: 'Atomic energy — revealed uranium and enabled nuclear devices and the Manhattan Project.',
    unlocks: 'Unlocks: Nuclear Device (project), Manhattan Project (project); reveals Uranium resource; enables Nuclear Fusion, Thermonuclear Device; boosted by Great Scientist or Spy.'
  },
  'Synthetic Materials': {
    desc: 'Man-made materials — boosted camp gold and enabled helicopter production and aerodromes.',
    unlocks: 'Unlocks: Helicopter (unit); +1 Gold to Camps; enables Nanotechnology, Robotics; boosted by building 2 Aerodromes.'
  },

  // Information Era
  'Telecommunications': {
    desc: 'Global communication networks — enabled nuclear submarines and dual broadcast center construction.',
    unlocks: 'Unlocks: Nuclear Submarine (unit); enables Satellites, Lasers; boosted by building 2 Broadcast Centers.'
  },
  'Satellites': {
    desc: 'Orbital technology — boosted space program progress enabling moon landings and mechanized infantry.',
    unlocks: 'Unlocks: Launch Moon Landing (project), Mechanized Infantry (unit); enables Robotics, Nanotechnology, Nuclear Fusion; boosted by Great Scientist or Spy.'
  },
  'Guidance Systems': {
    desc: 'Precision targeting technology — enabled mobile SAMs and rocket artillery.',
    unlocks: 'Unlocks: Mobile SAM (unit), Rocket Artillery (unit); enables Robotics, Lasers; boosted by killing a Fighter.'
  },
  'Lasers': {
    desc: 'Light amplification technology — unlocked jet fighters and missile cruisers.',
    unlocks: 'Unlocks: Jet Fighter (unit), Missile Cruiser (unit); enables Nuclear Fusion, Robotics; boosted by Great Scientist or Spy.'
  },
  'Composites': {
    desc: 'Advanced material science for modern armor — unlocked modern armor and anti-tank units.',
    unlocks: 'Unlocks: Modern Armor (unit), Modern AT (unit); enables Nanotechnology, Stealth Technology; boosted by owning 3 Tanks.'
  },
  'Stealth Technology': {
    desc: 'Cutting-edge radar evasion and stealth design — unlocked the Jet Bomber.',
    unlocks: 'Unlocks: Jet Bomber (unit); enables Nanotechnology; boosted by Great Scientist or Spy.'
  },

  // Future Era
  'Robotics': {
    desc: 'Automated manufacturing — boosted pasture production and enabled Mars habitation projects.',
    unlocks: 'Unlocks: Launch Mars Habitation (project); +1 Production to Pastures; enables Offworld Mission (GS); boosted by having the Globalization civic.'
  },
  'Nuclear Fusion': {
    desc: 'Controlled fusion energy — enabled thermonuclear devices, Mars reactors, and the Operation Ivy project.',
    unlocks: 'Unlocks: Thermonuclear Device (project), Launch Mars Reactor (project), Operation Ivy (project); enables Offworld Mission (GS); boosted by Great Scientist or Spy.'
  },
  'Nanotechnology': {
    desc: 'Molecular-level manufacturing — enabled aluminum mines and Mars hydroponics projects.',
    unlocks: 'Unlocks: Launch Mars Hydroponics (project); reveals Aluminum resource (if not revealed); enables Offworld Mission (GS); boosted by building an Aluminum Mine.'
  },
  'Future Tech': {
    desc: 'Theoretical science beyond current understanding — can be researched repeatedly for score victory benefits.',
    unlocks: 'Unlocks: +5% Production towards city projects (repeatable); Can be completed multiple times, increasing points towards Score Victory.'
  },

  // Gathering Storm / Rise and Fall techs
  'Calendar': {
    desc: 'Developed methods to track days, months, and years — essential for agriculture, religious observances, and administrative record-keeping.',
    unlocks: 'Unlocks: None directly; enables other medieval technologies. (Rise and Fall / Gathering Storm)'
  },
  'Refining': {
    desc: 'Processing raw materials into refined products — extracting niter from saltpeter and enabling advanced chemical manufacturing.',
    unlocks: 'Unlocks: Coal Power Plant (building); enables Combustion, Plastics; boosted by building 1 Coal Power Plant. (Gathering Storm)'
  },
  'Railroads': {
    desc: 'Railway transportation networks enabling rapid movement of troops, goods, and people across vast distances.',
    unlocks: 'Unlocks: Railroads (allows units to move freely on rail networks); enables Advanced Ballistics. (Rise and Fall)'
  },
  'Pharmaceuticals': {
    desc: 'Scientific study of medicinal compounds enabling the production of modern drugs and medical treatments for civilians and soldiers.',
    unlocks: 'Unlocks: None directly; enables other modern technologies. (Gathering Storm)'
  },
  'Atomic Theory': {
    desc: 'Understanding the fundamental structure of matter — unlocking nuclear energy and revealing uranium deposits.',
    unlocks: 'Unlocks: Reveals Uranium; enables Nuclear Fission. (Gathering Storm)'
  },
  'Refrigeration': {
    desc: 'Controlled cooling technology enabling food preservation, climate-controlled buildings, and industrial cold chains.',
    unlocks: 'Unlocks: Neighborhood (district) housing bonus; enables other modern technologies. (Gathering Storm)'
  },
  'Radar': {
    desc: 'Radio-based detection and ranging technology for identifying objects at a distance — revolutionizing navigation and military operations.',
    unlocks: 'Unlocks: None directly; enables Advanced Flight, Guidance Systems. (Gathering Storm)'
  },
  'Ecology': {
    desc: 'Scientific study of organisms and their environment — enabling pollution management and sustainable resource policies.',
    unlocks: 'Unlocks: Recycling Center (building); enables Offworld Mission, Seasteads. (Gathering Storm)'
  },
  'Advanced AI': {
    desc: 'Sophisticated artificial intelligence capable of learning, reasoning, and autonomous decision-making.',
    unlocks: 'Unlocks: None directly; Future Era technology. (Gathering Storm)'
  },
  'Advanced Power Cells': {
    desc: 'Next-generation energy storage beyond lithium-ion — enabling longer-lasting portable and vehicular power.',
    unlocks: 'Unlocks: None directly; Future Era technology. (Gathering Storm)'
  },
  'Smart Materials': {
    desc: 'Adaptive materials that respond to environmental stimuli — self-healing, shape-memory, and temperature-regulating substances.',
    unlocks: 'Unlocks: None directly; Future Era technology. (Gathering Storm)'
  },
  'Cybernetics': {
    desc: 'Integration of technology with biological systems — enhancing human capabilities through neural interfaces and prosthetics.',
    unlocks: 'Unlocks: None directly; Future Era technology. (Gathering Storm)'
  },
  'Offworld Mission': {
    desc: 'Crewed interplanetary missions establishing human presence beyond Earth — a critical step toward becoming a spacefaring civilization.',
    unlocks: 'Unlocks: Exoplanet Expedition (project); part of Science Victory. (Gathering Storm)'
  },
  'Seasteads': {
    desc: 'Permanent ocean-based settlements — floating cities that provide new territory and resources beyond national jurisdictions.',
    unlocks: 'Unlocks: Seastead (improvement); provides housing and production on coast tiles. (Gathering Storm)'
  },
  'Predictive Systems': {
    desc: 'Advanced modeling and simulation technology enabling accurate forecasting of weather, economic trends, and societal patterns.',
    unlocks: 'Unlocks: None directly; Future Era technology. (Gathering Storm)'
  },
};

// Update all techs with enhanced descriptions
let updated = 0;
for (const group of data) {
  for (const tech of group.techs) {
    const details = techDetails[tech.name];
    if (details) {
      tech.civilopediaText = [
        { text: details.desc },
        { text: details.unlocks }
      ];
      updated++;
    }
  }
}

fs.writeFileSync(path, JSON.stringify(data, null, 2) + '\n', { encoding: 'utf8' });
console.log('Updated', updated, 'techs with detailed descriptions including unlocks');
"