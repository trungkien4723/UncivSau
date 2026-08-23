package com.unciv.models.ruleset

import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.Ruleset
import com.unciv.ui.objectdescriptions.uniquesToCivilopediaTextLines
import com.unciv.ui.screens.civilopediascreen.FormattedLine
import com.unciv.ui.screens.civilopediascreen.ICivilopediaText

/**
 * Civ VI Governors (Rise and Fall) — 6D.
 *
 * A Governor is a named character that can be assigned to a single city. While assigned, the
 * governor provides its [uniques] (which are local to the city, like building uniques) plus a flat
 * [loyaltyBonus] applied to the city's loyalty pressure each turn.
 *
 * Governors can be promoted to unlock additional bonuses. Promotions are stored as a list of
 * levels, where each level contains a list of uniques that are applied when the governor
 * reaches that promotion level.
 */
class Governor : RulesetObject() {
    override fun getUniqueTarget() = UniqueTarget.Building

    /** Flat loyalty added to the assigned city each turn. */
    var loyaltyBonus: Int = 0

    /** Promotions by level (0-indexed). Each level is a list of uniques that unlock at that level.
     *  promotions[0] = level 1 promotion, promotions[1] = level 2, etc.
     *  Must be concrete ArrayList types (not List) - Gdx Json otherwise deserializes
     *  com.badlogic.gdx.utils.Array into it, crashing getPromotionObjects with a CCE. */
    var promotions: ArrayList<ArrayList<String>> = ArrayList()

    override fun makeLink() = "Governor/$name"

    /** Converted promotion uniques (lazy evaluation). */
    val promotionObjects: List<List<Unique>>
        get() = promotions.map { level -> level.map { Unique(it) } }

    override fun getCivilopediaTextLines(ruleset: Ruleset): List<FormattedLine> {
        val lineList = ArrayList<FormattedLine>()
        if (loyaltyBonus != 0)
            lineList += FormattedLine("Provides [$loyaltyBonus] Loyalty per turn to the assigned city")
        uniquesToCivilopediaTextLines(lineList)
        if (promotions.isNotEmpty()) {
            lineList += FormattedLine()
            lineList += FormattedLine("{Promotions}", header = 4)
            for ((i, level) in promotions.withIndex()) {
                lineList += FormattedLine("Level ${i + 1}:")
                for (uniqueStr in level) {
                    val unique = Unique(uniqueStr)
                    unique.getDisplayText().let { lineList += FormattedLine(it) }
                }
            }
        }
        return lineList
    }
}