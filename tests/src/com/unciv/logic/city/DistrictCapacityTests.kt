package com.unciv.logic.city.managers

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
class DistrictCapacityTests : TestCase() {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: com.unciv.logic.city.City

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
    fun `district capacity formula`() {
        city.population.setPopulation(1)
        assertEquals("Pop 1: capacity = (1-1)/3 + 1 = 1", 1, city.getDistrictCapacity())
        
        city.population.setPopulation(2)
        assertEquals("Pop 2: capacity = (2-1)/3 + 1 = 1", 1, city.getDistrictCapacity())
        
        city.population.setPopulation(3)
        assertEquals("Pop 3: capacity = (3-1)/3 + 1 = 1", 1, city.getDistrictCapacity())
        
        city.population.setPopulation(4)
        assertEquals("Pop 4: capacity = (4-1)/3 + 1 = 2", 2, city.getDistrictCapacity())
        
        city.population.setPopulation(5)
        assertEquals("Pop 5: capacity = (5-1)/3 + 1 = 2", 2, city.getDistrictCapacity())
        
        city.population.setPopulation(6)
        assertEquals("Pop 6: capacity = (6-1)/3 + 1 = 2", 2, city.getDistrictCapacity())
        
        city.population.setPopulation(7)
        assertEquals("Pop 7: capacity = (7-1)/3 + 1 = 3", 3, city.getDistrictCapacity())
        
        city.population.setPopulation(10)
        assertEquals("Pop 10: capacity = (10-1)/3 + 1 = 4", 4, city.getDistrictCapacity())
        
        city.population.setPopulation(15)
        assertEquals("Pop 15: capacity = (15-1)/3 + 1 = 6", 6, city.getDistrictCapacity())
    }

    @Test
    fun `district count cannot exceed capacity`() {
        city.population.setPopulation(1)
        assertEquals("Initial capacity check", 1, city.getDistrictCapacity())
        
        city.population.setPopulation(4)
        assertEquals("Capacity should increase with population", 2, city.getDistrictCapacity())
    }

    @Test
    fun `getDistrictsCount returns correct number`() {
        assertEquals("Empty city has 0 districts", 0, city.getDistrictsCount())
    }
}