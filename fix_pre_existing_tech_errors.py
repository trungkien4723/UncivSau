import json, re

def strip_comments(text):
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.DOTALL)
    lines = text.split('\n')
    result = []
    for line in lines:
        in_string = False
        string_char = None
        comment_pos = -1
        i = 0
        while i < len(line):
            c = line[i]
            if not in_string:
                if c in '"\'':
                    in_string = True
                    string_char = c
                elif c == '/' and i + 1 < len(line) and line[i+1] == '/':
                    comment_pos = i
                    break
            else:
                if c == '\\' and i + 1 < len(line):
                    i += 1
                elif c == string_char:
                    in_string = False
            i += 1
        if comment_pos >= 0:
            line = line[:comment_pos]
        result.append(line)
    text = '\n'.join(result)
    text = re.sub(r',(\s*[\]}])', r'\1', text)
    return text

def load_json(fp):
    with open(fp, encoding='utf-8') as f:
        return json.loads(strip_comments(f.read()))

units = load_json('jsons/Civ VI/Units.json')

# Find Military Engineer and fix it
for u in units:
    if u['name'] == 'Military Engineer':
        # Fix requiredTech
        old = u.get('requiredTech', '')
        print(f'Current requiredTech: "{old[:80]}..."')
        u['requiredTech'] = 'Military Engineering'
        print(f'Fixed to: Military Engineering')

# Also fix Rock Band - should use requiredCivic="Mass Media"
for u in units:
    if u['name'] == 'Rock Band':
        u['requiredCivic'] = u.pop('requiredTech', '')

# Fix Giant Death Robot - "Future Technology" should be "Future Tech"
for u in units:
    if u.get('requiredTech') == 'Future Technology':
        u['requiredTech'] = 'Future Tech'
        print(f'Fixed {u["name"]}: Future Technology -> Future Tech')

# Fix buildings with wrong requiredTech that should be requiredCivic
buildings = load_json('jsons/Civ VI/Buildings.json')
for b in buildings:
    if b['name'] == 'Bolshoi Theatre' or b['name'] == 'Film Studio':
        if b.get('requiredTech') == 'Mass Media':
            b['requiredCivic'] = b.pop('requiredTech')
            print(f'Fixed {b["name"]}: requiredTech=Mass Media -> requiredCivic=Mass Media')

# Write files
with open('jsons/Civ VI/Units.json', 'w', encoding='utf-8') as f:
    json.dump(units, f, indent=4, ensure_ascii=False)
with open('jsons/Civ VI/Buildings.json', 'w', encoding='utf-8') as f:
    json.dump(buildings, f, indent=4, ensure_ascii=False)

print('\nDone. Syncing to android...')
import shutil
shutil.copy2('jsons/Civ VI/Units.json', 'android/assets/jsons/Civ VI/Units.json')
shutil.copy2('jsons/Civ VI/Buildings.json', 'android/assets/jsons/Civ VI/Buildings.json')
print('Synced')
