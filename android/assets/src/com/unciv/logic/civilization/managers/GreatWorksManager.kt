package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Building
import com.unciv.models.ruleset.GreatWork
import com.unciv.models.ruleset.GreatWorkType
import com.unciv.models.stats.Stats
import yairm210.purity.annotations.Readonly

class GreatWorksManager : IsPartOfGameInfoSerialization {

    @Transient
    var civInfo: Civilization? = null

    /** All great works owned by this civilization, keyed by unique ID.
     *  Each work is placed on a specific building of a specific city (see [GreatWork.cityId] / [GreatWork.building]). */
    var greatWorks = LinkedHashMap<String, GreatWork>()

    /** Next ID counter for generating unique great work IDs */
    private var nextId = 0

    fun clone(): GreatWorksManager {
        val toReturn = GreatWorksManager()
        toReturn.greatWorks = LinkedHashMap(greatWorks)
        toReturn.nextId = nextId
        return toReturn
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
        // Migration: works saved before per-building placement get auto-placed into free slots
        for (work in greatWorks.values) {
            if (work.cityId.isEmpty() || work.building.isEmpty()) placeGreatWork(work)
        }
    }

    /** Creates a Great Work and places it into the first available slot of a matching building. */
    fun addGreatWork(type: GreatWorkType, name: String, creator: String = "", era: String = ""): GreatWork {
        val id = "greatwork_${nextId++}"
        val work = GreatWork(id, type, name, creator, era)
        placeGreatWork(work)
        greatWorks[id] = work
        return work
    }

    fun removeGreatWork(id: String) {
        greatWorks.remove(id)
    }

    /** Finds a building (in any city) with a free slot for [type] and places [work] on it. */
    private fun placeGreatWork(work: GreatWork) {
        val civ = civInfo ?: return
        for (city in civ.cities) {
            for (building in city.cityConstructions.getBuiltBuildings()) {
                val slots = building.greatWorkSlots[work.type.name] ?: 0
                if (slots == 0) continue
                if (getWorksInBuilding(city.id, building.name, work.type).size >= slots) continue
                work.cityId = city.id
                work.building = building.name
                return
            }
        }
    }

    @Readonly fun getWorksByType(type: GreatWorkType): List<GreatWork> {
        return greatWorks.values.filter { it.type == type }
    }

    /** Works currently displayed in the given building (optionally filtered by [type]). */
    @Readonly fun getWorksInBuilding(cityId: String, building: String, type: GreatWorkType? = null): List<GreatWork> {
        return greatWorks.values.filter { it.cityId == cityId && it.building == building && (type == null || it.type == type) }
    }

    /** Works in the given city (optionally filtered by [type]). */
    @Readonly fun getWorksInCity(cityId: String, type: GreatWorkType? = null): List<GreatWork> {
        return greatWorks.values.filter { it.cityId == cityId && (type == null || it.type == type) }
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

    /** Count how many empty slots of a given type are available across all buildings */
    @Readonly fun getAvailableSlots(type: GreatWorkType): Int {
        val civ = civInfo ?: return 0
        var slots = 0
        for (city in civ.cities) {
            for (building in city.cityConstructions.getBuiltBuildings()) {
                val buildingSlots = building.greatWorkSlots[type.name] ?: 0
                if (buildingSlots == 0) continue
                slots += (buildingSlots - getWorksInBuilding(city.id, building.name, type).size).coerceAtLeast(0)
            }
        }
        return slots
    }

    /** Check if there's an empty slot for the given type */
    @Readonly fun hasAvailableSlot(type: GreatWorkType): Boolean {
        return getAvailableSlots(type) > 0
    }

    /**
     * Theming bonus for a given type: for each building that has ALL of its slots of this type
     * fully filled, grants +1 extra Culture and +1 extra Tourism per great work of the themed type.
     */
    @Readonly fun getThemingStats(type: GreatWorkType): Stats {
        val civ = civInfo ?: return Stats()
        val stats = Stats()
        for (city in civ.cities) {
            for (building in city.cityConstructions.getBuiltBuildings()) {
                val buildingSlots = building.greatWorkSlots[type.name] ?: 0
                if (buildingSlots == 0) continue
                val works = getWorksInBuilding(city.id, building.name, type).size
                if (works < buildingSlots) continue
                stats.add(Stats(culture = buildingSlots.toFloat(), tourism = buildingSlots.toFloat()))
            }
        }
        return stats
    }
}