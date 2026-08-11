package com.unciv.logic.city

import com.unciv.Constants
import com.unciv.logic.automation.Timers.Companion.timeThis
import com.unciv.logic.map.tile.RoadStatus
import com.unciv.logic.map.tile.Tile
import com.unciv.models.Counter
import com.unciv.models.ruleset.Building
import com.unciv.models.ruleset.District
import com.unciv.models.ruleset.IConstruction
import com.unciv.models.ruleset.INonPerpetualConstruction
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.UniqueTarget
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.ruleset.unit.BaseUnit
import com.unciv.models.stats.Stat
import com.unciv.models.stats.StatMap
import com.unciv.models.stats.Stats
import com.unciv.ui.components.extensions.toPercent
import com.unciv.utils.DebugUtils
import yairm210.purity.annotations.InternalState
import yairm210.purity.annotations.LocalState
import yairm210.purity.annotations.Pure
import yairm210.purity.annotations.Readonly
import kotlin.math.max
import kotlin.math.min

@InternalState
class StatTreeNode {
    val children = LinkedHashMap<String, StatTreeNode>()
    private var innerStats: Stats? = null

    fun setInnerStat(stat: Stat, value: Float) {
        if (innerStats == null) innerStats = Stats()
        innerStats!![stat] = value
    }

    private fun addInnerStats(stats: Stats) {
        if (innerStats == null) innerStats = stats.clone() // Copy the stats instead of referencing them
        else innerStats!!.add(stats) // What happens if we add 2 stats to the same leaf?
    }

    fun addStats(newStats: Stats?, vararg hierarchyList: String) {
        if (newStats == null) return
        if (newStats.isEmpty()) return
        if (hierarchyList.isEmpty()) {
            addInnerStats(newStats)
            return
        }
        val childName = hierarchyList.first()
        if (!children.containsKey(childName))
            children[childName] = StatTreeNode()
        children[childName]!!.addStats(newStats, *hierarchyList.drop(1).toTypedArray())
    }

    fun add(otherTree: StatTreeNode) {
        if (otherTree.innerStats != null) addInnerStats(otherTree.innerStats!!)
        for ((key, value) in otherTree.children) {
            if (!children.containsKey(key)) children[key] = value
            else children[key]!!.add(value)
        }
    }

    fun clone() : StatTreeNode {
        val new = StatTreeNode()
        new.innerStats = this.innerStats?.clone()
        new.children.putAll(this.children.mapValues { it.value.clone() })
        return new
    }

    val totalStats: Stats
        get() {
            val toReturn = Stats()
            if (innerStats != null) toReturn.add(innerStats!!)
            for (child in children.values) toReturn.add(child.totalStats)
            return toReturn
        }
}

/** Holds and calculates [Stats] for a city.
 *
 * No field needs to be saved, all are calculated on the fly,
 * so its field in [City] is @Transient and no such annotation is needed here.
 */
class CityStats(val city: City) {
    //region Fields, Transient

    var baseStatTree = StatTreeNode()

    var statPercentBonusTree = StatTreeNode()

    // Computed from baseStatList and statPercentBonusList - this is so the players can see a breakdown
    var finalStatList = LinkedHashMap<String, Stats>()

    var housingList = LinkedHashMap<String, Float>()

    var amenitiesList = LinkedHashMap<String, Float>()

    var statsFromTiles = Stats()

    var currentCityStats: Stats = Stats()  // This is so we won't have to calculate this multiple times - takes a lot of time, especially on phones

    //endregion
    //region Pure Functions

