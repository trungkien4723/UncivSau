package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.models.Counter
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.TemporaryUnique
import com.unciv.models.ruleset.unique.UniqueType
import yairm210.purity.annotations.Readonly

/** Represents a single World Congress session: a set of resolutions to vote on */
class WorldCongressSession : IsPartOfGameInfoSerialization {
    var proposals = mutableListOf<String>()
    var resolutionVotes = HashMap<String, Counter<String>>() // resolution -> (civID -> favorSpent)
    var resolutionOutcomes = mutableMapOf<String, Boolean>() // resolution -> passed?
    var isDiplomaticVictorySession = false
    var sessionNumber = 0
}

class WorldCongressManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
    }

    var votesCast = HashMap<String, String?>()
    var activeResolutions = mutableSetOf<String>()
    var activeEmergencies = mutableSetOf<String>()
    var emergencyDataList = mutableListOf<EmergencyData>()
    var congressSession = 0
    var hasDiplomaticVictory = false

    /** Current session being voted on (set globally when congress is in session) */
    var currentSession: WorldCongressSession? = null

    companion object {
        const val DIPLOMATIC_VICTORY_THRESHOLD = 300
        const val FAVOR_COST_PER_VOTE = 10

        val AVAILABLE_RESOLUTIONS = listOf(
            "World Religion",
            "Global Ban on Nuclear Weapons",
            "Global Trade Agreements",
            "International Space Station",
            "Universal Human Rights",
            "City of a Thousand Domes",
            "Trade Embargo"
        )
    }

    fun clone(): WorldCongressManager {
        val toReturn = WorldCongressManager()
        // Don't copy diplomaticFavor - it comes from civInfo
        toReturn.votesCast.putAll(votesCast)
        toReturn.activeResolutions.addAll(activeResolutions)
        toReturn.activeEmergencies.addAll(activeEmergencies)
        toReturn.emergencyDataList.addAll(emergencyDataList.map { it.copy() })
        toReturn.congressSession = congressSession
        toReturn.hasDiplomaticVictory = hasDiplomaticVictory
        toReturn.currentSession = currentSession
        return toReturn
    }

    fun addDiplomaticFavor(amount: Int) {
        civInfo.addDiplomaticFavor(amount)
    }

    fun spendDiplomaticFavor(amount: Int): Boolean {
        if (civInfo.diplomaticFavor < amount) return false
        civInfo.addDiplomaticFavor(-amount)
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
        val session = WorldCongressSession()
        session.sessionNumber = congressSession
        session.isDiplomaticVictorySession = (congressSession % 3 == 0)

        // Pick 2 random resolutions for this session
        val chosen = AVAILABLE_RESOLUTIONS.shuffled().take(2)
        session.proposals.addAll(chosen)

        currentSession = session
        votesCast.clear()
    }

    fun endCongressSession(): WorldCongressSession? {
        val session = currentSession ?: return null
        resolveResolutions(session)
        currentSession = null
        return session
    }

    private fun resolveResolutions(session: WorldCongressSession) {
        for (proposal in session.proposals) {
            val votes = session.resolutionVotes[proposal] ?: Counter()
            val forVotes = votes.values.sum()
            val againstVotes = votes.values.filter { it < 0 }.sum().let { -it }
            val passed = forVotes > againstVotes && forVotes > 0
            session.resolutionOutcomes[proposal] = passed

            if (passed) {
                applyResolutionEffects(proposal)
                for (civ in civInfo.gameInfo.civilizations) {
                    if (!civ.isDefeated() && !civ.isBarbarian)
                        civ.addNotification("[${proposal}] has been passed by the World Congress!",
                            NotificationCategory.Diplomacy)
                }
            } else {
                for (civ in civInfo.gameInfo.civilizations) {
                    if (!civ.isDefeated() && !civ.isBarbarian)
                        civ.addNotification("[${proposal}] has been rejected by the World Congress.",
                            NotificationCategory.Diplomacy)
                }
            }
        }

        // Award favor to all participants
        val voters = session.resolutionVotes.flatMap { it.value.keys }.toSet()
        for (civId in voters) {
            val civ = civInfo.gameInfo.getCivilization(civId)
            if (civ != null) {
                civ.worldCongress.addDiplomaticFavor(5)
            }
        }
    }

    /** AI auto-votes on all active resolutions in the current session */
    fun aiAutoVote() {
        val session = currentSession ?: return
        if (civInfo.isDefeated() || civInfo.isBarbarian || civInfo.isSpectator()) return

        for (proposal in session.proposals) {
            // AI votes For if it likes the resolution, Against otherwise
            // Simple heuristic: always vote For if we can afford it
            if (civInfo.diplomaticFavor >= FAVOR_COST_PER_VOTE) {
                voteOnResolution(civInfo, proposal, FAVOR_COST_PER_VOTE, support = true)
            }
        }
    }

    fun voteOnResolution(voter: Civilization, resolution: String, favorAmount: Int, support: Boolean) {
        if (favorAmount <= 0) return
        if (!spendDiplomaticFavor(favorAmount)) return
        val session = currentSession ?: return
        if (resolution !in session.proposals) return

        val voteMap = session.resolutionVotes.getOrPut(resolution) { Counter() }
        val signedAmount = if (support) favorAmount else -favorAmount
        voteMap.add(voter.civID, signedAmount)
    }

    fun processEmergenciesEachTurn() {
        val currentTurn = civInfo.gameInfo.turns
        checkEmergencyTriggers(currentTurn)

        for (emergencyData in emergencyDataList.toList()) {
            if (emergencyData.isResolved) continue
            val turnsActive = currentTurn - emergencyData.triggerTurn
            if (turnsActive >= emergencyData.duration) {
                resolveSingleEmergency(emergencyData)
            }
        }
    }

    private fun applyResolutionEffects(resolution: String) {
        val duration = 60 // lasts until next world congress
        val globalUniques = getResolutionUniques(resolution)

        for (civ in civInfo.gameInfo.civilizations) {
            if (civ.isDefeated() || civ.isBarbarian) continue
            for (uniqueString in globalUniques) {
                val unique = Unique(uniqueString)
                civ.temporaryUniques.add(TemporaryUnique(unique, duration))
            }
            civ.worldCongress.addDiplomaticFavor(10)
        }
    }

    private fun getResolutionUniques(resolution: String): List<String> {
        return when (resolution) {
            "World Religion" -> listOf(
                "[+5] [Faith] from every [Holy Site]",
                "[+5] Religious Strength <for [All] units>"
            )
            "Global Ban on Nuclear Weapons" -> listOf(
                "Cannot build [Nuclear] units"
            )
            "Global Trade Agreements" -> listOf(
                "[+3] [Gold] from [Trade Routes]"
            )
            "International Space Station" -> listOf(
                "[+15]% [Science] [in all cities]"
            )
            "Universal Human Rights" -> listOf(
                "[+1] [Amenities] [in all cities]",
                "[+10]% [Culture] [in all cities]"
            )
            "City of a Thousand Domes" -> listOf(
                "[+4] [Housing] [in all cities]"
            )
            "Trade Embargo" -> listOf(
                "[-30]% [Gold] from Trade Routes"
            )
            else -> emptyList()
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
                    participant.worldCongress.addDiplomaticFavor(20)
                    participant.addNotification("Climate Emergency resolved! You earned [20] Diplomatic Favor!",
                        NotificationCategory.Diplomacy)
                }
            }
        }
    }

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
            totalFavor += civ.diplomaticFavor
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
    fun getTotalDiplomaticFavor(): Int = civInfo.diplomaticFavor

    fun canSpendDiplomaticFavor(amount: Int): Boolean = civInfo.diplomaticFavor >= amount

    fun getVotingPower(civID: String): Int {
        val civ = civInfo.gameInfo.getCivilization(civID) ?: return 0
        return civ.diplomaticFavor
    }

    fun totalVotingPower(): Int {
        return civInfo.gameInfo.civilizations.sumOf { it.diplomaticFavor }
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
        return civInfo.diplomaticFavor >= 5
    }

    fun spendFavorForVote(amount: Int): Boolean {
        return spendDiplomaticFavor(amount)
    }

    fun getSpecialSessionCooldown(): Int {
        return congressSession * 10
    }

    fun isEmergencyActive(emergency: String): Boolean = activeEmergencies.contains(emergency)
    fun isResolutionActive(resolution: String): Boolean = activeResolutions.contains(resolution)

    /** Get the current session's proposals for a player's pick screen */
    @Readonly
    fun getCurrentProposals(): List<String> = currentSession?.proposals?.toList() ?: emptyList()

    /** Check if this civ has voted on all resolutions in the current session */
    @Readonly
    fun hasVotedOnAllResolutions(): Boolean {
        val session = currentSession ?: return true
        if (session.proposals.isEmpty()) return true
        return session.proposals.all { proposal ->
            val votes = session.resolutionVotes[proposal] ?: return@all false
            civInfo.civID in votes
        }
    }

    /** Check if all major civs have voted */
    @Readonly
    fun allMajorCivsHaveVoted(): Boolean {
        val session = currentSession ?: return true
        val majorCivs = civInfo.gameInfo.civilizations.filter { it.isMajorCiv() && !it.isDefeated() }
        return majorCivs.all { civ ->
            session.proposals.all { proposal ->
                val votes = session.resolutionVotes[proposal]
                votes != null && civ.civID in votes
            }
        }
    }

    fun getFavorForResolution(resolution: String): Int {
        val votes = currentSession?.resolutionVotes?.get(resolution) ?: return 0
        return votes.values.sum()
    }

    fun getFavorForAgainst(resolution: String): Pair<Int, Int> {
        val votes = currentSession?.resolutionVotes?.get(resolution) ?: return 0 to 0
        var forFavor = 0
        var againstFavor = 0
        for ((_, amount) in votes) {
            if (amount > 0) forFavor += amount
            else againstFavor += -amount
        }
        return forFavor to againstFavor
    }
}
