package com.unciv.logic.city

import com.unciv.Constants
import com.unciv.logic.map.tile.TileAppeal
import com.unciv.models.metadata.BaseRuleset
import com.unciv.models.ruleset.RulesetCache
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class HousingTests {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: City

    @Before
    fun setUp() {
        RulesetCache.loadRulesets(noMods = true)
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!
        testGame.makeHexagonalMap(3)
        civ = testGame.addCiv()
        for (tech in ruleset.technologies.values)
            civ.tech.addTechnology(tech.name)
    }

    private fun foundCity(): City {
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
        return city
    }

    @Test
    fun `coastal city has 3 housing`() {
        // Set the terrain before founding: the coast adjacency transient is a lazy that is never invalidated
        testGame.setTileTerrain(testGame.getTile(1, 0).position, Constants.coast)
        foundCity()

        assertTrue("City should be adjacent to coast", city.getCenterTile().isAdjacentToCoast())
        assertEquals("Coastal city should have 3 housing from terrain", 3, city.getAvailableHousing())
    }

    @Test
    fun `fresh water city has 5 housing`() {
        val center = testGame.getTile(0, 0)
        center.setConnectedByRiver(testGame.getTile(1, 0), true)
        foundCity()

        assertTrue("City should be adjacent to fresh water", city.getCenterTile().isAdjacentTo(Constants.freshWater))
        assertEquals("Fresh water city should have 5 housing from terrain", 5, city.getAvailableHousing())
    }

    @Test
    fun `inland city has 2 housing`() {
        foundCity()

        assertFalse("City should not be adjacent to coast", city.getCenterTile().isAdjacentToCoast())
        assertFalse("City should not be adjacent to fresh water", city.getCenterTile().isAdjacentTo(Constants.freshWater))
        assertEquals("Inland city should have 2 housing from terrain", 2, city.getAvailableHousing())
    }

    @Test
    fun `district capacity increases with population`() {
        foundCity()
        city.population.setPopulation(1)
        assertEquals("Population 1 should allow 1 district", 1, city.getDistrictCapacity())
        
        city.population.setPopulation(4)
        assertEquals("Population 4 should allow 2 districts", 2, city.getDistrictCapacity())
        
        city.population.setPopulation(7)
        assertEquals("Population 7 should allow 3 districts", 3, city.getDistrictCapacity())
        
        city.population.setPopulation(10)
        assertEquals("Population 10 should allow 4 districts", 4, city.getDistrictCapacity())
    }

    @Test
    fun `Neighborhood housing scales with tile appeal`() {
        foundCity()
        val districtTile = city.getTiles().first { !it.isCityCenter() }
        testGame.addTileToCity(city, districtTile)

        val districtName = "Neighborhood Appeal Test"
        val district = testGame.createDistrict()
        district.name = districtName
        district.cost = 10
        testGame.ruleset.districts[districtName] = district

        val buildingName = "Neighborhood Building Test"
        val building = testGame.createBuilding("Housing based on tile appeal")
        building.name = buildingName
        building.housing = 2f
        building.district = districtName
        testGame.ruleset.buildings[buildingName] = building

        city.districts[districtTile.position] = districtName
        districtTile.district = districtName
        districtTile.setOwningCity(city)

        // Low appeal tile: base 2 housing
        city.cityConstructions.addBuilding(building, tryAddFreeBuildings = false)
        city.cityStats.update()
        assertEquals("Low appeal tile should give base 2 housing", 2, city.getAvailableHousing() - baseHousing())

        // High appeal tile: forest + jungle features give +1 each, expect Charming/Breathtaking housing
        testGame.setTileFeatures(districtTile.position, "Forest", "Jungle")
        val appeal = TileAppeal.getAppeal(districtTile, civ)
        assertTrue("Appeal should be positive", appeal >= 2)
        city.cityStats.update()
        val expected = when {
            appeal >= 4 -> 4
            else -> 3
        }
        assertEquals("Appeal-based housing should apply", expected, city.getAvailableHousing() - baseHousing())
    }

    private fun baseHousing(): Int {
        val terrain = if (city.isCoastal()) 3 else if (city.getCenterTile().isAdjacentTo(Constants.freshWater)) 5 else 2
        var housing = terrain
        for (building in city.cityConstructions.getBuiltBuildings())
            if (building.housing > 0 && building.getMatchingUniques(UniqueType.Civ6HousingBasedOnTileAppeal).firstOrNull() == null)
                housing += building.housing.toInt()
        return housing
    }
}