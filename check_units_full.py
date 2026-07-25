import json
with open('jsons/Civ VI/Units.json') as f:
    units = json.load(f)

# Create lookup
unit_lookup = {u.get('name', ''): u for u in units}

# Check upgrade chains
print("=== UPGRADE CHAINS ===")
chains = {
    'Scout': ['Skirmisher', 'Ranger', 'Spec Ops'],
    'Slinger': ['Archer', 'Crossbowman', 'Field Cannon', 'Machine Gun'],
    'Warrior': ['Swordsman', 'Man-At-Arms', 'Musketman', 'Line Infantry', 'Infantry', 'Mechanized Infantry'],
    'Spearman': ['Pikeman', 'Pike and Shot', 'AT Crew', 'Modern AT'],
    'Heavy Chariot': ['Knight', 'Cuirassier', 'Tank', 'Modern Armor'],
    'Horseman': ['Courser', 'Cavalry', 'Helicopter'],
    'Catapult': ['Trebuchet', 'Bombard', 'Artillery', 'Rocket Artillery'],
    'Galley': ['Caravel', 'Ironclad', 'Destroyer'],
    'Quadrireme': ['Frigate', 'Battleship', 'Missile Cruiser'],
    'Privateer': ['Submarine', 'Nuclear Submarine'],
    'Biplane': ['Fighter', 'Jet Fighter'],
    'Bomber': ['Jet Bomber'],
}

for start, chain in chains.items():
    print(f"\n{start} chain:")
    current = start
    for next_unit in chain:
        if current in unit_lookup:
            u = unit_lookup[current]
            print(f'  {current}: tech={u.get("requiredTech", "")} civic={u.get("requiredCivic", "")} -> {next_unit}')
        else:
            print(f'  {current}: MISSING FROM UNITS.JSON')
        current = next_unit
    # Check last unit
    if current in unit_lookup:
        u = unit_lookup[current]
        print(f'  {current}: tech={u.get("requiredTech", "")} civic={u.get("requiredCivic", "")} (end)')
    else:
        print(f'  {current}: MISSING FROM UNITS.JSON')

# Check for units that shouldn't exist
print("\n=== UNITS IN PROJECT BUT NOT IN WIKI ===")
wiki_units = {
    'Scout', 'Skirmisher', 'Ranger', 'Spec Ops',
    'Slinger', 'Archer', 'Crossbowman', 'Field Cannon', 'Machine Gun',
    'Warrior', 'Swordsman', 'Man-At-Arms', 'Musketman', 'Line Infantry', 'Infantry', 'Mechanized Infantry',
    'Spearman', 'Pikeman', 'Pike and Shot', 'AT Crew', 'Modern AT',
    'Heavy Chariot', 'Knight', 'Cuirassier', 'Tank', 'Modern Armor',
    'Horseman', 'Courser', 'Cavalry', 'Helicopter',
    'Catapult', 'Trebuchet', 'Bombard', 'Artillery', 'Rocket Artillery',
    'Galley', 'Caravel', 'Ironclad', 'Destroyer',
    'Quadrireme', 'Frigate', 'Battleship', 'Missile Cruiser',
    'Privateer', 'Submarine', 'Nuclear Submarine',
    'Biplane', 'Fighter', 'Jet Fighter',
    'Bomber', 'Jet Bomber',
    'Aircraft Carrier',
    'Giant Death Robot',
    # Unique units
    'Okihtcitaw', 'Warak\'aq', 'Highlander', 'Eagle Warrior', 'Gaesatae', 'Sabum Kibittum',
    'Hypaspist', 'Immortal', 'Legion', 'Ngao Mbeba', 'Toa',
    'Berserker', 'Khevsur', 'Samurai',
    'Conquistador', 'Janissary',
    'Garde Impériale', 'Redcoat',
    'Digger',
    'Hoplite', 'Impi',
    'Carolean',
    'War-Cart', 'Hetairoi', 'Varu',
    'Mamluk', 'Mandekalu Cavalry', 'Tagma',
    'Winged Hussar', 'Rough Rider',
    'Black Army', 'Oromo Cavalry',
    'Malón Raider', 'Cossack', 'Llanero', 'Huszár', 'Mountie',
    'Barbarian Horseman', 'Barbarian Horse Archer',
    'Maryannu Chariot Archer', 'Saka Horse Archer', 'Keshig',
    'Domrey',
    'Viking Longship', 'Bireme', 'Nau',
    'Dromon', 'Jong', 'De Zeven Provinciën',
    'Minas Geraes',
    'Barbary Corsair', 'Sea Dog',
    'U-Boat',
    'P-51 Mustang',
    'Nihang', 'Warrior Monk',
    'Pítati Archer', 'Hul\'che', 'Saka Horse Archer',
    'Crouching Tiger',
    'Voi Chiến',
    'Hwacha',
    'Battering Ram', 'Siege Tower', 'Military Engineer', 'Medic', 'Observation Balloon',
    'Anti-Air Gun', 'Mobile SAM', 'Drone', 'Supply Convoy',
    'Great Artist', 'Great Scientist', 'Great Merchant', 'Great Engineer', 'Great Prophet',
    'Great General', 'Khan', 'Great Admiral',
    'SS Booster', 'SS Cockpit', 'SS Engine', 'SS Stasis Chamber',
    'Trader', 'Great Writer', 'Great Musician', 'Great Diplomat',
    'Naturalist', 'Rock Band', 'Missionary', 'Apostle', 'Inquisitor', 'Guru',
    'Barbarian', 'Warrior Monk',
    'Composite Bowman', 'Landsknecht', 'Galleass', 'Camel Archer', 'Keshik', 'Chu-Ko-Nu', 'Longbowman', 'Hwacha',
    'Longswordsman', 'Turtle Ship', 'Musketeer', 'Minuteman', 'Tercio', 'Sea Beggar', 'Ship of the Line',
    'Lancer', 'Sipahi', 'Hakkapeliitta', 'Cannon', 'Gatling Gun', 'Rifleman', 'Mehal Sefari', 'Cavalry',
    'Great War Infantry', 'Foreign Legion', 'Triplane', 'Great War Bomber', 'Carrier',
    'Anti-Aircraft Gun', 'Landship', 'Marine', 'Zero', 'B17', 'Paratrooper', 'Panzer', 'Anti-Tank Gun',
    'Atomic Bomb', 'Guided Missile', 'Nuclear Missile', 'Helicopter Gunship', 'Stealth Bomber',
}

project_units = set(unit_lookup.keys())
extra = project_units - wiki_units
missing = wiki_units - project_units

if extra:
    print(f"Extra units in project ({len(extra)}):")
    for u in sorted(extra):
        print(f"  {u}")

if missing:
    print(f"Missing units from wiki ({len(missing)}):")
    for u in sorted(missing):
        print(f"  {u}")