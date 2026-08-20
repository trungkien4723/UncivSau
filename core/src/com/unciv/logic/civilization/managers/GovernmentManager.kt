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

    /** Civ 6: Whether to open the government picker (e.g., after unlocking a new government via civic) */
    var shouldOpenGovernmentPicker = false

    /** Used by NextTurnAction.PickGovernment.isChoice */
    @Readonly fun shouldShowGovernmentPicker(): Boolean = shouldOpenGovernmentPicker

    fun clone(): GovernmentManager {
        val toReturn = GovernmentManager()
        toReturn.currentGovernment = currentGovernment
        toReturn.assignedCards.addAll(assignedCards)
        toReturn.shouldOpenGovernmentPicker = shouldOpenGovernmentPicker
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
            if (currentGovernment.isNotEmpty()) {
                shouldOpenGovernmentPicker = true
            }
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
        shouldOpenGovernmentPicker = false
        rebuildTransients()
    }

    /** Assign (or replace) a policy card into slot [slotIndex]. A null/empty cardName clears that slot.
     *  The [assignedCards] list always mirrors the government's slot layout (one entry per slot), so
     *  clearing a slot can never shift or misalign the remaining cards. */
    fun assignCard(slotIndex: Int, cardName: String?) {
        val government = getGovernment() ?: return
        if (slotIndex < 0 || slotIndex >= government.totalSlots()) return
        val slots = government.getSlots()
        val slotType = slots[slotIndex]
        val name = cardName?.takeIf { it.isNotEmpty() } ?: ""
        if (name.isNotEmpty()) {
            val card = getRuleset().policyCards[name] ?: return
            if (slotType != "Wildcard" && card.slotType != "Wildcard" && card.slotType != slotType) return
            // A card cannot occupy two slots at once - clear it from any other slot first
            for (i in assignedCards.indices)
                if (i != slotIndex && assignedCards[i] == name) assignedCards[i] = ""
        }
        while (assignedCards.size <= slotIndex) assignedCards.add("")
        assignedCards[slotIndex] = name
        while (assignedCards.size < slots.size) assignedCards.add("")
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

    /** Civ 6: the policy cards that can be assigned to slot [slotIndex] - matching the slot type,
     *  whose required civic (if any) is researched, and not already assigned to another slot.
     *  Each policy card is unique and cannot occupy two slots at once. */
    @Readonly
    fun getAvailableCardsForSlot(slotIndex: Int): List<PolicyCard> {
        val government = getGovernment() ?: return emptyList()
        val slots = government.getSlots()
        if (slotIndex < 0 || slotIndex >= slots.size) return emptyList()
        val slotType = slots[slotIndex]
        val usedElsewhere = assignedCards.withIndex()
            .filter { it.index != slotIndex && it.value.isNotEmpty() }
            .map { it.value }
            .toHashSet()
        return getRuleset().policyCards.values.filter {
            (slotType == "Wildcard" || it.slotType == "Wildcard" || it.slotType == slotType)
                    && isCardAvailable(it)
                    && it.name !in usedElsewhere
        }.sortedBy { it.name }
    }
}
