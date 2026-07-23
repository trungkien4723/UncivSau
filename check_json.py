import re, json

with open('android/assets/jsons/Civ VI/Terrains.json', 'r', encoding='utf-8') as f:
    content = f.read()

content_no_comments = re.sub(r'//.*?\n', '\n', content)
content_no_comments = re.sub(r'/\*.*?\*/', '', content_no_comments, flags=re.DOTALL)

data = json.loads(content_no_comments)
land_terrains = [t for t in data if t.get('type') == 'Land']
print(f'Total terrains: {len(data)}')
print(f'Land terrains: {len(land_terrains)}')
for t in land_terrains:
    print(f'  - {t["name"]}')
