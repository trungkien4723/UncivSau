package com.unciv.logic.civilization.managers

import com.unciv.Constants
import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.automation.Timers.Companion.timeThis
import com.unciv.logic.civilization.*
import com.unciv.logic.map.tile.RoadStatus
import com.unciv.models.ruleset.INonPerpetualConstruction
import com.unciv.models.ruleset.civic.Civic
import com.unciv.models.ruleset.civic.CivicColumn
import com.unciv.models.ruleset.tech.Era
import com.unciv.models.ruleset.tile.TileResource
import com.unciv.models.ruleset.unique.GameContext
import com.unciv.models.ruleset.unique.UniqueMap
import com.unciv.models.ruleset.unique.UniqueTriggerActivation
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.ruleset.unit.BaseUnit
import com.unciv.models.translations.tr
import com.unciv.ui.components.fonts.Fonts
import com.unciv.ui.components.extensions.toPercent
import com.unciv.utils.withItem
import yairm210.purity.annotations.Readonly
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class CivicManager : IsPartOfGameInfoSerialization {
    @Transient
    var era: Era = Era()

    @Transient
    lateinit var civInfo: Civilization
    /** This is the Transient list of Civics */
    @Transient
    var researchedCivics = ArrayList<Civic>()
    @Transient
    internal var civicUniques = UniqueMap()

    var freeCivics = 0

    /** For calculating score */
    var cultureOfLast8Turns = IntArray(8)
    /** This is the list of strings, which is serialized */
    var civicsResearched = HashSet<String>()

    /** When moving towards a certain civic, the user doesn't have to manually pick every one. */
    var civicsToResearch = ArrayList<String>()
    private var overflowCulture = 0
    var civicsInProgress = HashMap<String, Int>()

    /** Civ VI "Inspiration" mechanic: names of civics whose Inspiration boost has already been granted.
     *  Serialized. Ensures each civic's Inspiration can only fire once. Empty by default (backward compatible). */
    var inspirationsTriggered = HashSet<String>()

    //region state-changing functions
    fun clone(): CivicManager {
        val toReturn = CivicManager()
        toReturn.civicsResearched.addAll(civicsResearched)
        toReturn.freeCivics = freeCivics
        toReturn.civicsInProgress.putAll(civicsInProgress)
        toReturn.civicsToResearch.addAll(civicsToResearch)
        toReturn.cultureOfLast8Turns = cultureOfLast8Turns.clone()
        toReturn.overflowCulture = overflowCulture
        toReturn.inspirationsTriggered.addAll(inspirationsTriggered)
        return toReturn
    }

    @Readonly fun getNumberOfCivicsResearched(): Int = civicsResearched.size

    /** Civ VI Inspiration: yields the [UniqueType.Inspiration] uniques of civics that are not yet researched
     *  and whose Inspiration has not yet fired. These need to participate in trigger dispatch even though the
     *  civic isn't researched (unlike regular civic uniques). Conditionals are checked later by the caller. */
    @Readonly
    fun getPendingInspirationUniques(): Sequence<com.unciv.models.ruleset.unique.Unique> {
        val ruleset = getRuleset()
        if (ruleset.civics.isEmpty()) return emptySequence()
        return ruleset.civics.values.asSequence()
            .filter { it.name !in civicsResearched && it.name !in inspirationsTriggered }
            .flatMap { civic -> civic.uniqueObjects.asSequence().filter { it.type == UniqueType.Inspiration } }
    }

    @Readonly fun getOverflowCulture(): Int = overflowCulture

    @Readonly
    private fun getCultureModifier(civicName: String): Float {
        val numberOfCivsResearchedThisCivic = civInfo.getKnownCivs()
            .count { it.isMajorCiv() && it.civics.isResearched(civicName) }
        val numberOfCivsRemaining = civInfo.gameInfo.civilizations
            .count { it.isMajorCiv() && !it.isDefeated() }
        return 1 + numberOfCivsResearchedThisCivic / numberOfCivsRemaining.toFloat() * 0.3f
    }

    @Readonly private fun getRuleset() = civInfo.gameInfo.ruleset

    @Readonly
    fun costOfCivic(civicName: String): Int {
        var civicCost = getRuleset().civics[civicName]!!.cost.toFloat()
        if (civInfo.isHuman())
            civicCost *= civInfo.getDifficulty().researchCostModifier
        civicCost *= civInfo.gameInfo.speed.cultureCostModifier
        civicCost /= getCultureModifier(civicName)
        val mapSizePredef = civInfo.gameInfo.tileMap.mapParameters.mapSize.getPredefinedOrNextSmaller()
        civicCost *= mapSizePredef.techCostMultiplier
        var cityModifier = (civInfo.cities.count { !it.isPuppet } - 1) * mapSizePredef.techCostPerCityModifier
        for (unique in civInfo.getMatchingUniques(UniqueType.LessTechCostFromCities)) cityModifier *= 1 - unique.params[0].toFloat() / 100
        for (unique in civInfo.getMatchingUniques(UniqueType.LessTechCost)) civicCost *= unique.params[0].toPercent()
        civicCost *= 1 + cityModifier
        return civicCost.toInt()
    }

    @Readonly
    fun currentCivic(): Civic? {
        val currentCivicName = currentCivicName() ?: return null
        return getRuleset().civics[currentCivicName]
    }

    @Readonly
    fun currentCivicName(): String? {
        return if (civicsToResearch.isEmpty()) null else civicsToResearch[0]
    }

    @Readonly fun cultureSpentOnCivic(civicName: String?) = civicsInProgress[civicName] ?: 0

    @Readonly
    fun remainingCultureToCivic(civicName: String): Int {
        val spareCulture = if (canBeResearched(civicName)) getOverflowCulture() else 0
        return costOfCivic(civicName) - cultureSpentOnCivic(civicName) - spareCulture
    }

    @Readonly
    fun turnsToCivic(civicName: String): String {
        val remainingCost = remainingCultureToCivic(civicName).toDouble()
        return when {
            remainingCost <= 0f -> (0).tr()
            civInfo.stats.statsForNextTurn.culture <= 0f -> Fonts.infinity.toString()
            else -> max(
                1,
                ceil(remainingCost / civInfo.stats.statsForNextTurn.culture).toInt()
            ).tr()
        }
    }

    @Readonly fun isResearched(civicName: String): Boolean { return civicsResearched.contains(civicName) }
    @Readonly fun isResearched(construction: INonPerpetualConstruction): Boolean = construction.requiredTechs().all{ requiredTech -> isResearched(requiredTech) }

    @Readonly
    fun isUnresearchable(civic: Civic): Boolean {
        if (civic.getMatchingUniques(UniqueType.OnlyAvailable, GameContext.IgnoreConditionals).any { !it.conditionalsApply(civInfo.state) })
            return true
        if (civic.hasUnique(UniqueType.Unavailable, civInfo.state)) return true
        return false
    }

    @Readonly
    fun canBeResearched(civicName: String): Boolean {
        val civic = getRuleset().civics[civicName]!!

        if (isUnresearchable(civic)) return false
        if (isResearched(civic.name) && !civic.isContinuallyResearchable()) return false

        return civic.prerequisites.all { isResearched(it) }
    }

    @Readonly fun allCivicsAreResearched() = getRuleset().civics.values.all { isResearched(it.name) || !canBeResearched(it.name) }

    //endregion

    /** Returns empty list if no path exists */
    fun getRequiredCivicsToDestination(destinationCivic: Civic): List<Civic> {
        val prerequisites = mutableListOf<Civic>()

        val checkPrerequisites = ArrayDeque<Civic>()
        if (isUnresearchable(destinationCivic)) return listOf()
        checkPrerequisites.add(destinationCivic)

        while (!checkPrerequisites.isEmpty()) {
            val civicToCheck = checkPrerequisites.removeFirst()
            if (isUnresearchable(civicToCheck)) return listOf()
            if (!civicToCheck.isContinuallyResearchable() &&
                    (isResearched(civicToCheck.name) || prerequisites.contains(civicToCheck)))
                continue
            for (prerequisite in civicToCheck.prerequisites)
                checkPrerequisites.add(getRuleset().civics[prerequisite]!!)
            prerequisites.add(civicToCheck)
        }

        return prerequisites.sortedBy { it.column!!.columnNumber }
    }

    private fun addCurrentCultureToCultureOfLast8Turns(culture: Int) {
        cultureOfLast8Turns[civInfo.gameInfo.turns % 8] = culture
    }

    @Readonly
    private fun limitOverflowCulture(overflowCulture: Int): Int {
        return min(overflowCulture, max(civInfo.stats.statsForNextTurn.culture.toInt() * 5,
                getRuleset().civics[currentCivicName()]!!.cost))
    }

    fun endTurn(cultureForNewTurn: Int) {
        addCurrentCultureToCultureOfLast8Turns(cultureForNewTurn)
        if (currentCivicName() == null) return

        var finalCultureToAdd = cultureForNewTurn

        if (overflowCulture != 0) {
            finalCultureToAdd += getOverflowCulture()
            overflowCulture = 0
        }

        addCulture(finalCultureToAdd)
    }

    fun addCulture(cultureGet: Int) {
        val currentCivic = currentCivicName() ?: return
        civicsInProgress[currentCivic] = cultureSpentOnCivic(currentCivic) + cultureGet
        if (civicsInProgress[currentCivic]!! < costOfCivic(currentCivic))
            return

        val extraCultureLeftOver = civicsInProgress[currentCivic]!! - costOfCivic(currentCivic)
        overflowCulture += limitOverflowCulture(extraCultureLeftOver)
        addCivic(currentCivic)
    }

    /**
     * Checks whether the research on the current civic can be completed
     * and, if so, completes the research.
     */
    fun updateResearchProgress() {
        val currentCivic = currentCivicName() ?: return
        val realOverflow = getOverflowCulture()
        val cultureSpent = cultureSpentOnCivic(currentCivic) + realOverflow
        if (cultureSpent >= costOfCivic(currentCivic)) {
            overflowCulture = 0
            addCulture(realOverflow)
        }
    }

    fun getFreeCivic(civicName: String) {
        freeCivics--
        addCivic(civicName)
    }

    fun addCivic(civicName: String, showNotification: Boolean = true) {
        val isNewCivic = civicsResearched.add(civicName)

        val newCivic = getRuleset().civics[civicName]!!
        if (!newCivic.isContinuallyResearchable())
            civicsToResearch.remove(civicName)
        civicsInProgress.remove(civicName)
        researchedCivics = researchedCivics.withItem(newCivic)
        addCivicToTransients(newCivic)

        if (!civInfo.isSpectator() && showNotification)
            civInfo.addNotification("Research of [$civicName] has completed!", CivicAction(civicName),
                NotificationCategory.General,
                NotificationIcon.Culture)
        if (isNewCivic)
            civInfo.popupAlerts.add(PopupAlert(AlertType.CivicResearched, civicName))

        val triggerNotificationText = "due to adopting [$civicName]"
        for (unique in newCivic.uniqueObjects) {
            if (!unique.isTriggerable || unique.hasTriggerConditional() || !unique.conditionalsApply(civInfo.state))
                continue
            repeat(unique.getUniqueMultiplier(civInfo.state)) {
                UniqueTriggerActivation.triggerUnique(unique, civInfo, triggerNotificationText = triggerNotificationText)
            }
        }

        for (unique in civInfo.getTriggeredUniques(UniqueType.TriggerUponAdoptingCivic) { newCivic.matchesFilter(it.params[0], civInfo.state) })
            UniqueTriggerActivation.triggerUnique(unique, civInfo, triggerNotificationText = triggerNotificationText)

        updateResearchProgress()
    }

    /** Adds a civic without any notification or popup alert - used for starting civics at game setup. */
    fun addCivicSilently(civicName: String) {
        val newCivic = getRuleset().civics[civicName]!!
        if (!newCivic.isContinuallyResearchable())
            civicsToResearch.remove(civicName)
        civicsInProgress.remove(civicName)
        researchedCivics = researchedCivics.withItem(newCivic)
        addCivicToTransients(newCivic)
        civicsResearched.add(civicName)
        updateResearchProgress()
    }

    private fun addCivicToTransients(civic: Civic) {
        civicUniques.addUniques(civic.uniqueObjects)
    }

    fun setTransients(civInfo: Civilization) {
        this.civInfo = civInfo
        researchedCivics.addAll(civicsResearched.map { getRuleset().civics[it]!! })
        researchedCivics.forEach { addCivicToTransients(it) }
    }

    @Readonly fun canResearchCivic(): Boolean = getRuleset().civics.values.any { canBeResearched(it.name) }
}
