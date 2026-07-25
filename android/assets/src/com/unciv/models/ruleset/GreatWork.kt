package com.unciv.models.ruleset

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.models.stats.Stats
import yairm210.purity.annotations.Readonly

data class GreatWork(
    val id: String,
    val type: GreatWorkType,
    val name: String,
    val creator: String = "",  // Great Person name who created it
    val era: String = ""
) : IsPartOfGameInfoSerialization {
    @Readonly fun getStats(): Stats = Stats(
        tourism = type.getTourism().toFloat(),
        culture = type.getCulture().toFloat()
    )
}
