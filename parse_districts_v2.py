#!/usr/bin/env python3

# Parse the Tilesets.atlas file to find district entries
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

# Filter district-related entries - look for district tiles and related buildings
district_entries = []
for entry in entries:
    name = entry.get('name', '')
    # Check for district-related names
    if any(keyword in name.lower() for keyword in [
        'district', 'academy', 'barbarian encampment', 'city center',
        'fort', 'holy site', 'landmark', 'manufactory', 'marble',
        'mine', 'plantation', 'quarry', 'trading post', 'city ruins',
        'pentagon', 'city walls', 'water mill', 'armory', 'copenhagen',
        'siena cathedral', 'ziggurat', 'alcatraz',
        'bingdao temple', 'cambridge university'
    ]):
        district_entries.append(entry)

print("District/Tile Entries found in Tilesets.atlas:")
print("========================================")

# Add total count
print(f"\nTotal districts found: {len(district_entries)}")

# Show first few entries to see what's detected
if district_entries:
    print("\nFirst 20 entries detected:")
    for i, entry in enumerate(district_entries[:20]):
        name = entry.get('name', '')
        print(f"{i+1}. {name}")
else:
    print("No district entries detected. Let's try a different approach.")
    # Quick search for relevant entries
    with open("D:\\lamviec\\test\\uncivSAU\\android\\assets\\Tilesets.atlas", 'r') as f:
        for line in f:
            line = line.strip()
            if '/Tiles/' in line or '/District/' in line or any(k in line.lower() for k in ['academy', 'barbarian', 'city center', 'fort', 'holy']):
                print(f"  {line}")
