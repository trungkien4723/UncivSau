package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.AlertType
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.CivilopediaAction
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.civilization.PopupAlert
import com.unciv.models.ruleset.unique.Unique
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
        // Thresholds scale with era number (Civ VI-like): Golden if score >= 2*era, Dark if < era.
        val goldenThreshold = 2 * newEraNumber.coerceAtLeast(1)
        val darkThreshold = newEraNumber.coerceAtLeast(1)
        
        val age = when {
            eraScore >= goldenThreshold -> "Golden"
            eraScore < darkThreshold -> "Dark"
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
        
        // Generate available dedications for the new age
        availableDedications = generateDedicationsForAge(finalAge).toMutableList()
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

    /** Select a dedication for the new age. */
    fun selectDedication(dedicationName: String) {
        if (dedicationName in availableDedications) {
            currentDedication = dedicationName
        }
}

    private fun generateDedicationsForAge(age: String): List<String> {
        val allDedications = mutableMapOf<String, List<String>>(
            "Dark" to listOf(
                "Exodus of the Evangelists",
                "Penitent",
                "Commune",
                "Inquisition"
            ),
            "Normal" to listOf(
                "Heartbeat of Steam",
                "To Arms!",
                "Monumentality",
                "Free Inquiry",
                "Bodyguard of Lies"
            ),
            "Golden" to listOf(
                "Hic Sunt Dracones",
                "Wonders of the Ancient World",
                "Civic Pride",
                "Bodyguard of Lies"
            ),
            "Heroic" to listOf(
                "Heroic Epic",
                "Age of Discovery",
                "Monumentality",
                "Free Inquiry"
            )
        )
        return allDedications[age] ?: allDedications["Normal"]!!
    }

    @Readonly
    fun getDedicationBonus(): List<Unique> {
        return if (currentDedication != null) {
            // Convert dedication name to uniques
            listOf(Unique("$currentDedication dedication"))
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
    
    /** Get available dedications for current age. */
    fun getDedicationsForAge(): List<String> {
        return generateDedicationsForAge(currentAge)
    }
    
    /** Get era score thresholds for UI. */
    @Readonly
    fun getGoldenThreshold(newEraNumber: Int): Int = 2 * newEraNumber.coerceAtLeast(1)
    
    @Readonly
    fun getDarkThreshold(newEraNumber: Int): Int = newEraNumber.coerceAtLeast(1)
}