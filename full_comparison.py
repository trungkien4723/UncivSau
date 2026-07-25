import json

# Load all data
with open('jsons/Civ VI/Techs.json') as f:
    techs_data = json.load(f)
with open('jsons/Civ VI/Civics.json') as f:
    civics_data = json.load(f)
with open('jsons/Civ VI/Units.json') as f:
    units = json.load(f)
with open('jsons/Civ VI/Buildings.json') as f:
    buildings = json.load(f)
with open('jsons/Civ VI/Districts.json') as f:
    districts = json.load(f)

# Extract tech/civic names
tech_names = []
for col in techs_data:
    for t in col.get('techs', []):
        tech_names.append(t.get('name', ''))

civic_names = []
for col in civics_data:
    for c in col.get('civics', []):
        civic_names.append(c.get('name', ''))

unit_names = [u.get('name', '') for u in units]
building_names = [b.get('name', '') for b in buildings]
district_names = [d.get('name', '') for d in districts]

# ===== WIKI TECH TREE =====
wiki_techs_by_era = {
    'Ancient': ['Pottery', 'Animal Husbandry', 'Mining', 'Sailing', 'Astrology', 'Irrigation', 'Writing', 'Archery', 'Masonry', 'Bronze Working', 'Wheel'],
    'Classical': ['Celestial Navigation', 'Currency', 'Horseback Riding', 'Iron Working', 'Shipbuilding', 'Mathematics', 'Construction', 'Engineering'],
    'Medieval': ['Military Tactics', 'Buttress', 'Apprenticeship', 'Education', 'Cartography', 'Mass Production', 'Machinery', 'Castles'],
    'Renaissance': ['Printing', 'Astronomy', 'Gunpowder', 'Banking', 'Square Rigging', 'Ballistics'],
    'Industrial': ['Steam Power', 'Military Science', 'Rifling', 'Industrialization', 'Economics', 'Mercantilism', 'Chemistry', 'Ballistics'],
    'Modern': ['Combustion', 'Electricity', 'Replaceable Parts', 'Radio', 'Chemistry', 'Rifling'],
    'Atomic': ['Advanced Ballistics', 'Synthetic Materials', 'Nuclear Fission', 'Computers', 'Composites', 'Flight'],
    'Information': ['Lasers', 'Stealth Technology', 'Satellites', 'Nuclear Fusion', 'Advanced Flight', 'Nanotechnology'],
    'Future': ['Future Tech'],
}

# Expected civics (base game + DLC)
wiki_civics = {
    'Ancient': ['Code of Laws', 'Craftsmanship', 'Foreign Trade', 'Political Philosophy', 'Mysticism', 'Early Empire', 'Drama and Poetry', 'Games and Recreation'],
    'Classical': ['Recorded History', 'Military Tradition', 'Feudalism', 'Defensive Tactics', 'Divine Right', 'Reformed Church', 'Theology', 'Naval Tradition'],
    'Medieval': ['Civil Service', 'Humanism', 'Medieval Faires', 'Guilds', 'Mercenaries', 'Civil Engineering', 'Exploration', 'Natural History'],
    'Renaissance': ['Diplomatic Service', 'Mercantilism', 'Mass Media', 'Opera and Ballet', 'Conservation', 'Capitalism', 'Ideology', 'Class Struggle'],
    'Industrial': ['Nationalism', 'Suffrage', 'Totalitarianism', 'Scorched Earth', 'Mobilization', 'Venture Politics', 'Cultural Hegemony', 'Globalization'],
    'Modern': ['Urbanization', 'Professional Sports', 'Social Media', 'Information Warfare', 'Optimization Imperative', 'Exodus Imperative', 'Distributed Sovereignty', 'Smart Power Doctrine'],
    'Atomic': ['Cold War', 'Nuclear Program', 'Rapid Deployment', 'Space Race', 'Global Warming Mitigation', 'Globalization', 'Future Civic'],
}

print('=' * 60)
print('TECH COMPARISON')
print('=' * 60)

all_wiki_techs = set()
for era, names in wiki_techs_by_era.items():
    all_wiki_techs.update(names)

missing_techs = all_wiki_techs - set(tech_names)
extra_techs = set(tech_names) - all_wiki_techs

print(f'\nMissing techs ({len(missing_techs)}):')
for t in sorted(missing_techs):
    print(f'  {t}')

print(f'\nExtra techs in project ({len(extra_techs)}):')
for t in sorted(extra_techs):
    print(f'  {t}')

