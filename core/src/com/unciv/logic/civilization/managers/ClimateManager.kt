package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.city.City
import com.unciv.logic.map.tile.Tile
import yairm210.purity.annotations.Readonly

class ClimateManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var totalCO2 = 0
    var currentYear = 1800
    var seaLevelRise = 0
    var _floodedTiles = mutableSetOf<String>()
    var destroyedDistricts = mutableSetOf<String>()

    fun clone(): ClimateManager {
        val toReturn = ClimateManager()
        toReturn.totalCO2 = totalCO2
        toReturn.currentYear = currentYear
        toReturn.seaLevelRise = seaLevelRise
        toReturn._floodedTiles.addAll(_floodedTiles)
        toReturn.destroyedDistricts.addAll(destroyedDistricts)
        return toReturn
    }

    fun addCO2(amount: Int) {
        totalCO2 += amount
        updateClimate()
    }

    fun reduceCO2(amount: Int) {
        totalCO2 = (totalCO2 - amount).coerceAtLeast(0)
        updateClimate()
    }

    private fun updateClimate() {
        currentYear++
        when {
            totalCO2 >= 1000 -> seaLevelRise = 3
            totalCO2 >= 500 -> seaLevelRise = 2
            totalCO2 >= 250 -> seaLevelRise = 1
            else -> seaLevelRise = 0
        }
    }

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
    fun getCO2Level(): Int = totalCO2

    @Readonly
    fun getFloodedTiles(): Set<String> = _floodedTiles
}