package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.government.Government
import com.unciv.models.ruleset.government.PolicyCard
import com.unciv.models.ruleset.unique.UniqueMap
import yairm210.purity.annotations.Readonly

/** Civ VI Government + Policy Card manager. Tracks the active government and the policy cards slotted into it. */
class GovernmentManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    /** Transient aggregate of uniques from the current government and all assigned policy cards. */
    @Transient
    internal var governmentUniques = UniqueMap()

    /** Currently adopted government (name). Serialized. Defaults to the ruleset's first government. */
    var currentGovernment: String = ""

    /** Names of policy cards currently assigned to slots, in slot order. Serialized. */
    var assignedCards = ArrayList<String>()

    fun clone(): GovernmentManager {
        val toReturn = GovernmentManager()
        toReturn.currentGovernment = currentGovernment
        toReturn.assignedCards.addAll(assignedCards)
        return toReturn
    }

    @Readonly
    private fun getRuleset() = civInfo.gameInfo.ruleset

    @Readonly
    fun getGovernment(): Government? = if (currentGovernment.isEmpty()) null else getRuleset().governments[currentGovernment]

    @Readonly
    fun isGovernmentAdopted(): Boolean = currentGovernment.isNotEmpty()

    @Readonly
    fun getAssignedCardObjects(): List<PolicyCard> =
        assignedCards.mapNotNull { getRuleset().policyCards[it] }

    /** True when the number and category of assigned cards matches the current government's slots. */
    @Readonly
    fun isValid(): Boolean {
        val government = getGovernment() ?: return false
        val slots = government.getSlots()
        if (assignedCards.size != slots.size) return false
        val cardsByName = getRuleset().policyCards
        for (i in slots.indices) {
            val card = cardsByName[assignedCards[i]] ?: return false
            if (slots[i] != "Wildcard" && card.slotType != "Wildcard" && card.slotType != slots[i])
                return false
        }
        return true
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
        if (currentGovernment.isEmpty()) {
            currentGovernment = getRuleset().governments.keys.firstOrNull() ?: ""
        }
        rebuildTransients()
    }

    private fun rebuildTransients() {
        governmentUniques.clear()
        val ruleset = getRuleset()
        ruleset.governments[currentGovernment]?.let { governmentUniques.addUniques(it.uniqueObjects) }
        for (cardName in assignedCards) {
            ruleset.policyCards[cardName]?.let { governmentUniques.addUniques(it.uniqueObjects) }
        }
    }

    /** Adopt a government, clearing any previously assigned cards (cards are government-specific). */
    fun adoptGovernment(governmentName: String) {
        currentGovernment = governmentName
        assignedCards.clear()
        rebuildTransients()
    }

    /** Assign (or replace) a policy card into slot [slotIndex]. A null/empty cardName clears that slot. */
    fun assignCard(slotIndex: Int, cardName: String?) {
        val government = getGovernment() ?: return
        if (slotIndex < 0 || slotIndex >= government.totalSlots()) return
        if (cardName == null || cardName.isEmpty()) {
            if (slotIndex < assignedCards.size) assignedCards.removeAt(slotIndex)
        } else {
            val slots = government.getSlots()
            val slotType = slots[slotIndex]
            val card = getRuleset().policyCards[cardName] ?: return
            if (slotType != "Wildcard" && card.slotType != "Wildcard" && card.slotType != slotType) return
            // Prevent the same card occupying two slots
            assignedCards.remove(cardName)
            while (assignedCards.size <= slotIndex) assignedCards.add("")
            assignedCards[slotIndex] = cardName
        }
        rebuildTransients()
    }

    /** Whether a policy card is currently available (its required civic, if any, is researched). */
    @Readonly
    fun isCardAvailable(card: PolicyCard): Boolean {
        if (card.requiredCivic.isEmpty()) return true
        return civInfo.civics.isResearched(card.requiredCivic)
    }

    /** Whether a government is currently available (its required civic, if any, is researched). */
    @Readonly
    fun isGovernmentAvailable(government: Government): Boolean {
        if (government.requiredCivic.isEmpty()) return true
        return civInfo.civics.isResearched(government.requiredCivic)
    }
}
