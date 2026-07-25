package com.unciv.logic.city

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.city.City
import yairm210.purity.annotations.Readonly

class CityTradeRoutes : IsPartOfGameInfoSerialization {
    var domesticRouteTo: String = ""  // City name this city sends domestic trade to
    var internationalRouteFrom: String = ""  // Civ name this city receives international trade from
    
    fun clone(): CityTradeRoutes {
        val toReturn = CityTradeRoutes()
        toReturn.domesticRouteTo = domesticRouteTo
        toReturn.internationalRouteFrom = internationalRouteFrom
        return toReturn
    }
    
    @Readonly fun hasDomesticRoute(): Boolean = domesticRouteTo.isNotEmpty()
    @Readonly fun hasInternationalRoute(): Boolean = internationalRouteFrom.isNotEmpty()
}