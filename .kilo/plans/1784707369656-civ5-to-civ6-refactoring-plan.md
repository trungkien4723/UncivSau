# Civ 5 to Civ 6 Refactoring Plan

## Overview

This plan outlines the complete refactoring of Unciv from Civilization V mechanics to Civilization VI mechanics. The project currently has a hybrid of both systems.

## Current State Analysis

### Already Implemented Civ VI Mechanics
- District model (`District.kt`) with placement, adjacency bonuses
- Buildings requiring specific districts (`Building.district`)
- Governors (`Governor.kt`) with loyalty bonuses
- Loyalty system (`CityLoyaltyManager.kt`)
- Policy Cards (`PolicyCard.kt`)
- Governments (`Government.kt`) with slot types
- Civics (`Civic.kt`)

### Civ V Mechanics to Remove
1. **Global Happiness System** - Replace with city-level Amenities
2. **Policy Branches** - Replace with Civ VI policy cards in governments
3. **Building Maintenance** - Remove gold maintenance costs
4. **Annex/Puppet Mechanics** - Simplify city capture
5. **Wonder Replacement Buildings** - Remove
6. **We Love The King Day** - Remove
7. **Trade Routes (Civ V style)** - Replace with Civ VI trade routes

## Phase 1: Population, Housing, and Amenities

### 1.1 Replace Happiness with Housing/Amenities

**Files to modify:**
- `core/src/com/unciv/logic/city/CityStats.kt`
- `core/src/com/unciv/logic/city/managers/CityLoyaltyManager.kt`
- `core/src/com/unciv/models/stats/Stat.kt`

**Changes:**
- Replace `happiness` stat with `housing` and `amenities`
- City growth requires both Food AND Housing
- Housing acts as soft cap, Amenities prevent growth penalties
- Remove global unhappiness calculations

### 1.2 Implement Housing Sources

**Files to modify:**
- `android/assets/jsons/Civ VI/Buildings.json`
- `core/src/com/unciv/models/ruleset/Buildings.json` (if exists)

**Add Housing from:**
- Fresh water placement (River/Lake/Oasis = 5, Coast = 3, Other = 2)
- Granary (+2 Housing)
- Aqueduct (+2 Housing, or sets to 6 if no fresh water)
- Neighborhood district (2-6 Housing based on Appeal)
- Lighthouses (+2 Housing if coastal)
- Policy cards (Insulae, Medina Quarter, etc.)

## Phase 2: District Population Limits

### 2.1 Implement District Carrying Capacity

**Files to modify:**
- `core/src/com/unciv/logic/city/City.kt`
- `core/src/com/unciv/logic/city/CityConstructions.kt`

**Changes:**
- 1 population = 1 district slot
- 4 population = 2 district slots
- 7 population = 3 district slots
- etc. (every 3 additional citizens = 1 more district)

### 2.2 Remove District Tile Yields

**Files to modify:**
- `core/src/com/unciv/logic/map/tile/Tile.kt`
- `core/src/com/unciv/logic/map/tile/TileStatFunctions.kt`

**Changes:**
- District tiles should not provide terrain yields
- District yields only come from adjacency bonuses and buildings

## Phase 3: Trade Routes (Civ VI Style)

### 3.1 Replace Civ V Trade Routes

**Files to modify:**
- `core/src/com/unciv/logic/city/CityStats.kt`
- `core/src/com/unciv/logic/city/CityResources.kt`
- `core/src/com/unciv/models/stats/Stat.kt`

**Changes:**
- Each city can have 1 domestic trade route (later +1 per policy card)
- International trade routes per civilization
- Routes provide Gold and Science based on destination city size
- Remove Civ V internal trade route formula

## Phase 4: Remove Civ V Mechanics

### 4.1 Happiness System Removal

**Files to modify:**
- `core/src/com/unciv/logic/civilization/Civilization.kt`
- `core/src/com/unciv/logic/city/CityStats.kt`
- `core/src/com/unciv/logic/automation/Automation.kt`
- `core/src/com/unciv/logic/automation/city/ConstructionAutomation.kt`
- `core/src/com/unciv/logic/automation/civilization/NextTurnAutomation.kt`
- `core/src/com/unciv/logic/civilization/managers/TurnManager.kt`

**Remove:**
- `getHappiness()` function
- Happiness-based growth modifiers
- Unhappiness from cities/population
- We Love The King Day mechanics
- Happiness-based AI decisions

### 4.2 Policy Branches Removal

**Files to modify:**
- `core/src/com/unciv/models/ruleset/Policy.kt`
- `core/src/com/unciv/models/ruleset/PolicyBranch.kt`
- `core/src/com/unciv/models/ruleset/PolicyColumn.kt`
- `core/src/com/unciv/logic/civilization/Civilization.kt` (policy adoption)

**Keep:**
- Policy cards (Civ VI style)
- Governments with slot types
- Civics tree

### 4.3 Building Maintenance Removal

