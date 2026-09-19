#!/usr/bin/env python3
"""
Advanced converter: Merge scraped data with existing Unciv data
"""

import json
import re
from pathlib import Path

SCRAPE_DIR = Path("D:/lamviec/test/uncivSAU/scrape_data")
UNCIV_DIR = Path("D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI")

def load_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_json(path, data):
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

# ============================================================
# LOAD DATA
# ============================================================

print("Loading existing Unciv data...")
nations_existing = load_json(UNCIV_DIR / "Nations.json")
buildings_existing = load_json(UNCIV_DIR / "Buildings.json")
units_existing = load_json(UNCIV_DIR / "Units.json")
districts_existing = load_json(UNCIV_DIR / "Districts.json")
improvements_existing = load_json(UNCIV_DIR / "TileImprovements.json")

print("Loading scraped data...")
scraped = load_json(SCRAPE_DIR / "parsed_civ6_data.json")
scraped_leaders = scraped['leaders']
scraped_buildings = scraped['buildings']
scraped_units = scraped['units']
scraped_wonders = scraped['wonders']

# ============================================================
# GROUP SCRAPED LEADERS BY CIVILIZATION
# ============================================================

civ_leaders = {}
for l in scraped_leaders:
    civ = l['civilization']
    if civ not in civ_leaders:
        civ_leaders[civ] = []
    civ_leaders[civ].append(l)

print(f"Found {len(civ_leaders)} unique civilizations in scraped data")

# ============================================================
# BUILD CIV META FROM EXISTING UNCIV DATA
# ============================================================

# Create lookup for existing civs
existing_civs = {n['name']: n for n in nations_existing if n['name'] != 'Spectator'}

# ============================================================
# PARSE UNIQUES TO UNCIV SYNTAX
# ============================================================

def parse_unique_text(text):
    """Convert scraped unique text to Unciv syntax"""
    if not text:
        return None
    
    # Clean up
    text = text.strip()
    
    # Handle special patterns
    replacements = [
        # (pattern, replacement)
        (r'Unlocks the Builder ability to construct (?:an? )?(.+?), unique to (\w+)\.', 
         r'CreatesOneImprovement <[\\1]> <on tile> <when built by [Builder]>'),
        (r'(\w+) unique (\w+) era (?:unit|building|improvement|district) that replaces (?:the )?(\w+)',
         r'Unique[\2] [\1] [\3]'),
        (r'\+(\d+)%?\s*(Food|Production|Gold|Science|Culture|Faith|Housing|Amenities|Tourism|Combat Strength|Defense|Movement|Range|Sight|Loyalty|Great Person points?)',
         r'[\+1] [\2]'),
        (r'(\d+)% faster', r'[\+1] [\2]'),
        (r'gain[s]? (\d+) (\w+)', r'[\+1] [\2]'),
    ]
    
    # For now, just clean up and format
    # Remove unicode artifacts
    text = text.replace('\u00e2\u20ac\u0153', '-')
    text = text.replace('\u00c2\u00a0', ' ')
    text = text.replace('\u00c2', '')
    
    # Convert to Unciv-style unique syntax
    # This is a simplified version - real conversion needs more work
    result = text
    
    # Fix brackets
    result = re.sub(r'([+-]\d+)', r'[\1]', result)
    result = re.sub(r'\[(\w+)\]', r'[\1]', result)
    
    return result

def extract_uniques_from_leader(leader):
    """Extract all uniques from a leader"""
    uniques = []
    
    if 'ability' in leader:
        uniques.append(leader['ability'])
    
    for u in leader.get('uniques', []):
        uniques.append(u)
    
    return uniques

# ============================================================
# MERGE NATIONS
# ============================================================

