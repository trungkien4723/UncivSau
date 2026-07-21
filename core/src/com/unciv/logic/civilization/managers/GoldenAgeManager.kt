package com.unciv.logic.civilization.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.civilization.AlertType
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.CivilopediaAction
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.civilization.PopupAlert
import com.unciv.models.ruleset.unique.UniqueTriggerActivation
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.ui.components.extensions.toPercent
import yairm210.purity.annotations.Readonly

class GoldenAgeManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var civInfo: Civilization

    var storedHappiness = 0
    private var numberOfGoldenAges = 0
    var turnsLeftForCurrentGoldenAge = 0

    // === Civ VI Era Score / Ages (6C) ===
    var eraScore = 0              // Era Score accumulated during the current era
    var totalEraScore = 0         // all-time Era Score
    var currentAge = "Normal"      // "Dark" | "Normal" | "Golden"
    var previousAge = "Normal"
    var eraScoreForLastAge = 0     // Era Score achieved in the age that just ended (for UI)

    fun clone(): GoldenAgeManager {
        val toReturn = GoldenAgeManager()
        toReturn.numberOfGoldenAges = numberOfGoldenAges
        toReturn.storedHappiness = storedHappiness
        toReturn.turnsLeftForCurrentGoldenAge = turnsLeftForCurrentGoldenAge
        toReturn.eraScore = eraScore
        toReturn.totalEraScore = totalEraScore
        toReturn.currentAge = currentAge
        toReturn.previousAge = previousAge
        toReturn.eraScoreForLastAge = eraScoreForLastAge
        return toReturn
    }

    @Readonly fun isGoldenAge(): Boolean = turnsLeftForCurrentGoldenAge > 0 || currentAge == "Golden"
    
    fun addHappiness(amount: Int) {
        storedHappiness += amount
    }

    @Readonly
    fun happinessRequiredForNextGoldenAge(): Int {
        var cost = (500 + numberOfGoldenAges * 250).toFloat()
        cost *= civInfo.cities.size.toPercent()  //https://forums.civfanatics.com/resources/complete-guide-to-happiness-vanilla.25584/
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
        civInfo.addNotification("You have entered a Golden Age!",
            CivilopediaAction("Tutorial/Golden Age"),
            NotificationCategory.General, "StatIcons/Happiness")
        civInfo.popupAlerts.add(PopupAlert(AlertType.GoldenAge, ""))

        for (unique in civInfo.getTriggeredUniques(UniqueType.TriggerUponEnteringGoldenAge))
            UniqueTriggerActivation.triggerUnique(unique, civInfo)
        //Golden Age can happen mid turn with Great Artist effects
        for (city in civInfo.cities)
            city.cityStats.update()
    }

    // === Civ VI Era Score / Ages (6C) ===

    /** Add Era Score (Civ VI Historic Moments). */
    fun addEraScore(amount: Int, source: String = "") {
        if (amount <= 0) return
        eraScore += amount
        totalEraScore += amount
        if (source.isNotEmpty()) {
            civInfo.addNotification("You earned [$amount] Era Score from [$source]!",
                NotificationCategory.General, "StatIcons/EraScore")
        }
    }

    /**
     * Called when the civilization enters a new era (from tech or civic research).
     * Decides the Age (Dark / Normal / Golden) based on Era Score accumulated in the era that just ended.
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
        previousAge = currentAge
        currentAge = age
        eraScoreForLastAge = eraScore
        eraScore = 0  // reset for the new era

        val label = when (age) {
            "Golden" -> "Golden Age"
            "Dark" -> "Dark Age"
            else -> "Normal Age"
        }
        civInfo.addNotification("You have entered a [$label]!",
            CivilopediaAction("Tutorial/Golden Age"), NotificationCategory.General, "StatIcons/EraScore")

        if (age == "Golden") {
            enterGoldenAge()
            numberOfGoldenAges++
        }
        return age
    }

    fun endTurn(happiness: Int) {
        if (!isGoldenAge())
            storedHappiness = (storedHappiness + happiness).coerceAtLeast(0)

        if (isGoldenAge()){
            turnsLeftForCurrentGoldenAge--
            if (turnsLeftForCurrentGoldenAge <= 0)
                for (unique in civInfo.getTriggeredUniques(UniqueType.TriggerUpponEndingGoldenAge))
                    UniqueTriggerActivation.triggerUnique(unique, civInfo)
        }
                
        else if (storedHappiness > happinessRequiredForNextGoldenAge()) {
            storedHappiness -= happinessRequiredForNextGoldenAge()
            enterGoldenAge()
            numberOfGoldenAges++
        }
    }

}
