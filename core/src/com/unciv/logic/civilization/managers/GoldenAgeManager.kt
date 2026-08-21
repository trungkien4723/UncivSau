package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.AlertType
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.CivilopediaAction
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.civilization.PopupAlert
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.ruleset.unique.TemporaryUnique
import com.unciv.models.ruleset.unique.UniqueTriggerActivation
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.ui.components.extensions.toPercent
import yairm210.purity.annotations.Readonly
import kotlin.math.max

class GoldenAgeManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var storedEraPoints = 0
    private var numberOfGoldenAges = 0
    var turnsLeftForCurrentGoldenAge = 0

    // === Civ VI Era Score / Ages (6C) ===
    var eraScore = 0              // Era Score accumulated during the current era
    var totalEraScore = 0         // all-time Era Score
    var currentAge = "Normal"     // "Dark" | "Normal" | "Golden" | "Heroic"
    var previousAge = "Normal"
    var eraScoreForLastAge = 0    // Era Score achieved in the age that just ended (for UI)
    
    // Dedications - selected at era transition
    var currentDedication: String? = null
    var availableDedications: MutableList<String> = mutableListOf()
        private set
    var pendingDedicationSelection: Boolean = false  // True when player needs to pick a dedication

    fun clone(): GoldenAgeManager {
        val toReturn = GoldenAgeManager()
        toReturn.numberOfGoldenAges = numberOfGoldenAges
        toReturn.storedEraPoints = storedEraPoints
        toReturn.turnsLeftForCurrentGoldenAge = turnsLeftForCurrentGoldenAge
        toReturn.eraScore = eraScore
        toReturn.totalEraScore = totalEraScore
        toReturn.currentAge = currentAge
        toReturn.previousAge = previousAge
        toReturn.eraScoreForLastAge = eraScoreForLastAge
        toReturn.currentDedication = currentDedication
        toReturn.availableDedications = availableDedications.toMutableList()
        toReturn.pendingDedicationSelection = pendingDedicationSelection
        return toReturn
    }

    @Readonly fun isGoldenAge(): Boolean = currentAge == "Golden" || currentAge == "Heroic" || turnsLeftForCurrentGoldenAge > 0
    @Readonly fun isDarkAge(): Boolean = currentAge == "Dark"
    @Readonly fun isHeroicAge(): Boolean = currentAge == "Heroic"
    @Readonly fun isNormalAge(): Boolean = currentAge == "Normal"
    
    fun addEraPoints(amount: Int) {
        storedEraPoints += amount
    }

    @Readonly
    fun eraPointsRequiredForNextGoldenAge(): Int {
        var cost = (500 + numberOfGoldenAges * 250).toFloat()
        cost *= civInfo.cities.size.toPercent()
        cost *= civInfo.gameInfo.speed.modifier
        return cost.toInt()
    }

    @Readonly
    fun calculateGoldenAgeLength(unmodifiedNumberOfTurns: Int): Int {
        var turnsToGoldenAge = unmodifiedNumberOfTurns.toFloat()
        for (unique in civInfo.getMatchingUniques(UniqueType.GoldenAgeLength))
            turnsToGoldenAge *= unique.params[0].toPercent()
        turnsToGoldenAge *= civInfo.gameInfo.speed.goldenAgeLengthModifier
        return turnsToGoldenAge.toInt()
    }

    fun enterGoldenAge(unmodifiedNumberOfTurns: Int = 10) {
        turnsLeftForCurrentGoldenAge += calculateGoldenAgeLength(unmodifiedNumberOfTurns)
        currentAge = "Golden"
        civInfo.addNotification("You have entered a Golden Age!",
            CivilopediaAction("Tutorial/Golden Age"),
            NotificationCategory.General, "StatIcons/Happiness")
        civInfo.popupAlerts.add(PopupAlert(AlertType.GoldenAge, ""))

        for (unique in civInfo.getTriggeredUniques(UniqueType.TriggerUponEnteringGoldenAge))
            UniqueTriggerActivation.triggerUnique(unique, civInfo)
        for (city in civInfo.cities)
            city.cityStats.update()
    }

    fun enterDarkAge() {
        currentAge = "Dark"
        civInfo.addNotification("You have entered a Dark Age!",
            CivilopediaAction("Tutorial/Dark Age"),
            NotificationCategory.General, "StatIcons/EraScore")
        civInfo.popupAlerts.add(PopupAlert(AlertType.GoldenAge, "")) // Uses same icon for now

        for (unique in civInfo.getTriggeredUniques(UniqueType.TriggerUponEnteringDarkAge))
            UniqueTriggerActivation.triggerUnique(unique, civInfo)
        for (city in civInfo.cities)
            city.cityStats.update()
    }

    fun enterHeroicAge() {
        currentAge = "Heroic"
        turnsLeftForCurrentGoldenAge += calculateGoldenAgeLength(15) // Heroic ages last longer
        civInfo.addNotification("You have achieved a Heroic Age!",
            CivilopediaAction("Tutorial/Heroic Age"),
            NotificationCategory.General, "StatIcons/EraScore")
        civInfo.popupAlerts.add(PopupAlert(AlertType.GoldenAge, ""))

        for (unique in civInfo.getTriggeredUniques(UniqueType.TriggerUponEnteringHeroicAge))
            UniqueTriggerActivation.triggerUnique(unique, civInfo)
        for (city in civInfo.cities)
            city.cityStats.update()
    }

    /** Add Era Score (Civ VI Historic Moments). */
    fun addEraScore(amount: Int, source: String = "") {
        if (amount <= 0) return
        eraScore += amount
        totalEraScore += amount
        if (source.isNotEmpty()) {
            civInfo.addNotification("You earned [$amount] Era Score from [$source]!",
                NotificationCategory.General, "StatIcons/EraScore")
        } else {
            civInfo.addNotification("You earned [$amount] Era Score!",
                NotificationCategory.General, "StatIcons/EraScore")
        }
    }

    /**
     * Called when the civilization enters a new era (from tech or civic research).
     * Decides the Age (Dark / Normal / Golden / Heroic) based on Era Score accumulated in the era that just ended.
     * Returns the chosen age so the caller can fire UI/triggers.
     */
    fun onEraTransition(newEraNumber: Int): String {
        val isDramaticAges = civInfo.gameModes.isDramaticAgesModeActive()
        
        // Thresholds scale with era number (Civ VI-like): Golden requires a real accumulation of
        // Era Score (mainly wonders, [4] each) rather than a single event; Dark punishes an empty era.
        val goldenThreshold = getGoldenThreshold(newEraNumber, isDramaticAges)
        val darkThreshold = getDarkThreshold(newEraNumber)
        
        val age = when {
            eraScore >= goldenThreshold -> "Golden"
            eraScore < darkThreshold -> "Dark"
            isDramaticAges -> "Dark" // No Normal age in Dramatic Ages
            else -> "Normal"
        }
        
        // Check for Heroic Age (Golden after Dark)
        val finalAge = if (age == "Golden" && previousAge == "Dark") {
            "Heroic"
        } else age
        
        previousAge = currentAge
        currentAge = finalAge
        eraScoreForLastAge = eraScore
        eraScore = 0  // reset for the new era
        
        currentDedication = null // Reset dedication for new age
        civInfo.temporaryUniques.clear() // Clear old dedication effects
        
        // Generate available dedications for the new age
        availableDedications = dedications.toMutableList()
        // Flag that player needs to pick a dedication (for human players)
        if (civInfo.isHuman()) {
            pendingDedicationSelection = true
        }
        
        val label = when (finalAge) {
            "Golden" -> "Golden Age"
            "Dark" -> "Dark Age"
            "Heroic" -> "Heroic Age"
            else -> "Normal Age"
        }
        
        civInfo.addNotification("You have entered a [$label]!",
            CivilopediaAction("Tutorial/Golden Age"), NotificationCategory.General, "StatIcons/EraScore")

        when (finalAge) {
            "Golden" -> {
                enterGoldenAge()
                numberOfGoldenAges++
            }
            "Heroic" -> {
                enterHeroicAge()
                numberOfGoldenAges++
            }
            "Dark" -> enterDarkAge()
        }
        return finalAge
    }

    /** Select a dedication for the new age and apply its effects. */
    fun selectDedication(dedicationName: String) {
        if (dedicationName in availableDedications) {
            currentDedication = dedicationName
            applyDedicationEffects(dedicationName)
        }
    }

    private fun applyDedicationEffects(dedicationName: String) {
        val uniques = getDedicationUniques(dedicationName)
        val duration = 60
        for (uniqueString in uniques) {
            val unique = Unique(uniqueString)
            civInfo.temporaryUniques.add(TemporaryUnique(unique, duration))
        }
        civInfo.addNotification("You have adopted the [$dedicationName] dedication!",
            NotificationCategory.General, "StatIcons/EraScore")
    }

    @Readonly
    private fun getDedicationUniques(dedicationName: String): List<String> {
        return when (dedicationName) {
            "Exodus of the Evangelists" -> listOf(
                "[+1] Movement <for [All] units>",
                "[+5] Religious Strength <for [All] units>"
            )
            "Penitent" -> listOf("[+50]% [Faith] [in all cities]")
            "Commune" -> listOf("[+1] [Housing] [in all cities]")
            "Inquisition" -> listOf("[+30]% [Production] [in all cities]")
            "Heartbeat of Steam" -> listOf(
                "[+20]% [Production] [in all cities] <when building [Industrial] buildings>"
            )
            "To Arms!" -> listOf(
                "[+20]% [Production] [in all cities] <when building [Military] units>"
            )
            "Monumentality" -> listOf(
                "May buy [Civilian] units for [Faith] [in all cities]",
                "[-50]% [Faith] cost of units [in all cities]"
            )
            "Free Inquiry" -> listOf("[+10]% [Science] [in all cities]")
            "Pen, Brush, and Voice" -> listOf(
                "[+10]% [Culture] [in all cities]",
                "[+1] [Gold] from every [Population] [in all cities]"
            )
            "Reform the Coinage" -> listOf(
                "[+2 Gold] per [Trade Route] [in all cities]",
                "[+100]% Yield from pillaging tiles"
            )
            "Bodyguard of Lies" -> listOf("[+2] Spy capacity", "[+10]% Spy effectiveness")
            "Hic Sunt Dracones" -> listOf(
                "[+3] Movement <for [Land] units>",
                "[+50]% Gold from pillaging [in all cities]"
            )
            "Wonders of the Ancient World" -> listOf(
                "[+50]% [Production] [in all cities] <when building [Wonder] buildings>"
            )
            "Civic Pride" -> listOf(
                "[+2] [Culture] from every [Monument]",
                "[+1] [Production] from every [Monument]"
            )
            "Heroic Epic" -> listOf("[+10]% Strength <for [All] units>")
            "Age of Discovery" -> listOf("[+50]% [Science] from [Trade Routes]")
            else -> emptyList()
        }
    }

    @Readonly
    fun getDedicationBonus(): List<Unique> {
        return if (currentDedication != null) {
            getDedicationUniques(currentDedication!!).map { Unique(it) }
        } else emptyList()
    }

    fun endTurn() {
        // Era points from amenities surplus
        val amenitiesSurplus = civInfo.getAmenities() - (civInfo.cities.sumOf { it.population.population } - civInfo.cities.size * 2)
        if (amenitiesSurplus >= 3) {
            storedEraPoints += 3
        } else if (amenitiesSurplus < 0) {
            storedEraPoints = maxOf(0, storedEraPoints - 1)
        }
        
        // Stored era points accumulate toward next Golden Age (only when not in Golden/Heroic)
        if (!isGoldenAge() && !isHeroicAge())
            storedEraPoints = (storedEraPoints + if (amenitiesSurplus >= 3) 1 else 0).coerceAtLeast(0)

        // Golden/Heroic age countdown
        if (isGoldenAge() || isHeroicAge()){
            turnsLeftForCurrentGoldenAge--
            if (turnsLeftForCurrentGoldenAge <= 0) {
                for (unique in civInfo.getTriggeredUniques(UniqueType.TriggerUpponEndingGoldenAge))
                    UniqueTriggerActivation.triggerUnique(unique, civInfo)
                if (isGoldenAge()) currentAge = "Normal"
                if (isHeroicAge()) currentAge = "Normal"
            }
        }

        // Auto-enter Golden Age if enough stored points
        else if (storedEraPoints > eraPointsRequiredForNextGoldenAge()) {
            storedEraPoints -= eraPointsRequiredForNextGoldenAge()
            enterGoldenAge()
            numberOfGoldenAges++
        }
    }
    
    /** Get loyalty pressure modifier based on current age. */
    @Readonly
    fun getLoyaltyModifier(): Int {
        return when (currentAge) {
            "Dark" -> -4
            "Normal" -> 0
            "Golden" -> 3
            "Heroic" -> 6
            else -> 0
        }
    }
    
    /** Get policy slot bonus/penalty based on age. */
    @Readonly
    fun getPolicySlotModifier(): Int {
        return when (currentAge) {
            "Dark" -> -1
            "Golden" -> 1
            "Heroic" -> 2
            else -> 0
        }
    }
    
    /** Get available dedications for current age - in Civ VI the same core dedications
     *  are offered every era, regardless of the age entered. */
    fun getDedicationsForAge(): List<String> = dedications

    /** The Civ VI dedications offered at every era transition. */
    private val dedications = listOf(
        "Monumentality",
        "Free Inquiry",
        "Pen, Brush, and Voice",
        "Exodus of the Evangelists",
        "Reform the Coinage",
        "To Arms!"
    )

    /** Civ VI: during a Normal or Dark Age the dedication acts as a quest generator,
     *  granting Era Score each time the player performs the dedicated action. */
    enum class DedicationEvent(val score: Int) {
        DistrictBuilt(1),
        EurekaTriggered(1),
        ScienceOrTradeBuildingBuilt(2),
        InspirationTriggered(1),
        CultureBuildingBuilt(2),
        ForeignCityConverted(2),
        InternationalTradeRouteCompleted(1),
        EnemyMilitaryUnitKilled(1),
        CityConquered(1)
    }

    /** Awards Era Score for [event] if it matches the active dedication and we are in a
     *  Normal or Dark Age (during a Golden/Heroic Age the dedication grants its power instead). */
    fun awardDedicationEraScore(event: DedicationEvent) {
        if (isGoldenAge() || isHeroicAge()) return
        val dedication = currentDedication ?: return
        val matches = when (dedication) {
            "Monumentality" -> event == DedicationEvent.DistrictBuilt
            "Free Inquiry" -> event == DedicationEvent.EurekaTriggered
                    || event == DedicationEvent.ScienceOrTradeBuildingBuilt
            "Pen, Brush, and Voice" -> event == DedicationEvent.InspirationTriggered
                    || event == DedicationEvent.CultureBuildingBuilt
            "Exodus of the Evangelists" -> event == DedicationEvent.ForeignCityConverted
            "Reform the Coinage" -> event == DedicationEvent.InternationalTradeRouteCompleted
            "To Arms!" -> event == DedicationEvent.EnemyMilitaryUnitKilled
                    || event == DedicationEvent.CityConquered
            else -> false
        }
        if (matches) addEraScore(event.score, dedication)
    }
    
    /** Get era score thresholds for UI and era transition.
     *  Era Score is earned mainly from wonders ([4] each), so the Golden Age threshold requires
     *  building several of them in an era (Civ VI): 14 in the first era, +4 per subsequent era. */
    @Readonly
    fun getGoldenThreshold(newEraNumber: Int, isDramaticAges: Boolean = civInfo.gameModes.isDramaticAgesModeActive()): Int {
        val base = 10 + 4 * newEraNumber.coerceAtLeast(1)
        return if (isDramaticAges) (base * 1.5).toInt() else base
    }
    
    @Readonly
    fun getDarkThreshold(newEraNumber: Int): Int = 2 + 2 * newEraNumber.coerceAtLeast(1)
}