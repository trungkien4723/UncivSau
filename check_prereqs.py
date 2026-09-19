import json

with open('jsons/Civ VI/Techs.json') as f:
    techs = json.load(f)
with open('jsons/Civ VI/Units.json') as f:
    units = json.load(f)
with open('jsons/Civ VI/Buildings.json') as f:
    buildings = json.load(f)
with open('jsons/Civ VI/Districts.json') as f:
    districts = json.load(f)

# Build tech name -> prerequisites mapping from tech tree
tech_prereqs = {}
for column in techs:
    for tech in column.get('techs', []):
        name = tech.get('name', '')
        prereqs = tech.get('prerequisites', [])
        tech_prereqs[name] = prereqs

print('=== TECH PREREQUISITES (from Techs.json) ===')
for name, prereqs in sorted(tech_prereqs.items()):
    if prereqs:
        print(f'  {name} -> requires: {", ".join(prereqs)}')
    else:
        print(f'  {name} -> (no prereqs)')

# Check units for required techs
print('\n=== UNITS WITH MISSING REQUIRED TECHS ===')
# Expected unit -> tech mapping from wiki
unit_tech_map = {
    'Archer': 'Archery',
    'Crossbowman': 'Machinery',
    'Field Cannon': 'Ballistics',
    'Machine Gun': 'Advanced Ballistics',
    'Swordsman': 'Iron Working',
    'Man-At-Arms': 'Apprenticeship',
    'Musketman': 'Gunpowder',
    'Line Infantry': 'Rifling',
    'Infantry': 'Replaceable Parts',
    'Mechanized Infantry': 'Satellites',
    'Spearman': 'Bronze Working',
    'Pikeman': 'Military Tactics',
    'Pike and Shot': 'Gunpowder',
    'AT Crew': 'Chemistry',
    'Modern AT': 'Composites',
    'Heavy Chariot': 'Wheel',
    'Knight': 'Stirrups',
    'Cuirassier': 'Military Science',
    'Tank': 'Combustion',
    'Modern Armor': 'Composites',
    'Horseman': 'Horseback Riding',
    'Courser': 'Stirrups',
    'Cavalry': 'Military Science',
    'Helicopter': 'Synthetic Materials',
    'Catapult': 'Engineering',
    'Trebuchet': 'Military Engineering',
    'Bombard': 'Metal Casting',
    'Artillery': 'Steel',
    'Rocket Artillery': 'Guidance Systems',
    'Galley': 'Sailing',
    'Caravel': 'Cartography',
    'Ironclad': 'Steam Power',
    'Destroyer': 'Combined Arms',
    'Quadrireme': 'Shipbuilding',
    'Frigate': 'Square Rigging',
    'Battleship': 'Steel',
    'Missile Cruiser': 'Lasers',
    'Privateer': 'Mercantilism',
    'Submarine': 'Electricity',
    'Nuclear Submarine': 'Telecommunications',
    'Aircraft Carrier': 'Combined Arms',
    'Biplane': 'Flight',
    'Fighter': 'Advanced Flight',
    'Jet Fighter': 'Lasers',
    'Bomber': 'Advanced Flight',
    'Jet Bomber': 'Stealth Technology',
    'Skirmisher': 'Horseback Riding',
    'Ranger': 'Rifling',
    'Spec Ops': 'Advanced Ballistics',
    'Battering Ram': 'Masonry',
    'Siege Tower': 'Construction',
}

unit_lookup = {u.get('name', ''): u for u in units}
missing_tech = 0
wrong_tech = 0
for unit_name, expected_tech in unit_tech_map.items():
    if unit_name in unit_lookup:
        u = unit_lookup[unit_name]
        actual_tech = u.get('requiredTech', '')
        actual_civic = u.get('requiredCivic', '')
        if expected_tech and not actual_tech and not actual_civic:
            print(f'  MISSING: {unit_name} - should require {expected_tech}')
            missing_tech += 1
        elif actual_tech != expected_tech and actual_civic != expected_tech:
            if actual_tech or actual_civic:
                print(f'  WRONG: {unit_name} - expected {expected_tech}, got tech={actual_tech} civic={actual_civic}')
                wrong_tech += 1
    else:
        print(f'  NOT FOUND: {unit_name}')

print(f'\nTotal missing: {missing_tech}, Total wrong: {wrong_tech}')

# Check buildings with missing tech requirements
print('\n=== BUILDINGS WITH COST > 0 BUT NO REQUIRED TECH/CIVIC ===')
for b in buildings:
    name = b.get('name', '')
    cost = b.get('cost', 0)
    tech = b.get('requiredTech', '')
    civic = b.get('requiredCivic', '')
    uniques = b.get('uniques', [])
    has_unbuildable = any('Unbuildable' in uq for uq in uniques)
    is_district = b.get('district', '')
    
    if cost > 0 and not tech and not civic and not has_unbuildable and not is_district and not b.get('uniqueTo'):
        # Check if it should have a tech
        print(f'  {name}: cost={cost}')

# Check districts
print('\n=== DISTRICTS WITH MISSING REQUIRED TECH ===')
district_tech_map = {
    'Campus': 'Writing',
    'Holy Site': 'Astrology',
    'Encampment': 'Bronze Working',
    'Commercial Hub': 'Currency',
    'Industrial Zone': 'Apprenticeship',
    'Theater Square': 'Drama and Poetry',
    'Harbor': 'Celestial Navigation',
    'Aqueduct': 'Engineering',
    'Neighborhood': 'Urbanization',
    'Spaceport': 'Rocketry',
    'Aerodrome': 'Flight',
    'Entertainment Complex': 'Games and Recreation',
    'Diplomatic Quarter': 'Diplomatic Service',
    'Preserve': 'Conservation',
}

district_lookup = {d.get('name', ''): d for d in districts}
for d_name, expected_tech in district_tech_map.items():
    if d_name in district_lookup:
        d = district_lookup[d_name]
        actual_tech = d.get('requiredTech', '')
        actual_civic = d.get('requiredCivic', '')
        if actual_tech != expected_tech and actual_civic != expected_tech:
            print(f'  {d_name}: expected tech={expected_tech}, got tech={actual_tech} civic={actual_civic}')
    else:
        print(f'  MISSING DISTRICT: {d_name}')

# Check unique district replacements
print('\n=== UNIQUE DISTRICT REPLACEMENTS ===')
for d in districts:
    name = d.get('name', '')
    replaces = d.get('replaces', '')
    unique_to = d.get('uniqueTo', '')
    if replaces:
        print(f'  {name} replaces {replaces} (unique to {unique_to})')