**Files to modify:**
- `core/src/com/unciv/models/ruleset/Building.kt`
- `core/src/com/unciv/logic/city/CityConstructions.kt`
- `core/src/com/unciv/logic/city/CityStats.kt`

**Changes:**
- Remove `maintenance` field from buildings
- Remove maintenance cost calculations
- Remove gold upkeep from city stats

### 4.4 Annex/Puppet Simplification

**Files to modify:**
- `core/src/com/unciv/logic/city/managers/CityConquestFunctions.kt`
- `core/src/com/unciv/logic/city/City.kt`

**Changes:**
- Cities are either owned or not (no puppet/annex states)
- Capture = immediate ownership transfer
- Remove resistance mechanics

### 4.5 Wonder Replacement Removal

**Files to modify:**
- `core/src/com/unciv/models/ruleset/Building.kt`
- `android/assets/jsons/Civ VI/Buildings.json`

**Changes:**
- Remove `replaces` and `uniqueTo` fields for buildings
- Buildings don't replace each other

## Phase 5: UI Updates

### 5.1 City Screen Updates

**Files to modify:**
- `core/src/com/unciv/ui/screens/cityscreen/CityScreen.kt`
- `core/src/com/unciv/ui/screens/cityscreen/CityScreenConstructionMenu.kt`
- `core/src/com/unciv/ui/screens/worldscreen/topbar/WorldScreenTopBarStats.kt`

**Changes:**
- Show Housing/Amenities instead of Happiness
- Show district capacity based on population
- Update construction queue for Civ VI policies

### 5.2 Diplomacy Screen Updates

**Files to modify:**
- `core/src/com/unciv/ui/screens/diplomacy/DiplomacyScreen.kt`

**Changes:**
- Remove annex/puppet options
- Update war/peace treaty mechanics

## Phase 6: AI Updates

### 6.1 District Placement AI

**Files to modify:**
- `core/src/com/unciv/logic/automation/Automation.kt`

**Changes:**
- Improve district adjacency bonus calculations
- Add priority for different district types based on city focus

### 6.2 Population Management AI

**Files to modify:**
- `core/src/com/unciv/logic/automation/city/ConstructionAutomation.kt`

**Changes:**
- Consider Housing limits when assigning population
- Prioritize districts based on available housing

## Phase 7: Testing

### 7.1 Update Existing Tests

**Files to modify:**
- `tests/src/com/unciv/logic/city/LoyaltyTests.kt`
- `tests/src/com/unciv/logic/city/managers/CivVIDistrictIntegrationTest.kt`

### 7.2 Add New Tests

**New test files:**
- `tests/src/com/unciv/logic/city/HousingTests.kt`
- `tests/src/com/unciv/logic/city/AmenitiesTests.kt`
- `tests/src/com/unciv/logic/city/DistrictCapacityTests.kt`

## Implementation Order

1. **Phase 1** - Population/Housing/Amenities (Foundation)
2. **Phase 2** - District Population Limits
3. **Phase 4** - Remove Civ V Mechanics (in parallel with above)
4. **Phase 3** - Trade Routes
5. **Phase 5** - UI Updates
6. **Phase 6** - AI Updates
7. **Phase 7** - Testing

## Key Technical Decisions

### District Tile Yields
- District tiles remove terrain yields
- Adjacency bonuses provide yields instead
- City Center is always worked and provides minimum 2 Food, 1 Production

### Housing Calculation
- Fresh water: 5 Housing
- Coast: 3 Housing  
- Other: 2 Housing
- Population growth stops at 5+ over Housing limit

### Amenities Calculation
- First 2 population: 0 required
- Each 2 population beyond 2: +1 required Amenity
- Growth bonus at +3 Amenities surplus

### Trade Routes
- Each city: 1 domestic route
- Each civ: 1 international route per other civ (max 6 with 7 civs)
- Routes provide Gold (domestic) and Gold + Science (international)

## Files Summary

### Core Logic Files
- `City.kt`, `CityStats.kt`, `CityConstructions.kt`, `CityResources.kt`
- `CityLoyaltyManager.kt`, `CityPopulationManager.kt`
- `District.kt`, `Building.kt`, `Governor.kt`, `Government.kt`, `PolicyCard.kt`
- `Civilization.kt`, `TurnManager.kt`
- `Automation.kt`, `ConstructionAutomation.kt`, `NextTurnAutomation.kt`

### UI Files
- `CityScreen.kt`, `CityScreenConstructionMenu.kt`
- `WorldScreenTopBarStats.kt`, `StatsOverviewTab.kt`
- `DiplomacyScreen.kt`

### Test Files
- `LoyaltyTests.kt`, `CivVIDistrictIntegrationTest.kt`
- New tests for Housing, Amenities, District Capacity

## Validation Plan

1. Run all existing tests after each phase
2. Manual playtesting for:
   - City growth with Housing limits
   - District placement and adjacency bonuses
   - Governor assignments and loyalty
   - Trade route mechanics
   - Policy card selection

3. Verify no Civ V mechanics remain active in Civ VI ruleset