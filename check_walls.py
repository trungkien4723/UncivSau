import json
with open('jsons/Civ VI/Buildings.json') as f:
    buildings = json.load(f)
for b in buildings:
    name = b.get('name', '')
    if name == 'Walls':
        print(f'REMOVE THIS: {name} - cost={b.get("cost")} tech={b.get("requiredTech")}')