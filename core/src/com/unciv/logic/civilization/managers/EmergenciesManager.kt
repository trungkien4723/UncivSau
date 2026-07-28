package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.civilization.NotificationIcon
import com.unciv.logic.civilization.diplomacy.DiplomaticStatus
import yairm210.purity.annotations.Readonly
import kotlin.math.max

data class Emergency(
    val id: String,
    val type: EmergencyType,
    val targetCivId: String,
    val initiatorCivId: String,
    val triggerTurn: Int,
    val duration: Int,
    val participantCivIds: MutableSet<String> = mutableSetOf(),
    val contributions: MutableMap<String, Int> = mutableMapOf(),
    var isResolved: Boolean = false,
    var isFailed: Boolean = false
) : IsPartOfGameInfoSerialization

class EmergenciesManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var activeEmergencies = mutableListOf<Emergency>()
    var completedEmergencies = mutableListOf<String>()
    var emergencyCounter = 0

    fun clone(): EmergenciesManager {
        val toReturn = EmergenciesManager()
        toReturn.activeEmergencies.addAll(activeEmergencies.map { it.copy(
            participantCivIds = it.participantCivIds.toMutableSet(),
            contributions = it.contributions.toMutableMap()
        ) })
        toReturn.completedEmergencies.addAll(completedEmergencies)
        toReturn.emergencyCounter = emergencyCounter
        return toReturn
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    @Readonly
    fun isParticipant(emergencyId: String, civId: String): Boolean {
        return activeEmergencies.any { it.id == emergencyId && civId in it.participantCivIds }
    }

    @Readonly
    fun getActiveEmergenciesForCiv(civId: String): List<Emergency> {
        return activeEmergencies.filter { civId in it.participantCivIds || it.targetCivId == civId }
    }

    /**
     * Trigger a new emergency.
     * Only the world leader or a major civ can trigger emergencies.
     */
    fun triggerEmergency(type: EmergencyType, targetCivId: String) {
        if (!civInfo.isMajorCiv()) return
        if (civInfo.isBarbarian || civInfo.isDefeated()) return

        emergencyCounter++
        val emergencyId = "emergency_${emergencyCounter}_${type.name}"

        val emergency = Emergency(
            id = emergencyId,
            type = type,
            targetCivId = targetCivId,
            initiatorCivId = civInfo.civID,
            triggerTurn = civInfo.gameInfo.turns,
            duration = getEmergencyDuration(type)
        )

        activeEmergencies.add(emergency)
        civInfo.gameModes.activateEmergency(emergencyId)

        civInfo.addNotification(
            "An emergency has been declared: [${getEmergencyName(type)}] against [${getCivName(targetCivId)}]!",
            NotificationCategory.Diplomacy,
            "StatIcons/Warning"
        )

        notifyOtherCivs(emergency)
    }

    @Readonly
    private fun getEmergencyName(type: EmergencyType): String = when (type) {
        EmergencyType.Military -> "Military Emergency"
        EmergencyType.Religious -> "Religious Emergency"
        EmergencyType.AidRequest -> "Aid Request"
        EmergencyType.Nuclear -> "Nuclear Emergency"
        EmergencyType.Climate -> "Climate Emergency"
    }

    @Readonly
    private fun getEmergencyDuration(type: EmergencyType): Int = when (type) {
        EmergencyType.Military -> 30
        EmergencyType.Religious -> 30
        EmergencyType.AidRequest -> 20
        EmergencyType.Nuclear -> 20
        EmergencyType.Climate -> 40
    }

    private fun getCivName(civId: String): String {
        return civInfo.gameInfo.getCivilization(civId).civName
    }

    /**
     * Ask other civs to join the emergency.
     */
    private fun notifyOtherCivs(emergency: Emergency) {
        for (otherCiv in civInfo.gameInfo.civilizations) {
            if (otherCiv.isDefeated() || otherCiv.isBarbarian || otherCiv.isSpectator()) continue
            if (otherCiv == civInfo) continue
            if (otherCiv.civID == emergency.targetCivId) continue

            if (shouldJoinEmergency(otherCiv, emergency)) {
                emergency.participantCivIds.add(otherCiv.civID)
                otherCiv.addNotification(
                    "You have joined the [${getEmergencyName(emergency.type)}] against [${getCivName(emergency.targetCivId)}]!",
                    NotificationCategory.Diplomacy,
                    "StatIcons/Warning"
                )
            }
        }
    }

    @Readonly
    private fun shouldJoinEmergency(civ: Civilization, emergency: Emergency): Boolean {
        if (!civ.isMajorCiv()) return false
        val targetCiv = civInfo.gameInfo.getCivilization(emergency.targetCivId)
        val diplo = civ.diplomacy[targetCiv.civID] ?: return false
        return diplo.diplomaticStatus == DiplomaticStatus.War
    }

    /**
     * Contribute to an emergency (e.g., gold for Aid Request, military units for Military).
     */
    fun contributeToEmergency(emergencyId: String, amount: Int) {
        val emergency = activeEmergencies.firstOrNull { it.id == emergencyId } ?: return
        val currentAmount = emergency.contributions[civInfo.civID] ?: 0
        emergency.contributions[civInfo.civID] = currentAmount + amount
    }

    /**
     * Process emergencies each turn.
     */
    fun processEmergenciesEachTurn() {
        val toRemove = mutableListOf<Emergency>()
        for (emergency in activeEmergencies.toList()) {
            if (emergency.isResolved || emergency.isFailed) {
                toRemove.add(emergency)
                continue
            }

            val turnsElapsed = civInfo.gameInfo.turns - emergency.triggerTurn
            if (turnsElapsed >= emergency.duration) {
                resolveEmergency(emergency)
                toRemove.add(emergency)
            }
        }
        for (e in toRemove) {
            activeEmergencies.remove(e)
            completedEmergencies.add(e.id)
            civInfo.gameModes.deactivateEmergency(e.id)
        }
    }

    private fun resolveEmergency(emergency: Emergency) {
        val targetCiv = civInfo.gameInfo.getCivilization(emergency.targetCivId)
        if (targetCiv.isDefeated()) {
            emergency.isResolved = true
            rewardParticipants(emergency)
        } else {
            emergency.isFailed = true
        }
    }

    private fun rewardParticipants(emergency: Emergency) {
        for (participantId in emergency.participantCivIds) {
            val participant = civInfo.gameInfo.getCivilization(participantId)
            if (participant.isDefeated()) continue

            val reward = getEmergencyReward(emergency.type)
            participant.addGold(reward)
            participant.addDiplomaticFavor(reward / 10)

            participant.addNotification(
                "You have been rewarded for participating in the [${getEmergencyName(emergency.type)}]!",
                NotificationCategory.Diplomacy,
                NotificationIcon.Gold
            )
        }
    }

    @Readonly
    private fun getEmergencyReward(type: EmergencyType): Int = when (type) {
        EmergencyType.Military -> 500
        EmergencyType.Religious -> 400
        EmergencyType.AidRequest -> 300
        EmergencyType.Nuclear -> 600
        EmergencyType.Climate -> 350
    }
}