    @Readonly
    private fun getStatsFromTradeRoute(): Stats {
        val stats = Stats()
        
        // Civ VI: Domestic trade route — source city gains Food + Production based on the destination's districts
        if (city.tradeRoutes.hasDomesticRoute()) {
            val destinationCityName = city.tradeRoutes.domesticRouteTo
            val destinationCity = city.civ.gameInfo.getCities().find { it.name == destinationCityName }
            if (destinationCity != null) {
                stats.add(getDomesticRouteYields(destinationCity))
            }
        }
        
        // Civ VI: International trade routes — destination city gains Gold/Science/Culture/Faith based on its own districts,
        // plus +1 Gold per Trading Post the sending civ holds in this civ
        for ((sourceCivName, turns) in city.tradeRoutes.internationalRoutes) {
            if (turns <= 0) continue
            val sourceCiv = city.civ.gameInfo.getCivilization(sourceCivName)
            if (sourceCiv != null) {
                stats.add(getInternationalRouteYields())
                val postsInThisCiv = sourceCiv.tradingPosts.count { postCityName ->
                    city.civ.cities.any { it.name == postCityName }
                }
                stats.gold += postsInThisCiv.toFloat()
            }
        }

        // Legacy: Fall back to capital connection if no trade routes set
        if (stats.isEmpty()) {
            if (city.isCapital()) return stats
            
            if (!city.isConnectedToCapital()) return stats
            
            val capital = city.civ.getCapital()!!
            val destinationPopulation = capital.population.population
            
            stats.gold = destinationPopulation.toFloat() * 2f
            
            if (city.civ != capital.civ) {
                stats.science = destinationPopulation.toFloat() * 0.5f
            }
        }
        
        for (unique in city.getMatchingUniques(UniqueType.StatsFromTradeRoute))
            stats.add(unique.stats)
        
        val percentageStats = Stats()
        for (unique in city.getMatchingUniques(UniqueType.StatPercentFromTradeRoutes))
            percentageStats[Stat.valueOf(unique.params[1])] += unique.params[0].toFloat()
        for ((stat) in stats) {
            stats[stat] *= percentageStats[stat].toPercent()
        }
        return stats
    }

    /** Civ VI domestic route: Food + Production per destination district.
     *  City Center +1F +1P; Campus/Theater Square/Holy Site +1F; Harbor/Industrial Zone +1P; others +1F. */
    @Readonly
    private fun getDomesticRouteYields(destinationCity: City): Stats {
        val stats = Stats()
        stats.add(Stats(food = 1f, production = 1f))  // City Center
        for ((_, district) in destinationCity.getDistricts()) {
            when (district.name) {
                "Campus", "Theater Square", "Holy Site" -> stats.food += 1f
                "Harbor", "Industrial Zone" -> stats.production += 1f
                else -> stats.food += 1f
            }
        }
        return stats
    }

    /** Civ VI international route: Gold/Science/Culture/Faith per this (destination) city's districts.
     *  Commercial Hub/Harbor +2G; Campus +1 Science; Theater Square +1 Culture; Holy Site +1 Faith; others +1G. */
    @Readonly
    private fun getInternationalRouteYields(): Stats {
        val stats = Stats()
        stats.gold += 1f  // City Center
        for ((_, district) in city.getDistricts()) {
            when (district.name) {
                "Commercial Hub", "Harbor" -> stats.gold += 2f
                "Campus" -> stats.science += 1f
                "Theater Square" -> stats.culture += 1f
                "Holy Site" -> stats.faith += 1f
                else -> stats.gold += 1f
            }
        }
        return stats
    }

    @Readonly
    private fun getStatsFromProduction(production: Float): Stats? {
        if (Stat.isStat(city.cityConstructions.currentConstructionName())) {
            val stats = Stats()
            val stat = Stat.valueOf(city.cityConstructions.currentConstructionName())
            stats[stat] = production * getStatConversionRate(stat)
            return stats
        }
        return null
    }

    @Readonly
    fun getStatConversionRate(stat: Stat): Float {
        var conversionRate = 1 / 4f
        val conversionUnique = city.civ.getMatchingUniques(UniqueType.ProductionToStatConversionBonus).firstOrNull { it.params[0] == stat.name }
        if (conversionUnique != null) {
            conversionRate *= conversionUnique.params[1].toPercent()
        }
        return conversionRate
    }

