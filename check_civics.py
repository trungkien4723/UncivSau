import json

with open('jsons/Civ VI/Civics.json') as f:
    civics = json.load(f)

# Build civic prerequisites
civic_prereqs = {}
for column in civics:
    for civic in column.get('civics', []):
        name = civic.get('name', '')
        prereqs = civic.get('prerequisites', [])
        civic_prereqs[name] = prereqs

print('=== CIVIC PREREQUISITES ===')
for name, prereqs in sorted(civic_prereqs.items()):
    if prereqs:
        print(f'  {name} -> requires: {", ".join(prereqs)}')
    else:
        print(f'  {name} -> (no prereqs)')

# Check if Mercantilism is a civic
print('\n=== CHECKING MERCANTILISM ===')
if 'Mercantilism' in civic_prereqs:
    print(f'Mercantilism is a Civic, prereqs: {civic_prereqs["Mercantilism"]}')
else:
    print('Mercantilism NOT found in Civics')

# Check for Military Science - is it a tech or civic?
print('\n=== CHECKING MILITARY SCIENCE ===')
with open('jsons/Civ VI/Techs.json') as f:
    techs = json.load(f)

tech_names = []
for column in techs:
    for tech in column.get('techs', []):
        tech_names.append(tech.get('name', ''))

if 'Military Science' in tech_names:
    print('Military Science is a TECH')
else:
    print('Military Science is NOT a tech')

if 'Military Science' in civic_prereqs:
    print('Military Science is a CIVIC')
else:
    print('Military Science is NOT a civic')

# Check for Castles tech vs civic
print('\n=== CHECKING CASTLES ===')
if 'Castles' in tech_names:
    print('Castles is a TECH')
if 'Castles' in civic_prereqs:
    print('Castles is a CIVIC')

# Check for Sanitation vs Urbanization
print('\n=== CHECKING URBANIZATION/SANITATION ===')
if 'Urbanization' in civic_prereqs:
    print(f'Urbanization is a CIVIC, prereqs: {civic_prereqs["Urbanization"]}')
else:
    print('Urbanization is NOT a civic')
if 'Urbanization' in tech_names:
    print('Urbanization is a TECH')
else:
    print('Urbanization is NOT a tech')

if 'Sanitation' in tech_names:
    print('Sanitation is a TECH')
if 'Sanitation' in civic_prereqs:
    print('Sanitation is a CIVIC')

# Check for Steam Power
print('\n=== CHECKING STEAM POWER ===')
if 'Steam Power' in tech_names:
    print('Steam Power is a TECH')
if 'Steam Power' in civic_prereqs:
    print('Steam Power is a CIVIC')

# Check for Industrialization
print('\n=== CHECKING INDUSTRIALIZATION ===')
if 'Industrialization' in tech_names:
    print('Industrialization is a TECH')
if 'Industrialization' in civic_prereqs:
    print('Industrialization is a CIVIC')

# Check for Radio
print('\n=== CHECKING RADIO ===')
if 'Radio' in tech_names:
    print('Radio is a TECH')
if 'Radio' in civic_prereqs:
    print('Radio is a CIVIC')