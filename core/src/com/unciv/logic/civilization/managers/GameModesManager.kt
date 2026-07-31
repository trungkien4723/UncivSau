package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import yairm210.purity.annotations.Readonly

class GameModesManager : IsPartOfGameInfoSerialization {
    companion object {
        // Game mode keys, shared between GameParameters and this manager
        const val ZOMBIE = "Zombie"
        const val APOCALYPSE = "Apocalypse"
        const val DRAMATIC_AGES = "DramaticAges"
        const val BARBARIAN_CLANS = "BarbarianClans"
        const val TECH_SHUFFLE = "TechShuffle"
        const val SECRET_SOCIETIES = "SecretSocieties"
        const val HEROES = "Heroes"
        const val MONOPOLIES = "Monopolies"
        const val CORPORATIONS = "Corporations"
        const val ROCK_BANDS = "RockBands"

        val allGameModes = listOf(
            ZOMBIE, APOCALYPSE, DRAMATIC_AGES, BARBARIAN_CLANS, TECH_SHUFFLE,
            SECRET_SOCIETIES, HEROES, MONOPOLIES, ROCK_BANDS
        )
    }

    @Transient
    lateinit var civInfo: Civilization

    /** Game modes the player chose to enable at game start (or via an in-game option).
     *  Content-driven modes only activate while their mode is listed here. */
    var enabledGameModes = mutableSetOf<String>()

    var activeSecretSocieties = mutableSetOf<String>()
    var spawnedHeroes = mutableListOf<String>()
    var activeRockBands = mutableSetOf<String>()
    var activeCorporations = mutableSetOf<String>()
    var activeMonopolies = mutableSetOf<String>()
    var activeEmergencies = mutableSetOf<String>()
    var isZombie = false
    var isApocalypse = false
    var isDramaticAges = false
    var isBarbarianClans = false
    var isTechShuffle = false

    fun clone(): GameModesManager {
        val toReturn = GameModesManager()
        toReturn.enabledGameModes.addAll(enabledGameModes)
        toReturn.activeSecretSocieties.addAll(activeSecretSocieties)
        toReturn.spawnedHeroes.addAll(spawnedHeroes)
        toReturn.activeRockBands.addAll(activeRockBands)
        toReturn.activeCorporations.addAll(activeCorporations)
        toReturn.activeMonopolies.addAll(activeMonopolies)
        toReturn.activeEmergencies.addAll(activeEmergencies)
        toReturn.isZombie = isZombie
        toReturn.isApocalypse = isApocalypse
        toReturn.isDramaticAges = isDramaticAges
        toReturn.isBarbarianClans = isBarbarianClans
        toReturn.isTechShuffle = isTechShuffle
        return toReturn
    }

    fun joinSecretSociety(society: String) {
        activeSecretSocieties.add(society)
        enabledGameModes.add(SECRET_SOCIETIES)
    }

    fun spawnHero(hero: String) {
        if (spawnedHeroes.size < 3) {
            spawnedHeroes.add(hero)
            enabledGameModes.add(HEROES)
        }
    }

    fun startRockBand(unitName: String) {
        activeRockBands.add(unitName)
        enabledGameModes.add(ROCK_BANDS)
    }

    fun foundCorporation(resource: String) {
        activeCorporations.add(resource)
        enabledGameModes.add(CORPORATIONS)
    }

    fun establishMonopoly(resource: String) {
        activeMonopolies.add(resource)
        enabledGameModes.add(MONOPOLIES)
    }

    fun setZombieMode(enabled: Boolean) {
        isZombie = enabled
        if (enabled) enabledGameModes.add(ZOMBIE) else enabledGameModes.remove(ZOMBIE)
    }

    fun setApocalypseMode(enabled: Boolean) {
        isApocalypse = enabled
        if (enabled) enabledGameModes.add(APOCALYPSE) else enabledGameModes.remove(APOCALYPSE)
    }

    fun setDramaticAgesMode(enabled: Boolean) {
        isDramaticAges = enabled
        if (enabled) enabledGameModes.add(DRAMATIC_AGES) else enabledGameModes.remove(DRAMATIC_AGES)
    }

    fun setBarbarianClansMode(enabled: Boolean) {
        isBarbarianClans = enabled
        if (enabled) enabledGameModes.add(BARBARIAN_CLANS) else enabledGameModes.remove(BARBARIAN_CLANS)
    }

    fun setTechShuffleMode(enabled: Boolean) {
        isTechShuffle = enabled
        if (enabled) enabledGameModes.add(TECH_SHUFFLE) else enabledGameModes.remove(TECH_SHUFFLE)
    }

