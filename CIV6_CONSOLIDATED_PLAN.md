# Civilization VI - Unciv Conversion: Consolidated Progress Tracker

> **Game:** Civilization VI (Civ 6) ruleset conversion for Unciv engine  
> **Base engine:** Unciv (Civ V ruleset, Kotlin + LibGDX)  
> **Ruleset data:** `jsons/Civ VI/`  
> **Code changes:** `core/src/`  
> **Last updated:** 2026-07-28

---

## Legend

- `[x]` = Completed / Implemented
- `[~]` = Partially implemented (missing some details)
- `[ ]` = Not started / Missing

---

## 1. Core Engine & Ruleset

| Item | Status | Notes |
|------|--------|-------|
| Ruleset loads "Civ VI" without errors | `[x]` | Phase 0 done |
| Civ V backward compatibility maintained | `[x]` | Separate ruleset |
| Unique system (~700+ UniqueTypes) | `[x]` | Core engine feature |
| Ruleset JSON loading (Techs, Civics, Buildings, Units, etc.) | `[x]` | All JSON files load |
| Ruleset validation (no errors) | `[x]` | AllObjectsTests pass |
| Game saves / loads | `[x]` | Engine feature |
| Multiplayer | `[x]` | Engine feature |

---

## 2. Civ VI Gameplay Mechanics (Code)

### 2.1 Ages & Great Ages
| Item | Status | Notes |
|------|--------|-------|
| 8 standard Ages (Ancient → Information) | `[x]` | In Eras.json |
| Future era (extra) | `[x]` | Added for testing |
| Golden Age / Dark Age logic | `[x]` | GoldenAgeManager |
| Dramatic Ages mode | `[x]` | No Normal age when active |
| Heroic Age | `[x]` | Higher thresholds |

### 2.2 Districts (Cities Unstacked)
| Item | Status | Notes |
|------|--------|-------|
| City Center district | `[x]` | Always present |
| Campus district | `[x]` | Via Library building |
| Theater Square district | `[x]` | Via Monument building |
| Holy Site district | `[x]` | Via Shrine building |
| Commercial Hub district | `[x]` | Via Market building |
| Industrial Zone district | `[x]` | Via Workshop building |
| Harbor district | `[x]` | via Lighthouse building |
| Encampment district | `[x]` | Via Barracks building |
| Entertainment Complex district | `[x]` | Via Arena building |
| Aqueduct district | `[x]` | Both district + building |
| Neighborhood district | `[x]` | Via Granary |
| Government Plaza district | `[x]` | Via Court building |
| Spaceport district | `[x]` | Via Rocket Silo building |
| Aerodrome district | `[x]` | Via Aerodrome building |
| Campus buildings (Research Lab, etc.) | `[x]` | |
| Theater Square buildings (Broadcast Center, etc.) | `[x]` | |
| Industrial Zone buildings (Power Plant, etc.) | `[x]` | Power Plant resource consumption done |
| **Canal as district** | `[~]` | Tile improvement; also a wonder exists |
| **Dam as district** | `[~]` | Tile improvement; also a wonder exists |
| District placement UI | `[x]` | DistrictPickerScreen |
| District pillaging | `[x]` | DistrictPillageTests pass |
| District capacity formula `(pop-1)/3 + 1` | `[x]` | |
| AI builds districts | `[~]` | Basic automation exists |

### 2.3 Adjacency System
| Item | Status | Notes |
|------|--------|-------|
| District adjacency bonuses | `[~]` | Partial - needs full adjacency mapping |
| Campus adjacency (Rainforest, Campus, etc.) | `[~]` | Basic |
| Harbor adjacency | `[~]` | Basic |
| Commercial Hub adjacency | `[~]` | Basic |
| Industry adjacency | `[~]` | Basic |