# Check tech requirements
print('\n' + '=' * 60)
print('TECH PREREQUISITE CHECK')
print('=' * 60)

# Build tech prerequisite map from project
tech_prereqs = {}
for col in techs_data:
    for t in col.get('techs', []):
        name = t.get('name', '')
        prereqs = t.get('prerequisites', [])
        tech_prereqs[name] = prereqs

# Expected prerequisites from wiki
expected_prereqs = {
    'Celestial Navigation': ['Sailing', 'Astrology'],
    'Currency': ['Writing'],
    'Horseback Riding': ['Animal Husbandry', 'Wheel'],
    'Iron Working': ['Bronze Working', 'Mining'],
    'Shipbuilding': ['Celestial Navigation', 'Sailing'],
    'Mathematics': ['Currency', 'Writing'],
    'Construction': ['Masonry', 'Wheel'],
    'Engineering': ['Construction', 'Mathematics'],
    'Military Tactics': ['Bronze Working', 'Construction'],
    'Buttress': ['Engineering', 'Mathematics'],
    'Apprenticeship': ['Iron Working', 'Construction'],
    'Education': ['Writing', 'Currency'],
    'Cartography': ['Celestial Navigation', 'Shipbuilding'],
    'Mass Production': ['Apprenticeship', 'Industrialization'],
    'Machinery': ['Engineering', 'Apprenticeship'],
    'Castles': ['Military Engineering', 'Masonry'],
    'Printing': ['Education', 'Writing'],
    'Astronomy': ['Education', 'Celestial Navigation'],
    'Gunpowder': ['Military Engineering', 'Military Tactics'],
    'Banking': ['Education', 'Currency'],
    'Square Rigging': ['Cartography', 'Shipbuilding'],
    'Ballistics': ['Military Engineering', 'Apprenticeship'],
    'Steam Power': ['Ballistics', 'Square Rigging'],
    'Military Science': ['Gunpowder', 'Military Tactics', 'Ballistics'],
    'Rifling': ['Gunpowder', 'Military Science'],
    'Industrialization': ['Steam Power', 'Economics'],
    'Economics': ['Banking', 'Mercantilism'],
    'Mercantilism': ['Banking', 'Square Rigging'],
    'Chemistry': ['Industrialization', 'Ballistics'],
    'Combustion': ['Steam Power', 'Industrialization', 'Military Science'],
    'Electricity': ['Steam Power', 'Radio'],
    'Replaceable Parts': ['Industrialization', 'Rifling'],
    'Radio': ['Electricity', 'Flight'],
    'Advanced Ballistics': ['Combustion', 'Replaceable Parts', 'Chemistry'],
    'Synthetic Materials': ['Replaceable Parts', 'Chemistry'],
    'Nuclear Fission': ['Electricity', 'Radio'],
    'Computers': ['Radio', 'Electronics'],
    'Composites': ['Replaceable Parts', 'Chemistry'],
    'Flight': ['Combustion', 'Radio'],
    'Lasers': ['Advanced Ballistics', 'Computers'],
    'Stealth Technology': ['Advanced Ballistics', 'Composites'],
    'Satellites': ['Computers', 'Rocketry'],
    'Nuclear Fusion': ['Nuclear Fission', 'Particle Physics'],
    'Advanced Flight': ['Flight', 'Composites'],
    'Nanotechnology': ['Synthetic Materials', 'Computers'],
    'Future Tech': ['Lasers', 'Stealth Technology', 'Satellites', 'Nuclear Fusion', 'Advanced Flight'],
}

print('Tech prerequisite mismatches:')
mismatches = 0
for tech, expected in expected_prereqs.items():
    if tech in tech_prereqs:
        actual = tech_prereqs[tech]
        if set(actual) != set(expected):
            print(f'  {tech}: expected {expected}, got {actual}')
            mismatches += 1
    else:
        print(f'  {tech}: MISSING from project (expected {expected})')
        mismatches += 1
if mismatches == 0:
    print('  No mismatches found!')

# ===== UNIT CHECK =====
print('\n' + '=' * 60)
print('UNIT CHECK')
print('=' * 60)

