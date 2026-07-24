#!/usr/bin/env python3
"""
Convert parsed Civ 6 data to Unciv JSON format
"""

import json
import re
from pathlib import Path

DATA_DIR = Path(__file__).parent
OUTPUT_DIR = Path(r"D:\lamviec\test\uncivSAU\android\assets\jsons\Civ VI")

# Load parsed data
with open(DATA_DIR / "parsed_civ6_data.json", "r", encoding="utf-8") as f:
    parsed = json.load(f)

leaders = parsed["leaders"]
buildings = parsed["buildings"]
units = parsed["units"]
wonders = parsed["wonders"]

print(f"Loaded: {len(leaders)} leaders, {len(buildings)} buildings, {len(units)} units, {len(wonders)} wonders")

# ============================================================
# LEADER/NATION MAPPING
# ============================================================

# Known Unciv personality types (from Personalities.json)
PERSONALITIES = [
    "Nebuchadnezzar II", "Alexander", "Wu Zetian", "Ramesses II", "Elizabeth",
    "Gandhi", "Montezuma", "Hammurabi", "Pedro II", "Basil II", "Theodora",
    "Wilfrid Laurier", "Eleanor of Aquitaine (England)", "Elizabeth I", "Victoria (Age of Empire)",
    "Victoria (Age of Steam)", "Menelik II", "Catherine de Medici (Black Queen)",
    "Catherine de Medici (Magnificence)", "Eleanor of Aquitaine (France)", "Ambiorix", "Tamar",
    "Frederick Barbarossa", "Ludwig II", "Simon Bolivar", "Gorgo", "Pericles",
    "Matthias Corvinus", "Pachacuti", "Chandragupta", "Gandhi", "Gitarja",
    "Hojo Tokimune", "Tokugawa", "Jayavarman VII", "Mvemba a Nzinga", "Nzinga Mbande",
    "Sejong", "Seondeok", "Alexander", "Mansa Musa", "Sundiata Keita", "Kupe",
    "Lautaro", "Lady Six Sky", "Genghis Khan", "Kublai Khan (Mongolia)", "Wilhelmina",
    "Harald Hardrada (Varangian)", "Harald Hardrada (Konge)", "Amanitore",
    "Suleiman (Kanuni)", "Suleiman (Muhteşem)", "Cyrus", "Nader Shah", "Dido",
    "Jadwiga", "Joao III", "Julius Caesar", "Trajan", "Peter", "Robert the Bruce",
    "Tomyris", "Philip II", "Gilgamesh", "Abraham Lincoln", "Teddy Roosevelt (Bull Moose)",
    "Teddy Roosevelt (Rough Rider)", "Saladin (Vizier)", "Saladin (Sultan)", "John Curtin"
]

# Known agenda types
AGENDAS = [
    "Cultured", "Warmonger Hater", "Nature Lover", "Piety", "Short Life of Glory",
    "City Planner", "King of the Eburones", "Kandake of Meroë", "Industrial Revolution",
    "Flower of Scotland", "Preserver of the Union", "Big Stick Policy", "Counter Reformer",
    "Ayyubid Dynasty", "Righteousness of the Faith", "Citadel of Civilization", "Magnificence",
    "Delian League", "Golden Liberty", "Three-Six Stratagems", "Bannockburn",
    "Ma'at", "Abu Simbel", "Wall of 10,000 Li", "Backstab Averse", "Ally of Enkidu"
]

def find_personality(leader_name):
    """Match leader to known personality"""
    for p in PERSONALITIES:
        if p.lower() in leader_name.lower() or leader_name.lower() in p.lower():
            return p
    return "Gandhi"  # default

