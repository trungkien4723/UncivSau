package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.city.City
import com.unciv.logic.map.tile.Tile
import yairm210.purity.annotations.Readonly
import kotlin.math.max

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

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    /** Update climate based on global CO2 from power plants */
    fun updateClimate() {
        currentYear++
        val totalCO2 = civInfo.powerManager.totalCO2
        val isApocalypse = civInfo.gameModes.isApocalypseModeActive()

        val thresholdMod = if (isApocalypse) 0.5f else 1.0f
        val adjustedCO2 = (totalCO2 * thresholdMod).toInt()

        when {
            adjustedCO2 >= 1000 -> {
                seaLevelRise = if (isApocalypse) 5 else 3
                climatePhase = ClimatePhase.PHASE_IV
            }
            adjustedCO2 >= 750 -> {
                seaLevelRise = if (isApocalypse) 3 else 2
                climatePhase = ClimatePhase.PHASE_III
            }
            adjustedCO2 >= 500 -> {
                seaLevelRise = if (isApocalypse) 3 else 2
                climatePhase = ClimatePhase.PHASE_II
            }
            adjustedCO2 >= 250 -> {
                seaLevelRise = if (isApocalypse) 2 else 1
                climatePhase = ClimatePhase.PHASE_I
            }
            else -> {
                seaLevelRise = 0
                climatePhase = ClimatePhase.NONE
            }
        }

        triggerClimateDisasters()
    }

    /** Trigger climate-phase driven natural disasters */
    private fun triggerClimateDisasters() {
        val multiplier = getDisasterFrequencyMultiplier()
        val isApocalypse = civInfo.gameModes.isApocalypseModeActive()
        if (multiplier <= 1.0f && !isApocalypse) return

        val random = kotlin.random.Random
        val baseChance = if (isApocalypse) 0.15f else 0.1f
        if (random.nextFloat() < baseChance * multiplier) {
            val disasterTypes = mutableListOf("Flood", "Storm", "Drought")
            if (isApocalypse) {
                disasterTypes.addAll(listOf("Solar Flare", "Tornado", "Blizzard"))
            }
            val disaster = disasterTypes.random()
            val targetCity = civInfo.cities.randomOrNull()
            val targetTile = targetCity?.getCenterTile()
            civInfo.disasterManager.triggerDisaster(disaster, targetTile)
            accumulatedDisasters++

            civInfo.addNotification(
                "Climate change has triggered a ${disaster.lowercase()}!",
                NotificationCategory.General, "StatIcons/Disaster"
            )
        }
    }

    @Readonly
    fun getCO2Level(): Int = civInfo.powerManager.totalCO2

    @Readonly
    fun getPhaseDescription(): String = when (climatePhase) {
        ClimatePhase.NONE -> "Pre-Industrial Climate"
        ClimatePhase.PHASE_I -> "Phase I: Rising Seas - Minor coastal flooding"
        ClimatePhase.PHASE_II -> "Phase II: Warming World - Major flooding, desertification"
        ClimatePhase.PHASE_III -> "Phase III: Melting Poles - Severe flooding, ice sheet collapse"
        ClimatePhase.PHASE_IV -> "Phase IV: Runaway Climate - Catastrophic sea rise"
    }

    @Readonly
    fun getDisasterFrequencyMultiplier(): Float {
        val base = when (climatePhase) {
            ClimatePhase.NONE -> 1.0f
            ClimatePhase.PHASE_I -> 1.25f
            ClimatePhase.PHASE_II -> 1.5f
            ClimatePhase.PHASE_III -> 2.0f
            ClimatePhase.PHASE_IV -> 3.0f
        }
        val apocalypseMult = if (civInfo.gameModes.isApocalypseModeActive()) 2.0f else 1.0f
        return base * apocalypseMult
    }

    fun isTileFloodProne(tile: Tile): Boolean {
        if (seaLevelRise == 0) return false
        return tile.isAdjacentToCoast()
    }

    fun getFloodRiskTiles(cities: Set<City>): Set<Tile> {
        if (seaLevelRise == 0) return emptySet()
        return cities.flatMap { city ->
            city.tilesInRange.asSequence().filter { isTileFloodProne(it) }.toSet()
        }.toSet()
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
                        if (capital != null &&
                            !capital.cityConstructions.getBuiltBuildings().any { it.name == "Flood Barrier" }) {
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
        5 -> 0.3f
        4 -> 0.4f
        3 -> 0.5f
        2 -> 0.7f
        1 -> 0.9f
        else -> 1.0f
    }

    @Readonly
    fun getFloodedTiles(): Set<String> = _floodedTiles
}