# Standard units from wiki
wiki_standard_units = {
    'Ancient': ['Warrior', 'Slinger', 'Scout', 'Spearman', 'Archer', 'Heavy Chariot', 'Horseman', 'Catapult', 'Galley', 'Quadrireme', 'Battering Ram'],
    'Classical': ['Swordsman', 'Crossbowman', 'Pikeman', 'Knight', 'Courser', 'Trebuchet', 'Caravel', 'Siege Tower'],
    'Medieval': ['Man-At-Arms', 'Musketman', 'Pike and Shot', 'Cuirassier', 'Cavalry', 'Bombard', 'Ironclad', 'Frigate', 'Privateer'],
    'Renaissance': ['Field Cannon', 'Line Infantry', 'AT Crew', 'Artillery', 'Destroyer', 'Battleship', 'Submarine'],
    'Modern': ['Machine Gun', 'Infantry', 'Modern AT', 'Modern Armor', 'Rocket Artillery', 'Missile Cruiser', 'Nuclear Submarine', 'Aircraft Carrier', 'Biplane', 'Fighter', 'Bomber', 'Helicopter'],
    'Atomic': ['Jet Fighter', 'Jet Bomber', 'Mechanized Infantry', 'Stealth Bomber'],
    'Information': ['Giant Death Robot'],
}

all_wiki_units = set()
for era, names in wiki_standard_units.items():
    all_wiki_units.update(names)

missing_units = all_wiki_units - set(unit_names)
extra_units = set(unit_names) - all_wiki_units

print(f'\nMissing standard units ({len(missing_units)}):')
for u in sorted(missing_units):
    print(f'  {u}')

print(f'\nExtra units in project ({len(extra_units)}):')
# Filter to only show non-unique, non-category units
for u in sorted(extra_units):
    u_data = next((u for u in units if u.get('name') == u), None)
    if u_data:
        uniques = u_data.get('uniques', [])
        unique_to = u_data.get('uniqueTo', '')
        cost = u_data.get('cost', 0)
        is_category = u_data.get('unitType') in ['Anti Cavalry', 'Heavy Cavalry', 'Light Cavalry', 'Recon', 'GDR']
        if not is_category and cost > 0 and not unique_to and not any('Unbuildable' in uq for uq in uniques):
            print(f'  {u} (cost={cost}, uniqueTo={unique_to})')

# ===== BUILDING CHECK =====
print('\n' + '=' * 60)
print('BUILDING CHECK - WALLS')
print('=' * 60)

wall_buildings = [b for b in buildings if 'wall' in b.get('name', '').lower()]
print('Wall-related buildings:')
for b in wall_buildings:
    name = b.get('name', '')
    tech = b.get('requiredTech', '')
    civic = b.get('requiredCivic', '')
    cost = b.get('cost', 0)
    replaces = b.get('replaces', '')
    print(f'  {name}: tech={tech} civic={civic} cost={cost} replaces={replaces}')

# ===== DISTRICT CHECK =====
print('\n' + '=' * 60)
print('DISTRICT CHECK')
print('=' * 60)

# Expected districts and their requirements from wiki
wiki_districts = {
    'Campus': {'tech': 'Writing'},
    'Theater Square': {'civic': 'Drama and Poetry'},
    'Holy Site': {'tech': 'Astrology'},
    'Commercial Hub': {'tech': 'Currency'},
    'Industrial Zone': {'tech': 'Apprenticeship'},
    'Neighborhood': {'tech': 'Sanitation'},
    'Encampment': {'tech': 'Bronze Working'},
    'Harbor': {'tech': 'Sailing'},
    'Aqueduct': {'tech': 'Engineering'},
    'Entertainment Complex': {'civic': 'Games and Recreation'},
    'Spaceport': {'tech': 'Rocketry'},
    'Government Plaza': {'civic': 'State Workforce'},
    'Aerodrome': {'tech': 'Flight'},
    'Preserve': {'civic': 'Conservation'},
    'Diplomatic Quarter': {'civic': 'Diplomatic Service'},
    'Aqueduct': {'tech': 'Engineering'},
}

print('District requirement check:')
for d in districts:
    name = d.get('name', '')
    tech = d.get('requiredTech', '')
    civic = d.get('requiredCivic', '')
    if name in wiki_districts:
        expected = wiki_districts[name]
        expected_tech = expected.get('tech', '')
        expected_civic = expected.get('civic', '')
        if tech != expected_tech or civic != expected_civic:
            print(f'  {name}: expected tech={expected_tech} civic={expected_civic}, got tech={tech} civic={civic}')
        else:
            print(f'  {name}: OK (tech={tech} civic={civic})')
    else:
        print(f'  {name}: tech={tech} civic={civic} (not in wiki standard list)')

print('\nDone!')