    @Readonly
    private fun getStatPercentBonusesFromRailroad(): Stats? {
        val railroadImprovement = city.getRuleset().railroadImprovement
            ?: return null // for mods
        val techEnablingRailroad = railroadImprovement.techRequired
        // If we conquered enemy cities connected by railroad, but we don't yet have that tech,
        // we shouldn't get bonuses, it's as if the tracks are laid out but we can't operate them.
        if ( (techEnablingRailroad == null || city.civ.tech.isResearched(techEnablingRailroad))
                && (city.isCapital() || isConnectedToCapital(RoadStatus.Railroad)))
            return Stats(production = 25f)
        return null
    }

    @Readonly
    private fun getStatPercentBonusesFromPuppetCity(): Stats? {
        if (!city.isPuppet) return null
        return Stats(science = -25f, culture = -25f)
    }

    @Readonly
    fun getGrowthBonus(totalFood: Float): StatMap {
        val growthSources = StatMap()
        // "[amount]% growth [cityFilter]"
        city.forEachMatchingUnique(UniqueType.GrowthPercentBonus, city.state) { unique: Unique ->
            if (!city.matchesFilter(unique.params[1])) return@forEachMatchingUnique

            growthSources.add(
                unique.getSourceNameForUser(),
                Stats(food = unique.params[0].toFloat() / 100f * totalFood)
            )
        }
        return growthSources
    }

    @Readonly
    fun getStatsOfSpecialist(specialistName: String): Stats {
        val specialist = city.getRuleset().specialists[specialistName]
            ?: return Stats()
        @LocalState val stats = specialist.cloneStats()
        city.forEachMatchingUnique(UniqueType.StatsFromSpecialist, city.state) { unique: Unique ->
            if (city.matchesFilter(unique.params[1]))
                stats.add(unique.stats)
        }
        city.forEachMatchingUnique(UniqueType.StatsFromObject, city.state) { unique: Unique ->
            if (unique.params[1] == specialistName)
                stats.add(unique.stats)
        }
        return stats
    }

    @Readonly
    private fun getStatsFromSpecialists(specialists: Counter<String>): Stats {
        val stats = Stats()
        for ((key, value) in specialists.filter { it.value > 0 }.toList()) // avoid concurrent modification when calculating construction costs
            stats.add(getStatsOfSpecialist(key) * value)
        return stats
    }


    @Readonly
    private fun getStatsFromUniquesBySource(): StatTreeNode {
        val sourceToStats = StatTreeNode()

        val cityStateStatsMultipliers = city.civ.getMatchingUniques(UniqueType.BonusStatsFromCityStates).toList()

        fun addUniqueStats(unique: Unique) {
            @LocalState val stats = unique.stats.clone()
            if (unique.sourceObjectType==UniqueTarget.CityState)
                for (multiplierUnique in cityStateStatsMultipliers)
                    stats[Stat.valueOf(multiplierUnique.params[1])] *= multiplierUnique.params[0].toPercent()
            sourceToStats.addStats(stats, unique.getSourceNameForUser(), unique.sourceObjectName ?: "")
        }

        for (unique in city.getMatchingUniques(UniqueType.StatsPerCity))
            if (city.matchesFilter(unique.params[1]))
                addUniqueStats(unique)

        // "[stats] per [amount] population [cityFilter]"
        for (unique in city.getMatchingUniques(UniqueType.StatsPerPopulation))
            if (city.matchesFilter(unique.params[2])) {
                val amountOfEffects = (city.population.population / unique.params[1].toInt()).toFloat()
                sourceToStats.addStats(unique.stats.times(amountOfEffects), unique.getSourceNameForUser(), unique.sourceObjectName ?: "")
            }

        for (unique in city.getMatchingUniques(UniqueType.StatsFromCitiesOnSpecificTiles))
            if (city.getCenterTile().matchesTerrainFilter(unique.params[1], city.civ))
                addUniqueStats(unique)



        return sourceToStats
    }

    @Pure
    private fun getStatPercentBonusesFromGoldenAge(isGoldenAge: Boolean): Stats? {
        if (!isGoldenAge) return null
        return Stats(production = 20f, culture = 20f)
    }

