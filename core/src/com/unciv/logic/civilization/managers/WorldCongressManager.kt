package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.models.Counter
import yairm210.purity.annotations.Readonly

class WorldCongressManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var diplomaticFavor = 0
    var votesCast = HashMap<String, String?>()
    var activeResolutions = mutableSetOf<String>()
    var activeEmergencies = mutableSetOf<String>()
    var congressSession = 0
    var hasDiplomaticVictory = false

    companion object {
        const val DIPLOMATIC_VICTORY_THRESHOLD = 300
    }

    fun clone(): WorldCongressManager {
        val toReturn = WorldCongressManager()
        toReturn.diplomaticFavor = diplomaticFavor
        toReturn.votesCast.putAll(votesCast)
        toReturn.activeResolutions.addAll(activeResolutions)
        toReturn.activeEmergencies.addAll(activeEmergencies)
        toReturn.congressSession = congressSession
        toReturn.hasDiplomaticVictory = hasDiplomaticVictory
        return toReturn
    }

    fun addDiplomaticFavor(amount: Int) {
        diplomaticFavor += amount
    }

    fun spendDiplomaticFavor(amount: Int): Boolean {
        if (diplomaticFavor < amount) return false
        diplomaticFavor -= amount
        return true
    }

    fun castVote(voterCivID: String, targetCivID: String?) {
        votesCast[voterCivID] = targetCivID
    }

    fun proposeResolution(resolution: String) {
        activeResolutions.add(resolution)
    }

    fun declareEmergency(emergency: String) {
        activeEmergencies.add(emergency)
    }

    fun startCongressSession() {
        congressSession++
        votesCast.clear()
    }

    fun endCongressSession() {
        resolveResolutions()
        resolveEmergencies()
    }

    private fun resolveResolutions() {
        for (resolution in activeResolutions.toList()) {
            if (shouldResolutionPass(resolution)) {
                applyResolutionEffects(resolution)
            }
            activeResolutions.remove(resolution)
        }
    }

    private fun resolveEmergencies() {
        for (emergency in activeEmergencies.toList()) {
            applyEmergencyEffects(emergency)
        }
    }

    private fun shouldResolutionPass(resolution: String): Boolean {
        val target = votesCast.values.count { it == "Pass" }
        val oppose = votesCast.values.count { it == "Oppose" }
        return target > oppose
    }

    private fun applyResolutionEffects(resolution: String) {
        when (resolution) {
            "World Religion" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.worldCongress.addDiplomaticFavor(10)
                }
            }
            "Global Ban on Nuclear Weapons" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.gameInfo.gameParameters.nuclearWeaponsEnabled = false
                }
            }
            "Global Trade Agreements" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.worldCongress.addDiplomaticFavor(5)
                }
            }
            "International Space Station" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.worldCongress.addDiplomaticFavor(8)
                }
            }
            "Universal Human Rights" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.worldCongress.addDiplomaticFavor(6)
                }
            }
            "City of a Thousand Domes" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.worldCongress.addDiplomaticFavor(7)
                }
            }
        }
    }

    private fun applyEmergencyEffects(emergency: String) {
        when (emergency) {
            "Natural Disaster" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    if (civ != civInfo) {
                        civ.addNotification("World Congress declared a Natural Disaster Emergency!", NotificationCategory.General)
                    }
                }
            }
            "Climate Crisis" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.climateManager.addCO2(-50)
                }
            }
            "Invasion" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.worldCongress.addDiplomaticFavor(15)
                }
            }
            "Plague" -> {
                for (civ in civInfo.gameInfo.civilizations) {
                    civ.worldCongress.addDiplomaticFavor(12)
                }
            }
        }
    }

    fun checkDiplomaticVictory(): Boolean {
        if (hasDiplomaticVictory) return true
        val totalFavor = getDiplomaticVictoryProgress()
        if (totalFavor >= DIPLOMATIC_VICTORY_THRESHOLD) {
            hasDiplomaticVictory = true
            return true
        }
        return false
    }

    fun getDiplomaticVictoryProgress(): Int {
        var totalFavor = 0
        for (civ in civInfo.gameInfo.civilizations) {
            totalFavor += civ.worldCongress.diplomaticFavor
        }
        return totalFavor
    }

    @Readonly
    fun getVotesFor(civID: String): Int {
        var votes = 0
        for ((voter, target) in votesCast) {
            if (target == civID) votes++
        }
        return votes
    }

    @Readonly
    fun getTotalDiplomaticFavor(): Int = diplomaticFavor

    fun canSpendDiplomaticFavor(amount: Int): Boolean = diplomaticFavor >= amount

    fun getVotingPower(civID: String): Int {
        val civ = civInfo.gameInfo.getCivilization(civID) ?: return 0
        return civ.worldCongress.diplomaticFavor
    }

    fun totalVotingPower(): Int {
        return civInfo.gameInfo.civilizations.sumOf { it.worldCongress.diplomaticFavor }
    }

    fun resetEmergency(emergency: String) {
        activeEmergencies.remove(emergency)
    }

    fun isDiplomaticVictoryAchieved(): Boolean = hasDiplomaticVictory

    fun triggerDiplomaticVictory() {
        hasDiplomaticVictory = true
        for (civ in civInfo.gameInfo.civilizations) {
            civ.addNotification("Diplomatic Victory achieved!", NotificationCategory.Diplomacy)
        }
    }

    fun getResolutionsCount(): Int = activeResolutions.size

    fun getEmergenciesCount(): Int = activeEmergencies.size

    fun canProposeResolution(resolution: String): Boolean {
        return diplomaticFavor >= 5
    }

    fun spendFavorForVote(amount: Int): Boolean {
        return spendDiplomaticFavor(amount)
    }

    fun getSpecialSessionCooldown(): Int {
        return congressSession * 10
    }

    fun isEmergencyActive(emergency: String): Boolean = activeEmergencies.contains(emergency)

    fun isResolutionActive(resolution: String): Boolean = activeResolutions.contains(resolution)
}