### 2.4 Great Works System
| Item | Status | Notes |
|------|--------|-------|
| GreatWork data class | `[x]` | |
| GreatWorkType enum (Writing, Art, Artifact, Music, Relic) | `[x]` | |
| GreatWorksManager (add/remove/list) | `[x]` | |
| Archaeological Museum (3 Artifact slots) | `[x]` | |
| Art Museum (3 Art slots) | `[x]` | |
| Amphitheater (1 Writing slot) | `[x]` | |
| Louvre wonder (Great Artist points) | `[x]` | |
| **Archaeologist as Great Person** | `[x]` | Creates Artifacts in city center |
| **Antiquity Sites** | `[x]` | Added as tile improvement, spawns at game start |
| **Archaeologist excavation action** | `[x]` | New ExcavateAntiquitySite action |
| **Artifact creation from excavation** | `[x]` | Removes Antiquity Site, creates Artifact |
| **Museum Theming Bonuses** | `[x]` | +1 Culture/Tourism per work when all slots filled |
| Great Works placement UI (select which building) | `[~]` | Simplified - global pool, no per-building assignment |
| Theming bonus per individual museum | `[~]` | Global calculation, not per-building |
| Great Works strategy overview tab | `[x]` | |

### 2.5 Corporations & Monopolies
| Item | Status | Notes |
|------|--------|-------|
| CorporationManager | `[x]` | Monopoly detection, founding, product research |
| Monopoly detection (>50% world supply) | `[x]` | |
| Corporation founding | `[x]` | |
| Corporation products & research | `[x]` | |

### 2.6 Heroes & Legends
| Item | Status | Notes |
|------|--------|-------|
| HeroesManager | `[x]` | Max 4 heroes, 30-turn lifespan |
| Hero defeat → 10-turn cooldown → revival at capital | `[x]` | |
| Leader promotion system | `[~]` | Basic |

### 2.7 Secret Societies
| Item | Status | Notes |
|------|--------|-------|
| SecretSocieties.json (4 societies) | `[x]` | |
| Rank-specific uniques | `[x]` | |
| Trigger logic | `[~]` | Partial |

### 2.8 Emergency System
| Item | Status | Notes |
|------|--------|-------|
| EmergenciesManager | `[x]` | 5 types |
| Auto-join logic (diplomatic status based) | `[x]` | |
| Rewards on resolution | `[x]` | |
| Military, Religious, Aid Request, Nuclear, Climate | `[x]` | |

### 2.9 Apocalypse Mode / Climate
| Item | Status | Notes |
|------|--------|-------|
| DisasterManager | `[x]` | 2× frequency, 1.5× damage, 2× radius |
| ClimateManager (CO2 thresholds, disaster types) | `[x]` | Extra disaster types |
| Extra disaster types | `[x]` | |

### 2.10 Zombie Defense Mode
| Item | Status | Notes |
|------|--------|-------|
| ZombieManager | `[x]` | Spawns on unit death, downed instead of dead |
| Zombie revival after 3 turns | `[x]` | |

---

## 3. Civics & Policy System

| Item | Status | Notes |
|------|--------|-------|
| Civics.json (61 civics) | `[x]` | Full tree with prerequisites |
| CivicPickerScreen | `[x]` | |
| PolicyCards.json (84 cards) | `[x]` | 4 slot types: Military, Wildcard, Economic, Diplomatic |
| requiredCivic linking | `[x]` | |
| Government types + Policy slot assignment | `[x]` | |
| Inspiration system | `[x]` | Eureka / Inspiration for techs and civics |
| **Theming bonus civic inspiration** | `[x]` | "Upon Theming a Museum" |
| **Cultural Heritage civic** | `[x]` | Inspiration from themed museums |

---

## 4. Technology System

| Item | Status | Notes |
|------|--------|-------|
| Techs.json (~84 techs) | `[~]` | Slight gaps in Industrial/Modern era |
| Research system (TechManager) | `[x]` | |
| Eureka boosts (10% cost reduction) | `[x]` | 10 tech samples |
| Tech cost scaling | `[x]` | |
| Tech prerequisites / tree | `[x]` | |
| Era-based tech unlocking | `[x]` | |
| **Tech Shuffle** | `[x]` | Randomized tech order |

---

