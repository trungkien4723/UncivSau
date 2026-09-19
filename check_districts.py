import json

with open('jsons/Civ VI/Districts.json') as f:
    districts = json.load(f)

district_lookup = {d.get('name', ''): d for d in districts}

print('=== HARBOR ===')
if 'Harbor' in district_lookup:
    d = district_lookup['Harbor']
    print(f'  requiredTech: {d.get("requiredTech", "")}')
    print(f'  requiredCivic: {d.get("requiredCivic", "")}')

print('\n=== NEIGHBORHOOD ===')
if 'Neighborhood' in district_lookup:
    d = district_lookup['Neighborhood']
    print(f'  requiredTech: {d.get("requiredTech", "")}')
    print(f'  requiredCivic: {d.get("requiredCivic", "")}')

print('\n=== ALL DISTRICTS ===')
for d in districts:
    name = d.get('name', '')
    tech = d.get('requiredTech', '')
    civic = d.get('requiredCivic', '')
    print(f'  {name}: tech={tech} civic={civic}')