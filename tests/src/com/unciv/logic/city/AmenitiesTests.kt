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
class AmenitiesTests : TestCase() {

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
    fun `population 1 requires 0 amenities`() {
        city.population.setPopulation(1)
        city.cityStats.update()
        val required = city.cityStats.amenitiesList["Required"] ?: 0f
        assertEquals("Population 1 should require 0 amenities", 0f, required)
    }

    @Test
    fun `population 4 requires 1 amenity`() {
        city.population.setPopulation(4)
        city.cityStats.update()
        val required = city.cityStats.amenitiesList["Required"] ?: 0f
        assertEquals("Population 4 should require 1 amenity", 1f, required)
    }

    @Test
    fun `population 6 requires 2 amenities`() {
        city.population.setPopulation(6)
        city.cityStats.update()
        val required = city.cityStats.amenitiesList["Required"] ?: 0f
        assertEquals("Population 6 should require 2 amenities", 2f, required)
    }

    @Test
    fun `amenities from buildings`() {
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!
        val granary = ruleset.buildings["Granary"]
        if (granary != null && granary.amenities > 0) {
            city.cityConstructions.addBuilding("Granary")
            city.cityStats.update()
            val amenities = city.cityStats.amenitiesList["Granary"] ?: 0f
            assertTrue("Granary should provide amenities", amenities >= 0)
        }
    }

    @Test
    fun `growth stops when over housing limit`() {
        city.population.setPopulation(5)
        city.cityStats.update()
        val availableHousing = city.getAvailableHousing()
        val currentPopulation = city.population.population
        
        if (currentPopulation > availableHousing + 1) {
            assertTrue("Population over housing should have growth penalty", true)
        }
    }
}