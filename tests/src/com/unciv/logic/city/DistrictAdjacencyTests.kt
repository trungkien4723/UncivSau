package com.unciv.logic.city

import com.unciv.logic.map.tile.Tile
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.stats.Stats
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that every standard Civ 6 district's adjacency bonus is defined and executes
 * with the correct yield value. Guards against the "dead unique" bug where adjacency
 * uniques with a trailing word ("for each adjacent [X] district") fail to parse and
 * silently contribute nothing.
 */
@RunWith(GdxTestRunner::class)
class DistrictAdjacencyTests {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: com.unciv.logic.city.City
    private lateinit var centerTile: Tile

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(3)
        civ = testGame.addCiv()
        for (tech in testGame.ruleset.technologies.values)
            civ.tech.addTechnology(tech.name)
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
        centerTile = city.getCenterTile()
    }

    private fun placeDistrict(tile: Tile, name: String) {
        tile.setOwningCity(city)
        city.tiles.add(tile.position)
        city.districts[tile.position] = name
        tile.district = name
    }

    private fun adjacencyOf(tile: Tile): Stats =
        city.cityStats.getDistrictAdjacencyStats(tile, city.getDistrictAt(tile)!!)

    /** A tile at distance 2 from the city center - no City Center adjacency pollutes the results. */
    private fun farTile(): Tile = testGame.tileMap[2, 0]

    @Test
    fun allAdjacencyUniquesParseToStatsForAdjacentDistrict() {
        for (district in testGame.ruleset.districts.values) {
            for (text in district.uniques) {
                if (text.contains("for each adjacent")) {
                    val unique = Unique(text)
                    assertTrue(
                        "District [$district] adjacency unique does not parse: '$text'",
                        unique.type == UniqueType.StatsForAdjacentDistrict
                    )
                }
            }
        }
    }

    @Test
    fun campusGetsStandardAdjacency() {
        val campusTile = farTile()
        placeDistrict(campusTile, "Campus")
        val neighbors = campusTile.neighbors.toList()
        testGame.setTileTerrain(neighbors[0].position, "Mountain")
        testGame.setTileFeatures(neighbors[1].position, "Jungle")
        placeDistrict(neighbors[2], "Government Plaza")

        val adjacency = adjacencyOf(campusTile)
        // Mountain +1, Jungle +0.5, Government Plaza +1 (giver) + 0.5 (as a district)
        assertEquals(3.0f, adjacency.science, 0.001f)
        assertEquals(0f, adjacency.culture, 0.001f)
    }

    @Test
    fun holySiteGetsStandardAdjacency() {
        val holySiteTile = farTile()
        placeDistrict(holySiteTile, "Holy Site")
        val neighbors = holySiteTile.neighbors.toList()
        testGame.setTileTerrain(neighbors[0].position, "Mountain")
        testGame.setTileFeatures(neighbors[1].position, "Forest")
        testGame.setTileTerrain(neighbors[2].position, "Yosemite")
        placeDistrict(neighbors[3], "Government Plaza")

        val adjacency = adjacencyOf(holySiteTile)
        // Mountain +1, Forest +0.5, Natural Wonder +2, Government Plaza +1 (giver) + 0.5 (as a district)
        assertEquals(5.0f, adjacency.faith, 0.001f)
    }

    @Test
    fun commercialHubGetsStandardAdjacency() {
        val hubTile = farTile()
        placeDistrict(hubTile, "Commercial Hub")
        val neighbors = hubTile.neighbors.toList()

        val riverNeighbor = neighbors[0]
        riverNeighbor.hasBottomRiver = true
        riverNeighbor.setTerrainTransients()
        placeDistrict(neighbors[1], "Harbor")
        placeDistrict(neighbors[2], "Government Plaza")

        val adjacency = adjacencyOf(hubTile)
        // River +2, Harbor +2 + 0.5 (as a district), Government Plaza +1 (giver) + 0.5 (as a district)
        assertEquals(6.0f, adjacency.gold, 0.001f)
    }

    @Test
    fun harborGetsStandardAdjacency() {
        val harborTile = farTile()
        placeDistrict(harborTile, "Harbor")
        val neighbors = harborTile.neighbors.toList()

        // Water (sea) resource +1
        val seaTile = neighbors[0]
        testGame.setTileTerrain(seaTile.position, "Coast")
        seaTile.tileResource = testGame.ruleset.tileResources["Fish"]
        seaTile.setTerrainTransients()

        // River neighbor must NOT grant gold (removed from standard Harbor adjacency)
        val riverNeighbor = neighbors[1]
        riverNeighbor.hasBottomRiver = true
        riverNeighbor.setTerrainTransients()

        // Commercial Hub neighbor grants only the 0.5 district bonus, not a Harbor bonus
        placeDistrict(neighbors[2], "Commercial Hub")

        placeDistrict(neighbors[3], "Government Plaza")

        val adjacency = adjacencyOf(harborTile)
        // Sea resource +1, river 0, Commercial Hub +0.5 (district), Government Plaza +1 (giver) + 0.5 (district)
        assertEquals(3.0f, adjacency.gold, 0.001f)
    }

    @Test
    fun harborGetsCityCenterAdjacency() {
        val harborTile = centerTile.neighbors.first()
        placeDistrict(harborTile, "Harbor")

        val adjacency = adjacencyOf(harborTile)
        // City Center +2 (special) + 0.5 (as a district)
        assertEquals(2.5f, adjacency.gold, 0.001f)
    }

    @Test
    fun industrialZoneGetsStandardAdjacency() {
        val izTile = farTile()
        placeDistrict(izTile, "Industrial Zone")
        val neighbors = izTile.neighbors.toList()

        placeDistrict(neighbors[0], "Aqueduct")

        val mineTile = neighbors[1]
        mineTile.improvement = "Mine"
        mineTile.setTerrainTransients()

        val strategicTile = neighbors[2]
        strategicTile.tileResource = testGame.ruleset.tileResources["Iron"]
        strategicTile.setTerrainTransients()

        placeDistrict(neighbors[3], "Government Plaza")

        val adjacency = adjacencyOf(izTile)
        // Aqueduct +2 + 0.5 (district), Mine +1, Strategic +1, Government Plaza +1 (giver) + 0.5 (district)
        assertEquals(6.0f, adjacency.production, 0.001f)
    }

    @Test
    fun industrialZoneIgnoresBonusResources() {
        val izTile = farTile()
        placeDistrict(izTile, "Industrial Zone")
        val neighbors = izTile.neighbors.toList()

        val bonusTile = neighbors[0]
        bonusTile.tileResource = testGame.ruleset.tileResources["Cattle"]
        bonusTile.setTerrainTransients()

        val adjacency = adjacencyOf(izTile)
        // No adjacency from bonus resources - must be zero with no other sources
        assertEquals(0f, adjacency.production, 0.001f)
    }

    @Test
    fun theaterSquareGetsStandardAdjacency() {
        val theaterTile = farTile()
        placeDistrict(theaterTile, "Theater Square")
        val neighbors = theaterTile.neighbors.toList()

        placeDistrict(neighbors[0], "Entertainment Complex")
        placeDistrict(neighbors[1], "Government Plaza")
        placeDistrict(neighbors[2], "Campus")

        val adjacency = adjacencyOf(theaterTile)
        // Entertainment Complex +2 + 0.5 (district), Government Plaza +1 (giver) + 0.5 (district),
        // Campus +0.5 (district)
        assertEquals(4.5f, adjacency.culture, 0.001f)
    }

    @Test
    fun theaterSquareGetsAdjacencyFromCityWithWonder() {
        val theaterTile = centerTile.neighbors.first()
        placeDistrict(theaterTile, "Theater Square")
        city.cityConstructions.addBuilding("Temple of Artemis")

        val adjacency = adjacencyOf(theaterTile)
        // Adjacent city center: +0.5 (as a district) + 1 (city has built a wonder)
        assertEquals(1.5f, adjacency.culture, 0.001f)
    }

    @Test
    fun theaterSquareGetsNoWonderBonusFromCityWithoutWonder() {
        val theaterTile = centerTile.neighbors.first()
        placeDistrict(theaterTile, "Theater Square")

        val adjacency = adjacencyOf(theaterTile)
        // Adjacent city center grants only the +0.5 district bonus, no wonder bonus
        assertEquals(0.5f, adjacency.culture, 0.001f)
    }

    @Test
    fun theaterSquareGetsAdjacencyFromForeignCityWithWonder() {
        val theaterTile = farTile()
        placeDistrict(theaterTile, "Theater Square")
        val otherCiv = testGame.addCiv()
        val wonderCityTile = theaterTile.neighbors.first { it.getCity() == null }
        testGame.addCity(otherCiv, wonderCityTile).cityConstructions.addBuilding("Temple of Artemis")

        val adjacency = adjacencyOf(theaterTile)
        // Adjacent foreign city center with a wonder: +0.5 (as a district) + 1 (has a wonder)
        assertEquals(1.5f, adjacency.culture, 0.001f)
    }

    @Test
    fun governmentPlazaGivesButDoesNotReceive() {
        val gpTile = farTile()
        placeDistrict(gpTile, "Government Plaza")
        val neighbors = gpTile.neighbors.toList()
        placeDistrict(neighbors[0], "Campus")
        placeDistrict(neighbors[1], "Industrial Zone")

        val adjacency = adjacencyOf(gpTile)
        // The Government Plaza is a giver, not a receiver - must have no adjacency of its own
        assertEquals(0f, adjacency.production, 0.001f)
        assertEquals(0f, adjacency.science, 0.001f)
        assertEquals(0f, adjacency.culture, 0.001f)
        assertEquals(0f, adjacency.faith, 0.001f)
        assertEquals(0f, adjacency.gold, 0.001f)
    }

    @Test
    fun encampmentHasNoAdjacency() {
        val encampmentTile = farTile()
        placeDistrict(encampmentTile, "Encampment")
        val neighbors = encampmentTile.neighbors.toList()

        val bonusTile = neighbors[0]
        bonusTile.tileResource = testGame.ruleset.tileResources["Cattle"]
        bonusTile.setTerrainTransients()
        placeDistrict(neighbors[1], "Campus")

        val adjacency = adjacencyOf(encampmentTile)
        assertEquals(0f, adjacency.production, 0.001f)
    }

    @Test
    fun campusAdjacentToGovernmentPlazaGetsGiverBonus() {
        val campusTile = farTile()
        placeDistrict(campusTile, "Campus")
        val neighbors = campusTile.neighbors.toList()
        placeDistrict(neighbors[0], "Government Plaza")

        val adjacency = adjacencyOf(campusTile)
        // Government Plaza +1 (giver) + 0.5 (as a district)
        assertEquals(1.5f, adjacency.science, 0.001f)
    }
}
