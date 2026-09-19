import json, re, os

def strip_comments(text):
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.DOTALL)
    lines = text.split('\n')
    result = []
    for line in lines:
        in_string = False; string_char = None; comment_pos = -1; i = 0
        while i < len(line):
            c = line[i]
            if not in_string:
                if c in '"\'': in_string = True; string_char = c
                elif c == '/' and i+1 < len(line) and line[i+1] == '/': comment_pos = i; break
            else:
                if c == '\\': i += 1
                elif c == string_char: in_string = False
            i += 1
        if comment_pos >= 0: line = line[:comment_pos]
        result.append(line)
    text = '\n'.join(result)
    text = re.sub(r',(\s*[\]}])', r'\1', text)
    return json.loads(text)

# Load all tech names
tech_data = strip_comments('jsons/Civ VI/Techs.json')
civic_data = strip_comments('jsons/Civ VI/Civics.json')

all_tech = set()
for col in tech_data:
    if isinstance(col, dict) and 'techs' in col:
        for t in col['techs']:
            all_tech.add(t['name'])

all_civic = set()
for col in civic_data:
    if isinstance(col, dict) and 'civics' in col:
        for c in col['civics']:
            all_civic.add(c['name'])

print(f'Total techs: {len(all_tech)}')
print(f'Total civics: {len(all_civic)}')

# Check if Military Training and Electronics are in either set
print(f'\n"Military Training" in techs: {"Military Training" in all_tech}')
print(f"Military Training" in civics: {"Military Training" in all_civic}')
print(f'"Electronics" in techs: {"Electronics" in all_tech}')
print(f'"Electronics" in civics: {"Electronics" in all_civic}')

# Print what they actually are
print(f'\nCivics containing "Military":')
for c in all_civic:
    if 'military' in c.lower() or 'training' in c.lower():
        print(f'  {c}')

print(f'\nCivics containing "Electronic":')
for c in all_civic:
    if 'electronic' in c.lower():
        print(f'  {c}')

# Now check ALL JSON files for any remaining Military Training / Electronics as requiredTech
print('\n=== Searching ALL JSON files for Military Training/Electronics references ===')
json_dir = 'jsons/Civ VI'
for fname in os.listdir(json_dir):
    if not fname.endswith('.json'): continue
    fpath = os.path.join(json_dir, fname)
    content = open(fpath, encoding='utf-8').read()
    # Quick text search without full parsing
    for target in ['Military Training', 'Electronics']:
        # Find the target in the raw text
        idx = 0
        while True:
            idx = content.find(f'"{target}"', idx)
            if idx == -1: break
            # Get surrounding context
            start = max(0, idx - 80)
            end = min(len(content), idx + len(target) + 80)
            context = content[start:end].replace('\n', ' ')
            print(f'  [{fname}] ...{context}...')
            idx += 1
