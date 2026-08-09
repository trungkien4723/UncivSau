package com.unciv.logic.map

import com.unciv.logic.map.tile.RoadStatus
import com.unciv.logic.map.tile.Tile
import com.unciv.models.UnitActionType
import com.unciv.models.ruleset.unique.GameContext
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import com.unciv.ui.screens.worldscreen.unit.actions.UnitActions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class RoadImprovementTests {

    private val testGame = TestGame()
    val tileMap get() = testGame.tileMap

    private fun makeLandTile(x: Int, y: Int): Tile {
        val tile = tileMap[x, y]
        tile.baseTerrain = "Plains"
        tile.setTransients()
        return tile
    }

    private fun addCivWithAllTechs() = testGame.addCiv().apply {
        for (techEntry in testGame.ruleset.technologies.values)
            tech.addTechnology(techEntry.name)
    }

    @Test
    fun builderCannotConstructRoadsOrRailroads() {
        testGame.makeHexagonalMap(2)
        val civ = addCivWithAllTechs()
        val tile = makeLandTile(1, 1)
        val builder = testGame.addUnit("Builder", civ, tile)

        val actions = UnitActions.getUnitActions(builder, UnitActionType.CreateImprovement).toList()
        val roadActions = actions.filter { it.title.contains("Road") || it.title.contains("Railroad") }
        assertTrue("Builder should not offer road-building actions: ${roadActions.map { it.title }}", roadActions.isEmpty())

        val filter = builder.getMatchingUniques(UniqueType.ConstructImprovementInstantly).first().params[0]
        val gameContext = GameContext(civInfo = civ, unit = builder, tile = tile)
        assertFalse(testGame.ruleset.tileImprovements["Road"]!!.matchesFilter(filter, gameContext))
        assertFalse(testGame.ruleset.tileImprovements["Railroad"]!!.matchesFilter(filter, gameContext))
    }

    @Test
    fun builderHasNoGenericImprovementPicker() {
        testGame.makeHexagonalMap(2)
        val civ = addCivWithAllTechs()
        val tile = makeLandTile(1, 1)
        val builder = testGame.addUnit("Builder", civ, tile)

        val pickerActions = UnitActions.getUnitActions(builder, UnitActionType.ConstructImprovement).toList()
        assertTrue("Builder should not offer the generic improvement picker: ${pickerActions.map { it.title }}", pickerActions.isEmpty())

        val createActions = UnitActions.getUnitActions(builder, UnitActionType.CreateImprovement).toList()
        assertTrue("Builder should still offer per-tile Create actions, got: ${createActions.map { it.title }}", createActions.isNotEmpty())
    }

    @Test
    fun militaryEngineerCanConstructRoads() {
        testGame.makeHexagonalMap(2)
        val civ = addCivWithAllTechs()
        val tile = makeLandTile(1, 1)
        val engineer = testGame.addUnit("Military Engineer", civ, tile)

        val actions = UnitActions.getUnitActions(engineer, UnitActionType.CreateImprovement).toList()
        val roadAction = actions.firstOrNull { it.title.contains("Road") && !it.title.contains("Railroad") }
        assertNotNull("Military Engineer should be able to build roads, got: ${actions.map { it.title }}", roadAction)
        val invoke = roadAction!!.action
        assertNotNull("Road action should be enabled", invoke)
        assertEquals(RoadStatus.None, tile.roadStatus)
        invoke!!.invoke()
        assertEquals(RoadStatus.Road, tile.roadStatus)
    }

    @Test
    fun traderPavesRoadsAlongEstablishedRoute() {
        testGame.makeHexagonalMap(2)
        val civ = addCivWithAllTechs()
        val sourceTile = makeLandTile(0, 0)
        val middleTile = makeLandTile(1, 0)
        val destTile = makeLandTile(2, 0)
        val sourceCity = testGame.addCity(civ, sourceTile)
        val destCity = testGame.addCity(civ, destTile)
        val trader = testGame.addUnit("Trader", civ, destTile)
        trader.currentMovement = trader.getMaxMovement().toFloat()

        assertEquals(RoadStatus.None, middleTile.roadStatus)

        val actions = UnitActions.getUnitActions(trader, UnitActionType.CreateTradeRoute).toList()
        val routeAction = actions.firstOrNull { it.action != null }
        assertNotNull("Trader should offer a trade route action, got: ${actions.map { it.title }}", routeAction)
        routeAction!!.action!!.invoke()

        assertEquals(destCity.name, sourceCity.tradeRoutes.domesticRouteTo)
        assertNotEquals("Trader should pave a road along the route", RoadStatus.None, middleTile.roadStatus)
    }
}
