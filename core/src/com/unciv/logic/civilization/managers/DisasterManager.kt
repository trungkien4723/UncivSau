package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.unit.GreatPersonType
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

    fun getDisasterFrequencyMultiplier(): Float {
        val climatePhase = civInfo.climateManager.getClimatePhase()
        return when (climatePhase) {
            com.unciv.logic.civilization.managers.ClimatePhase.NONE -> 1.0f
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_I -> 1.25f
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_II -> 1.5f
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_III -> 2.0f
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_IV -> 3.0f
        }
    }

    fun triggerDisaster(disasterType: String) {
        disasterCount++
        when (disasterType) {
            "Flood" -> floodCount++
            "Volcano" -> volcanoCount++
            "Storm" -> stormCount++
            "Drought" -> droughtCount++
        }
    }

    fun applyDisasterEffects(disasterType: String) {
        when (disasterType) {
            "Flood" -> applyFloodEffects()
            "Volcano" -> applyVolcanoEffects()
            "Storm" -> applyStormEffects()
            "Drought" -> applyDroughtEffects()
        }
    }

    fun applyFloodEffects() {
        // Flood effects on tiles and cities
        civInfo.cities.forEach { city ->
            // Flood causes -1 population in affected cities
            if (city.populated) {
                city.population.population--
            }
            // Flood can damage units
        }
        // Flood affects river tiles
    }

    fun applyVolcanoEffects() {
        // Volcanic effects
        civInfo.cities.forEach { city ->
            // Volcano causes -5 population in affected cities
            if (city.populated && city.population.population >= 5) {
                city.population.population -= 5
            }
            // Create volcanic soil
        }
    }

    fun applyStormEffects() {
        // Storm effects on units and tiles
        civInfo.units.forEach { unit ->
            // Storm causes unit damage
            // Storm reduces movement speed by 80%
        }
    }

    fun applyDroughtEffects() {
        // Drought effects on growth
        civInfo.cities.forEach { city ->
            // Drought reduces food from plains and grassland by 50%
            city.cityStats.update()
        }
    }

    fun isTileFloodAffected(): Boolean = floodCount > 0
    fun isTileVolcanoAffected(): Boolean = volcanoCount > 0
    fun isTileStormAffected(): Boolean = stormCount > 0
    fun isTileDroughtAffected(): Boolean = droughtCount > 0

    fun clearDisasterEffects() {
        floodCount = 0
        volcanoCount = 0
        stormCount = 0
        droughtCount = 0
    }

    // Additional disaster types
    var stormCount = 0
    var droughtCount = 0

    fun clone(): DisasterManager {
        val toReturn = DisasterManager()
        toReturn.disasterCount = disasterCount
        toReturn.floodCount = floodCount
        toReturn.volcanoCount = volcanoCount
        toReturn.stormCount = stormCount
        toReturn.droughtCount = droughtCount
        return toReturn
    }
}

object DisasterEffects {
    fun applyFloodEffects(civInfo: Civilization, tile: Tile) {
        // Flood affects river tiles and damage units
        tile.tileObject?.construction?.let { construction ->
            construction.destroysOnTile() // Pillage construction
        }
        
        tile.improvement?.let { improvement ->
            // Flood pillages tile improvements
        }
        
        civInfo.unitsOnTile(tile).forEach { unit ->
            // Flood can damage units
        }
    }

    fun applyVolcanoEffects(civInfo: Civilization, tile: Tile) {
        // Volcanic ash destroys terrain, creates volcanic soil
        tile.setTerrainType("Volcanic Soil")
        
        tile.tileObject?.terrain?.let { terrain ->
            terrain.applyDamage() // Destroy terrain
        }
        
        civInfo.unitsOnTile(tile).forEach { unit ->
            if (!unit.isAirUnit()) {
                // Volcano damages non-air units
            }
        }
    }

    fun applyStormEffects(civInfo: Civilization, tile: Tile) {
        // Storm reduces movement speed and sight
        civInfo.unitsOnTile(tile).forEach { unit ->
            if (!unit.isAirUnit()) {
                unit.movement.currentMovement = 0
                unit.sight = max(0, unit.sight - 4)
            }
        }
        
        // Scatter Great Person points
        if (tile.improvement?.uniqueObject?.contains("Great Person") == true) {
            tile.improvement?.uniqueObject = "" // Clear GP from tile
            civInfo.addGreatPersonPointsToRandomCity(GreatPersonType.General, 1)
        }
    }

    fun applyDroughtEffects(civInfo: Civilization, tile: Tile) {
        // Expand deserts and reduce food
        when (tile.terrainType) {
            "Plains", "Grassland" -> tile.setTerrainType("Desert")
            else -> { }
        }
        
        tile.tileObject?.improvement?.let { improvement ->
            improvement.destroysOnTile() // Drought pillages
        }
    }
}