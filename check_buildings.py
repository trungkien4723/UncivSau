import json
with open('jsons/Civ VI/Buildings.json') as f:
    buildings = json.load(f)
for b in buildings:
    name = b.get('name', '')
    if 'wall' in name.lower():
        print(f'{name}: tech={b.get("requiredTech", "")} civic={b.get("requiredCivic", "")} cost={b.get("cost", 0)}')