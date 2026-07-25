package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import yairm210.purity.annotations.Readonly

class GameModesManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

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
    }

    fun spawnHero(hero: String) {
        if (spawnedHeroes.size < 3) {
            spawnedHeroes.add(hero)
        }
    }

    fun startRockBand(unitName: String) {
        activeRockBands.add(unitName)
    }

    fun foundCorporation(resource: String) {
        activeCorporations.add(resource)
    }

    fun establishMonopoly(resource: String) {
        activeMonopolies.add(resource)
    }

    fun setZombieMode(enabled: Boolean) {
        isZombie = enabled
    }

    fun setApocalypseMode(enabled: Boolean) {
        isApocalypse = enabled
    }

    fun setDramaticAgesMode(enabled: Boolean) {
        isDramaticAges = enabled
    }

    fun setBarbarianClansMode(enabled: Boolean) {
        isBarbarianClans = enabled
    }

    fun setTechShuffleMode(enabled: Boolean) {
        isTechShuffle = enabled
    }

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
        return when (mode) {
            "Zombie" -> isZombie
            "Apocalypse" -> isApocalypse
            "DramaticAges" -> isDramaticAges
            "SecretSocieties" -> activeSecretSocieties.isNotEmpty()
            "Heroes" -> spawnedHeroes.isNotEmpty()
            "Monopolies" -> activeMonopolies.isNotEmpty()
            "Corporations" -> activeCorporations.isNotEmpty()
            "RockBands" -> activeRockBands.isNotEmpty()
            "BarbarianClans" -> isBarbarianClans
            "TechShuffle" -> isTechShuffle
            else -> false
        }
    }

    @Readonly
    fun getAllActiveGameModes(): List<String> {
        val modes = mutableListOf<String>()
        if (isZombie) modes.add("Zombie")
        if (isApocalypse) modes.add("Apocalypse")
        if (isDramaticAges) modes.add("DramaticAges")
        if (activeSecretSocieties.isNotEmpty()) modes.add("SecretSocieties")
        if (spawnedHeroes.isNotEmpty()) modes.add("Heroes")
        if (activeMonopolies.isNotEmpty()) modes.add("Monopolies")
        if (activeCorporations.isNotEmpty()) modes.add("Corporations")
        if (activeRockBands.isNotEmpty()) modes.add("RockBands")
        if (isBarbarianClans) modes.add("BarbarianClans")
        if (isTechShuffle) modes.add("TechShuffle")
        return modes
    }

    fun activateAllGameModes() {
        isZombie = true
        isApocalypse = true
        isDramaticAges = true
        isBarbarianClans = true
        isTechShuffle = true
    }
}