## 5. Diplomacy & World Congress

| Item | Status | Notes |
|------|--------|-------|
| Diplomacy system (DiplomacyManager) | `[x]` | |
| City-State allies/protectors | `[x]` | |
| World Congress session system | `[x]` | |
| World Congress voting | `[x]` | |
| Diplomatic Victory (UN building, votes) | `[x]` | Fully implemented |
| World Congress resolutions | `[x]` | |
| Diplomatic favor system | `[x]` | |
| AI auto-voting | `[x]` | |
| Diplomatic vote result screen | `[x]` | |
| Alliance bonuses | `[x]` | Research/Military/Economic/Cultural/Religious |
| Emergency invitations in World Congress | `[x]` | |
| War Support system (replaces War Weariness) | `[~]` | Partial |
| Trade Embargo resolution | `[~]` | Partial |

---

## 6. City-State System

| Item | Status | Notes |
|------|--------|-------|
| CityStateTypes.json (6 types) | `[x]` | Scientific, Cultured, Maritime, Mercantile, Militaristic, Religious |
| City-state generation | `[~]` | Dynamic, ruleset entries may be incomplete |
| City-state suzerain bonuses | `[~]` | Partial |
| City-state gifts / quests | `[~]` | Basic |
| City-State diplomatic marriage | `[~]` | Partial |

---

## 7. Units & Combat

### 7.1 Unit Types
| Item | Status | Notes |
|------|--------|-------|
| Melee units (Warrior, Swordsman, etc.) | `[x]` | |
| Ranged units (Archer, Catapult, etc.) | `[x]` | |
| Cavalry units (Horseman, Knight, etc.) | `[x]` | |
| Siege units (Battering Ram, Trebuchet) | `[x]` | Battering Ram +50%, Siege Tower +100% bonuses |
| Anti-cavalry (Spearman) | `[x]` | |
| Mounted / Armor units | `[x]` | |
| Air units (Fighter, Bomber) | `[x]` | |
| Naval units | `[x]` | |
| Civilian units (Builder, Scout, Trader) | `[x]` | |
| **Spy unit** | `[ ]` | **MISSING** - no Spy unit in ruleset JSON |
| Great Person units (12 types) | `[x]` | |

### 7.2 Combat
| Item | Status | Notes |
|------|--------|-------|
| Base combat damage | `[x]` | |
| Terrain defense bonus | `[x]` | |
| Flanking bonus (adjacent melee allies) | `[x]` | |
| Support unit bonuses (Battering Ram, Siege Tower) | `[x]` | |
| Medic healing (+5 HP from adjacent Medic) | `[x]` | |
| City defense bonus | `[x]` | |
| Rough terrain defense | `[x]` | |
| Air sweep | `[x]` | |
| Pillaging | `[x]` | |
| Bombard / Ranged attack | `[x]` | |
| City bombardment | `[x]` | |
| Zone of Control | `[x]` | |

### 7.3 Unit Upgrades
| Item | Status | Notes |
|------|--------|-------|
| Unit upgrade system | `[x]` | |
| Era-scaled upgrades | `[x]` | |
| Unique unit upgrades | `[~]` | Partial |

### 7.4 Great People Generation
| Item | Status | Notes |
|------|--------|-------|
| GP points from specialists | `[x]` | 4 specialist types |
| GP from buildings | `[x]` | |
| GP name groups | `[x]` | |
| GP consumption / Great Person action | `[x]` | |
| **Rock Band performance** | `[x]` | Consumes unit, gives tourism |
| **Trade Mission** | `[x]` | Gold + influence from city-states |
| **Hurry Research** | `[x]` | Spend GP to rush tech |
| **Hurry Policy** | `[x]` | Spend GP to rush civic |
| **Hurry Wonder** | `[x]` | Spend GP to rush wonder |
| **Hurry Building** | `[x]` | Spend GP to rush building |

---

## 8. Buildings & Wonders

