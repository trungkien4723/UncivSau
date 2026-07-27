package com.unciv.logic.city

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.city.City
import yairm210.purity.annotations.Readonly

class CityTradeRoutes : IsPartOfGameInfoSerialization {
    var domesticRouteTo: String = ""  // City name this city sends domestic trade to
    var domesticRouteTurns: Int = 0   // Turns remaining on the domestic route

    // International routes incoming — key = source civ name, value = turns remaining
    val internationalRoutes = HashMap<String, Int>()

    fun clone(): CityTradeRoutes {
        val toReturn = CityTradeRoutes()
        toReturn.domesticRouteTo = domesticRouteTo
        toReturn.domesticRouteTurns = domesticRouteTurns
        toReturn.internationalRoutes.putAll(internationalRoutes)
        return toReturn
    }

    @Readonly fun hasDomesticRoute(): Boolean = domesticRouteTurns > 0 && domesticRouteTo.isNotEmpty()
    @Readonly fun hasInternationalRoute(): Boolean = internationalRoutes.any { it.value > 0 }
}