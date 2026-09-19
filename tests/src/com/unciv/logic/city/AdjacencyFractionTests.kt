package com.unciv.logic.city

import com.unciv.logic.map.tile.Tile
import com.unciv.models.stats.Stats
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class AdjacencyFractionTests {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: com.unciv.logic.city.City
    private var counter = 0

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(3)
        civ = testGame.addCiv()
        for (tech in testGame.ruleset.technologies.values)
            civ.tech.addTechnology(tech.name)
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
    }

    /** Places [unique] district on the first tile adjacent to the city center and returns that tile. */
    private fun placeHalfDistrictBesideCityCenter(unique: String): Tile {
        val district = testGame.createDistrict(unique)
        district.name = "Adjacency District ${counter++}"
        district.cost = 10
        testGame.ruleset.districts[district.name] = district

        val centerTile = city.getCenterTile()
        val tile = centerTile.neighbors.first()
        city.tiles.add(tile.position)
        city.districts[tile.position] = district.name
        tile.district = district.name
        tile.setOwningCity(city)
        return tile
    }

    @Test
    fun fractionalAdjacencyFromCityCenter() {
        // Civ VI: "+0.5 Science for each adjacent District" — a district next to the
        // City Center (which counts as a district) must get half a Science.
        val tile = placeHalfDistrictBesideCityCenter("[+0.5 Science] for each adjacent [District]")
        val district = city.getDistrictAt(tile)!!
        val adjacency = city.cityStats.getDistrictAdjacencyStats(tile, district)
        assertEquals("City Center adjacent should give 0.5", 0.5f, adjacency.science, 0.001f)
    }

    @Test
    fun fractionalAdjacencyFromTwoDistricts() {
        // Two adjacent district tiles both yield their half for the computed total.
        val tile = placeHalfDistrictBesideCityCenter("[+0.5 Science] for each adjacent [District]")
        val district = city.getDistrictAt(tile)!!

        // Place a second district adjacent to the first one's tile
        val secondTile = tile.neighbors.first { it != city.getCenterTile() && it.district == null }
        val second = testGame.createDistrict("[+1 Science] [in this city]")
        second.name = "Second District ${counter++}"
        second.cost = 10
        testGame.ruleset.districts[second.name] = second
        city.tiles.add(secondTile.position)
        city.districts[secondTile.position] = second.name
        secondTile.district = second.name
        secondTile.setOwningCity(city)

        // Now the first district is adjacent to City Center AND the second district => 0.5 * 2 = 1.0
        val adjacency = city.cityStats.getDistrictAdjacencyStats(tile, district)
        assertEquals("Two adjacent districts should give 1.0 total", 1.0f, adjacency.science, 0.001f)
    }

    @Test
    fun wholeNumberAdjacencyUnchanged() {
        val tile = placeHalfDistrictBesideCityCenter("[+2 Gold] for each adjacent [District]")
        val district = city.getDistrictAt(tile)!!
        val adjacency = city.cityStats.getDistrictAdjacencyStats(tile, district)
        assertEquals("City Center adjacent should give 2 gold", 2f, adjacency.gold, 0.001f)
        assertEquals("No science should be produced", 0f, adjacency.science, 0.001f)
    }
}