### 8.1 Core Buildings
| Item | Status | Notes |
|------|--------|-------|
| All Civ VI buildings (241 entries) | `[~]` | Most present |
| Campus buildings (Library, University, Research Lab) | `[x]` | |
| Theater Square buildings (Amphitheater, Broadcast Center, Museum, Art Museum, Archaeological Museum) | `[x]` | |
| Holy Site buildings (Shrine, Temple, Cathedral, etc.) | `[x]` | |
| Commercial Hub buildings (Market, Bank, Stock Exchange) | `[x]` | |
| Industrial Zone buildings (Workshop, Factory, Power Plant) | `[x]` | Power Plant resource consumption done |
| Harbor buildings (Lighthouse, Shipyard, Seaport) | `[x]` | |
| Encampment buildings (Barracks, Stable, Armory) | `[x]` | |
| **Aircraft Factory** | `[x]` | Added to Aerodrome district |
| Government Plaza buildings (complete set) | `[x]` | |
| Entertainment buildings (Arena, Stadium, Zoo) | `[x]` | |

### 8.2 Wonders
| Item | Status | Notes |
|------|--------|-------|
| Ancient wonders (Pyramids, Stonehenge, etc.) | `[x]` | |
| Classical/Cultural wonders | `[x]` | |
| Medieval wonders (Notre Dame, Alhambra, etc.) | `[x]` | |
| Renaissance wonders | `[x]` | |
| Industrial wonders (Statue of Liberty, Panama Canal) | `[x]` | |
| Modern/Atomic wonders (UN, Apollo Program) | `[x]` | |
| Information era wonders | `[x]` | |

### 8.3 Unique Buildings
| Item | Status | Notes |
|------|--------|-------|
| Civ-unique buildings | `[~]` | Many present |
| Building uniqueness (uniqueTo, requiredBuilding) | `[x]` | |

---

## 9. Map Features

| Item | Status | Notes |
|------|--------|-------|
| Natural Wonders (~14 of 16) | `[x]` | Added Grand Canyon, Nile River; all major NWs present |
| **Goody Huts** | `[x]` | Added tile improvement, spawns at game start, reuses ruins reward system |
| Ancient Ruins | `[x]` | |
| **Barbarian encampments** | `[x]` | |
| Resources (bonus, luxury, strategic) | `[x]` | |
| Terrain types (Tundra, Desert, Plains, etc.) | `[x]` | |
| Features (Forest, Jungle, etc.) | `[x]` | |
| Improvements (Farm, Mine, etc.) | `[x]` | |
| Roads & Railroads | `[x]` | |
| Citadel | `[x]` | |
| Trade routes (land/sea) | `[x]` | |
| Map generation (land, water, elevation, rivers) | `[x]` | |
| Map editor support | `[x]` | |
| **Antiquity Sites** | `[x]` | New in this session |

---

## 10. City-State & Diplomacy Suzerain Bonuses

| Item | Status | Notes |
|------|--------|-------|
| Suzerain bonus: +2 free units per era | `[~]` | Partial |
| Suzerain bonus: increased border growth | `[~]` | Partial |
| Suzerain bonus: +1 spy capacity | `[~]` | Partial |
| Suzerain bonus: free tech when discovering | `[~]` | Partial |
| Suzerain bonus: extra spy mission | `[~]` | Partial |
| Suzerain bonus: +10% production in capital | `[~]` | Partial |
| Suzerain bonus: additional diplomatic vote | `[~]` | Partial |
| Suzerain bonus: free building in city | `[~]` | Partial |

---

## 11. AI & Automation

| Item | Status | Notes |
|------|--------|-------|
| City placement AI | `[~]` | Basic |
| Unit automation (military/scout) | `[~]` | Basic |
| Worker/Builder automation | `[~]` | Basic |
| Research AI (tech choices) | `[~]` | Basic |
| Diplomacy AI (city-state relationships) | `[~]` | Basic |
| Military AI (war/defense) | `[~]` | Basic |
| Build queue AI (buildings/districts) | `[~]` | Basic |
| **Districts placement AI** | `[~]` | Basic automation exists |
| **Wonder construction priority** | `[~]` | Basic |

