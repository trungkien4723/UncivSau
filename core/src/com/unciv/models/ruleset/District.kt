package com.unciv.models.ruleset

import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.ui.objectdescriptions.uniquesToCivilopediaTextLines
import com.unciv.ui.screens.civilopediascreen.FormattedLine
import com.unciv.ui.screens.civilopediascreen.ICivilopediaText

/**
 * Civ VI District: a specialization built on its own tile, separate from the city center.
 * Buildings may require a district to be present in the city ([Building.district]).
 * Districts provide their own yields plus adjacency bonuses (via the [UniqueType.StatsForAdjacentDistrict] unique).
 */
class District : RulesetStatsObject() {
    override fun getUniqueTarget() = UniqueTarget.District

    /** Tech required before this district can be built. */
    var requiredTech: String? = null

    /** Civic required before this district can be built. */
    var requiredCivic: String? = null

    /** Cost in production to build this district. */
    var cost: Int = 1

    /** Building names that are allowed to be constructed inside this district. */
    var buildings: List<String> = listOf()

    /** Optional terrain/feature filter restricting where the district may be placed. */
    var onlyBuildableOn: String = ""

    @yairm210.purity.annotations.Readonly
    fun matchesFilter(filter: String): Boolean {
        return filter == name || filter == "all districts"
    }

    override fun makeLink() = "District/$name"

    override fun getCivilopediaTextLines(ruleset: Ruleset): List<FormattedLine> {
        val lineList = ArrayList<FormattedLine>()
        if (requiredTech != null)
            lineList += FormattedLine("Requires [$requiredTech]", link = "Tech/$requiredTech")
        if (requiredCivic != null)
            lineList += FormattedLine("Requires [$requiredCivic]", link = "Civic/$requiredCivic")
        if (onlyBuildableOn.isNotEmpty())
            lineList += FormattedLine("Can only be built on [$onlyBuildableOn]")
        if (buildings.isNotEmpty()) {
            lineList += FormattedLine("Buildings:")
            for (building in buildings)
                lineList += FormattedLine(building, link = "Building/$building", indent = 1)
        }
        uniquesToCivilopediaTextLines(lineList)
        return lineList
    }
}
