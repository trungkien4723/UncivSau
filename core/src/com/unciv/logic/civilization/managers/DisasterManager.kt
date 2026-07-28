package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.map.HexCoord
import com.unciv.logic.map.tile.Tile
import yairm210.purity.annotations.Readonly
import kotlin.math.max

class DisasterManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var disasterCount = 0
    var floodCount = 0
    var volcanoCount = 0
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

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    @Readonly
    fun getDisasterFrequencyMultiplier(): Float {
        val climatePhase = civInfo.climateManager.climatePhase
        val baseMultiplier = when (climatePhase) {
            ClimatePhase.NONE -> 1.0f
            ClimatePhase.PHASE_I -> 1.25f
            ClimatePhase.PHASE_II -> 1.5f
            ClimatePhase.PHASE_III -> 2.0f
            ClimatePhase.PHASE_IV -> 3.0f
            else -> 1.0f
        }
        val apocalypseMultiplier = if (civInfo.gameModes.isApocalypseModeActive()) 2.0f else 1.0f
        return baseMultiplier * apocalypseMultiplier
    }

    @Readonly
    fun getDisasterDamageMultiplier(): Float {
        return if (civInfo.gameModes.isApocalypseModeActive()) 1.5f else 1.0f
    }

    @Readonly
    fun getDisasterRadiusMultiplier(): Int {
        return if (civInfo.gameModes.isApocalypseModeActive()) 2 else 1
    }

    fun triggerDisaster(disasterType: String, targetTile: Tile? = null) {
        disasterCount++
        when (disasterType) {
            "Flood" -> { floodCount++; applyFloodEffects(targetTile) }
            "Volcano" -> { volcanoCount++; applyVolcanoEffects(targetTile) }
            "Storm" -> { stormCount++; applyStormEffects(targetTile) }
            "Drought" -> { droughtCount++; applyDroughtEffects(targetTile) }
            "Tornado" -> applyTornadoEffects(targetTile)
            "Blizzard" -> applyBlizzardEffects(targetTile)
            "Solar Flare" -> applySolarFlareEffects()
        }

        civInfo.addNotification(
            "A [$disasterType] has occurred!",
            targetTile?.position ?: HexCoord(0, 0),
            NotificationCategory.General,
            "StatIcons/Disaster"
        )
    }

    private fun applyFloodEffects(targetTile: Tile? = null) {
        val damageMult = getDisasterDamageMultiplier()
        val radius = getDisasterRadiusMultiplier()

        val tilesToAffect: Sequence<Tile> = if (targetTile != null)
            targetTile.getTilesInDistance(radius)
        else
            civInfo.cities.asSequence().flatMap { it.getTiles().asSequence() }

        for (tile in tilesToAffect) {
            if (!tile.isAdjacentToRiver() && !tile.isAdjacentToCoast()) continue

            tile.improvement = null
            tile.setTileResource(null)

            tile.getUnits().forEach { unit ->
                unit.health = max(0, unit.health - (20 * damageMult).toInt())
            }

            val city = tile.getCity()
            if (city != null && city.civ == civInfo) {
                val newPop = max(1, city.population.population - (1 * damageMult).toInt())
                city.population.setPopulation(newPop)
            }
        }
    }

    private fun applyVolcanoEffects(targetTile: Tile? = null) {
        val damageMult = getDisasterDamageMultiplier()
        val radius = getDisasterRadiusMultiplier()

        val tilesToAffect: Sequence<Tile> = if (targetTile != null)
            targetTile.getTilesInDistance(radius)
        else
            civInfo.cities.asSequence().flatMap { it.getTiles().asSequence() }

        for (tile in tilesToAffect) {
            tile.getUnits().forEach { unit ->
                unit.health = max(0, unit.health - (50 * damageMult).toInt())
            }

            val city = tile.getCity()
            if (city != null && city.civ == civInfo) {
                val newPop = max(1, city.population.population - (3 * damageMult).toInt())
                city.population.setPopulation(newPop)
            }
        }
    }

    private fun applyStormEffects(targetTile: Tile? = null) {
        val damageMult = getDisasterDamageMultiplier()
        val radius = getDisasterRadiusMultiplier()

        val tilesToAffect: Sequence<Tile> = if (targetTile != null)
            targetTile.getTilesInDistance(radius)
        else
            civInfo.cities.asSequence().flatMap { it.getTiles().asSequence() }

        for (tile in tilesToAffect) {
            tile.getUnits().forEach { unit ->
                if (!unit.baseUnit.isAirUnit()) {
                    unit.health = max(0, unit.health - (15 * damageMult).toInt())
                    unit.currentMovement = max(0f, unit.currentMovement - 1f)
                }
            }
        }
    }

    private fun applyDroughtEffects(targetTile: Tile? = null) {
        val damageMult = getDisasterDamageMultiplier()

        val tilesToAffect: Sequence<Tile> = if (targetTile != null)
            sequenceOf(targetTile)
        else
            civInfo.cities.asSequence().flatMap { it.getTiles().asSequence() }

        for (tile in tilesToAffect) {
            tile.improvement = null

            val city = tile.getCity()
            if (city != null && city.civ == civInfo) {
                city.population.foodStored = max(0, city.population.foodStored - (10 * damageMult).toInt())
            }
        }
    }

    private fun applyTornadoEffects(targetTile: Tile? = null) {
        if (targetTile == null) return
        val damageMult = getDisasterDamageMultiplier()
        val radius = getDisasterRadiusMultiplier()

        val tilesToAffect = targetTile.getTilesInDistance(radius)
        for (tile in tilesToAffect) {
            tile.improvement = null
            tile.setTileResource(null)

            tile.getUnits().forEach { unit ->
                unit.health = max(0, unit.health - (40 * damageMult).toInt())
            }

            val city = tile.getCity()
            if (city != null && city.civ == civInfo) {
                val newPop = max(1, city.population.population - (2 * damageMult).toInt())
                city.population.setPopulation(newPop)
            }
        }
    }

    private fun applyBlizzardEffects(targetTile: Tile? = null) {
        val damageMult = getDisasterDamageMultiplier()
        val radius = getDisasterRadiusMultiplier()

        val tilesToAffect: Sequence<Tile> = if (targetTile != null)
            targetTile.getTilesInDistance(radius)
        else
            civInfo.cities.asSequence().flatMap { it.getTiles().asSequence() }

        for (tile in tilesToAffect) {
            tile.getUnits().forEach { unit ->
                unit.health = max(0, unit.health - (25 * damageMult).toInt())
                unit.currentMovement = max(0f, unit.currentMovement - 2f)
            }

            val city = tile.getCity()
            if (city != null && city.civ == civInfo) {
                city.population.foodStored = max(0, city.population.foodStored - (15 * damageMult).toInt())
            }
        }
    }

    private fun applySolarFlareEffects() {
        for (unit in civInfo.units.getCivUnits()) {
            unit.health = max(0, unit.health - 30)
        }
    }

    @Readonly
    fun isTileFloodAffected(): Boolean = floodCount > 0
    @Readonly
    fun isTileVolcanoAffected(): Boolean = volcanoCount > 0
    @Readonly
    fun isTileStormAffected(): Boolean = stormCount > 0
    @Readonly
    fun isTileDroughtAffected(): Boolean = droughtCount > 0

    fun clearDisasterEffects() {
        floodCount = 0
        volcanoCount = 0
        stormCount = 0
        droughtCount = 0
    }

    fun calculatePower() {
        val frequencyMult = getDisasterFrequencyMultiplier()
        if (frequencyMult <= 1.0f) return

        val random = kotlin.random.Random
        val disasterChance = 0.05f * frequencyMult
        if (random.nextFloat() < disasterChance) {
            val disasterTypes = mutableListOf("Flood", "Volcano", "Storm", "Drought")
            if (civInfo.gameModes.isApocalypseModeActive()) {
                disasterTypes.addAll(listOf("Tornado", "Blizzard", "Solar Flare"))
            }
            val disaster = disasterTypes.random()
            val targetCity = civInfo.cities.randomOrNull()
            val targetTile = targetCity?.getCenterTile()
            triggerDisaster(disaster, targetTile)
        }
    }
}
