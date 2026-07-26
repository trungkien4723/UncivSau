#!/usr/bin/env python3

# Parse and categorize districts/tiles from Tilesets.atlas
with open("D:\\lamviec\\test\\uncivSAU\\android\\assets\\Tilesets.atlas", 'r') as f:
    content = f.read()

entries = []
current = {}
lines = content.split('\n')

for line in lines:
    line = line.rstrip()
    if line and line.startswith('TileSets/'):
        if current:
            entries.append(current)
        current = {'Name': line}
    elif ':' in line:
        if line.strip() and not line.startswith(' '):
            if current:
                entries.append(current)
            current = {}
            parts = line.split(':', 1)
            if len(parts) == 2:
                current['Name'] = parts[0].strip()
                current[parts[1].strip().lower()] = ''
        else:
            parts = line.strip().split(':', 1)
            if len(parts) == 2:
                current[parts[0].strip().lower()] = parts[1].strip()

if current:
    entries.append(current)

# Categorize districts and buildings by type
district_categories = {
    'city_building': [],
    'special_building': [],
    'resource_building': [],
    'religious_building': [],
    'industrial_building': [],
    'defense_building': [],
    'unique_building': []
}

# Analyze each entry
for entry in entries:
    name = entry.get('name', '')
    if not name:
        continue

    # Extract the base name after the last '/'
    base_name = name.split('/')[-1]
    
    # Map to categories
    if any(x in name.lower() for x in ['/tiles/']):
        # These are terrain/tile decorations, not districts
        continue
    elif 'city center' in name.lower():
        district_categories['city_building'].append(base_name)
    elif any(x in name.lower() for x in ['academy', 'manufactory', 'landmark']):
        district_categories['special_building'].append(base_name)
    elif any(x in name.lower() for x in ['marble', 'mine', 'quarry', 'aluminum', 'gold ore', 'silver', 'copper', 'iron', 'uranium']):
        district_categories['resource_building'].append(base_name)
    elif any(x in name.lower() for x in ['holy site']):
        district_categories['religious_building'].append(base_name)
    elif any(x in name.lower() for x in ['fort', 'citadel', 'stockade']):
        district_categories['defense_building'].append(base_name)
    else:
        district_categories['unique_building'].append(base_name)

# Print summary
print("DISTRICT AND BUILDING MAPPING FROM Tilesets.atlas")
print("=" * 60)

for category, items in district_categories.items():
    print(f"\n{category.upper().replace('_', ' ')}:")
    print("-" * 40)
    # Sort and display unique items
    unique_items = sorted(set(items))
    for item in unique_items[:15]:  # Limit display for readability
        print(f"  {item}")
    if len(unique_items) > 15:
        print(f"  ... and {len(unique_items) - 15} more")

print(f"\nTotal districts/buildings mapped: {sum(len(v) for v in district_categories.values())}")

# Specifically focus on city-district related items
print("\n\nKEY CITY DISTRICTS AND BUILDINGS:")
print("=" * 60)

key_categories = {
    'City Center': [],
    'Special Districts': [],
    'Resource Districts': [],
    'Defense Districts': [],
    'Religious/Cultural': [],
    'Industrial': []
}

for entry in entries:
    name = entry.get('name', '')
    if not name:
        continue
    
    base_name = name.split('/')[-1]
    # Clean up name for display
    display_name = base_name.replace('-', ' ').replace('_', ' ')
    
    if 'city center' in name.lower():
        key_categories['City Center'].append(display_name)
    elif any(x in name.lower() for x in ['academy', 'manufactory', 'landmark', 'customs house']):
        key_categories['Special Districts'].append(display_name)
    elif any(x in name.lower() for x in ['marble', 'mine', 'quarry', 'aluminum', 'gold ore', 'silver', 'copper', 'iron', 'uranium']):
        key_categories['Resource Districts'].append(display_name)
    elif any(x in name.lower() for x in ['fort', 'citadel', 'stockade', 'barbarian encampment']):
        key_categories['Defense Districts'].append(display_name)
    elif any(x in name.lower() for x in ['holy site', 'church', 'mosque', 'temple']):
        key_categories['Religious/Cultural'].append(display_name)
    elif any(x in name.lower() for x in ['plantation', 'plantations', 'farm', 'pasture', 'camp']):
        key_categories['Industrial'].append(display_name)

for category, items in key_categories.items():
    if items:
        print(f"\n{category} (Total: {len(items)}):")
        for item in items:
            print(f"  {item}")
