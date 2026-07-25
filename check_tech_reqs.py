import json
with open('jsons/Civ VI/Units.json') as f:
    units = json.load(f)

unit_lookup = {u.get('name', ''): u for u in units}

wiki_tech_requirements = {
    'Warrior': ('', ''),
    'Slinger': ('', ''),
    'Scout': ('', ''),
    'Spearman': ('Bronze Working', ''),
    'Archer': ('Archery', ''),
    'Heavy Chariot': ('Wheel', ''),
    'Horseman': ('Horseback Riding', ''),
    'Catapult': ('Engineering', ''),
    'Galley': ('Sailing', ''),
    'Trireme': ('Sailing', ''),
    'Quadrireme': ('Shipbuilding', ''),
    'Battering Ram': ('Masonry', ''),
    'Siege Tower': ('Construction', ''),
    'Swordsman': ('Iron Working', ''),
    'Crossbowman': ('Machinery', ''),
    'Pikeman': ('Military Tactics', ''),
    'Knight': ('Stirrups', ''),
    'Man-At-Arms': ('Apprenticeship', ''),
    'Musketman': ('Gunpowder', ''),
    'Line Infantry': ('Rifling', ''),
    'Infantry': ('Replaceable Parts', ''),
    'Mechanized Infantry': ('Satellites', ''),
    'Trebuchet': ('Military Engineering', ''),
    'Bombard': ('Metal Casting', ''),
    'Artillery': ('Steel', ''),
    'Rocket Artillery': ('Guidance Systems', ''),
    'Cuirassier': ('Military Science', ''),
    'Tank': ('Combustion', ''),
    'Modern Armor': ('Composites', ''),
    'Courser': ('Stirrups', ''),
    'Cavalry': ('Military Science', ''),
    'Helicopter': ('Synthetic Materials', ''),
    'Pike and Shot': ('Gunpowder', ''),
    'AT Crew': ('Chemistry', ''),
    'Modern AT': ('Composites', ''),
    'Caravel': ('Cartography', ''),
    'Frigate': ('Square Rigging', ''),
    'Battleship': ('Steel', ''),
    'Missile Cruiser': ('Lasers', ''),
    'Privateer': ('', 'Mercantilism'),
    'Submarine': ('Electricity', ''),
    'Nuclear Submarine': ('Telecommunications', ''),
    'Aircraft Carrier': ('Combined Arms', ''),
    'Biplane': ('Flight', ''),
    'Fighter': ('Advanced Flight', ''),
    'Jet Fighter': ('Lasers', ''),
    'Bomber': ('Advanced Flight', ''),
    'Jet Bomber': ('Stealth Technology', ''),
    'Skirmisher': ('Horseback Riding', ''),
    'Ranger': ('Rifling', ''),
    'Spec Ops': ('Advanced Ballistics', ''),
    'Field Cannon': ('Ballistics', ''),
    'Machine Gun': ('Advanced Ballistics', ''),
    'Destroyer': ('Combined Arms', ''),
}

print("=== TECH/CIVIC REQUIREMENT CHECK ===")
mismatches = 0
for name, (expected_tech, expected_civic) in wiki_tech_requirements.items():
    if name in unit_lookup:
        u = unit_lookup[name]
        actual_tech = u.get('requiredTech', '')
        actual_civic = u.get('requiredCivic', '')
        if actual_tech != expected_tech or actual_civic != expected_civic:
            print(f"MISMATCH {name}: expected tech={expected_tech} civic={expected_civic}, got tech={actual_tech} civic={actual_civic}")
            mismatches += 1
    else:
        print(f"MISSING {name}")
        mismatches += 1

if mismatches == 0:
    print("All checked units have correct requirements!")
else:
    print(f"Total mismatches: {mismatches}")

# Check non-buildable category units
print("\n=== NON-BUILDABLE CATEGORY UNITS ===")
category_units = ['Anti Cavalry', 'Heavy Cavalry', 'Light Cavalry', 'Recon', 'GDR', 'Builder', 'Settler', 'Trader']
for name in category_units:
    if name in unit_lookup:
        u = unit_lookup[name]
        cost = u.get('cost', 0)
        uniques = u.get('uniques', [])
        has_unbuildable = any('Unbuildable' in uq for uq in uniques)
        print(f"{name}: cost={cost}, has_Unbuildable={has_unbuildable}, uniques={uniques}")

# Check for any unit with cost > 0 but no requirement
print("\n=== UNITS WITH COST > 0 BUT NO TECH/CIVIC REQUIREMENT ===")
for u in units:
    name = u.get('name', '')
    cost = u.get('cost', 0)
    tech = u.get('requiredTech', '')
    civic = u.get('requiredCivic', '')
    uniques = u.get('uniques', [])
    is_unbuildable = any('Unbuildable' in uq for uq in uniques)
    if cost > 0 and not tech and not civic and not is_unbuildable and 'uniqueTo' not in u:
        print(f"{name}: cost={cost}, tech={tech}, civic={civic}, uniqueTo={u.get('uniqueTo', '')}")