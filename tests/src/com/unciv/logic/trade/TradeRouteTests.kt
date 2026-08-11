package com.unciv.logic.trade

import com.unciv.logic.civilization.Civilization
import com.unciv.logic.map.tile.RoadStatus
import com.unciv.logic.map.tile.Tile
import com.unciv.models.UnitActionType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import com.unciv.ui.screens.worldscreen.unit.actions.UnitActions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class TradeRouteTests {

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

    private fun placeDistrict(city: com.unciv.logic.city.City, districtName: String, tile: Tile) {
        city.tiles.add(tile.position)
        val district = testGame.ruleset.districts[districtName]!!
        city.cityConstructions.buildDistrict(district, tile)
    }

    @Test
    fun establishingRouteCreatesTradingPostsAtBothEnds() {
        testGame.makeHexagonalMap(2)
        val civ = addCivWithAllTechs()
        val sourceTile = makeLandTile(0, 0)
        val destTile = makeLandTile(2, 0)
        val sourceCity = testGame.addCity(civ, sourceTile)
        val destCity = testGame.addCity(civ, destTile)
        val trader = testGame.addUnit("Trader", civ, destTile)
        trader.currentMovement = trader.getMaxMovement().toFloat()

        val actions = UnitActions.getUnitActions(trader, UnitActionType.CreateTradeRoute).toList()
        val routeAction = actions.firstOrNull { it.action != null }
        assertNotNull("Trader should offer a trade route action, got: ${actions.map { it.title }}", routeAction)
        routeAction!!.action!!.invoke()

        assertTrue("Trading post should be established at the source (capital) city", civ.tradingPosts.contains(sourceCity.name))
        assertTrue("Trading post should be established at the destination city", civ.tradingPosts.contains(destCity.name))
    }

    @Test
    fun domesticRouteYieldsFoodAndProductionFromDestinationDistricts() {
        testGame.makeHexagonalMap(2)
        val civ = testGame.addCiv()
        val sourceTile = makeLandTile(0, 0)
        val destTile = makeLandTile(2, 0)
        val sourceCity = testGame.addCity(civ, sourceTile)
        val destCity = testGame.addCity(civ, destTile)

        placeDistrict(destCity, "Campus", makeLandTile(1, 0))

        sourceCity.tradeRoutes.domesticRouteTo = destCity.name
        sourceCity.tradeRoutes.domesticRouteTurns = 30

        sourceCity.cityStats.update()
        val routeStats = sourceCity.cityStats.finalStatList["Trade routes"]!!
        assertEquals("Domestic route should give +1 Food from City Center and +1 Food from Campus",
            2f, routeStats.food, 0.001f)
        assertEquals("Domestic route should give +1 Production from City Center",
            1f, routeStats.production, 0.001f)
    }

    @Test
    fun internationalRouteYieldsGoldFromDestinationDistrictsAndTradingPosts() {
        testGame.makeHexagonalMap(2)
        val sourceCiv = testGame.addCiv()
        val destCiv = testGame.addCiv()
        val sourceTile = makeLandTile(0, 0)
        val destTile = makeLandTile(2, 0)
        testGame.addCity(sourceCiv, sourceTile)
        val destCity = testGame.addCity(destCiv, destTile)

        placeDistrict(destCity, "Commercial Hub", makeLandTile(1, 0))

        destCity.tradeRoutes.internationalRoutes[sourceCiv.civName] = 30
        // A trading post held by the source civ in the destination civ grants +1 Gold
        sourceCiv.tradingPosts.add(destCity.name)

        destCity.cityStats.update()
        val routeStats = destCity.cityStats.finalStatList["Trade routes"]!!
        assertEquals("International route should give +1 Gold from City Center, +2 from Commercial Hub, +1 from Trading Post",
            4f, routeStats.gold, 0.001f)
    }

    @Test
    fun tradingPostsInForeignCitiesExtendTradeRouteRange() {
        testGame.makeHexagonalMap(5)
        val civ = addCivWithAllTechs()
        val otherCiv = addCivWithAllTechs()
        val sourceTile = makeLandTile(0, 0)
        val farCiv = addCivWithAllTechs()

        testGame.addCity(civ, sourceTile)
        val foreignCity = testGame.addCity(otherCiv, makeLandTile(3, 0))
        testGame.addCity(farCiv, makeLandTile(4, 0))

        assertEquals("Base trade route range should be 15 tiles without foreign trading posts",
            15, civ.getTradeRouteRange())

        civ.tradingPosts.add(foreignCity.name)
        assertEquals("Each trading post in a foreign city extends range by +5",
            20, civ.getTradeRouteRange())
    }

    @Test
    fun traderCannotCreateRouteBeyondRange() {
        testGame.makeHexagonalMap(10)
        val civ = addCivWithAllTechs()
        val sourceTile = makeLandTile(-10, 0)
        val sourceCity = testGame.addCity(civ, sourceTile)

        // 20 tiles away — beyond the base range of 15
        val farTile = makeLandTile(10, 0)
        testGame.addCity(civ, farTile)
        val farTrader = testGame.addUnit("Trader", civ, farTile)
        farTrader.currentMovement = farTrader.getMaxMovement().toFloat()
        val farActions = UnitActions.getUnitActions(farTrader, UnitActionType.CreateTradeRoute).toList()
        assertTrue("Route beyond range should NOT be offered, got: ${farActions.map { it.title }}",
            farActions.none { it.action != null })

        // After establishing a trading post in a foreign city within 20 tiles, the same route becomes available
        val foreignCiv = addCivWithAllTechs()
        val foreignCity = testGame.addCity(foreignCiv, makeLandTile(-5, 0))
        civ.tradingPosts.add(foreignCity.name)
        assertEquals(20, civ.getTradeRouteRange())
        val farActionsAfterPost = UnitActions.getUnitActions(farTrader, UnitActionType.CreateTradeRoute).toList()
        assertTrue("Route should be offered once trading posts extend range",
            farActionsAfterPost.any { it.action != null })
    }
}
