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

    /** Maps city id to governor XP (Civ VI style) */
    var governorXP = HashMap<String, Int>()

    /** Extra governor titles granted by buildings (e.g. Government Plaza) */
    var extraGovernorTitles: Int = 0

    fun clone(): GovernorManager {
        val toReturn = GovernorManager()
        toReturn.appointedGovernors.putAll(appointedGovernors)
        toReturn.governorPromotions.putAll(governorPromotions)
        toReturn.governorXP.putAll(governorXP)
        toReturn.extraGovernorTitles = extraGovernorTitles
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
        return minOf(fromCivics + extraGovernorTitles, all)
    }

    /** Grant [amount] additional governor title(s) from e.g. Government Plaza buildings. */
    fun addGovernorTitle(amount: Int = 1) {
        extraGovernorTitles += amount
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
        governorXP[city.id] = 0
    }

    /** Recall the governor from [city], leaving it empty. */
    fun recallGovernor(city: City) {
        appointedGovernors.remove(city.id)
        governorPromotions.remove(city.id)
        governorXP.remove(city.id)
        city.governor = null
    }

    /** Add XP to the governor in [city]. Returns true if XP was added (governor exists). */
    fun addGovernorXP(city: City, amount: Int): Boolean {
        if (!appointedGovernors.containsKey(city.id)) return false
        val currentXP = governorXP[city.id] ?: 0
        governorXP[city.id] = currentXP + amount
        
        // Check for auto-promotion based on XP thresholds
        checkAndPromoteGovernor(city)
        return true
    }

    /** Check if governor should be promoted based on XP. Returns true if promoted. */
    fun checkAndPromoteGovernor(city: City): Boolean {
        val currentLevel = getGovernorPromotionLevel(city)
        val governor = getGovernorForCity(city) ?: return false
        val currentXP = governorXP[city.id] ?: 0
        
        // XP thresholds for promotion: 10, 50, 100, 200, etc.
        val xpForNextLevel = when (currentLevel) {
            0 -> 10
            1 -> 50
            2 -> 150
            3 -> 300
            else -> 9999
        }
        
        if (currentXP >= xpForNextLevel && currentLevel < governor.promotionObjects.size) {
            return promoteGovernor(city)
        }
        return false
    }

    /** Check if governor can be promoted and promote if possible. Returns true if promoted. */
    fun promoteGovernor(city: City): Boolean {
        val currentLevel = getGovernorPromotionLevel(city)
        val governor = getGovernorForCity(city) ?: return false
        if (currentLevel >= governor.promotionObjects.size) return false
        if (getAvailableGovernors() <= 0) return false  // Need a governor title to promote

        governorPromotions[city.id] = currentLevel + 1
        
        // Apply the promotion's unique effects
        val promotion = governor.promotionObjects[currentLevel]
        promotion.uniques.forEach { unique ->
            UniqueTriggerActivation.triggerUnique(unique, city.civ, city)
        }
        
        civInfo.addNotification(
            "[${governor.name}] in [${city.name}] has been promoted to level ${currentLevel + 1}!",
            NotificationCategory.General, "StatIcons/Governor"
        )
        return true
    }
}