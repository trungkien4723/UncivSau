import json

# Load all JSON files
with open('jsons/Civ VI/Techs.json') as f:
    techs = json.load(f)
with open('jsons/Civ VI/Civics.json') as f:
    civics = json.load(f)
with open('jsons/Civ VI/Units.json') as f:
    units = json.load(f)
with open('jsons/Civ VI/Buildings.json') as f:
    buildings = json.load(f)
with open('jsons/Civ VI/Districts.json') as f:
    districts = json.load(f)

# Extract tech names from nested structure
def extract_techs(data):
    names = []
    for column in data:
        for tech in column.get('techs', []):
            names.append(tech.get('name', ''))
    return names

def extract_civics(data):
    names = []
    for column in data:
        for civic in column.get('civics', []):
            names.append(civic.get('name', ''))
    return names

tech_names = extract_techs(techs)
civic_names = extract_civics(civics)
unit_names = [u.get('name', '') for u in units]
building_names = [b.get('name', '') for b in buildings]
district_names = [d.get('name', '') for d in districts]

print('=== TECHS IN PROJECT (from Techs.json) ===')
for name in sorted(tech_names):
    print(f'  {name}')

print(f'\nTotal techs: {len(tech_names)}')

# Expected techs from wiki
expected_techs = {
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

print('\n=== CHECKING MISSING TECHS ===')
all_expected = set()
for era, names in expected_techs.items():
    all_expected.update(names)

missing_techs = all_expected - set(tech_names)
extra_techs = set(tech_names) - all_expected

if missing_techs:
    print('MISSING TECHS:')
    for t in sorted(missing_techs):
        print(f'  {t}')
else:
    print('No missing techs!')

if extra_techs:
    print('EXTRA TECHS (not in wiki):')
    for t in sorted(extra_techs):
        print(f'  {t}')

print('\n=== CIVICS IN PROJECT ===')
for name in sorted(civic_names):
    print(f'  {name}')
print(f'\nTotal civics: {len(civic_names)}')

print('\n=== UNITS COUNT ===')
print(f'Total units: {len(unit_names)}')

print('\n=== BUILDINGS COUNT ===')
print(f'Total buildings: {len(building_names)}')

print('\n=== DISTRICTS COUNT ===')
for name in sorted(district_names):
    print(f'  {name}')
print(f'\nTotal districts: {len(district_names)}')

# Check for units that are category/non-buildable
category_units = ['Anti Cavalry', 'Heavy Cavalry', 'Light Cavalry', 'Recon', 'GDR']
print('\n=== CATEGORY UNITS (should have cost=-1 or Unbuildable) ===')
for name in category_units:
    u = next((u for u in units if u.get('name') == name), None)
    if u:
        cost = u.get('cost', 0)
        uniques = u.get('uniques', [])
        has_unbuildable = any('Unbuildable' in uq for uq in uniques)
        print(f'  {name}: cost={cost}, has_Unbuildable={has_unbuildable}')
    else:
        print(f'  {name}: MISSING')

# Check buildings that are walls
wall_buildings = [b for b in buildings if 'wall' in b.get('name', '').lower()]
print('\n=== WALL BUILDINGS ===')
for b in wall_buildings:
    name = b.get('name', '')
    tech = b.get('requiredTech', '')
    civic = b.get('requiredCivic', '')
    cost = b.get('cost', 0)
    replaces = b.get('replaces', '')
    print(f'  {name}: tech={tech} civic={civic} cost={cost} replaces={replaces}')