package com.unciv.logic.city

import com.unciv.Constants
import com.unciv.models.metadata.BaseRuleset
import com.unciv.models.ruleset.RulesetCache
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
}