    /** Apply the game modes selected at game start (see [com.unciv.models.metadata.GameParameters.gameModes]). */
    fun setGameModes(gameModes: Collection<String>) {
        enabledGameModes.clear()
        enabledGameModes.addAll(gameModes)
        isZombie = ZOMBIE in gameModes
        isApocalypse = APOCALYPSE in gameModes
        isDramaticAges = DRAMATIC_AGES in gameModes
        isBarbarianClans = BARBARIAN_CLANS in gameModes
        isTechShuffle = TECH_SHUFFLE in gameModes
    }

    fun enableGameMode(mode: String) {
        enabledGameModes.add(mode)
    }

    @Readonly
    fun isGameModeEnabled(mode: String): Boolean = mode in enabledGameModes

    @Readonly
    fun hasSecretSociety(society: String): Boolean = activeSecretSocieties.contains(society)

    @Readonly
    fun getHeroCount(): Int = spawnedHeroes.size

    @Readonly
    fun hasMonopoly(resource: String): Boolean = activeMonopolies.contains(resource)

    @Readonly
    fun hasCorporation(resource: String): Boolean = activeCorporations.contains(resource)

    @Readonly
    fun isZombieModeActive(): Boolean = isZombie

    @Readonly
    fun isApocalypseModeActive(): Boolean = isApocalypse

    @Readonly
    fun isDramaticAgesModeActive(): Boolean = isDramaticAges

    fun isRockBandActive(unitName: String): Boolean = activeRockBands.contains(unitName)

    fun stopRockBand(unitName: String) {
        activeRockBands.remove(unitName)
    }

    fun getCultureFromRockBand(): Int {
        return activeRockBands.size * 30
    }

    fun getGoldFromRockBandPillage(): Int {
        return activeRockBands.size * 60
    }

    fun isSecretSocietyActive(society: String): Boolean = activeSecretSocieties.contains(society)

    fun canSpawnHero(): Boolean = spawnedHeroes.size < 3

    @Readonly
    fun getHeroes(): List<String> = spawnedHeroes.toList()

    fun isApocalypseDisasterActive(): Boolean = isApocalypse && activeEmergencies.isNotEmpty()

    fun activateEmergency(emergency: String) {
        activeEmergencies.add(emergency)
    }

    fun deactivateEmergency(emergency: String) {
        activeEmergencies.remove(emergency)
    }

    fun hasEmergency(emergency: String): Boolean = activeEmergencies.contains(emergency)

    @Readonly
    fun isGameModeActive(mode: String): Boolean {
        if (mode !in enabledGameModes) return false
        return when (mode) {
            ZOMBIE -> isZombie
            APOCALYPSE -> isApocalypse
            DRAMATIC_AGES -> isDramaticAges
            SECRET_SOCIETIES -> activeSecretSocieties.isNotEmpty()
            HEROES -> spawnedHeroes.isNotEmpty()
            MONOPOLIES -> activeMonopolies.isNotEmpty()
            CORPORATIONS -> activeCorporations.isNotEmpty()
            ROCK_BANDS -> activeRockBands.isNotEmpty()
            BARBARIAN_CLANS -> isBarbarianClans
            TECH_SHUFFLE -> isTechShuffle
            else -> false
        }
    }

    @Readonly
    fun getAllActiveGameModes(): List<String> {
        val modes = mutableListOf<String>()
        if (isZombie) modes.add(ZOMBIE)
        if (isApocalypse) modes.add(APOCALYPSE)
        if (isDramaticAges) modes.add(DRAMATIC_AGES)
        if (activeSecretSocieties.isNotEmpty()) modes.add(SECRET_SOCIETIES)
        if (spawnedHeroes.isNotEmpty()) modes.add(HEROES)
        if (activeMonopolies.isNotEmpty()) modes.add(MONOPOLIES)
        if (activeCorporations.isNotEmpty()) modes.add(CORPORATIONS)
        if (activeRockBands.isNotEmpty()) modes.add(ROCK_BANDS)
        if (isBarbarianClans) modes.add(BARBARIAN_CLANS)
        if (isTechShuffle) modes.add(TECH_SHUFFLE)
        return modes.filter { it in enabledGameModes }
    }

    fun activateAllGameModes() {
        enabledGameModes.addAll(allGameModes)
        isZombie = true
        isApocalypse = true
        isDramaticAges = true
        isBarbarianClans = true
        isTechShuffle = true
    }
}