    @Readonly
    private fun getStatsPercentBonusesFromUniquesBySource(currentConstruction: IConstruction): StatTreeNode {
        val sourceToStats = StatTreeNode()

        fun addUniqueStats(unique: Unique, stat: Stat, amount: Float) {
            val stats = Stats()
            stats.add(stat, amount)
            sourceToStats.addStats(stats, unique.getSourceNameForUser(), unique.sourceObjectName ?: "")
        }

        for (unique in city.getMatchingUniques(UniqueType.StatPercentBonus)) {
            addUniqueStats(unique, Stat.valueOf(unique.params[1]), unique.params[0].toFloat())
        }


        for (unique in city.getMatchingUniques(UniqueType.StatPercentBonusCities)) {
            if (city.matchesFilter(unique.params[2]))
                addUniqueStats(unique, Stat.valueOf(unique.params[1]), unique.params[0].toFloat())
        }

        val uniquesToCheck =
            when {
                currentConstruction is BaseUnit ->
                    city.getMatchingUniques(UniqueType.PercentProductionUnits)
                currentConstruction is Building && currentConstruction.isAnyWonder() ->
                    city.getMatchingUniques(UniqueType.PercentProductionWonders)
                currentConstruction is Building && !currentConstruction.isAnyWonder() ->
                    city.getMatchingUniques(UniqueType.PercentProductionBuildings)
                else -> emptySequence() // Science/Gold production
            }

        for (unique in uniquesToCheck) {
            if (constructionMatchesFilter(currentConstruction, unique.params[1])
                && city.matchesFilter(unique.params[2])
            )
                addUniqueStats(unique, Stat.Production, unique.params[0].toFloat())
        }


        for (unique in city.getMatchingUniques(UniqueType.StatPercentFromReligionFollowers))
            addUniqueStats(unique, Stat.valueOf(unique.params[1]),
                min(
                    unique.params[0].toFloat() * city.religion.getFollowersOfMajorityReligion(),
                    unique.params[2].toFloat()
                ))

        if (currentConstruction is Building
            && city.civ.getCapital()?.cityConstructions?.isBuilt(currentConstruction.name) == true
        ) {
            for (unique in city.getMatchingUniques(UniqueType.PercentProductionBuildingsInCapital))
                addUniqueStats(unique, Stat.Production, unique.params[0].toFloat())
        }

        return sourceToStats
    }

    @Readonly
    private fun getStatPercentBonusesFromUnitSupply(): Stats? {
        val supplyDeficit = city.civ.stats.getUnitSupplyDeficit()
        if (supplyDeficit > 0)
            return Stats(production = city.civ.stats.getUnitSupplyProductionPenalty())
        return null
    }

    @Readonly
    private fun constructionMatchesFilter(construction: IConstruction, filter: String): Boolean {
        val state = city.state
        if (construction is Building) return construction.matchesFilter(filter, state)
        if (construction is BaseUnit) return construction.matchesFilter(filter, state)
        return false
    }

    @Readonly
    fun isConnectedToCapital(roadType: RoadStatus): Boolean {
        if (city.civ.cities.size < 2) return false // first city!

        // Railroad, or harbor from railroad
        return if (roadType == RoadStatus.Railroad)
                city.isConnectedToCapital {
                    mediums ->
                    mediums.any { it.roadType == RoadStatus.Railroad }
                }
            else city.isConnectedToCapital()
    }

    @Readonly
    fun getRoadTypeOfConnectionToCapital(): RoadStatus {
        return city.civ.cache.citiesConnectedToCapitalToMediums[city]?.maxOfOrNull { it.roadType }
            ?: RoadStatus.None
    }

    
    //region State-Changing Methods

