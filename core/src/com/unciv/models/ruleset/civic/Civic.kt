package com.unciv.models.ruleset.civic

import com.unciv.Constants
import com.unciv.logic.MultiFilter
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Ruleset
import com.unciv.models.ruleset.RulesetObject
import com.unciv.models.ruleset.unique.GameContext
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.ui.objectdescriptions.CivicDescriptions
import yairm210.purity.annotations.Readonly

class Civic: RulesetObject() {

    var cost: Int = 0
    var prerequisites = HashSet<String>()
    override fun getUniqueTarget() = UniqueTarget.Civic

    var column: CivicColumn? = null // The column that this civic is in the civic tree
    var row: Int = 0
    var quote = ""

    override fun getSortGroup(ruleset: Ruleset) = if (column == null) -1 else era(ruleset)?.eraNumber ?: -1
    override fun getSubCategory(ruleset: Ruleset): String? = if (column == null) null else era(ruleset)?.name

    @Readonly fun era(): String = column!!.era

    @Readonly fun isContinuallyResearchable() = hasUnique(UniqueType.ResearchableMultipleTimes)


    /** Get Civilization-specific description for CivicPicker or AlertType.CivicResearched */
    fun getDescription(viewingCiv: Civilization) =
            CivicDescriptions.getDescription(this, viewingCiv)

    override fun makeLink() = "Civic/$name"

    override fun getCivilopediaTextLines(ruleset: Ruleset) =
            CivicDescriptions.getCivilopediaTextLines(this, ruleset)

    override fun era(ruleset: Ruleset) = ruleset.eras[era()]

    /** Implements [UniqueParameterType.CivicFilter][com.unciv.models.ruleset.unique.UniqueParameterType.CivicFilter] */
    @Readonly
    fun matchesFilter(filter: String, state: GameContext? = null, multiFilter: Boolean = true): Boolean {
        return if (multiFilter) MultiFilter.multiFilter(filter, {
            matchesSingleFilter(filter, state) ||
                state != null && hasTagUnique(filter, state) ||
                state == null && hasTagUnique(filter)
        })
        else matchesSingleFilter(filter, state) ||
            state != null && hasTagUnique(filter, state) ||
            state == null && hasTagUnique(filter)
    }

    @Readonly
    fun matchesSingleFilter(filter: String, state: GameContext? = null): Boolean {
        return when (filter) {
            in Constants.all -> true
            name -> true
            era() -> true
            else -> state?.gameInfo?.ruleset?.eras?.get(era())?.matchesFilter(filter, state, false) == true
        }
    }

    @Readonly
    fun uniqueIsRequirementForThisCivic(unique: Unique): Boolean =
            unique.type == UniqueType.OnlyAvailable
            && unique.modifiers.size == 1
            && unique.modifiers[0].let { it.type == UniqueType.ConditionalTech && it.params[0] == name }

    @Readonly fun uniqueIsNotRequirementForThisCivic(unique: Unique): Boolean = !uniqueIsRequirementForThisCivic(unique)
}
