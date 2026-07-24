#!/usr/bin/env python3
"""
Complete converter: Merge scraped Civ 6 data with Unciv format
"""

import json
import re
from pathlib import Path

SCRAPE_DIR = Path(r"D:\lamviec\test\uncivSAU\scrape_data")
UNCIV_DIR = Path(r"D:\lamviec\test\uncivSAU\android\assets\jsons\Civ VI")

def load_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_json(path, data):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent='\t', ensure_ascii=False)

# ============================================================
# LOAD ALL DATA
# ============================================================

print("Loading data...")

# Existing Unciv data (fixed trailing commas)
nations_existing = load_json(UNCIV_DIR / "Nations.json")
buildings_existing = load_json(UNCIV_DIR / "Buildings.json")
units_existing = load_json(UNCIV_DIR / "Units.json")
districts_existing = load_json(UNCIV_DIR / "Districts.json")
improvements_existing = load_json(UNCIV_DIR / "TileImprovements.json")

# Scraped data
scraped = load_json(SCRAPE_DIR / "parsed_civ6_data.json")
scraped_leaders = scraped['leaders']
scraped_buildings = scraped['buildings']
scraped_units = scraped['units']
scraped_wonders = scraped['wonders']

# Metadata from existing nations
meta_map = {}
for n in nations_existing:
    if n.get('name') and n['name'] != 'Spectator':
        meta_map[n['name']] = {
            'adjective': n.get('adjective', []),
            'startBias': n.get('startBias', []),
            'preferredVictoryType': n.get('preferredVictoryType', ''),
            'personality': n.get('personality', ''),
            'agenda': n.get('agenda', ''),
            'hiddenAgendas': n.get('hiddenAgendas', []),
            'outerColor': n.get('outerColor', [128,128,128]),
            'innerColor': n.get('innerColor', [255,255,255]),
            'favoredReligion': n.get('favoredReligion', ''),
            'uniqueName': n.get('uniqueName', ''),
            'cities': n.get('cities', []),
            'spyNames': n.get('spyNames', []),
            'uniqueBuildings': n.get('uniqueBuildings', []),
            'uniqueDistricts': n.get('uniqueDistricts', []),
        }

# ============================================================
# GROUP SCRAPED LEADERS BY CIV
# ============================================================

civ_leaders = {}
for l in scraped_leaders:
    civ = l['civilization']
    if civ not in civ_leaders:
        civ_leaders[civ] = []
    civ_leaders[civ].append(l)

print(f"Existing civs with metadata: {len(meta_map)}")
print(f"Scraped civs with leaders: {len(civ_leaders)}")

# ============================================================
# CONVERT UNIQUE TEXT TO UNCIV SYNTAX
# ============================================================

def to_unciv_unique(text):
    """Convert scraped unique ability text to Unciv syntax"""
    if not text:
        return None
    
    text = text.strip()
    
    # Skip very generic/unparseable text
    if len(text) < 5:
        return None
    
    # Clean up artifacts
    text = text.replace('\u00e2\u20ac\u0153', '-')  # â€“
    text = text.replace('\u00c2\u00a0', ' ')  # non-breaking space
    text = text.replace('\u00c2', '')
    
    # Pattern replacements to Unciv syntax
    # Format: [+X] [StatName] or UniqueType <params>
    
    # Percentages
    text = re.sub(r'\+(\d+)%', r'[\+\1]%', text)
    text = re.sub(r'-(\d+)%', r'[-\1]%', text)
    text = re.sub(r'(\d+)%', r'[\1]%', text)
    
    # Yields with +
    text = re.sub(r'\+(\d+)\s*(Food|Production|Gold|Science|Culture|Faith|Housing|Amenities|Tourism|Combat Strength|Defense|Movement|Range|Sight|Loyalty|Great (?:Person )?points?)', 
                  r'[\+\1] [\2]', text)
    
    # "X is earned Y% faster" -> "[Y]% [X] earned"
    text = re.sub(r'(\w+(?:\s+\w+)*) is earned (\d+)% faster', r'[\+\2]% [\1] earned', text)
    text = re.sub(r'(\w+(?:\s+\w+)*) earned (\d+)% faster', r'[\+\2]% [\1] earned', text)
    
    # "Free [X] appears" -> "Free [X] appears <upon ...>"
    text = re.sub(r'Free \[([^\]]+)\] appears', r'Free [\1] appears', text)
    
    # "Unlocks the Builder ability to construct X" -> CreatesOneImprovement
    text = re.sub(r'Unlocks the Builder ability to construct (?:an? )?([^,]+), unique to (\w+)', 
                  r'CreatesOneImprovement <[\1]> <on tile> <when built by [Builder]>', text)
    
    # Unique unit/building/improvement
    text = re.sub(r'(\w+) unique (\w+) era (?:unit|building|improvement|district) that replaces (?:the )?(\w+)', 
                  r'Unique[\2] [\1] [\3]', text)
    
    # "Stronger than X" -> just note it
    text = re.sub(r'Stronger than the (\w+)', r'Stronger than [\1]', text)
    
    # "Has a chance to capture" -> capture ability
    text = re.sub(r'Has a chance to capture other civilization\'?s? (\w+) by turning them into (\w+)', 
                  r'Chance to capture [\1] as [\2]', text)
    
    # "Earned from kills" 
    text = re.sub(r'Culture is earned from kills on their (\w+)', r'Culture from kills on [\1]', text)
    
    # "Additional X as you advance" -> scaling
    text = re.sub(r'Additional (\w+) and (\w+) as you advance through the (\w+) and (\w+) Tree', 
                  r'Scales with [\1] [\2]', text)
    
    # "Provides X per turn" -> yield
    text = re.sub(r'Provides (\d+) (\w+) per turn', r'[\+\1] [\2] per turn', text)
    
    # Replace "Unlocked by" with Unciv requirement syntax
    text = re.sub(r'Unlocked by (\w+) (\w+)', r'<requires [\1] [\2]>', text)
    text = re.sub(r'Unlocked by (\w+)', r'<requires [\1]>', text)
    
    # Clean up multiple spaces
    text = re.sub(r'\s+', ' ', text).strip()
    
    # Remove trailing periods
    text = text.rstrip('.')
    
    # Skip if still too generic
    if text.lower() in ['unique ability', 'unique unit', 'unique building', 'unique improvement', 'unique district']:
        return None
    
    return text