    fun updateTileStats():Unit = timeThis("updateTileStats") {
        val stats = Stats()
        val workedTiles = city.tilesInRange.asSequence()
            .filter {
                city.location.toHexCoord() == it.position  // city center always counted
                        || (city.isWorked(it) && it.district == null)  // worked tiles, exclude district tiles to avoid double-counting
                        || (it.owningCity == city && it.district == null && (it.getUnpillagedTileImprovement()
                    ?.hasUnique(UniqueType.TileProvidesYieldWithoutPopulation, it.stateThisTile) == true
                        || it.terrainHasUnique(UniqueType.TileProvidesYieldWithoutPopulation, it.stateThisTile)))
            }
        for (tile in workedTiles) {
            if (tile.isBlockaded() && city.isWorked(tile)) {
                city.workedTiles.remove(tile.position)
                city.lockedTiles.remove(tile.position)
                city.shouldReassignPopulation = true
                continue
            }
            val tileStats = tile.stats.getTileStats(city, city.civ)
            stats.add(tileStats)
        }
        // Civ VI districts: each district tile provides its own stats and adjacency bonuses,
        // independent of worked population. Pillaged districts contribute nothing.
        for ((tile, district) in city.getDistricts()) {
            if (tile.districtIsPillaged) continue
            val districtStats = district.cloneStats()
            districtStats.add(getDistrictAdjacencyStats(tile, district))
            stats.add(districtStats)
        }
        statsFromTiles = stats
    }

    /** Adjacency bonuses for a [district] placed on [tile], from [UniqueType.StatsForAdjacentDistrict]. */
    @Readonly
    fun getDistrictAdjacencyStats(tile: Tile, district: District): Stats {
        val stats = Stats()
        for (unique in district.getMatchingUniques(UniqueType.StatsForAdjacentDistrict)) {
            val filter = unique.params[1].removePrefix("districtFilter: ")
            val adjacent = tile.neighbors.count { neighbor ->
                neighbor.getDistrict()?.name == filter
                        || (filter == "District" && (neighbor.getDistrict() != null || neighbor.isCityCenter()))
                        || neighbor.matchesFilter(filter, city.civ)
            }
            if (adjacent > 0) stats.add(unique.stats.times(adjacent.toFloat()))
        }
        return stats
    }


    fun updateCityHousingAndAmenities(statsFromBuildings: StatTreeNode) {
        val civInfo = city.civ
        
        val newHousingList = LinkedHashMap<String, Float>()
        val newAmenitiesList = LinkedHashMap<String, Float>()

        var housingFromTerrain = 0f
        
        if (city.isCoastal()) {
            housingFromTerrain = 3f
        } else if (city.getCenterTile().isAdjacentTo(Constants.freshWater)) {
            housingFromTerrain = 5f
        } else {
            housingFromTerrain = 2f
        }
        
        newHousingList["Terrain"] = housingFromTerrain

        for (building in city.cityConstructions.getBuiltBuildings()) {
            if (building.housing > 0) {
                newHousingList[building.name] = city.getHousingFromBuilding(building)
            }
        }

        for (district in city.getDistricts()) {
            if (district.second.housing > 0) {
                newHousingList[district.first.position.toString()] = district.second.housing
            }
        }

        for (unique in city.getMatchingUniques(UniqueType.Housing)) {
            if (city.matchesFilter(unique.params[1])) {
                newHousingList[unique.getSourceNameForUser()] = unique.params[0].toFloat()
            }
        }

        for (unique in civInfo.getMatchingUniques(UniqueType.Housing)) {
            if (unique.conditionalsApply(city.state)) {
                newHousingList[unique.getSourceNameForUser()] = unique.params[0].toFloat()
            }
        }

        var totalHousing = housingFromTerrain
        for ((source, value) in newHousingList) {
            if (source != "Terrain") totalHousing += value
        }
        newHousingList["Total"] = totalHousing
        housingList = newHousingList

        newAmenitiesList["Population"] = 0f

        for (building in city.cityConstructions.getBuiltBuildings()) {
            if (building.amenities > 0) {
                newAmenitiesList[building.name] = building.amenities
            }
        }

        for (unique in city.getMatchingUniques(UniqueType.Amenities)) {
            if (city.matchesFilter(unique.params[1])) {
                newAmenitiesList[unique.getSourceNameForUser()] = unique.params[0].toFloat()
            }
        }

        for (unique in civInfo.getMatchingUniques(UniqueType.Amenities)) {
            if (unique.conditionalsApply(city.state)) {
                newAmenitiesList[unique.getSourceNameForUser()] = unique.params[0].toFloat()
            }
        }

        val requiredAmenities = max(0, (city.population.population - 2) / 2)
        newAmenitiesList["Required"] = requiredAmenities.toFloat()

        var totalAmenities = 0f
        for ((source, value) in newAmenitiesList) {
            if (source != "Population" && source != "Required") totalAmenities += value
        }
        newAmenitiesList["Total"] = totalAmenities
        amenitiesList = newAmenitiesList
    }

