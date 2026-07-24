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
    var emergencyDataList = mutableListOf<EmergencyData>()
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
        toReturn.emergencyDataList.addAll(emergencyDataList.map { it.copy() })
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
    }

    fun processEmergenciesEachTurn() {
        val currentTurn = civInfo.gameInfo.turns

        // Check for new emergency triggers
        checkEmergencyTriggers(currentTurn)

        // Process active emergencies
        for (emergencyData in emergencyDataList.toList()) {
            if (emergencyData.isResolved) continue

            val turnsActive = currentTurn - emergencyData.triggerTurn
            if (turnsActive >= emergencyData.duration) {
                resolveSingleEmergency(emergencyData)
            }
        }
    }

    private fun resolveResolutions() {
        for (resolution in activeResolutions.toList()) {
            if (shouldResolutionPass(resolution)) {
                applyResolutionEffects(resolution)
            }
            activeResolutions.remove(resolution)
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

    private fun checkEmergencyTriggers(currentTurn: Int) {
        if (emergencyDataList.any { !it.isResolved }) return

        val aliveMajorCivs = civInfo.gameInfo.civilizations.filter { it.isMajorCiv() && !it.isDefeated() }
        if (aliveMajorCivs.size < 3) return

        for (civ in aliveMajorCivs) {
            if (civ == civInfo) continue

            val recentCaptures = civ.cities.count { city ->
                city.foundingCivObject != null && city.foundingCivObject != civ
                        && city.foundingCivObject != civInfo
            }
            if (recentCaptures >= 2) {
                val participants = aliveMajorCivs.filter { it != civ && it != civInfo }
                val emergency = EmergencyData(
                    type = EmergencyType.Military,
                    targetCivId = civ.civID,
                    triggerTurn = currentTurn,
                    duration = 30,
                    participantCivIds = participants.map { it.civID }.toMutableSet()
                )
                emergencyDataList.add(emergency)
                activeEmergencies.add("Military_${civ.civID}")

                for (participant in participants) {
                    participant.addNotification("A Military Emergency has been declared against [${civ.civName}]! Join the war to earn rewards!",
                        NotificationCategory.Diplomacy, civ.civName)
                }
                civ.addNotification("A Military Emergency has been declared against you!",
                    NotificationCategory.Diplomacy)
                return
            }
        }

        for (civ in aliveMajorCivs) {
            val religionObj = civ.religionManager.religion ?: continue
            val convertedCities = civInfo.gameInfo.getCities().count { city ->
                city.religion.getMajorityReligionName() == religionObj.name
            }
            val totalNonReligiousCivs = aliveMajorCivs.count { it.religionManager.religion == null }
            if (convertedCities >= 20 && totalNonReligiousCivs <= 2) {
                val participants = aliveMajorCivs.filter { it != civ && it.religionManager.religion?.name != religionObj.name }
                if (participants.size >= 2) {
                    val emergency = EmergencyData(
                        type = EmergencyType.Religious,
                        targetCivId = civ.civID,
                        triggerTurn = currentTurn,
                        duration = 30,
                        participantCivIds = participants.map { it.civID }.toMutableSet()
                    )
                    emergencyDataList.add(emergency)
                    activeEmergencies.add("Religious_${civ.civID}")
                    for (participant in participants) {
                        participant.addNotification("A Religious Emergency has been declared against [${civ.civName}]! Convert their cities to stop them!",
                            NotificationCategory.Diplomacy, civ.civName)
                    }
                    return
                }
            }
        }
    }

    private fun resolveSingleEmergency(emergencyData: EmergencyData) {
        emergencyData.isResolved = true
        activeEmergencies.remove("${emergencyData.type}_${emergencyData.targetCivId}")

        val targetCiv = civInfo.gameInfo.getCivilization(emergencyData.targetCivId) ?: return

        when (emergencyData.type) {
            EmergencyType.Military -> {
                // Participants who went to war with target get rewards
                for (participantId in emergencyData.participantCivIds) {
                    val participant = civInfo.gameInfo.getCivilization(participantId)
                    if (participant.isAtWarWith(targetCiv)) {
                        val reward = 30
                        participant.worldCongress.addDiplomaticFavor(reward)
                        participant.addNotification("Military Emergency resolved! You remained at war with [${targetCiv.civName}] and earned [$reward] Diplomatic Favor!",
                            NotificationCategory.Diplomacy, targetCiv.civName)
                    }
                }
                if (targetCiv.isDefeated()) {
                    for (participantId in emergencyData.participantCivIds) {
                        val participant = civInfo.gameInfo.getCivilization(participantId)
                        val reward = 50
                        participant.worldCongress.addDiplomaticFavor(reward)
                        participant.addNotification("[${targetCiv.civName}] has been defeated! Emergency participants earn [$reward] bonus Diplomatic Favor!",
                            NotificationCategory.Diplomacy, targetCiv.civName)
                    }
                }
            }
            EmergencyType.Religious -> {
                for (participantId in emergencyData.participantCivIds) {
                    val participant = civInfo.gameInfo.getCivilization(participantId)
                    val targetReligion = targetCiv.religionManager.religion
                    val convertedBack = if (targetReligion != null) {
                        civInfo.gameInfo.getCities().count { city ->
                            city.religion.getMajorityReligionName() == targetReligion.name
                        }
                    } else 0
                    val reward = (convertedBack * 2).coerceAtLeast(10)
                    participant.worldCongress.addDiplomaticFavor(reward)
                    participant.addNotification("Religious Emergency resolved! You helped contain [${targetCiv.civName}]'s religion and earned [$reward] Diplomatic Favor!",
                        NotificationCategory.Diplomacy, targetCiv.civName)
                }
            }
            EmergencyType.AidRequest -> {
                val totalContributed = emergencyData.contributions.values.sum()
                for ((civId, contributed) in emergencyData.contributions) {
                    val contributor = civInfo.gameInfo.getCivilization(civId)
                    val reward = (contributed * 2f / totalContributed * 50).toInt().coerceAtLeast(5)
                    contributor.worldCongress.addDiplomaticFavor(reward)
                    contributor.addNotification("Aid Emergency resolved! Your contribution of [$contributed] gold earned you [$reward] Diplomatic Favor!",
                        NotificationCategory.Diplomacy)
                }
            }
            EmergencyType.Nuclear -> {
                for (participantId in emergencyData.participantCivIds) {
                    val participant = civInfo.gameInfo.getCivilization(participantId)
                    if (participant.isAtWarWith(targetCiv)) {
                        val reward = 40
                        participant.worldCongress.addDiplomaticFavor(reward)
                        participant.addNotification("Nuclear Emergency resolved! You helped contain [${targetCiv.civName}] and earned [$reward] Diplomatic Favor!",
                            NotificationCategory.Diplomacy, targetCiv.civName)
                    }
                }
            }
            EmergencyType.Climate -> {
                for (participantId in emergencyData.participantCivIds) {
                    val participant = civInfo.gameInfo.getCivilization(participantId)
                    val co2Reduced = 50
                    participant.climateManager.addCO2(-co2Reduced)
                    val reward = 20
                    participant.worldCongress.addDiplomaticFavor(reward)
                    participant.addNotification("Climate Emergency resolved! You helped reduce CO2 and earned [$reward] Diplomatic Favor!",
                        NotificationCategory.Diplomacy)
                }
            }
        }
    }

    /** Called when a civ uses a nuclear weapon */
    fun triggerNuclearEmergency(attackerCiv: Civilization) {
        if (emergencyDataList.any { it.type == EmergencyType.Nuclear && !it.isResolved }) return
        val participants = civInfo.gameInfo.civilizations.filter {
            it.isMajorCiv() && !it.isDefeated() && it != attackerCiv && it != civInfo
        }
        if (participants.size < 2) return
        val emergency = EmergencyData(
            type = EmergencyType.Nuclear,
            targetCivId = attackerCiv.civID,
            triggerTurn = civInfo.gameInfo.turns,
            duration = 30,
            participantCivIds = participants.map { it.civID }.toMutableSet()
        )
        emergencyDataList.add(emergency)
        activeEmergencies.add("Nuclear_${attackerCiv.civID}")
        for (participant in participants) {
            participant.addNotification("A Nuclear Emergency has been declared against [${attackerCiv.civName}] for using nuclear weapons!",
                NotificationCategory.Diplomacy, attackerCiv.civName)
        }
    }

    /** Called when a major natural disaster occurs */
    fun triggerAidRequestEmergency(targetCiv: Civilization) {
        if (emergencyDataList.any { it.type == EmergencyType.AidRequest && !it.isResolved }) return
        val participants = civInfo.gameInfo.civilizations.filter {
            it.isMajorCiv() && !it.isDefeated() && it != targetCiv && it != civInfo
        }
        if (participants.size < 2) return
        val emergency = EmergencyData(
            type = EmergencyType.AidRequest,
            targetCivId = targetCiv.civID,
            triggerTurn = civInfo.gameInfo.turns,
            duration = 20,
            participantCivIds = participants.map { it.civID }.toMutableSet()
        )
        emergencyDataList.add(emergency)
        activeEmergencies.add("AidRequest_${targetCiv.civID}")
        for (participant in participants) {
            participant.addNotification("An Aid Request has been issued for [${targetCiv.civName}]! Contribute gold to earn rewards!",
                NotificationCategory.Diplomacy, targetCiv.civName)
        }
    }

    fun contributeToAidRequest(contributorCiv: Civilization, amount: Int) {
        val aidEmergency = emergencyDataList.firstOrNull {
            it.type == EmergencyType.AidRequest && !it.isResolved
        } ?: return
        aidEmergency.contributions[contributorCiv.civID] =
            aidEmergency.contributions.getOrDefault(contributorCiv.civID, 0) + amount
    }

    fun getActiveEmergency(): EmergencyData? {
        return emergencyDataList.firstOrNull { !it.isResolved }
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