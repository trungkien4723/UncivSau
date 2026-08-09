package com.unciv.logic.automation

import com.unciv.logic.city.City
import com.unciv.models.ruleset.District
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class AutomationDistrictRankingTests {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: City
    private var counter = 0

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(3)
        civ = testGame.addCiv()
        for (tech in testGame.ruleset.technologies.values)
            civ.tech.addTechnology(tech.name)
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
    }

    private fun createDistrict(vararg uniques: String): District {
        val district = testGame.createDistrict(*uniques)
        district.name = "District ${counter++}"
        district.cost = 10
        testGame.ruleset.districts[district.name] = district
        return district
    }

    @Test
    fun rankDistrictValueIsZeroWithoutValidTile() {
        val district = createDistrict("[+1 Science] [in this city]")
        city.tiles.clear()
        // No tiles in the city besides the (excluded) city center
        val value = Automation.rankDistrictValue(city, district)
        assertEquals(0f, value, 0.001f)
    }

    @Test
    fun rankDistrictValueRewardsBaseYields() {
        val district = createDistrict("[+1 Science] [in this city]")
        val center = city.getCenterTile()
        city.tiles.addAll(center.neighbors.map { it.position })

        val value = Automation.rankDistrictValue(city, district)
        assertTrue("District with base science yield should rank above zero, was $value", value > 0f)
    }

    @Test
    fun rankDistrictValueRewardsAdjacency() {
        val district = createDistrict("[+2 Science] for each adjacent [Jungle]")
        val center = city.getCenterTile()
        val neighborTiles = center.neighbors.toList()
        city.tiles.addAll(neighborTiles.map { it.position })

        // Jungle on the last neighbor so remaining candidate tiles are adjacent to it
        val jungleTile = neighborTiles.last()
        testGame.setTileFeatures(jungleTile.position, "Jungle")
        for (tile in neighborTiles) tile.setTerrainTransients()

        val value = Automation.rankDistrictValue(city, district)
        assertTrue("Adjacency-bearing district should rank above base-only value", value > 1f)
        val baseValue = Automation.rankDistrictValue(city, createDistrict("[+1 Science] [in this city]"))
        assertTrue("Adjacency should raise ranking above a plain science district", value > baseValue)
    }
}