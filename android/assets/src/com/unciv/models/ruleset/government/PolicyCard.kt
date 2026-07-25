package com.unciv.models.ruleset.government

import com.unciv.models.ruleset.Ruleset
import com.unciv.models.ruleset.RulesetObject
import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.ui.objectdescriptions.uniquesToCivilopediaTextLines
import com.unciv.ui.screens.civilopediascreen.FormattedLine
import com.unciv.ui.screens.civilopediascreen.ICivilopediaText

/** Civ VI Policy Card: a single policy that occupies one slot of [slotType] in the current Government. */
class PolicyCard : RulesetObject() {
    override fun getUniqueTarget() = UniqueTarget.PolicyCard

    /** One of Military / Economic / Diplomatic / Wildcard. */
    var slotType: String = "Wildcard"

    /** Civic that must be researched before this card becomes available (empty = available from start). */
    var requiredCivic: String = ""

    override fun makeLink() = "PolicyCard/$name"

    override fun getCivilopediaTextLines(ruleset: Ruleset): List<FormattedLine> {
        val lineList = ArrayList<FormattedLine>()
        lineList += FormattedLine("Slot: {$slotType}")
        if (requiredCivic.isNotEmpty())
            lineList += FormattedLine("Requires [$requiredCivic]", link = "Civic/$requiredCivic")
        uniquesToCivilopediaTextLines(lineList)
        return lineList
    }
}