def find_agenda(leader_name):
    """Match leader to known agenda"""
    # This is a simplified mapping - would need full mapping
    agenda_map = {
        "Nebuchadnezzar II": "Cultured",
        "Alexander": "Short Life of Glory",
        "Wu Zetian": "Great Wall",
        "Ramesses II": "Abu Simbel",
        "Elizabeth": "Sun Never Sets",
        "Gandhi": "Satyagraha",
        "Montezuma": "Legend of the Five Suns",
        "Hammurabi": "Nebuchadnezzar II",
        "Pedro II": "Magnanimous",
        "Basil II": "Bannockburn",
        "Theodora": "Metropolitan",
        "Wilfrid Laurier": "Four Faces of Peace",
        "Eleanor of Aquitaine (England)": "Court of Love",
        "Elizabeth I": "British Museum",
        "Victoria (Age of Empire)": "Sun Never Sets",
        "Victoria (Age of Steam)": "Industrial Revolution",
        "Menelik II": "Cradle of Humanity",
        "Catherine de Medici (Black Queen)": "Black Queen",
        "Catherine de Medici (Magnificence)": "Magnificence",
        "Eleanor of Aquitaine (France)": "Court of Love",
        "Ambiorix": "King of the Eburones",
        "Tamar": "Golden Age",
        "Frederick Barbarossa": "Free Imperial Cities",
        "Ludwig II": "Swan King",
        "Simon Bolivar": "Campaña Admirable",
        "Gorgo": "Delian League",
        "Pericles": "Delian League",
        "Matthias Corvinus": "Black Army",
        "Pachacuti": "Sapa Inca",
        "Chandragupta": "Arthashastra",
        "Gandhi": "Satyagraha",
        "Gitarja": "Exalted Goddess of the Three Worlds",
        "Hojo Tokimune": "Meiji Restoration",
        "Tokugawa": "Sakoku",
        "Jayavarman VII": "Monasteries of the King",
        "Mvemba a Nzinga": "Enthusiastic Disciple",
        "Nzinga Mbande": "Queen of Ndongo and Matamba",
        "Sejong": "Hangul",
        "Seondeok": "Hwarang",
        "Mansa Musa": "Sahel Merchants",
        "Sundiata Keita": "Manden Charter",
        "Kupe": "Kupe's Voyage",
        "Lautaro": "Toqui",
        "Lady Six Sky": "Lady of the Sky",
        "Genghis Khan": "Mongol Horde",
        "Kublai Khan (Mongolia)": "Pax Mongolica",
        "Wilhelmina": "Radio Oranje",
        "Harald Hardrada (Varangian)": "Varangian Guard",
        "Harald Hardrada (Konge)": "Last Viking King",
        "Amanitore": "Kandake of Meroë",
        "Suleiman (Kanuni)": "Lawgiver",
        "Suleiman (Muhteşem)": "Magnificent",
        "Cyrus": "Fall of Babylon",
        "Nader Shah": "Sword of Persia",
        "Dido": "Phoenician Trade",
        "Jadwiga": "Golden Liberty",
        "Joao III": "Portuguese Trade",
        "Julius Caesar": "Veni Vidi Vici",
        "Trajan": "Trajan's Column",
        "Peter": "Westernizer",
        "Robert the Bruce": "Flower of Scotland",
        "Tomyris": "Massagetae",
        "Philip II": "Counter Reformer",
        "Gilgamesh": "Adventures with Enkidu",
        "Abraham Lincoln": "Emancipation Proclamation",
        "Teddy Roosevelt (Bull Moose)": "Big Stick Policy",
        "Teddy Roosevelt (Rough Rider)": "Rough Rider",
        "Saladin (Vizier)": "Righteousness of the Faith",
        "Saladin (Sultan)": "Ayyubid Dynasty",
        "John Curtin": "Citadel of Civilization"
    }
    return agenda_map.get(leader_name, "Cultured")

def parse_uniques(text_list):
    """Parse unique abilities text into Unciv format"""
    uniques = []
    for text in text_list:
        # Clean up text
        text = text.strip()
        if not text:
            continue
        # Convert to Unciv format: [+X]% etc.
        text = re.sub(r'\+(\d+)%', r'[+\1]%', text)
        text = re.sub(r'\+(\d+)\s*(\w+)', r'[\+\1] \2', text)
        text = re.sub(r'-(\d+)%', r'[-\1]%', text)
        text = re.sub(r'(\d+)%', r'[\1]%', text)
        # Replace special markers
        text = text.replace("unique to", "unique to")
        text = text.replace("replaces the", "replaces [")
        text = text.replace("Unlocked by", "<")
        text = text.replace("Technology", ">")
        text = text.replace("Civic", ">")
        uniques.append(text)
    return uniques

# Group leaders by civilization
civ_leaders = {}
for leader in leaders:
    civ = leader["civilization"]
    if civ not in civ_leaders:
        civ_leaders[civ] = []
    civ_leaders[civ].append(leader)

# ============================================================
# BUILDINGS MAPPING
# ============================================================

# District name mapping
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

# ============================================================
# CONVERT NATIONS
# ============================================================

def convert_nations():
    """Convert leaders to Unciv Nations.json format"""
    nations = []
    
    # Add Spectator first
    nations.append({
        "name": "Spectator",
        "outerColor": [255, 255, 255]
    })
    
    for civ_name, leader_list in sorted(civ_leaders.items()):
        # Use first leader as primary
        primary = leader_list[0]
        
        # Collect all unique buildings/districts/units for this civ
        unique_buildings = []
        unique_districts = []
        unique_units = []
        all_uniques = []
        
        for leader in leader_list:
            # Parse ability
            if "ability" in leader:
                all_uniques.append(leader["ability"])
            
            # Parse uniques
            if "uniques" in leader:
                all_uniques.extend(leader["uniques"])
            
            # Categorize uniques
            for u in leader.get("uniques", []):
                u_lower = u.lower()
                if "unique district" in u_lower or "replaces the" in u_lower and "district" in u_lower:
                    # Extract district name
                    match = re.search(r"unique\s+(\w+\s+\w+)\s+district", u_lower)
                    if match:
                        unique_districts.append(match.group(1).title())
                elif "unique" in u_lower and ("building" in u_lower or "improvement" in u_lower):
                    match = re.search(r"unique\s+(\w+\s+\w+)", u_lower)
                    if match:
                        unique_buildings.append(match.group(1).title())
                elif "unique" in u_lower and "unit" in u_lower:
                    match = re.search(r"unique\s+(\w+\s+\w+)\s+(?:unit|era)", u_lower)
                    if match:
                        unique_units.append(match.group(1).title())
        
        # Deduplicate
        unique_buildings = list(dict.fromkeys(unique_buildings))
        unique_districts = list(dict.fromkeys(unique_districts))
        unique_units = list(dict.fromkeys(unique_units))
        
        # Process all uniques text
        unique_texts = parse_uniques(all_uniques)
        
        # Build nation object
        leader_name = primary["leader"]
        nation = {
            "name": civ_name,
            "leaderName": leader_name,
            "adjective": [civ_name],  # Will need proper adjectives
            "preferredVictoryType": "Scientific",  # Default
            "personality": find_personality(leader_name),
            "agenda": find_agenda(leader_name),
            "hiddenAgendas": ["Warmonger Hater", "Nature Lover", "Piety"],
            "outerColor": [128, 128, 128],  # Default
            "innerColor": [255, 255, 255],   # Default
            "favoredReligion": "Christianity",
            "uniqueName": "Unique Ability",
            "uniques": unique_texts,
            "cities": [],
            "spyNames": []
        }
        
        if unique_buildings:
            nation["uniqueBuildings"] = unique_buildings
        if unique_districts:
            nation["uniqueDistricts"] = unique_districts
        
        nations.append(nation)
    
    return nations

