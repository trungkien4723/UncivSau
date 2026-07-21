package com.unciv.models.ruleset

import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.models.ruleset.Ruleset
import com.unciv.ui.objectdescriptions.uniquesToCivilopediaTextLines
import com.unciv.ui.screens.civilopediascreen.FormattedLine
import com.unciv.ui.screens.civilopediascreen.ICivilopediaText
import yairm210.purity.annotations.Readonly

/**
 * Civ VI Governors (Rise and Fall) — 6D.
 *
 * A Governor is a named character that can be assigned to a single city. While assigned, the
 * governor provides its [uniques] (which are local to the city, like building uniques) plus a flat
 * [loyaltyBonus] applied to the city's loyalty pressure each turn.
 */
class Governor : RulesetObject() {
    override fun getUniqueTarget() = UniqueTarget.Building

    /** Flat loyalty added to the assigned city each turn. */
    var loyaltyBonus: Int = 0

    override fun makeLink() = "Governor/$name"

    override fun getCivilopediaTextLines(ruleset: Ruleset): List<FormattedLine> {
        val lineList = ArrayList<FormattedLine>()
        if (loyaltyBonus != 0)
            lineList += FormattedLine("Provides [$loyaltyBonus] Loyalty per turn to the assigned city")
        uniquesToCivilopediaTextLines(lineList)
        return lineList
    }
}
