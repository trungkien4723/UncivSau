#!/usr/bin/env python3
"""
Parse Civ 6 data from civ6bbg.github.io HTML pages
"""

import re
import json
from pathlib import Path
from bs4 import BeautifulSoup

DATA_DIR = Path(__file__).parent

def parse_leaders():
    """Parse leaders from leaders_6.0.html"""
    with open(DATA_DIR / "leaders_6.0.html", "r", encoding="utf-8") as f:
        soup = BeautifulSoup(f.read(), "html.parser")
    
    leaders = []
    # Find all leader entries in the sidebar
    for li in soup.select(".sidebar-body-menu li a"):
        href = li.get("href", "")
        name = li.select_one("p")
        if name:
            full_name = name.get_text(strip=True)
            if full_name and " " in full_name:
                parts = full_name.split(" ", 1)
                civ = parts[0]
                leader = parts[1]
                leaders.append({
                    "civilization": civ,
                    "leader": leader,
                    "anchor": href.replace("#", "")
                })
    
    # Now parse the main content for each leader's details
    main_content = soup.select_one(".leaders-data main") or soup.select_one("main")
    if main_content:
        for section in main_content.select("section, article, div[id]"):
            anchor = section.get("id", "")
            if not anchor:
                continue
            
            # Extract leader ability, agenda, unique units/buildings
            data = {"anchor": anchor}
            
            # Look for leader ability (usually first h2/h3 after anchor)
            for elem in section.find_all(["h2", "h3", "h4", "p", "ul", "li"]):
                text = elem.get_text(strip=True)
                if "ability" in text.lower() or "trait" in text.lower():
                    data["ability"] = text
                if "agenda" in text.lower():
                    data["agenda"] = text
                if "unique" in text.lower():
                    if "unique" not in data:
                        data["uniques"] = []
                    data["uniques"].append(text)
            
            # Match with leader list
            for leader in leaders:
                if leader["anchor"] == anchor:
                    leader.update(data)
                    break
    
    return leaders

def parse_buildings():
    """Parse buildings from buildings_6.0.html"""
    with open(DATA_DIR / "buildings_6.0.html", "r", encoding="utf-8") as f:
        soup = BeautifulSoup(f.read(), "html.parser")
    
    buildings = []
    main = soup.select_one("main") or soup.select_one(".leaders-data main")
    
    current_district = None
    for elem in main.find_all(["h2", "h3", "div"]):
        classes = elem.get("class", [])
        if "civ-name" in classes:
            # District or building name
            text = elem.get_text(strip=True)
            if "district" in text.lower() or text in ["City Center", "Encampment", "Holy Site", "Campus", 
                    "Entertainment Complex", "Harbor", "Commercial Hub", "Theater Square", 
                    "Government Plaza", "Preserve", "Diplomatic Quarter", "Industrial Zone", 
                    "Water Park", "Aerodrome", "Neighborhood", "Dam"]:
                current_district = text
            else:
                # This is a building
                building = {
                    "name": text,
                    "district": current_district,
                    "yields": [],
                    "cost": None,
                    "maintenance": None,
                    "requires": None,
                    "unique_to": None
                }
                
                # Get sibling paragraphs for yields/cost
                for sibling in elem.find_next_siblings():
                    if sibling.name == "h2" and "civ-name" in sibling.get("class", []):
                        break
                    if sibling.name == "p":
                        text = sibling.get_text(strip=True)
                        if "Production Cost" in text:
                            cost_match = re.search(r"Production Cost\s*=\s*(\d+)", text)
                            if cost_match:
                                building["cost"] = int(cost_match.group(1))
                        elif "Maintenance" in text:
                            maint_match = re.search(r"Maintenance:\s*(\d+)", text)
                            if maint_match:
                                building["maintenance"] = int(maint_match.group(1))
                        elif any(kw in text for kw in ["Food", "Production", "Gold", "Science", "Culture", "Faith", "Housing", "Amenities", "Citizen slot", "Great"]):
                            building["yields"].append(text)
                        elif "unique to" in text.lower() or "replaces" in text.lower():
                            building["unique_to"] = text
                
                buildings.append(building)
    
    return buildings

