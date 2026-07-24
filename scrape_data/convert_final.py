#!/usr/bin/env python3
"""
Load JSON files with JavaScript-style comments
"""

import re
import json
from pathlib import Path

def load_jsonc(path):
    """Load JSON with comments (// and /* */)"""
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Remove /* */ comments
    content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
    # Remove // comments
    content = re.sub(r'//.*', '', content)
    # Remove trailing commas before } or ]
    content = re.sub(r',\s*[}\]]', lambda m: m.group(0).replace(',', ''), content)
    
    return json.loads(content)

def save_json(path, data):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

UNCIV_DIR = Path("D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI")

# Test
print("Loading Nations.json...")
nations = load_jsonc(UNCIV_DIR / "Nations.json")
print(f"  Loaded {len(nations)} nations")

print("Loading Buildings.json...")
buildings = load_jsonc(UNCIV_DIR / "Buildings.json")
print(f"  Loaded {len(buildings)} buildings")

print("Loading Units.json...")
units = load_jsonc(UNCIV_DIR / "Units.json")
print(f"  Loaded {len(units)} units")

print("Loading Districts.json...")
districts = load_jsonc(UNCIV_DIR / "Districts.json")
print(f"  Loaded {len(districts)} districts")

print("Loading TileImprovements.json...")
improvements = load_jsonc(UNCIV_DIR / "TileImprovements.json")
print(f"  Loaded {len(improvements)} improvements")

# Now the full converter
SCRAPE_DIR = Path("D:/lamviec/test/uncivSAU/scrape_data")

print("\nLoading scraped data...")
scraped = load_jsonc(SCRAPE_DIR / "parsed_civ6_data.json")
scraped_leaders = scraped['leaders']
scraped_buildings = scraped['buildings']
scraped_units = scraped['units']
scraped_wonders = scraped['wonders']

print(f"  Leaders: {len(scraped_leaders)}")
print(f"  Buildings: {len(scraped_buildings)}")
print(f"  Units: {len(scraped_units)}")
print(f"  Wonders: {len(scraped_wonders)}")

# Group leaders by civ
civ_leaders = {}
for l in scraped_leaders:
    civ = l['civilization']
    if civ not in civ_leaders:
        civ_leaders[civ] = []
    civ_leaders[civ].append(l)

print(f"\nUnique civs: {len(civ_leaders)}")
for civ, leaders in sorted(civ_leaders.items()):
        pass  # Suppress print due to encoding issues

# Build meta map from existing nations
meta_map = {}
for n in nations:
    if n['name'] == 'Spectator':
        continue
    meta_map[n['name']] = n

# ============================================================
# UNIQUE TEXT CONVERSION
# ============================================================

def to_unciv_unique(text):
    """Convert scraped unique text to Unciv syntax"""
    if not text:
        return None
    
    text = text.strip()
    if not text:
        return None
    
    # Clean up encoding artifacts
    text = text.replace('\u00e2\u20ac\u0153', '-')
    text = text.replace('\u00c2\u00a0', ' ')
    text = text.replace('\u00c2', '')
    text = text.replace('\u2014', '-')
    text = text.replace('\u2013', '-')
    text = text.replace('\u2018', "'")
    text = text.replace('\u2019', "'")
    text = text.replace('\u201c', '"')
    text = text.replace('\u201d', '"')
    
    # Convert Unciv-style markers
    # [+X]% -> [+X]%
    text = re.sub(r'\[([+-]\d+)%\]', r'[\1]%', text)
    # +X% -> [+X]%
    text = re.sub(r'([+-]\d+)%', r'[\1]%', text)
    # +X Stat -> [+X] Stat
    text = re.sub(r'([+-]\d+)\s+(Food|Production|Gold|Science|Culture|Faith|Housing|Amenities|Tourism|Combat Strength|Defense|Movement|Range|Sight|Loyalty|Great Person)', 
                  r'[\1] \2', text)
    # <Tech> -> <Tech>
    text = re.sub(r'Unlocked by\s+(.+?)\s+(Technology|Civic)', r'<\1>', text)
    text = re.sub(r'<(.+?)>.*?(Technology|Civic)', r'<\1>', text)
    
    # Clean up remaining artifacts
    text = re.sub(r'\[(\w+)\]', r'[\1]', text)  # Ensure single brackets
    
    return text

# ============================================================
# CONVERT NATIONS
# ============================================================

print("\nConverting nations...")
new_nations = [n for n in nations if n['name'] == 'Spectator']