---

## 12. UI & Screens

| Item | Status | Notes |
|------|--------|-------|
| Main menu | `[x]` | |
| Map screen (worldscreen) | `[x]` | |
| City screen (cityscreen) | `[x]` | |
| Tech tree screen | `[x]` | |
| Civic tree screen | `[x]` | |
| Diplomacy screen | `[x]` | |
| Great People picker | `[x]` | |
| Great Works screen | `[x]` | Includes theming bonus display |
| World Congress screen | `[x]` | |
| Victory screen | `[x]` | |
| District picker | `[x]` | |
| Improvement picker | `[x]` | |
| Policy card screen | `[x]` | |
| Unit actions (right-click menu) | `[x]` | Includes excavation action |
| Empire overview (stats tab) | `[x]` | Includes theming bonus |
| **Map editor** | `[x]` | Includes Antiquity Sites step |
| **Tooltips for all uniques** | `[~]` | Partial |

---

## 13. Mod Support & Extensibility

| Item | Status | Notes |
|------|--------|-------|
| Mod loading system | `[x]` | |
| JSON-based ruleset overriding | `[x]` | |
| Unique system extensible by mods | `[x]` | |
| Custom civilizations via JSON | `[x]` | |
| Translation system | `[x]` | |
| **Save/load compatibility** | `[x]` | Serialization tests pass |

---

## 14. Missing Compared to Real Civ VI (Summary)

### Critical Missing (blocks full Civ VI experience)
1. **Spy unit** - No spy in ruleset JSON, espionage mechanics incomplete
2. **Canal/Dam as districts** - Still tile improvements; also wonders exist as districts

### Significant Gaps (reduced depth)
3. **District adjacency system** - Basic, not fully Civ VI-style
4. **War Support mechanic** - Not fully implemented (uses old War Weariness)
9. **Trade route nuances** - Basic implementation
10. **Great Works placement** - Global pool, no per-building assignment
11. **Theming bonus per individual museum** - Global only, not per-building
12. **AI district placement intelligence** - Basic automation only
13. **Leader Agendas** - Not fully implemented

### Quality of Life / Polish
14. **Zone of Control** - May not be fully Civ VI-style
15. **City-state diplomatic marriage** - Incomplete
16. **Suzerain bonus detail** - Many bonuses partial or missing
17. **Era Score rewards** - Exists but could have more triggers
18. **Natural Wonder variety** - Some missing or inconsistent
19. **Policy tree layout** - No grid position data in JSON

### Mod Content
20. **Some DLC civilizations** - May be incomplete (e.g., Mapuche, Gran Colombia, Scythia details)
21. **Leader personalities** - Basic agendas only

---

## 15. Completed This Session (2026-07-28)

### High Priority (all completed)
- [x] Golden Age Dedications UI (DedicationPickerScreen trigger)
- [x] Power Plant resource consumption per turn
- [x] Siege Support Units (Battering Ram +50%, Siege Tower +100%)
- [x] Medic adjacent healing (+5 HP/turn)
- [x] **Antiquity Sites** system (tile improvement, map spawning)
- [x] **Archaeologist excavation** action (ExcavateAntiquitySite)
- [x] **Artifact creation** from Antiquity Sites
- [x] **Museum Theming Bonuses** (getThemingStats, stat map integration)

### Easy Additions (this session)
- [x] **Goody Huts** — tile improvement reusing ruins reward system, spawns at map generation
- [x] **Aircraft Factory** — added to Aerodrome district (Building JSON)
- [x] **Grand Canyon** — Natural Wonder added to Terrains.json
- [x] **Nile River** — Natural Wonder added to Terrains.json

### Build Environment
- [x] Gradle JDK configuration: set `org.gradle.java.home` to JDK 21 in `gradle.properties`
- [x] World Congress / Diplomatic Victory (already fully implemented in prior sessions)
  (Resolves Kotlin compiler error with Java 25 version string "25.0.3")

---

## Changelog

See `changelog.md` for individual change history.
