package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.city.City
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Governor
import yairm210.purity.annotations.Readonly

/**
 * Civ VI Governors (Rise and Fall) — 6D.
 *
 * Tracks which governor (by name) is assigned to which city, how many governor "titles" the
 * civilization currently has available, and governor promotions.
 * Titles grow as the civilization researches more Civics, so governors become available
 * progressively through the game.
 */
class GovernorManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    /** Maps a city's id to the name of the governor assigned to it. */
    var appointedGovernors = HashMap<String, String>()

    /** Maps city id to the number of promotion levels each appointed governor has (0 = no promotion) */
    var governorPromotions = HashMap<String, Int>()

    fun clone(): GovernorManager {
        val toReturn = GovernorManager()
        toReturn.appointedGovernors.putAll(appointedGovernors)
        toReturn.governorPromotions.putAll(governorPromotions)
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
        if (!civInfo.civics.isResearched("Early Empire")) return 0
        // First governor unlocked by Early Empire, +1 for every 7 civics researched.
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
    fun getGovernorPromotionLevel(city: City): Int = governorPromotions[city.id] ?: 0

    @Readonly
    fun getGovernorLevel(city: City): Int = getGovernorPromotionLevel(city) + 1

    @Readonly
    fun isGovernorAssigned(governorName: String): Boolean =
        appointedGovernors.values.contains(governorName)

    /** Assign [governorName] to [city]. Replaces any governor currently in that city. */
    fun assignGovernor(city: City, governorName: String) {
        appointedGovernors[city.id] = governorName
        city.governor = governorName
        // Initialize promotion level to 0
        governorPromotions[city.id] = 0
    }

    /** Recall the governor from [city], leaving it empty. */
    fun recallGovernor(city: City) {
        appointedGovernors.remove(city.id)
        governorPromotions.remove(city.id)
        city.governor = null
    }

    /** Promote the governor in [city] by one level if possible. Returns true if promoted. */
    fun promoteGovernor(city: City): Boolean {
        val currentLevel = getGovernorPromotionLevel(city)
        val governor = getGovernorForCity(city) ?: return false
        if (currentLevel >= governor.promotionObjects.size) return false
        if (getAvailableGovernors() <= 0) return false  // Need a governor title to promote

        governorPromotions[city.id] = currentLevel + 1
        return true
    }
}