def parse_units():
    """Parse units from units_6.0.html"""
    with open(DATA_DIR / "units_6.0.html", "r", encoding="utf-8") as f:
        soup = BeautifulSoup(f.read(), "html.parser")
    
    units = []
    main = soup.select_one("main") or soup.select_one(".leaders-data main")
    
    current_class = None
    for elem in main.find_all(["h2", "h3", "div"]):
        classes = elem.get("class", [])
        if "civ-name" in classes:
            text = elem.get_text(strip=True)
            if text in ["Melee", "Anti-Cavalry", "Ranged", "Cavalry", "Siege", "Naval Melee", 
                       "Naval Ranged", "Air", "Support", "Civilian", "Religious"]:
                current_class = text
            else:
                unit = {
                    "name": text,
                    "unitClass": current_class,
                    "strength": None,
                    "rangedStrength": None,
                    "movement": None,
                    "cost": None,
                    "requires": None,
                    "upgradesTo": None,
                    "uniqueTo": None,
                    "abilities": []
                }
                
                for sibling in elem.find_next_siblings():
                    if sibling.name == "h2" and "civ-name" in sibling.get("class", []):
                        break
                    if sibling.name == "p":
                        text = sibling.get_text(strip=True)
                        if "Combat Strength" in text or "Ranged Strength" in text:
                            str_match = re.search(r"(\d+)", text)
                            if str_match:
                                if "Ranged" in text:
                                    unit["rangedStrength"] = int(str_match.group(1))
                                else:
                                    unit["strength"] = int(str_match.group(1))
                        elif "Movement" in text:
                            mov_match = re.search(r"(\d+)", text)
                            if mov_match:
                                unit["movement"] = int(mov_match.group(1))
                        elif "Production Cost" in text:
                            cost_match = re.search(r"(\d+)", text)
                            if cost_match:
                                unit["cost"] = int(cost_match.group(1))
                        elif "Requires" in text or "Unlocked" in text:
                            unit["requires"] = text
                        elif "Upgrades to" in text:
                            unit["upgradesTo"] = text
                        elif "unique to" in text.lower() or "replaces" in text.lower():
                            unit["uniqueTo"] = text
                        else:
                            unit["abilities"].append(text)
                
                units.append(unit)
    
    return units

def parse_wonders():
    """Parse wonders from world_wonder_6.0.html"""
    with open(DATA_DIR / "wonders_6.0.html", "r", encoding="utf-8") as f:
        soup = BeautifulSoup(f.read(), "html.parser")
    
    wonders = []
    main = soup.select_one("main") or soup.select_one(".leaders-data main")
    
    for elem in main.find_all(["h2", "h3", "div"]):
        classes = elem.get("class", [])
        if "civ-name" in classes:
            text = elem.get_text(strip=True)
            wonder = {
                "name": text,
                "era": None,
                "cost": None,
                "requires": None,
                "placement": None,
                "effects": []
            }
            
            for sibling in elem.find_next_siblings():
                if sibling.name in ["h2", "h3"] and "civ-name" in sibling.get("class", []):
                    break
                if sibling.name == "p":
                    text = sibling.get_text(strip=True)
                    if "Era" in text and wonder["era"] is None:
                        wonder["era"] = text
                    elif "Production Cost" in text or "Cost" in text:
                        cost_match = re.search(r"(\d+)", text)
                        if cost_match:
                            wonder["cost"] = int(cost_match.group(1))
                    elif "Requires" in text or "Unlocked" in text:
                        wonder["requires"] = text
                    elif "Must be built" in text or "placement" in text.lower() or "adjacent" in text.lower():
                        wonder["placement"] = text
                    else:
                        wonder["effects"].append(text)
            
            wonders.append(wonder)
    
    return wonders

def main():
    print("Parsing leaders...")
    leaders = parse_leaders()
    print(f"Found {len(leaders)} leaders")
    
    print("Parsing buildings...")
    buildings = parse_buildings()
    print(f"Found {len(buildings)} buildings")
    
    print("Parsing units...")
    units = parse_units()
    print(f"Found {len(units)} units")
    
    print("Parsing wonders...")
    wonders = parse_wonders()
    print(f"Found {len(wonders)} wonders")
    
    # Save parsed data
    output = {
        "leaders": leaders,
        "buildings": buildings,
        "units": units,
        "wonders": wonders
    }
    
    with open(DATA_DIR / "parsed_civ6_data.json", "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
    
    print("Saved to parsed_civ6_data.json")

if __name__ == "__main__":
    main()