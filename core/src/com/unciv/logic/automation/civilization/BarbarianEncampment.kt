package com.unciv.logic.automation.civilization

import com.unciv.logic.GameInfo
import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.map.HexCoord
import com.unciv.logic.map.tile.Tile

class BarbarianEncampment() : IsPartOfGameInfoSerialization {
    var position = HexCoord()
    var countdown = 0
    var spawnedUnits = -1
    var destroyed = false // destroyed encampments haunt the vicinity for 15 turns preventing new spawns
    var clansConversionTurns = 0 // Barbarian Clans mode: turns until camp converts to city-state

    // Barbarian Clans mode: clan type (Scientific, Cultured, Maritime, Mercantile, Militaristic, Religious)
    var clanType: String? = null

    @Transient
    lateinit var gameInfo: GameInfo

    constructor(position: HexCoord): this() {
        this.position = position
    }

    fun clone(): BarbarianEncampment {
        val toReturn = BarbarianEncampment(position)
        toReturn.countdown = countdown
        toReturn.spawnedUnits = spawnedUnits
        toReturn.destroyed = destroyed
        toReturn.clansConversionTurns = clansConversionTurns
        toReturn.clanType = clanType
        return toReturn
    }

    fun update() {
        if (countdown > 0) // Not yet
            countdown--
        // Countdown at 0, try to spawn a barbarian
        else if (!destroyed && gameInfo.barbarians.spawnBarbarian(gameInfo.tileMap[position]) != null) { 
            // Successful
            spawnedUnits++
            resetCountdown()
        }
    }

    fun updateClansConversion() {
        if (destroyed) return
        val barbarianCiv = gameInfo.getBarbarianCivilization()
        val isBarbarianClans = barbarianCiv.gameModes.isBarbarianClans
        if (!isBarbarianClans) return

        if (clansConversionTurns > 0) {
            clansConversionTurns--
            if (clansConversionTurns == 0) {
                convertToCityState()
            }
        }
    }

    private fun convertToCityState() {
        val tile = gameInfo.tileMap[position] ?: return
        tile.setImprovement(null)
        destroyed = true

        val clanTypeName = clanType ?: "Cultured"
        // Create actual city-state with the clan type
        val barbarianCiv = gameInfo.getBarbarianCivilization()
        val cityState = barbarianCiv.cityStateFunctions.createCityStateFromBarbarianCamp(tile, clanTypeName)

        for (civ in gameInfo.civilizations.filter { it.isMajorCiv() && !it.isDefeated() }) {
            if (civ.hasExplored(tile)) {
                civ.addNotification(
                    "A barbarian camp has become the [${cityState.civName}] city-state! (+50 Gold, +20 Culture)",
                    tile.position,
                    com.unciv.logic.civilization.NotificationCategory.Diplomacy,
                    com.unciv.logic.civilization.NotificationIcon.Gold)
                civ.addGold(50)
                civ.addStat(com.unciv.models.stats.Stat.Culture, 20)
            }
        }
    }

    fun wasAttacked() {
        if (!destroyed)
            countdown /= 2
    }

    fun wasDestroyed() {
        if (!destroyed) {
            countdown = 15
            destroyed = true
        }
    }

    /** When a barbarian is spawned, seed the counter for next spawn */
    private fun resetCountdown() {
        val rng = gameInfo.getBarbarianCivilization().state.stateBasedRandom("BarbarianManager.resetCooldown")
        // Base 8-12 turns
        countdown = 8 + rng.nextInt(5)
        // Quicker on Raging Barbarians
        if (gameInfo.gameParameters.ragingBarbarians)
            countdown /= 2
        // Higher on low difficulties
        countdown += gameInfo.ruleset.difficulties[gameInfo.gameParameters.difficulty]!!.barbarianSpawnDelay
        // Quicker if this camp has already spawned units
        countdown -= spawnedUnits.coerceAtMost(3)

        countdown = (countdown * gameInfo.speed.barbarianModifier).toInt()
    }
}
