package com.unciv.logic.map

import com.unciv.logic.city.City
import com.unciv.logic.map.tile.Tile
import com.unciv.models.ruleset.District
import com.unciv.models.stats.Stats
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestCase
import com.unciv.testing.TestGame
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class DistrictPillageTests {

    private lateinit var civInfo: com.unciv.logic.civilization.Civilization
    private lateinit var tileMap: TileMap
    private lateinit var city: City
    private lateinit var districtTile: Tile

    val testGame = TestGame()

    @Before
    fun initTheWorld() {
        testGame.makeHexagonalMap(3)
        tileMap = testGame.tileMap
        civInfo = testGame.addCiv()
        for (tech in testGame.ruleset.technologies.values)
            civInfo.tech.addTechnology(tech.name)
        city = testGame.addCity(civInfo, tileMap[0, 0])
        // Pick an owned, non-center tile for the district
        districtTile = city.getTiles().first { !it.isCityCenter() }
        testGame.addTileToCity(city, districtTile)
    }

    private fun placeDistrict(stats: Stats): District {
        val district = testGame.createDistrict(
            "[+${stats.science.toInt()}] [Science] [in this city]"
        )
        district.name = "Test District"
        district.cost = 10
        testGame.ruleset.districts[district.name] = district
        city.districts[districtTile.position] = district.name
        districtTile.district = district.name
        districtTile.setOwningCity(city)
        city.cityStats.update()
        return district
    }

    @Test
    fun districtCanBePillagedAndRepaired() {
        placeDistrict(Stats(science = 3f))

        // Sanity: before pillage the district yields science
        Assert.assertTrue(districtTile.canPillageDistrict())
        Assert.assertFalse(districtTile.districtIsPillaged)
        Assert.assertTrue(districtTile.getDistrict() != null)

        // Pillage
        districtTile.setPillaged()
        Assert.assertTrue(districtTile.districtIsPillaged)
        Assert.assertNull(districtTile.getDistrict()) // pillaged district no longer contributes
        Assert.assertTrue(districtTile.isPillaged())

        // Repair
        districtTile.setRepaired()
        Assert.assertFalse(districtTile.districtIsPillaged)
        Assert.assertNotNull(districtTile.getDistrict())
    }

    @Test
    fun pillagedDistrictExcludedFromYieldLoop() {
        placeDistrict(Stats(science = 4f))
        // The district is present
        Assert.assertEquals(1, city.getDistricts().count())
        Assert.assertNotNull(districtTile.getDistrict())

        // While pillaged, getDistrict() returns null so the yield loop skips it,
        // and the district can no longer be pillaged again until repaired.
        districtTile.setPillaged()
        Assert.assertNull(districtTile.getDistrict())
        Assert.assertTrue(districtTile.isPillaged())
        Assert.assertFalse(districtTile.canPillageDistrict())

        // Repair restores access
        districtTile.setRepaired()
        Assert.assertNotNull(districtTile.getDistrict())
        Assert.assertFalse(districtTile.isPillaged())
    }
}