    private fun updateBaseStatList(statsFromBuildings: StatTreeNode) {
        val newBaseStatTree = StatTreeNode()

        val newBaseStatList = StatMap()

        newBaseStatTree.addStats(Stats(
            science = city.population.population.toFloat(),
            production = city.population.getFreePopulation().toFloat()
        ), "Population")
        newBaseStatList["Tile yields"] = statsFromTiles
        newBaseStatList["Specialists"] =
            getStatsFromSpecialists(city.population.getNewSpecialists())
        newBaseStatList["Trade routes"] = getStatsFromTradeRoute()
        newBaseStatTree.children["Buildings"] = statsFromBuildings

        newBaseStatTree.addStats(Stats(housing = 1f), "Housing")
        newBaseStatTree.addStats(Stats(amenities = 1f), "Amenities")

        for ((source, stats) in newBaseStatList)
            newBaseStatTree.addStats(stats, source)

        newBaseStatTree.add(getStatsFromUniquesBySource())
        baseStatTree = newBaseStatTree
    }
    
    @Readonly
    private fun getStatPercentBonusList(currentConstruction: IConstruction): StatTreeNode = timeThis("CityStats.getStatPercentBonusList") {
        val newStatsBonusTree = StatTreeNode()

        newStatsBonusTree.addStats(getStatPercentBonusesFromGoldenAge(city.civ.goldenAges.isGoldenAge()),"Golden Age")
        newStatsBonusTree.addStats(getStatPercentBonusesFromRailroad(), "Railroad")
        newStatsBonusTree.addStats(getStatPercentBonusesFromPuppetCity(), "Puppet City")
        newStatsBonusTree.addStats(getStatPercentBonusesFromUnitSupply(), "Unit Supply")
        newStatsBonusTree.add(getStatsPercentBonusesFromUniquesBySource(currentConstruction))
        
        for (building in city.cityConstructions.getBuiltBuildings())
            newStatsBonusTree.addStats(building.getStatPercentageBonuses(city),
                "Buildings", building.name)


        if (DebugUtils.SUPERCHARGED) {
            val stats = Stats()
            for (stat in Stat.entries) stats[stat] = 10000f
            newStatsBonusTree.addStats(stats, "Supercharged")
        }
        return newStatsBonusTree
    }
    
    private fun updateStatPercentBonusList(currentConstruction: IConstruction){
        statPercentBonusTree = getStatPercentBonusList(currentConstruction)
    }

    fun update(currentConstruction: IConstruction = city.cityConstructions.getCurrentConstruction(),
               updateTileStats:Boolean = true,
               updateCivStats:Boolean = true,
               calculateGrowthModifiers:Boolean = true): Unit = timeThis<Unit>("CityStats.update") {

        if (updateTileStats) updateTileStats()

        val statsFromBuildings = city.cityConstructions.getStats()
        updateBaseStatList(statsFromBuildings)
        updateCityHousingAndAmenities(statsFromBuildings)
        updateStatPercentBonusList(currentConstruction)

        updateFinalStatList(currentConstruction, calculateGrowthModifiers) // again, we don't edit the existing currentCityStats directly, in order to avoid concurrency exceptions

        val newCurrentCityStats = Stats()
        for (stat in finalStatList.values) newCurrentCityStats.add(stat)
        currentCityStats = newCurrentCityStats

        if (updateCivStats) city.civ.updateStatsForNextTurn()
    }