def merge_civs():
    """Merge scraped leaders into existing nation data"""
    new_nations = [n for n in nations_existing if n['name'] == 'Spectator']
    
    for civ_name, leaders in civ_leaders.items():
        if civ_name in existing_civs:
            # Update existing civ
            base = existing_civs[civ_name].copy()
            
            # Collect all uniques from all leaders
            all_uniques = []
            for leader in leaders:
                all_uniques.extend(extract_uniques_from_leader(leader))
            
            # Convert to Unciv syntax
            unique_texts = []
            for u in all_uniques:
                parsed = parse_unique_text(u)
                if parsed:
                    unique_texts.append(parsed)
            
            # Deduplicate while preserving order
            seen = set()
            unique_texts = [x for x in unique_texts if not (x in seen or seen.add(x))]
            
            # Update base with new uniques
            base['uniques'] = unique_texts
            
            # Update leader name to primary (first)
            base['leaderName'] = leaders[0]['leader']
            base['personality'] = leaders[0]['leader']
            
            new_nations.append(base)
            print(f"  Updated: {civ_name} ({len(leaders)} leaders)")
        else:
            # New civ not in existing data
            print(f"  NEW CIV: {civ_name} ({len(leaders)} leaders)")
            # Create basic entry
            primary = leaders[0]
            all_uniques = []
            for leader in leaders:
                all_uniques.extend(extract_uniques_from_leader(leader))
            
            unique_texts = [parse_unique_text(u) for u in all_uniques]
            unique_texts = [x for x in unique_texts if x]
            
            nation = {
                "name": civ_name,
                "leaderName": primary['leader'],
                "adjective": [civ_name],
                "preferredVictoryType": "Scientific",
                "personality": primary['leader'],
                "agenda": "Agenda",
                "hiddenAgendas": ["Warmonger Hater", "Nature Lover", "Piety"],
                "outerColor": [128, 128, 128],
                "innerColor": [255, 255, 255],
                "favoredReligion": "Christianity",
                "uniqueName": "Unique Ability",
                "uniques": unique_texts,
                "cities": [],
                "spyNames": []
            }
            new_nations.append(nation)
    
    return new_nations

# ============================================================
# CONVERT BUILDINGS
# ============================================================

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

def parse_building_yields(yields):
    """Parse yield texts to stats dict"""
    stats = {}
    for y in yields:
        # Match patterns like "+1 Food" or "+2 Production"
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
                "Great": "greatPersonPoints"
            }
            if stat in stat_map:
                stats[stat_map[stat]] = int(val)
    return stats

def convert_buildings():
    """Convert scraped buildings to Unciv format"""
    new_buildings = []
    
    # First, keep existing buildings that aren't in scraped data
    scraped_names = {b['name'] for b in scraped_buildings}
    
    for b in buildings_existing:
        if b['name'] not in scraped_names:
            new_buildings.append(b)
    
    # Add/update scraped buildings
    for b in scraped_buildings:
        district = DISTRICT_MAP.get(b.get('district', ''), b.get('district', ''))
        
        building = {
            "name": b['name'],
            "district": district,
            "cost": b.get('cost', 0) or 0,
            "maintenance": b.get('maintenance', 0) or 0,
            "stats": parse_building_yields(b.get('yields', [])),
            "uniques": []
        }
        
        if b.get('unique_to'):
            building['uniques'].append(b['unique_to'])
        
        # Try to find required tech/civic from existing data
        existing = next((x for x in buildings_existing if x['name'] == b['name']), None)
        if existing:
            if 'requiredTech' in existing:
                building['requiredTech'] = existing['requiredTech']
            if 'requiredCivic' in existing:
                building['requiredCivic'] = existing['requiredCivic']
            if 'buildings' in existing:
                building['buildings'] = existing['buildings']
        
        new_buildings.append(building)
    
    return new_buildings

# ============================================================
# CONVERT UNITS
# ============================================================

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

def convert_units():
    """Convert scraped units to Unciv format"""
    new_units = []
    scraped_names = {u['name'] for u in scraped_units}
    
    # Keep existing non-scraped
    for u in units_existing:
        if u['name'] not in scraped_names:
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
    
    return new_units

# ============================================================
# MAIN
# ============================================================

if __name__ == "__main__":
    print("Merging nations...")
    new_nations = merge_civs()
    
    print("Converting buildings...")
    new_buildings = convert_buildings()
    
    print("Converting units...")
    new_units = convert_units()
    
    # Save
    save_json(UNCIV_DIR / "Nations_merged.json", new_nations)
    save_json(UNCIV_DIR / "Buildings_merged.json", new_buildings)
    save_json(UNCIV_DIR / "Units_merged.json", new_units)
    
    print(f"Done! {len(new_nations)} nations, {len(new_buildings)} buildings, {len(new_units)} units")
    print("Files saved with _merged suffix")