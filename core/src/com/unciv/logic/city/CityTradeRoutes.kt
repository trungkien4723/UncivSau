package com.unciv.logic.city

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.city.City
import com.unciv.logic.map.HexCoord
import yairm210.purity.annotations.Readonly

class CityTradeRoutes : IsPartOfGameInfoSerialization {
    var domesticRouteTo: String = ""  // City name this city sends domestic trade to
    var domesticRouteTurns: Int = 0   // Turns remaining on the domestic route

    // International routes incoming — key = source civ name, value = turns remaining
    val internationalRoutes = HashMap<String, Int>()

    // Civ VI: a Trader walks to its destination over several turns before its route becomes active.
    // travellingTraderId is the id of the Trader walking from this city,
    // travelDestination is the city it is walking to,
    // travelPath are the tiles still to traverse (excluding the tile the Trader is standing on).
    var travellingTraderId: Int = -1
    var travelDestination: String = ""
    val travelPath = ArrayList<HexCoord>()

    fun clone(): CityTradeRoutes {
        val toReturn = CityTradeRoutes()
        toReturn.domesticRouteTo = domesticRouteTo
        toReturn.domesticRouteTurns = domesticRouteTurns
        toReturn.internationalRoutes.putAll(internationalRoutes)
        toReturn.travellingTraderId = travellingTraderId
        toReturn.travelDestination = travelDestination
        toReturn.travelPath.addAll(travelPath)
        return toReturn
    }

    @Readonly fun hasDomesticRoute(): Boolean = domesticRouteTurns > 0 && domesticRouteTo.isNotEmpty()
    @Readonly fun hasInternationalRoute(): Boolean = internationalRoutes.any { it.value > 0 }
    @Readonly fun isTravelling(): Boolean = travellingTraderId != -1
}