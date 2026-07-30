package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.GreatWork
import com.unciv.models.ruleset.GreatWorkType
import com.unciv.models.stats.Stats
import yairm210.purity.annotations.Readonly

class GreatWorksManager : IsPartOfGameInfoSerialization {

    @Transient
    lateinit var civInfo: Civilization

    /** All great works owned by this civilization, keyed by unique ID */
    var greatWorks = LinkedHashMap<String, GreatWork>()

    /** Next ID counter for generating unique great work IDs */
    private var nextId = 0

    fun clone(): GreatWorksManager {
        val toReturn = GreatWorksManager()
        toReturn.greatWorks = LinkedHashMap(greatWorks)
        toReturn.nextId = nextId
        return toReturn
    }

    fun addGreatWork(type: GreatWorkType, name: String, creator: String = "", era: String = ""): GreatWork {
        val id = "greatwork_${nextId++}"
        val work = GreatWork(id, type, name, creator, era)
        greatWorks[id] = work
        return work
    }

    fun removeGreatWork(id: String) {
        greatWorks.remove(id)
    }

    @Readonly fun getGreatWorksByType(type: GreatWorkType): List<GreatWork> {
        return greatWorks.values.filter { it.type == type }
    }

    /** Get total Tourism from all great works */
    @Readonly fun getTotalTourism(): Float {
        return greatWorks.values.sumOf { it.type.getTourism().toDouble() }.toFloat()
    }

    /** Get total Culture from all great works */
    @Readonly fun getTotalCulture(): Float {
        return greatWorks.values.sumOf { it.type.getCulture().toDouble() }.toFloat()
    }

    /** Get all stats from great works */
    @Readonly fun getTotalStats(): Stats {
        val stats = Stats()
        for (work in greatWorks.values) {
            stats.add(work.getStats())
        }
        return stats
    }

    /** Count how many slots of a given type are available across all buildings */
    @Readonly fun getAvailableSlots(type: GreatWorkType): Int {
        var slots = 0
        for (city in civInfo.cities) {
            for (building in city.cityConstructions.getBuiltBuildings()) {
                slots += building.greatWorkSlots[type.name] ?: 0
            }
        }
        val filled = greatWorks.values.count { it.type == type }
        return (slots - filled).coerceAtLeast(0)
    }

    /** Check if there's an empty slot for the given type */
    @Readonly fun hasAvailableSlot(type: GreatWorkType): Boolean {
        return getAvailableSlots(type) > 0
    }

    /**
     * Theming bonus for a given type: returns additional [Stats] bonus if all great works
     * slots of this type are fully filled.
     * Bonus: +1 extra Culture and +1 extra Tourism per great work of the themed type.
     */
    @Readonly fun getThemingStats(type: GreatWorkType): Stats {
        val totalSlots = civInfo.cities.sumOf { city ->
            city.cityConstructions.getBuiltBuildings().sumOf { it.greatWorkSlots[type.name] ?: 0 }
        }
        val totalWorks = greatWorks.values.count { it.type == type }
        if (totalSlots == 0 || totalWorks < totalSlots) return Stats()

        return Stats(culture = totalWorks.toFloat(), tourism = totalWorks.toFloat())
    }
}
