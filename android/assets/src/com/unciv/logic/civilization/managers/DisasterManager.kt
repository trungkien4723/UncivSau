package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import yairm210.purity.annotations.Readonly

class DisasterManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var disasterCount = 0
    var floodCount = 0
    var volcanoCount = 0

    fun clone(): DisasterManager {
        val toReturn = DisasterManager()
        toReturn.disasterCount = disasterCount
        toReturn.floodCount = floodCount
        toReturn.volcanoCount = volcanoCount
        return toReturn
    }

    fun triggerDisaster(disasterType: String) {
        disasterCount++
        when (disasterType) {
            "Flood" -> floodCount++
            "Volcano" -> volcanoCount++
            "Storm" -> { /* Storm affects units */ }
            "Drought" -> { /* Drought affects growth */ }
        }
    }

    fun isTileFloodAffected(): Boolean = floodCount > 0
    fun isTileVolcanoAffected(): Boolean = volcanoCount > 0

    fun clearDisasterEffects() {
        floodCount = 0
        volcanoCount = 0
    }
}