import json
with open('jsons/Civ VI/Units.json') as f:
    units = json.load(f)
for u in units:
    if u.get('name') in ['Trebuchet', 'Privateer', 'Barbary Corsair', 'Catapult', 'Ballista', 'Bombard', 'Cannon', 'Artillery']:
        tech = u.get('requiredTech', '')
        civic = u.get('requiredCivic', '')
        unique = u.get('uniqueTo', '')
        cost = u.get('cost', 0)
        print(f'{u["name"]}: tech={tech} civic={civic} unique={unique} cost={cost}')