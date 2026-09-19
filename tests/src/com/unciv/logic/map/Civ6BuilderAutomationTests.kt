package com.unciv.logic.map

import com.unciv.logic.city.City
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.logic.map.tile.Tile
import com.unciv.models.UnitActionType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import com.unciv.ui.screens.worldscreen.unit.actions.UnitActions
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class Civ6BuilderAutomationTests {

    private val testGame = TestGame()

    private fun plainTile(x: Int, y: Int): Tile {
        val tile = testGame.tileMap[x, y]
        tile.baseTerrain = "Plains"
        tile.setTransients()
        return tile
    }

    private fun addCivWithAllTechs() = testGame.addCiv().apply {
        for (techEntry in testGame.ruleset.technologies.values)
            tech.addTechnology(techEntry.name)
    }

    private fun setUp(): Pair<Civilization, City> {
        testGame.makeHexagonalMap(3)
        val civ = addCivWithAllTechs()
        val cityTile = plainTile(0, 0)
        val city = testGame.addCity(civ, cityTile)
        city.population.addPopulation(3)
        for (t in testGame.tileMap.values)
            if (t.getOwner() == civ || t.owningCity == city) testGame.addTileToCity(city, t)
        return civ to city
    }

    private fun addBuilder(civ: Civilization, tile: Tile): MapUnit {
        val builder = testGame.addUnit("Builder", civ, tile)
        builder.currentMovement = builder.getMaxMovement().toFloat()
        return builder
    }

    private fun createActions(builder: MapUnit): List<String> =
        UnitActions.getUnitActions(builder, UnitActionType.CreateImprovement).toList().map { it.title }

    @Test
    fun builderOffersYieldImprovementsInOwnedCity() {
        val (civ, _) = setUp()
        val tile = plainTile(1, 0)
        val titles = createActions(addBuilder(civ, tile))
        println("PLAIN: $titles")
        assertTrue("Builder should offer a Farm on owned Plains, got: $titles",
            titles.any { it.contains("[Farm]") })
    }

    @Test
    fun builderRepairsPillagedDistrictWithACharge() {
        val (civ, city) = setUp()
        val target = plainTile(1, 0)
        testGame.addTileToCity(city, target)
        val district = testGame.createDistrict("[+1] [Science] [in this city]")
        district.name = "Test District"
        district.cost = 5
        testGame.ruleset.districts[district.name] = district
        target.district = district.name
        target.setPillaged()
        target.setTransients()

        val builder = addBuilder(civ, target)
        val titles = createActions(builder)
        println("PILLAGED: $titles")
        val repairAction = UnitActions.getUnitActions(builder, UnitActionType.CreateImprovement).toList()
            .firstOrNull { it.title.contains("Repair [", ignoreCase = true) }
        assertNotNull("Builder should offer a charge-based Repair on a pillaged district, got: $titles", repairAction)
        assertTrue(target.isPillaged())

        val invoke = repairAction!!.action
        assertNotNull("Repair should be available", invoke)
        invoke!!.invoke()

        assertFalse("Repairing should restore the district", target.isPillaged())
        assertFalse(target.districtIsPillaged)

        // The repair consumed the builder's charge, so a second repair is no longer offered.
        assertTrue("Repairing should consume the builder charge",
            createActions(builder).none { it.contains("Repair [", ignoreCase = true) })
    }

    @Test
    fun builderCanHarvestBonusResource() {
        val (civ, city) = setUp()
        val target = plainTile(1, 0)
        target.baseTerrain = "Grassland"
        target.setTerrainFeatures(listOf("Jungle"))
        val bananas = testGame.ruleset.tileResources["Bananas"]!!
        target.tileResource = bananas
        target.resourceAmount = 3
        testGame.addTileToCity(city, target)
        target.setTransients()

        val builder = addBuilder(civ, target)
        val actions = UnitActions.getUnitActions(builder, UnitActionType.HarvestResource).toList()
        val harvestAction = actions.firstOrNull { it.title.contains("Harvest [") }
        assertNotNull("Builder should offer a Harvest action on a Banana tile, got: ${actions.map { it.title }}", harvestAction)

        val foodBefore = city.population.foodStored
        val invoke = harvestAction!!.action
        assertNotNull("Harvest should be available", invoke)
        invoke!!.invoke()

        assertTrue("Harvesting should remove the resource", target.tileResource == null)
        assertTrue("Harvesting should grant Food to the city", city.population.foodStored > foodBefore)
    }
}