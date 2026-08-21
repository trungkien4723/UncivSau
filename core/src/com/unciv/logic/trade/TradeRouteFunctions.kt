package com.unciv.logic.trade

import com.unciv.Constants
import com.unciv.logic.city.City
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.civilization.managers.GoldenAgeManager
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.logic.map.tile.RoadStatus
import com.unciv.logic.map.tile.Tile
import yairm210.purity.annotations.Readonly

/** Civ VI trade route logic: choosing a destination city for a Trader, walking it there over
 *  several turns while paving roads, then running the route until it completes. */
object TradeRouteFunctions {

    /** Candidate destination cities for a trade route from [sourceCity] - within range
     *  (15 tiles over land, 30 over water, extended by foreign trading posts), not barbarian,
     *  and known/explored (own cities are always reachable). */
    @Readonly
    fun getTradeRouteDestinations(sourceCity: City): List<City> {
        val civ = sourceCity.civ
        return civ.gameInfo.getCities()
            .filter { destination ->
                destination != sourceCity
                        && !destination.civ.isBarbarian
                        && sourceCity.getCenterTile().aerialDistanceTo(destination.getCenterTile()) <= civ.getTradeRouteRange(destination)
                        && (destination.civ == civ
                            || civ.knows(destination.civ) && civ.hasExplored(destination.getCenterTile()))
            }
            .sortedWith(compareBy({ it.civ == civ }, { it.civ.civName }, { it.name }))
            .toList()
    }

    /** Starts a trade route: the [trader] will walk from [sourceCity] toward [destinationCity] at
     *  1 tile per turn, paving roads on the land tiles it crosses. The route only becomes active
     *  (yielding stats and running its duration) once the Trader arrives, and a Trading Post is
     *  established at the destination when the route's duration completes. */
    fun startTradeRoute(civ: Civilization, sourceCity: City, destinationCity: City, trader: MapUnit) {
        val path = sourceCity.getRoadPath(destinationCity)
            ?: getFallbackPath(sourceCity.getCenterTile(), destinationCity.getCenterTile())
            ?: return
        // The path includes the source city center, where the Trader is standing - skip it
        val remainingPath = path.drop(1).map { it.position }
        val routes = sourceCity.tradeRoutes
        routes.travellingTraderId = trader.id
        routes.travelDestination = destinationCity.name
        routes.travelPath.clear()
        routes.travelPath.addAll(remainingPath)
        trader.currentMovement = 0f  // the Trader is committed to this route now
        civ.addNotification(
            "A [Trader] from [${sourceCity.name}] is travelling to [${destinationCity.name}]...",
            NotificationCategory.Trade, "TradeRoute")
    }

    /** Advances all travelling Traders of [civInfo] by one tile (called each turn end).
     *  Traders pave roads on the land tiles they enter; once a Trader arrives, its route becomes
     *  active (domestic or international) and the Trader is consumed. */
    fun advanceTravellingTraders(civInfo: Civilization) {
        for (city in civInfo.cities) {
            val routes = city.tradeRoutes
            if (!routes.isTravelling()) continue
            val destinationCity = civInfo.gameInfo.getCities().firstOrNull { it.name == routes.travelDestination }
            if (destinationCity == null) {
                routes.travellingTraderId = -1
                routes.travelDestination = ""
                routes.travelPath.clear()
                continue
            }
            val trader = civInfo.units.getUnitById(routes.travellingTraderId)
            if (trader == null || trader.isDestroyed) {
                routes.travellingTraderId = -1
                routes.travelDestination = ""
                routes.travelPath.clear()
                continue
            }
            if (routes.travelPath.isEmpty()) {
                activateRouteOnArrival(civInfo, city, destinationCity, trader)
                routes.travellingTraderId = -1
                routes.travelDestination = ""
                continue
            }
            // Take one step: pave the next land tile and move the Trader onto it
            val nextTile = civInfo.gameInfo.tileMap[routes.travelPath.removeAt(0)]
            if (nextTile.isLand) {
                val roadStatus = civInfo.tech.getBestRoadAvailable()
                if (roadStatus != RoadStatus.None) nextTile.setRoadStatus(roadStatus, civInfo)
            }
            if (trader.getTile() != nextTile) {
                trader.removeFromTile()
                trader.putInTile(nextTile)
            }
        }
    }

