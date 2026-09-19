package com.unciv.logic.trade

import com.unciv.logic.civilization.Civilization
import com.unciv.logic.trade.TradeRouteFunctions
import com.unciv.logic.map.tile.Tile
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
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
    fun establishingRouteCreatesTradingPostsOnCompletion() {
        testGame.makeHexagonalMap(2)
        val civ = addCivWithAllTechs()
        val sourceTile = makeLandTile(0, 0)
        val destTile = makeLandTile(2, 0)
        val sourceCity = testGame.addCity(civ, sourceTile)
        val destCity = testGame.addCity(civ, destTile)

        val destinations = TradeRouteFunctions.getTradeRouteDestinations(sourceCity)
        assertTrue("The other own city should be offered as a destination within range",
            destinations.contains(destCity))

        val trader = testGame.addUnit("Trader", civ, sourceTile)
        TradeRouteFunctions.startTradeRoute(civ, sourceCity, destCity, trader)

        assertTrue("No trading posts yet while the Trader is travelling", civ.tradingPosts.isEmpty())
        assertTrue("The Trader should still be alive while travelling", !trader.isDestroyed)

        // Walk the Trader to its destination (1 tile per turn)
        while (sourceCity.tradeRoutes.isTravelling()) {
            TradeRouteFunctions.advanceTravellingTraders(civ)
        }
        // The route is now active (duration runs)
        assertEquals(destCity.name, sourceCity.tradeRoutes.domesticRouteTo)
        assertEquals(com.unciv.Constants.tradeRouteDuration, sourceCity.tradeRoutes.domesticRouteTurns)

        // Complete the route's duration - only now is a Trading Post established at the destination
        sourceCity.tradeRoutes.domesticRouteTurns = 1
        TradeRouteFunctions.advanceTradeRouteDurations(civ)
        assertTrue("A Trading Post is established at the destination only when the route completes",
            civ.tradingPosts.contains(destCity.name))
        assertTrue("No route should remain after it completes", !sourceCity.tradeRoutes.hasDomesticRoute())
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
        // Make the foreign city coastal (set the water BEFORE founding, since coastal-ness is cached)
        testGame.setTileTerrain(com.unciv.logic.map.HexCoord(3, 1), "Coast")
        val foreignCity = testGame.addCity(otherCiv, makeLandTile(3, 0))
        testGame.addCity(farCiv, makeLandTile(4, 0))

        assertEquals("Base trade route range should be 15 tiles for a land route without foreign trading posts",
            15, civ.getTradeRouteRange())
        assertEquals("Base trade route range should be 30 tiles for a water route without foreign trading posts",
            30, civ.getTradeRouteRange(foreignCity))

        civ.tradingPosts.add(foreignCity.name)
        assertEquals("Each trading post in a foreign land city extends range by +15",
            30, civ.getTradeRouteRange())
        assertEquals("Each trading post in a foreign coastal city extends range by +30",
            60, civ.getTradeRouteRange(foreignCity))
    }

    @Test
    fun traderCannotCreateRouteBeyondRange() {
        testGame.makeHexagonalMap(10)
        val civ = addCivWithAllTechs()
        val sourceTile = makeLandTile(-10, 0)
        val sourceCity = testGame.addCity(civ, sourceTile)

        // 20 tiles away — beyond the base range of 15
        val farTile = makeLandTile(10, 0)
        val farCity = testGame.addCity(civ, farTile)
        assertTrue("A city 20 tiles away should be beyond the base range of 15",
            TradeRouteFunctions.getTradeRouteDestinations(farCity).none { it == sourceCity })

        // After establishing a trading post in a foreign city within 20 tiles, the same route becomes available
        val foreignCiv = addCivWithAllTechs()
        val foreignCity = testGame.addCity(foreignCiv, makeLandTile(-5, 0))
        civ.tradingPosts.add(foreignCity.name)
        assertEquals("A single foreign trading post extends land routes to 30 tiles",
            30, civ.getTradeRouteRange())
        assertTrue("Route should be available once trading posts extend range",
            TradeRouteFunctions.getTradeRouteDestinations(farCity).any { it == sourceCity })
    }
}