    private fun updateFinalStatList(currentConstruction: IConstruction, calculateGrowthModifiers: Boolean = true) {
        val newFinalStatList = StatMap() // again, we don't edit the existing currentCityStats directly, in order to avoid concurrency exceptions

        for ((key, value) in baseStatTree.children)
            newFinalStatList[key] = value.totalStats.clone()

        val statPercentBonusesSum = statPercentBonusTree.totalStats

        for (entry in newFinalStatList.values)
            entry.production *= statPercentBonusesSum.production.toPercent()

        // We only add the 'extra stats from production' AFTER we calculate the production INCLUDING BONUSES
        val statsFromProduction = getStatsFromProduction(newFinalStatList.values.map { it.production }.sum())
        if (statsFromProduction != null && !statsFromProduction.isEmpty()) {
            baseStatTree = StatTreeNode().apply {
                children.putAll(baseStatTree.children)
                addStats(statsFromProduction, "Production")
            } // concurrency-safe addition
            newFinalStatList["Construction"] = statsFromProduction
        }

        for (entry in newFinalStatList.values) {
            entry.gold *= statPercentBonusesSum.gold.toPercent()
            entry.culture *= statPercentBonusesSum.culture.toPercent()
            entry.food *= statPercentBonusesSum.food.toPercent()
            entry.faith *= statPercentBonusesSum.faith.toPercent()
            entry.tourism *= statPercentBonusesSum.tourism.toPercent()
        }

        // AFTER we've gotten all the gold stats figured out, only THEN do we plonk that gold into Science
        if (city.getRuleset().modOptions.hasUnique(UniqueType.ConvertGoldToScience)) {
            val amountConverted = (newFinalStatList.values.sumOf { it.gold.toDouble() }
                    * city.civ.tech.goldPercentConvertedToScience).toInt().toFloat()
            if (amountConverted > 0) // Don't want you converting negative gold to negative science yaknow
                newFinalStatList["Gold -> Science"] = Stats(science = amountConverted, gold = -amountConverted)
        }
        for (entry in newFinalStatList.values) {
            entry.science *= statPercentBonusesSum.science.toPercent()
        }

        for ((unique, statToBeRemoved) in city.getMatchingUniques(UniqueType.NullifiesStat)
            .map { it to Stat.valueOf(it.params[0]) }
            .distinct()
        ) {
            val removedAmount = newFinalStatList.values.sumOf { it[statToBeRemoved].toDouble() }

            newFinalStatList.add(
                unique.getSourceNameForUser(),
                Stats().apply { this[statToBeRemoved] = -removedAmount.toFloat() }
            )
        }

/* Okay, food calculation is complicated.
         First we see how much food we generate. Then we apply production bonuses to it.
         Up till here, business as usual.
         Then, we deduct food eaten (from the total produced).
         Now we have the excess food, to which "growth" modifiers apply
         Some policies have bonuses for growth only, not general food production. */

        val foodEaten = calcFoodEaten()
        newFinalStatList["Population"]!!.food -= foodEaten

        var totalFood = newFinalStatList.values.map { it.food }.sum()

        val availableHousing = city.getAvailableHousing()
        val currentPopulation = city.population.population

        if (currentPopulation > availableHousing + 1) {
            val excessPopulation = currentPopulation - availableHousing
            if (totalFood > 0) {
                val housingPenalty = 1f - (excessPopulation * 0.25f).coerceAtMost(0.75f)
                totalFood = (totalFood * housingPenalty).coerceAtMost(0f)
                newFinalStatList["Housing Penalty"] = Stats(food = totalFood - (totalFood * housingPenalty))
            }
        }

        // Apply growth modifier only when positive food
        if (totalFood > 0 && calculateGrowthModifiers) {
            val growthBonuses = getGrowthBonus(totalFood)
            for (growthBonus in growthBonuses) {
                newFinalStatList.add("[${growthBonus.key}] ([Growth])", growthBonus.value)
            }
            totalFood = newFinalStatList.values.map { it.food }.sum()
        }

        // Power calculation (Civ VI)
        val powerDeficit = calculatePowerDeficit()
        if (powerDeficit > 0) {
            // Apply -25% production penalty per power deficit unit
            // This affects all production, not just specific buildings
            val powerPenaltyPercent = min(1.0f, powerDeficit * 0.25f)
            val powerPenalty = Stats(production = -(powerPenaltyPercent))
            newFinalStatList.add("Power Deficit", powerPenalty)
        }

        if (canConvertFoodToProduction(totalFood, currentConstruction)) {
            newFinalStatList["Excess food to production"] =
                Stats(production = getProductionFromExcessiveFood(totalFood), food = -totalFood)
        }

        val growthNullifyingUnique = city.getMatchingUniques(UniqueType.NullifiesGrowth).firstOrNull()
        if (growthNullifyingUnique != null) {
            // Does not nullify negative growth (starvation)
            val currentGrowth = newFinalStatList.values.sumOf { it[Stat.Food].toDouble() }
            if (currentGrowth > 0)
                newFinalStatList.add(
                    growthNullifyingUnique.getSourceNameForUser(),
                    Stats(food = -currentGrowth.toFloat())
                )
        }

        if (city.isInResistance())
            newFinalStatList.clear()  // NOPE

        if (newFinalStatList.values.map { it.production }.sum() < 1)  // Minimum production for things to progress
            newFinalStatList["Production"] = Stats(production = 1f)
        finalStatList = newFinalStatList
    }

