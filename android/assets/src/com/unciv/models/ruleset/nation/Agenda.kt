package com.unciv.models.ruleset.nation

import com.unciv.models.ruleset.RulesetObject
import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.ui.objectdescriptions.uniquesToCivilopediaTextLines
import com.unciv.ui.screens.civilopediascreen.FormattedLine
import com.unciv.ui.screens.civilopediascreen.ICivilopediaText
import yairm210.purity.annotations.Readonly

/**
 * Civ VI Leader Agenda: a historical (always known) or hidden (randomly assigned) agenda
 * that shifts an AI civilization's opinion of other civs based on their behavior.
 *
 * The [likes]/[dislikes] fields are [Nation]/[Civilization] filters (see [com.unciv.models.ruleset.unique.UniqueParameterType.CivilizationFilter]).
 * When an AI civ holding this agenda evaluates another civ, a positive [DiplomaticModifiers.AgendaLike]
 * is applied if the other civ matches [likes], and a negative one if it matches [dislikes].
 */
class Agenda : RulesetObject() {
    override fun getUniqueTarget() = UniqueTarget.Nation

    /** Other civs matching this filter are viewed more favorably. */
    var likes: String = ""

    /** Other civs matching this filter are viewed less favorably. */
    var dislikes: String = ""

    @Readonly
    override fun makeLink() = "Agenda/$name"

    override fun getCivilopediaTextLines(ruleset: com.unciv.models.ruleset.Ruleset): List<FormattedLine> {
        val lineList = ArrayList<FormattedLine>()
        if (likes.isNotEmpty())
            lineList += FormattedLine("Likes: [$likes]")
        if (dislikes.isNotEmpty())
            lineList += FormattedLine("Dislikes: [$dislikes]")
        uniquesToCivilopediaTextLines(lineList)
        return lineList
    }
}