# ============================================================
# MERGE NATIONS
# ============================================================

print("\nMerging nations...")

new_nations = []

# Keep Spectator
for n in nations_existing:
    if n.get('name') == 'Spectator':
        new_nations.append(n)
        break

for civ_name, leaders in sorted(civ_leaders.items()):
    # Get primary leader (first one)
    primary = leaders[0]
    leader_name = primary['leader']
    
    # Get metadata
    meta = meta_map.get(civ_name, {})
    
    # Collect all uniques from all leaders
    all_uniques = []
    for l in leaders:
        if 'ability' in l:
            all_uniques.append(l['ability'])
        all_uniques.extend(l.get('uniques', []))
    
    # Convert to Unciv syntax
    unique_texts = []
    for u in all_uniques:
        parsed = to_unciv_unique(u)
        if parsed:
            unique_texts.append(parsed)
    
    # Deduplicate
    seen = set()
    unique_texts = [x for x in unique_texts if not (x in seen or seen.add(x))]
    
    # If no uniques parsed, use existing
    if not unique_texts and 'uniques' in meta:
        unique_texts = meta.get('uniques', [])
    
    # Build nation
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
    print(f"  {civ_name} ({leader_name}) - {len(unique_texts)} uniques")

print(f"\nTotal nations: {len(new_nations)}")

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
        # Match "+1 Food", "+2 Production", etc.
        matches = re.findall(r'([+-]\d+)\s+(\w+)', y)
        for val, stat in matches:
            stat_map = {
                "Food": "food",
                "Production": "production", 
                "Gold": "gold",
                "Science": "science",
                "Culture": "culture",
                "Faith": "faith",
                "Housing": "housing",
                "Amenities": "amenities",
                "Citizen": "citizenSlot",
                "Great": "greatPersonPoints",
                "Tourism": "tourism",
                "Power": "power",
            }
            if stat in stat_map:
                stats[stat_map[stat]] = int(val)
            elif stat == "slot":
                stats["greatWorkSlots"] = int(val)
    return stats

new_buildings = []

# Keep existing non-scraped buildings
scraped_building_names = {b['name'] for b in scraped_buildings}
for b in buildings_existing:
    if b['name'] not in scraped_building_names:
        new_buildings.append(b)

# Add/update scraped buildings
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
    
    # Try to find required tech/civic from existing
    existing = next((x for x in buildings_existing if x['name'] == b['name']), None)
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
    "Melee": "Sword",
    "Anti-Cavalry": "Spear", 
    "Ranged": "Archery",
    "Cavalry": "Horse",
    "Siege": "Siege",
    "Naval Melee": "NavalMelee",
    "Naval Ranged": "NavalRanged",
    "Air": "Air",
    "Support": "Support",
    "Civilian": "Civilian",
    "Religious": "Religious"
}

new_units = []
scraped_unit_names = {u['name'] for u in scraped_units}

for u in units_existing:
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
# CONVERT DISTRICTS
# ============================================================

print("\nConverting districts...")

# The districts.json from Unciv is already complete
# Just save it as-is (it should match)

new_districts = districts_existing

# ============================================================
# SAVE ALL
# ============================================================

print("\nSaving files...")

save_json(UNCIV_DIR / "Nations.json", new_nations)
save_json(UNCIV_DIR / "Buildings.json", new_buildings)
save_json(UNCIV_DIR / "Units.json", new_units)
save_json(UNCIV_DIR / "Districts.json", new_districts)

print("Done!")
print(f"  Nations: {len(new_nations)}")
print(f"  Buildings: {len(new_buildings)}")
print(f"  Units: {len(new_units)}")
print(f"  Districts: {len(new_districts)}")