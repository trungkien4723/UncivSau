package com.unciv.logic.city

import com.unciv.models.metadata.BaseRuleset
import com.unciv.models.ruleset.RulesetCache
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestCase
import com.unciv.testing.TestGame
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class HousingTests : TestCase() {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: City

    @Before
    fun setUp() {
        RulesetCache.loadRulesets(noMods = true)
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!
        testGame.makeHexagonalMap(3)
        testGame.ruleset
        civ = testGame.addCiv()
        for (tech in ruleset.technologies.values)
            civ.tech.addTechnology(tech.name)
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
    }

    @Test
    fun `coastal city has 3 housing`() {
        val coastalTile = testGame.tileMap.getTilesInDistance(1).first { it.isCoastal() }
        testGame.addTileToCity(city, coastalTile)
        val housing = city.getAvailableHousing()
        assertTrue("Coastal city should have 3 housing from terrain", housing >= 3)
    }

    @Test
    fun `fresh water city has 5 housing`() {
        val riverTile = testGame.tileMap.getTilesInDistance(1).first { it.isAdjacentToRiver() }
        testGame.addTileToCity(city, riverTile)
        val housing = city.getAvailableHousing()
        assertTrue("Fresh water city should have 5 housing from terrain", housing >= 5)
    }

    @Test
    fun `inland city has 2 housing`() {
        val inlandTile = testGame.tileMap.getTilesInDistance(1).first { 
            !it.isCoastal() && !it.isAdjacentToRiver() 
        }
        testGame.addTileToCity(city, inlandTile)
        val housing = city.getAvailableHousing()
        assertTrue("Inland city should have 2 housing from terrain", housing >= 2)
    }

    @Test
    fun `district capacity increases with population`() {
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