for civ_name, leaders in sorted(civ_leaders.items()):
    primary = leaders[0]
    leader_name = primary['leader']
    meta = meta_map.get(civ_name, {})
    
    # Collect all uniques
    all_uniques = []
    for l in leaders:
        if 'ability' in l:
            all_uniques.append(l['ability'])
        all_uniques.extend(l.get('uniques', []))
    
    # Convert
    unique_texts = []
    for u in all_uniques:
        parsed = to_unciv_unique(u)
        if parsed:
            unique_texts.append(parsed)
    
    # Deduplicate
    seen = set()
    unique_texts = [x for x in unique_texts if not (x in seen or seen.add(x))]
    
    # Fallback to existing
    if not unique_texts and 'uniques' in meta:
        unique_texts = meta.get('uniques', [])
    
    nation = {
        "name": civ_name,
        "leaderName": leader_name,
        "adjective": meta.get('adjective', [civ_name]),
        "startBias": meta.get('startBias', []),
        "preferredVictoryType": meta.get('preferredVictoryType', 'Scientific'),
        "personality": meta.get('personality', leader_name),
        "agenda": meta.get('agenda', 'Agenda'),
        "hiddenAgendas": meta.get('hiddenAgendas', ["Warmonger Hater", "Nature Lover", "Piety"]),
        "outerColor": meta.get('outerColor', [128,128,128]),
        "innerColor": meta.get('innerColor', [255,255,255]),
        "favoredReligion": meta.get('favoredReligion', 'Christianity'),
        "uniqueName": meta.get('uniqueName', 'Unique Ability'),
        "uniques": unique_texts,
        "cities": meta.get('cities', []),
        "spyNames": meta.get('spyNames', []),
    }
    
    if meta.get('uniqueBuildings'):
        nation['uniqueBuildings'] = meta['uniqueBuildings']
    if meta.get('uniqueDistricts'):
        nation['uniqueDistricts'] = meta['uniqueDistricts']
    
    new_nations.append(nation)
    # print(f"  {civ_name} ({leader_name}) - {len(unique_texts)} uniques")

# ============================================================
# CONVERT BUILDINGS
# ============================================================

print("\nConverting buildings...")

DISTRICT_MAP = {
    "City Center": "City Center",
    "Encampment": "Encampment",
    "Holy Site": "Holy Site",
    "Campus": "Campus",
    "Entertainment Complex": "Entertainment Complex",
    "Harbor": "Harbor",
    "Commercial Hub": "Commercial Hub",
    "Theater Square": "Theater Square",
    "Government Plaza": "Government Plaza",
    "Preserve": "Preserve",
    "Diplomatic Quarter": "Diplomatic Quarter",
    "Industrial Zone": "Industrial Zone",
    "Water Park": "Water Park",
    "Aerodrome": "Aerodrome",
    "Neighborhood": "Neighborhood",
    "Dam": "Dam",
    "Aqueduct": "Aqueduct",
    "Canal": "Canal",
    "Spaceport": "Spaceport"
}

def parse_yields(yield_list):
    stats = {}
    for y in yield_list:
        matches = re.findall(r'([+-]\d+)\s+(\w+)', y)
        for val, stat in matches:
            stat_map = {
                "Food": "food", "Production": "production", "Gold": "gold",
                "Science": "science", "Culture": "culture", "Faith": "faith",
                "Housing": "housing", "Amenities": "amenities", "Citizen": "citizenSlot",
                "Great": "greatPersonPoints", "Tourism": "tourism", "Power": "power",
            }
            if stat in stat_map:
                stats[stat_map[stat]] = int(val)
    return stats

new_buildings = []
scraped_building_names = {b['name'] for b in scraped_buildings}

# Keep existing non-scraped
for b in buildings:
    if b['name'] not in scraped_building_names:
        new_buildings.append(b)

# Add/update scraped
for b in scraped_buildings:
    district = DISTRICT_MAP.get(b.get('district', ''), b.get('district', ''))
    
    building = {
        "name": b['name'],
        "district": district,
        "cost": b.get('cost', 0) or 0,
        "maintenance": b.get('maintenance', 0) or 0,
        "stats": parse_yields(b.get('yields', [])),
        "uniques": []
    }
    
    if b.get('unique_to'):
        building['uniques'].append(b['unique_to'])
    
    existing = next((x for x in buildings if x['name'] == b['name']), None)
    if existing:
        if 'requiredTech' in existing:
            building['requiredTech'] = existing['requiredTech']
        if 'requiredCivic' in existing:
            building['requiredCivic'] = existing['requiredCivic']
        if 'buildings' in existing:
            building['buildings'] = existing['buildings']
    
    new_buildings.append(building)

print(f"Total buildings: {len(new_buildings)}")

# ============================================================
# CONVERT UNITS
# ============================================================

print("\nConverting units...")

UNIT_TYPE_MAP = {
    "Melee": "Sword", "Anti-Cavalry": "Spear", "Ranged": "Archery",
    "Cavalry": "Horse", "Siege": "Siege", "Naval Melee": "NavalMelee",
    "Naval Ranged": "NavalRanged", "Air": "Air", "Support": "Support",
    "Civilian": "Civilian", "Religious": "Religious"
}

new_units = []
scraped_unit_names = {u['name'] for u in scraped_units}

for u in units:
    if u['name'] not in scraped_unit_names:
        new_units.append(u)

for u in scraped_units:
    unit = {
        "name": u['name'],
        "unitType": UNIT_TYPE_MAP.get(u.get('unitClass', ''), u.get('unitClass', 'Sword')),
        "movement": u.get('movement', 2),
        "strength": u.get('strength', 0),
        "rangedStrength": u.get('rangedStrength', 0),
        "cost": u.get('cost', 0),
        "requiredTech": u.get('requires', ''),
        "upgradesTo": u.get('upgradesTo', ''),
        "uniqueTo": u.get('uniqueTo', ''),
        "uniques": u.get('abilities', [])
    }
    new_units.append(unit)

print(f"Total units: {len(new_units)}")

# ============================================================
# SAVE
# ============================================================

print("\nSaving...")
save_json(UNCIV_DIR / "Nations.json", new_nations)
save_json(UNCIV_DIR / "Buildings.json", new_buildings)
save_json(UNCIV_DIR / "Units.json", new_units)

print("Done!")
print(f"  Nations: {len(new_nations)}")
print(f"  Buildings: {len(new_buildings)}")
print(f"  Units: {len(new_units)}")