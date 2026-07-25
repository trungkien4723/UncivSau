package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.city.City
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Governor
import yairm210.purity.annotations.Readonly

/**
 * Civ VI Governors (Rise and Fall) — 6D.
 *
 * Tracks which governor (by name) is assigned to which city, and how many governor "titles" the
 * civilization currently has available. Titles grow as the civilization researches more Civics, so
 * governors become available progressively through the game.
 */
class GovernorManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    /** Maps a city's id to the name of the governor assigned to it. */
    var appointedGovernors = HashMap<String, String>()

    fun clone(): GovernorManager {
        val toReturn = GovernorManager()
        toReturn.appointedGovernors.putAll(appointedGovernors)
        return toReturn
    }

    fun setTransients(civ: Civilization) {
        civInfo = civ
    }

    /** Maximum number of governors that can be assigned at once. */
    @Readonly
    fun getMaxGovernors(): Int {
        val all = civInfo.gameInfo.ruleset.governors.size
        if (all == 0) return 0
        // Start with one governor, unlock an additional one for every 7 civics researched.
        val fromCivics = 1 + civInfo.civics.getNumberOfCivicsResearched() / 7
        return minOf(fromCivics, all)
    }

    @Readonly
    fun getAvailableGovernors(): Int = (getMaxGovernors() - appointedGovernors.size).coerceAtLeast(0)

    @Readonly
    fun getGovernorForCity(city: City): Governor? {
        val name = appointedGovernors[city.id] ?: return null
        return civInfo.gameInfo.ruleset.governors[name]
    }

    @Readonly
    fun isGovernorAssigned(governorName: String): Boolean =
        appointedGovernors.values.contains(governorName)

    /** Assign [governorName] to [city]. Replaces any governor currently in that city. */
    fun assignGovernor(city: City, governorName: String) {
        appointedGovernors[city.id] = governorName
        city.governor = governorName
    }

    /** Recall the governor from [city], leaving it empty. */
    fun recallGovernor(city: City) {
        appointedGovernors.remove(city.id)
        city.governor = null
    }
}
