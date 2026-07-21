package com.unciv.models.ruleset.government

import com.unciv.models.ruleset.RulesetObject
import com.unciv.models.ruleset.unique.UniqueTarget
import yairm210.purity.annotations.Readonly
import com.unciv.ui.objectdescriptions.uniquesToCivilopediaTextLines
import com.unciv.ui.screens.civilopediascreen.FormattedLine
import com.unciv.ui.screens.civilopediascreen.ICivilopediaText
import com.unciv.models.ruleset.Ruleset

/** Civ VI Government: defines the number of policy card slots per category. */
class Government : RulesetObject() {
    override fun getUniqueTarget() = UniqueTarget.Government

    var militarySlots: Int = 0
    var economicSlots: Int = 0
    var diplomaticSlots: Int = 0
    var wildcardSlots: Int = 0

    /** Civic that must be researched before this government becomes available (empty = available from start). */
    var requiredCivic: String = ""

    /** Total number of policy card slots this government provides. */
    @Suppress("MemberVisibilityCanBePrivate")
    fun totalSlots() = militarySlots + economicSlots + diplomaticSlots + wildcardSlots

    /** The ordered list of slot categories (expanded by count) for UI assignment. */
    @Readonly
    fun getSlots(): List<String> {
        val slots = ArrayList<String>()
        repeat(militarySlots) { slots.add("Military") }
        repeat(economicSlots) { slots.add("Economic") }
        repeat(diplomaticSlots) { slots.add("Diplomatic") }
        repeat(wildcardSlots) { slots.add("Wildcard") }
        return slots
    }

    override fun makeLink() = "Government/$name"

    override fun getCivilopediaTextLines(ruleset: Ruleset): List<FormattedLine> {
        val lineList = ArrayList<FormattedLine>()
        if (requiredCivic.isNotEmpty())
            lineList += FormattedLine("Requires [$requiredCivic]", link = "Civic/$requiredCivic")
        lineList += FormattedLine("{Military} slots: $militarySlots")
        lineList += FormattedLine("{Economic} slots: $economicSlots")
        lineList += FormattedLine("{Diplomatic} slots: $diplomaticSlots")
        lineList += FormattedLine("{Wildcard} slots: $wildcardSlots")
        uniquesToCivilopediaTextLines(lineList)
        return lineList
    }
}
