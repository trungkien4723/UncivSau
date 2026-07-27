package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.city.City
import com.unciv.logic.map.tile.Tile
import com.unciv.logic.civilization.NotificationCategory
import yairm210.purity.annotations.Readonly

enum class ClimatePhase {
    NONE,       // Pre-industrial
    PHASE_I,    // 250-500 CO2 - minor floods
    PHASE_II,   // 500-750 CO2 - major floods, desertification
    PHASE_III,  // 750-1000 CO2 - severe floods, polar ice melt
    PHASE_IV    // 1000+ CO2 - catastrophic
}

class ClimateManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var currentYear = 1800
    var seaLevelRise = 0
    var _floodedTiles = mutableSetOf<String>()
    var destroyedDistricts = mutableSetOf<String>()
    var climatePhase = ClimatePhase.NONE
    var accumulatedDisasters = 0

    fun clone(): ClimateManager {
        val toReturn = ClimateManager()
        toReturn.currentYear = currentYear
        toReturn.seaLevelRise = seaLevelRise
        toReturn._floodedTiles.addAll(_floodedTiles)
        toReturn.destroyedDistricts.addAll(destroyedDistricts)
        toReturn.climatePhase = climatePhase
        toReturn.accumulatedDisasters = accumulatedDisasters
        return toReturn
    }

    /** Update climate based on global CO2 from power plants */
    fun updateClimate() {
        currentYear++
        val totalCO2 = civInfo.powerManager.totalCO2
        when {
            totalCO2 >= 1000 -> {
                seaLevelRise = 3
                climatePhase = ClimatePhase.PHASE_IV
            }
            totalCO2 >= 750 -> {
                seaLevelRise = 2
                climatePhase = ClimatePhase.PHASE_III
            }
            totalCO2 >= 500 -> {
                seaLevelRise = 2
                climatePhase = ClimatePhase.PHASE_II
            }
            totalCO2 >= 250 -> {
                seaLevelRise = 1
                climatePhase = ClimatePhase.PHASE_I
            }
            else -> {
                seaLevelRise = 0
                climatePhase = ClimatePhase.NONE
            }
        }
        
        // Trigger climate-driven disasters
        triggerClimateDisasters()
    }

    /** Trigger climate-phase driven natural disasters */
    private fun triggerClimateDisasters() {
        val multiplier = getDisasterFrequencyMultiplier()
        if (multiplier <= 1.0f) return
        
        // Roll for climate-driven disasters
        val random = kotlin.random.Random
        if (random.nextFloat() < 0.1f * multiplier) {
            // Trigger climate-driven disaster
            val disasterTypes = listOf("Flood", "Storm", "Drought")
            val disaster = disasterTypes.random()
            civInfo.disasterManager.triggerDisaster(disaster)
            civInfo.disasterManager.applyDisasterEffects(disaster)
            accumulatedDisasters++
            
            // Notify player
            civInfo.addNotification(
                "Climate change has triggered a ${disaster.toLowerCase()}!",
                NotificationCategory.Diplomacy, "StatIcons/Disaster"
            )
        }
    }

    /** Get total CO2 from power manager */
    @Readonly
    fun getCO2Level(): Int = civInfo.powerManager.totalCO2

    /** Get current climate phase */
    @Readonly
    fun getClimatePhase(): ClimatePhase = climatePhase

    /** Get phase description for UI */
    @Readonly
    fun getPhaseDescription(): String = when (climatePhase) {
        ClimatePhase.NONE -> "Pre-Industrial Climate"
        ClimatePhase.PHASE_I -> "Phase I: Rising Seas - Minor coastal flooding"
        ClimatePhase.PHASE_II -> "Phase II: Warming World - Major flooding, desertification"
        ClimatePhase.PHASE_III -> "Phase III: Melting Poles - Severe flooding, ice sheet collapse"
        ClimatePhase.PHASE_IV -> "Phase IV: Runaway Climate - Catastrophic sea rise"
    }

    /** Check if climate phase should trigger more disasters */
    @Readonly
    fun getDisasterFrequencyMultiplier(): Float = when (climatePhase) {
        ClimatePhase.NONE -> 1.0f
        ClimatePhase.PHASE_I -> 1.25f
        ClimatePhase.PHASE_II -> 1.5f
        ClimatePhase.PHASE_III -> 2.0f
        ClimatePhase.PHASE_IV -> 3.0f
    }

    /** Get total CO2 from power manager */
    @Readonly
    fun getCO2Level(): Int = civInfo.powerManager.totalCO2

    fun isTileFloodProne(tile: Tile): Boolean {
        if (seaLevelRise == 0) return false
        return tile.isAdjacentToCoast()
    }

    fun getFloodRiskTiles(cities: Set<City>): Set<Tile> {
        if (seaLevelRise == 0) return emptySet()
        return cities.flatMap { city -> city.tilesInRange.asSequence().filter { isTileFloodProne(it) }.toSet() }.toSet()
    }

    fun applyFloodEffects() {
        if (seaLevelRise == 0) return
        
        for (city in civInfo.cities) {
            for (tile in city.tilesInRange) {
                if (!isTileFloodProne(tile)) continue
                val tileKey = tile.position.toString()
                if (tileKey in _floodedTiles) continue
                
                _floodedTiles.add(tileKey)
                
                if (tile.district != null) {
                    val district = tile.district!!
                    val districtKey = "$tileKey|$district"
                    if (districtKey !in destroyedDistricts) {
                        val capital = civInfo.getCapital()
                        if (capital != null && !capital.cityConstructions.getBuiltBuildings().any { it.name == "Flood Barrier" }) {
                            destroyedDistricts.add(districtKey)
                            val owningCity = tile.getCity()
                            if (owningCity != null)
                                owningCity.districts.remove(tile.position)
                            tile.district = null
                        }
                    }
                }
            }
        }
    }

    @Readonly
    fun isFloodProne(): Boolean = seaLevelRise > 0

    @Readonly
    fun getFloodDamageModifier(): Float = when (seaLevelRise) {
        3 -> 0.5f
        2 -> 0.7f
        1 -> 0.9f
        else -> 1.0f
    }

    @Readonly
    fun getCO2Level(): Int = civInfo.powerManager.totalCO2

    @Readonly
    fun getFloodedTiles(): Set<String> = _floodedTiles
}