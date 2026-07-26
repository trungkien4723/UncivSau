#!/usr/bin/env python3

# Parse the Tilesets.atlas file to find district entries
with open("D:\\lamviec\\test\\uncivSAU\\android\\assets\\Tilesets.atlas", 'r') as f:
    content = f.read()

entries = []
current = {}
lines = content.split('\n')

for line in lines:
    if line.startswith('TileSets/'):
        if current:
            entries.append(current)
        current = {'Name': line.strip()}
    elif ':' in line:
        key, value = line.split(':', 1)
        current[key.strip().lower()] = value.strip()
    elif line.strip() == '':
        if current:
            entries.append(current)
        current = {}

if current:
    entries.append(current)

# Filter district-related entries
district_entries = []
for entry in entries:
    name = entry.get('name', '')
    if any(keyword in name.lower() for keyword in [
        'district', 'academy', 'barbarian encampment', 'city center',
        'fort', 'holy site', 'landmark', 'manufactory', 'marble',
        'mine', 'plantation', 'quarry', 'trading post', 'city ruins'
    ]):
        district_entries.append(entry)

print("District/Tile Entries found in Tilesets.atlas:")
print("========================================")

for entry in district_entries:
    name = entry.get('name', '')
    x = entry.get('xy', '').split(',')[0] if entry.get('xy') else ''
    y = entry.get('xy', '').split(',')[1] if entry.get('xy') else ''
    width = entry.get('size', '').split(',')[0] if entry.get('size') else ''
    height = entry.get('size', '').split(',')[1] if entry.get('size') else ''
    
    # Clean up name for readability
    clean_name = name
    if '/Tiles/' in name:
        clean_name = name.split('/')[2]
    if name.startswith('/TileSets/FantasyHex/') and '/Units/' not in name:
        clean_name = name.split('/')[2]
    elif name.startswith('/TileSets/Minimal/') and '/Units/' not in name:
        clean_name = name.split('/')[2]
    elif name.startswith('/TileSets/HexaRealm/') and '/Units/' not in name and '/Tiles/' not in name:
        clean_name = name.split('/')[3]
    
    # Replace underscores and kebab case with spaces
    clean_name = clean_name.replace('-', ' ').replace('_', ' ')
    
    print(f"District: {clean_name.title()}")
    print(f"  Full Path: {name}")
    print(f"  Position: X={x}, Y={y}")
    print(f"  Size: {width} x {height}")
    print("  ---")

print(f"\nTotal districts found: {len(district_entries)}")
