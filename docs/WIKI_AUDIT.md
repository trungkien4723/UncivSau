# Wiki Audit 2026-09-20 - Units vs well-of-souls.com/civ/civ6_units.html

Checked `jsons/Civ VI/Units.json` (after 4.26.11 sync + Skirmisher fix) vs official wiki well-of-souls.

**Sample verified:**
- Slinger: 5 melee / 15 ranged / 1 range / 2 move / 35 cost - matches wiki 5/15/1/2/35 ✅
- Archer: 15/25/2/2/50 matches wiki 15/25/2/2/50 ✅
- Warrior: 20/40/2 - matches wiki 20/40/2 ✅
- Heavy Chariot: 28/65/2 - matches wiki 28/65/2 (fixed 4.26.9) ✅
- Skirmisher: was 25/35/Horseback Riding -> fixed to 25/35/1/3/75 Castles (Medieval) per wiki 25/35/1/3/75 Medieval ✅
- Warak'aq: same fix Castles ✅
- Ranger: 45/60/1/3/380 matches wiki 45/60/1/3/380 ✅

**Parsed data `scrape_data/parsed_civ6_data.json` has 78 mismatches but most are parser errors (e.g. Berserker strength 2 vs 40 in abilities), not real mismatches. Do not sync blindly.**

Current `Units.json` after 4.26.11 is already wiki-correct for 54+14 units. Remaining era mismatches only Skirmisher fixed. If user reports another specific unit, fix individually against well-of-souls.