    @Readonly
    fun canConvertFoodToProduction(food: Float, currentConstruction: IConstruction): Boolean {
        return (food > 0
            && currentConstruction is INonPerpetualConstruction
            && currentConstruction.hasUnique(UniqueType.ConvertFoodToProductionWhenConstructed))
    }

    /**
     * Calculate the conversion of the excessive food to production when
     * [UniqueType.ConvertFoodToProductionWhenConstructed] is at front of the build queue
     * @param food is amount of excess Food generates this turn
     * See for details: https://civilization.fandom.com/wiki/Settler_(Civ5)
     * @see calcFoodEaten as well for Food consumed this turn
     */
    @Pure
    fun getProductionFromExcessiveFood(food : Float): Float {
        return if (food >= 4.0f ) 2.0f + (food / 4.0f).toInt()
          else if (food >= 2.0f ) 2.0f
          else if (food >= 1.0f ) 1.0f
        else 0.0f
    }

    @Readonly
    private fun calcFoodEaten(): Float {
        var foodEatenBySpecialists = 2f * city.population.getNumberOfSpecialists()
        var foodEaten = city.population.population.toFloat() * 2 - foodEatenBySpecialists
        
        for (unique in city.getMatchingUniques(UniqueType.FoodConsumptionBySpecialists))
            if (city.matchesFilter(unique.params[1]))
                foodEatenBySpecialists *= unique.params[0].toPercent()

        foodEaten += foodEatenBySpecialists
        
        for (unique in city.getMatchingUniques(UniqueType.FoodConsumptionByPopulation)) {
            if (!city.matchesFilter(unique.params[2])) continue
            val foodEatenByPopulationFilter = 2f * city.population.getPopulationFilterAmount(unique.params[1])
            foodEaten -= foodEatenByPopulationFilter * (1f - unique.params[0].toPercent())
        }
        
        return foodEaten
    }

    @Readonly
    fun calculatePowerDeficit(): Float {
        var totalConsumption = 0
        for (building in city.cityConstructions.getBuiltBuildings()) {
            for (unique in building.uniqueObjects) {
                if (unique.type == UniqueType.PowerConsumption) {
                    totalConsumption += unique.params[0].toInt()
                }
            }
        }
        var totalProduction = 0
        for (building in city.cityConstructions.getBuiltBuildings()) {
            for (unique in building.uniqueObjects) {
                if (unique.type == UniqueType.PowerProduction) {
                    totalProduction += unique.params[0].toInt()
                }
            }
        }
        return (totalConsumption - totalProduction).toFloat()
    }

    //endregion
}