    /** Advances the duration of all active routes of [civInfo] by one turn (called each turn end).
     *  When a route's duration completes, a Trading Post is established at its destination
     *  and the route is removed. */
    fun advanceTradeRouteDurations(civInfo: Civilization) {
        for (city in civInfo.cities) {
            val routes = city.tradeRoutes
            if (routes.domesticRouteTurns > 0) {
                routes.domesticRouteTurns--
                if (routes.domesticRouteTurns <= 0) {
                    // Civ VI: completing a route establishes a Trading Post at the destination
                    val destinationCity = civInfo.gameInfo.getCities().firstOrNull { it.name == routes.domesticRouteTo }
                    if (destinationCity != null) {
                        civInfo.tradingPosts.add(destinationCity.name)
                        civInfo.addNotification(
                            "Trading Post established in [${destinationCity.name}]!",
                            NotificationCategory.Trade, "TradeRoute")
                    }
                    routes.domesticRouteTo = ""
                }
            }
            val toRemove = mutableListOf<String>()
            for ((sourceCivName, turns) in routes.internationalRoutes) {
                val newTurns = turns - 1
                if (newTurns <= 0) {
                    // Civ VI: completing a route establishes a Trading Post at the destination
                    val sourceCiv = civInfo.gameInfo.civilizations.firstOrNull { it.civName == sourceCivName }
                    if (sourceCiv != null) {
                        sourceCiv.tradingPosts.add(city.name)
                        sourceCiv.addNotification(
                            "Trading Post established in [${city.name}]!",
                            NotificationCategory.Trade, "TradeRoute")
                        // Civ VI dedication (Reform the Coinage): Era Score for an international route
                        sourceCiv.goldenAges.awardDedicationEraScore(
                            GoldenAgeManager.DedicationEvent.InternationalTradeRouteCompleted)
                    }
                    toRemove.add(sourceCivName)
                } else routes.internationalRoutes[sourceCivName] = newTurns
            }
            for (sourceCivName in toRemove) routes.internationalRoutes.remove(sourceCivName)
        }
    }

    /** Shortest path between [source] and [destination] over land tiles, used when the civ's road
     *  construction pathing can't reach the destination (e.g. a foreign city where roads can't be
     *  built, but a Trader can still walk to). */
    private fun getFallbackPath(source: Tile, destination: Tile): List<Tile>? {
        val queue = ArrayDeque<List<Tile>>()
        queue.add(listOf(source))
        val visited = HashSet<Tile>()
        visited.add(source)
        var guard = 0
        while (queue.isNotEmpty()) {
            if (++guard > 5000) return null
            val path = queue.removeFirst()
            val current = path.last()
            if (current == destination) return path
            for (neighbor in current.neighbors) {
                if (neighbor in visited || !neighbor.isLand) continue
                visited.add(neighbor)
                queue.add(path + neighbor)
            }
        }
        return null
    }

    private fun activateRouteOnArrival(civ: Civilization, sourceCity: City, destinationCity: City, trader: MapUnit) {        if (destinationCity.civ == civ) {
            // Domestic route
            sourceCity.tradeRoutes.domesticRouteTo = destinationCity.name
            sourceCity.tradeRoutes.domesticRouteTurns = Constants.tradeRouteDuration
            civ.addNotification(
                "Established a domestic trade route from [${sourceCity.name}] to [${destinationCity.name}]!",
                NotificationCategory.Trade, "TradeRoute")
        } else {
            // International route
            destinationCity.tradeRoutes.internationalRoutes[civ.civName] = Constants.tradeRouteDuration
            civ.addNotification(
                "Established an international trade route from [${sourceCity.name}] to [${destinationCity.name}]!",
                NotificationCategory.Trade, "TradeRoute")
        }
        trader.destroy()
    }
}