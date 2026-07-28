package com.unciv.logic.city.managers

import com.unciv.logic.IsPartOfGameInfoSerialization
import com.unciv.logic.city.City
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.civilization.PopupAlert
import com.unciv.models.Counter
import com.unciv.models.ruleset.Governor
import yairm210.purity.annotations.Readonly

/**
 * Civ VI Loyalty (Rise and Fall) — 6D.
 * Each city accumulates loyalty. When loyalty reaches 0 the city leaves its owner
 * (becoming a Free City or being absorbed by a neighbouring civilization).
 */
class CityLoyaltyManager : IsPartOfGameInfoSerialization {
    @Transient
    lateinit var city: City

    /** Current loyalty, 0..100. Starts at 100 for a newly founded/annexed city. */
    var loyalty = 100

    /** Debug: last calculated net pressure (positive = growing, negative = decaying). */
    var lastPressure = 0

    fun clone(): CityLoyaltyManager {
        val toReturn = CityLoyaltyManager()
        toReturn.loyalty = loyalty
        toReturn.lastPressure = lastPressure
        return toReturn
    }

    @Readonly
    fun isLoyal(): Boolean = loyalty > 0

    /** Called at the start of the owner's turn. */
    fun startTurn() {
        val civ = city.civ
        if (civ.isDefeated() || civ.isSpectator()) return
        // Loyalty is a Civ VI mechanic — only active when the ruleset defines governors.
        if (civ.gameInfo.ruleset.governors.isEmpty()) return

        val pressure = calculatePressure(civ)
        lastPressure = pressure
        loyalty = (loyalty + pressure).coerceIn(0, 100)

        if (loyalty <= 0) {
            cityFalls()
        } else if (loyalty < 25) {
            city.civ.addNotification(
                "The citizens of [${city.name}] are protesting! Loyalty is critical ([${loyalty}]).",
                NotificationCategory.Cities, "StatIcons/Loyalty"
            )
        }
    }

    /** Net loyalty pressure this turn. */
    @Readonly
    private fun calculatePressure(civ: Civilization): Int {
        // Base growth when stable and happy
        var pressure = 4

        // Occupied / recently conquered cities lose loyalty
        if (city.turnAcquired > 0 && civ.gameInfo.turns - city.turnAcquired < 10) pressure -= 6

        // Happiness of the owning civ
        val amenities = civ.getAmenities()
        pressure += when {
            amenities >= 10 -> 2
            amenities >= 0 -> 0
            amenities >= -10 -> -2
            else -> -4
        }

        // Distance to capital — farther cities are harder to keep loyal
        val capital = civ.getCapital()
        if (capital != null && !city.isCapital()) {
            val distance = city.getCenterTile().aerialDistanceTo(capital.getCenterTile())
            if (distance > 12) pressure -= 3
            else if (distance > 8) pressure -= 1
        }

        // Garrison unit present helps
        if (city.getCenterTile().militaryUnit != null) pressure += 2

        // Assigned governor provides loyalty pressure
        val governor = city.getGovernor()
        if (governor != null) pressure += governor.loyaltyBonus

        // Nearby enemy military units apply negative pressure
        val enemyPressure = Counter<String>()
        for (tile in city.getCenterTile().getTilesInDistance(3)) {
            val unit = tile.militaryUnit
            if (unit != null && unit.civ != civ && civ.isAtWarWith(unit.civ)) {
                enemyPressure.add(unit.civ.civName, 2)
            }
        }
        if (enemyPressure.isNotEmpty()) {
            val maxEnemy = enemyPressure.maxByOrNull { it.value }?.value ?: 0
            pressure -= maxEnemy.coerceAtMost(8)
        }

        // Climate phase penalty - higher climate phases reduce loyalty
        val climatePhase = civ.climateManager.climatePhase
        when (climatePhase) {
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_I -> pressure -= 1
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_II -> pressure -= 2
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_III -> pressure -= 3
            com.unciv.logic.civilization.managers.ClimatePhase.PHASE_IV -> pressure -= 5
            else -> {}
        }

        return pressure
    }

    /** City falls: becomes a Free City or is absorbed by the civ with the highest pressure. */
    private fun cityFalls() {
        val civ = city.civ
        val pressures = Counter<String>()
        for (tile in city.getCenterTile().getTilesInDistance(3)) {
            val unit = tile.militaryUnit
            if (unit != null && unit.civ != civ && civ.isAtWarWith(unit.civ)) {
                pressures.add(unit.civ.civName, 1)
            }
        }
        var newOwner: String? = null
        var maxP = 0
        for ((owner, p) in pressures) {
            if (p > maxP) { maxP = p; newOwner = owner }
        }
        if (newOwner != null) {
            val newCiv = civ.gameInfo.getCivilization(newOwner)
            if (newCiv.isAlive()) {
                city.puppetCity(newCiv)
                newCiv.addNotification(
                    "[${city.name}] has joined your empire through Loyalty!",
                    NotificationCategory.Cities, "StatIcons/Loyalty"
                )
                civ.addNotification(
                    "Your city [${city.name}] has been lost to [${newCiv.civName}] due to low Loyalty!",
                    NotificationCategory.Cities, "StatIcons/Loyalty"
                )
                return
            }
        }
        // No neighbouring enemy — city falls into disorder and is destroyed (Free City simplified)
        city.civ.addNotification(
            "[${city.name}] has fallen into disorder due to low Loyalty!",
            NotificationCategory.Cities, "StatIcons/Loyalty"
        )
        city.destroyCity()
        loyalty = 0
    }
}
