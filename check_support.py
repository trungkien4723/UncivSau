import json
with open('jsons/Civ VI/Units.json') as f:
    units = json.load(f)
for u in units:
    name = u.get('name', '')
    unit_type = u.get('unitType', '')
    if 'support' in unit_type.lower():
        print(f'{name}: tech={u.get("requiredTech", "")} civic={u.get("requiredCivic", "")} cost={u.get("cost", 0)}')