# ============================================================
# CONVERT BUILDINGS
# ============================================================

def convert_buildings():
    """Convert buildings to Unciv Buildings.json format"""
    unciv_buildings = []
    
    for b in buildings:
        # Parse yields
        yields = {}
        for yield_text in b.get("yields", []):
            # Parse yield text like "+1 Food" or "+2 Production"
            match = re.search(r'([+-]\d+)\s+(\w+)', yield_text)
            if match:
                stat = match.group(2)
                value = int(match.group(1))
                # Map to Unciv stat names
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
                    yields[stat_map[stat]] = value
        
        building = {
            "name": b["name"],
            "district": DISTRICT_MAP.get(b.get("district", ""), b.get("district", "")),
            "cost": b.get("cost", 0),
            "maintenance": b.get("maintenance", 0),
            "stats": yields,
            "uniques": []
        }
        
        if b.get("unique_to"):
            building["uniques"].append(b["unique_to"])
        
        unciv_buildings.append(building)
    
    return unciv_buildings

# ============================================================
# CONVERT UNITS
# ============================================================

def convert_units():
    """Convert units to Unciv Units.json format"""
    unciv_units = []
    
    for u in units:
        unit = {
            "name": u["name"],
            "unitType": u.get("unitClass", "Melee"),
            "baseUnit": u.get("uniqueTo", "").split(" ")[-1] if u.get("uniqueTo") else u["name"],
            "movement": u.get("movement", 2),
            "strength": u.get("strength", 0),
            "rangedStrength": u.get("rangedStrength", 0),
            "cost": u.get("cost", 0),
            "requiredTech": u.get("requires", ""),
            "upgradesTo": u.get("upgradesTo", ""),
            "uniqueTo": u.get("uniqueTo", ""),
            "uniques": u.get("abilities", [])
        }
        unciv_units.append(unit)
    
    return unciv_units

# ============================================================
# CONVERT WONDERS
# ============================================================

def convert_wonders():
    """Convert wonders to Unciv Buildings.json (wonders section)"""
    unciv_wonders = []
    
    for w in wonders:
        wonder = {
            "name": w["name"],
            "cost": w.get("cost", 0),
            "era": w.get("era", ""),
            "requires": w.get("requires", ""),
            "placement": w.get("placement", ""),
            "stats": {},
            "uniques": w.get("effects", [])
        }
        unciv_wonders.append(wonder)
    
    return unciv_wonders

# ============================================================
# MAIN
# ============================================================

def main():
    print("Converting nations...")
    nations = convert_nations()
    
    print("Converting buildings...")
    buildings_out = convert_buildings()
    
    print("Converting units...")
    units_out = convert_units()
    
    print("Converting wonders...")
    wonders_out = convert_wonders()
    
    # Save files
    with open(OUTPUT_DIR / "Nations_new.json", "w", encoding="utf-8") as f:
        json.dump(nations, f, indent=2, ensure_ascii=False)
    
    with open(OUTPUT_DIR / "Buildings_new.json", "w", encoding="utf-8") as f:
        json.dump(buildings_out, f, indent=2, ensure_ascii=False)
    
    with open(OUTPUT_DIR / "Units_new.json", "w", encoding="utf-8") as f:
        json.dump(units_out, f, indent=2, ensure_ascii=False)
    
    with open(OUTPUT_DIR / "Wonders_new.json", "w", encoding="utf-8") as f:
        json.dump(wonders_out, f, indent=2, ensure_ascii=False)
    
    print(f"Saved {len(nations)} nations, {len(buildings_out)} buildings, {len(units_out)} units, {len(wonders_out)} wonders")
    print("Files saved to:", OUTPUT_DIR)

if __name__ == "__main__":
    main()