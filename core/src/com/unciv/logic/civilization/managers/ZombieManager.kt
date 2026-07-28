package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.logic.map.tile.Tile
import yairm210.purity.annotations.Readonly
import kotlin.math.max

data class DownedZombie(
    val tileX: Int,
    val tileY: Int,
    var turnsLeft: Int
)

class ZombieManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var zombieUnitsSpawned = 0
    var zombieTurnsToRevive = 3
    var zombieSpawnChance = 0.5f
    var downedZombies = mutableListOf<DownedZombie>()

    fun clone(): ZombieManager {
        val toReturn = ZombieManager()
        toReturn.zombieUnitsSpawned = zombieUnitsSpawned
        toReturn.zombieTurnsToRevive = zombieTurnsToRevive
        toReturn.zombieSpawnChance = zombieSpawnChance
        toReturn.downedZombies.addAll(downedZombies.map { it.copy() })
        return toReturn
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    @Readonly
    fun isZombieUnit(unit: MapUnit): Boolean {
        return unit.name.contains("Zombie", ignoreCase = true)
    }

    /**
     * Called when a unit is killed/destroyed.
     * If Zombie mode is active and the killed unit is not a zombie,
     * there's a chance a zombie barbarian unit spawns on the tile.
     */
    fun onUnitKilled(killedUnit: MapUnit, tile: Tile?) {
        if (!civInfo.gameModes.isZombieModeActive()) return
        if (tile == null) return
        if (isZombieUnit(killedUnit)) return

        val random = kotlin.random.Random
        if (random.nextFloat() >= zombieSpawnChance) return

        spawnZombie(tile)
    }

    /**
     * Spawn a zombie unit on or near the given tile.
     * Zombie units belong to the barbarian civilization.
     */
    private fun spawnZombie(tile: Tile) {
        val barbarianCiv = try {
            civInfo.gameInfo.getBarbarianCivilization()
        } catch (e: NoSuchElementException) {
            return
        }

        val zombieUnitName = getZombieUnitName()
        if (!civInfo.gameInfo.ruleset.units.containsKey(zombieUnitName)) return

        val spawnedUnit = civInfo.gameInfo.tileMap.placeUnitNearTile(tile.position, zombieUnitName, barbarianCiv)
        if (spawnedUnit != null) {
            zombieUnitsSpawned++
            civInfo.addNotification(
                "A Zombie has risen from the dead!",
                tile.position,
                NotificationCategory.Units,
                zombieUnitName
            )
        }
    }

    @Readonly
    private fun getZombieUnitName(): String {
        val zombieUnits = civInfo.gameInfo.ruleset.units.values.filter {
            it.name.contains("Zombie", ignoreCase = true)
        }
        if (zombieUnits.isEmpty()) return "Zombie"
        return zombieUnits.first().name
    }

    /**
     * When a zombie is "killed" (health reaches 0), instead of being destroyed,
     * it goes down and revives after zombieTurnsToRevive turns.
     * Returns true if the zombie was downed (not destroyed).
     */
    fun downZombie(zombie: MapUnit): Boolean {
        if (!isZombieUnit(zombie)) return false

        val tile = zombie.currentTile
        downedZombies.add(DownedZombie(tile.position.x, tile.position.y, zombieTurnsToRevive))
        zombieUnitsSpawned = max(0, zombieUnitsSpawned - 1)

        civInfo.addNotification(
            "A Zombie has been knocked down! It will revive in [$zombieTurnsToRevive] turns.",
            tile.position,
            NotificationCategory.Units,
            zombie.name
        )
        return true
    }

    /**
     * Process zombie revival.
     * Called each turn to revive downed zombies.
     */
    fun processZombieRevival() {
        if (!civInfo.gameModes.isZombieModeActive()) return

        val toRevive = mutableListOf<DownedZombie>()
        val iterator = downedZombies.iterator()
        while (iterator.hasNext()) {
            val dz = iterator.next()
            dz.turnsLeft--
            if (dz.turnsLeft <= 0) {
                iterator.remove()
                toRevive.add(dz)
            }
        }

        for (dz in toRevive) {
            val tile = civInfo.gameInfo.tileMap[dz.tileX, dz.tileY]
            if (tile != null) {
                spawnZombie(tile)
            }
        }
    }
}
