package com.unciv.logic.city.managers

import com.unciv.logic.automation.Timers.Companion.timeThis
import com.unciv.logic.city.City
import com.unciv.logic.city.CityFlags
import com.unciv.logic.city.CityFocus
import com.unciv.logic.civilization.CityAction
import com.unciv.logic.civilization.LocationAction
import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.civilization.NotificationIcon
import com.unciv.logic.civilization.OverviewAction
import com.unciv.models.ruleset.unique.UniqueTriggerActivation
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.ui.screens.overviewscreen.EmpireOverviewCategories

class CityTurnManager(val city: City) {


    fun startTurn():Unit = timeThis("CityTurnManager.startTurn") {
        city.clearCaches()
        
        for (resource in city.getResourcesGeneratedByCity()) {
            if (resource.resource.isStockpiled && resource.resource.isCityWide)
                city.gainStockpiledResource(resource.resource, resource.amount)
        }
        for (unique in city.getTriggeredUniques(UniqueType.TriggerUponTurnStart, includeCivUniques = false).toList()) {
            UniqueTriggerActivation.triggerUnique(unique, city)
        }

        // Construct units at the beginning of the turn,
        // so they won't be generated out in the open and vulnerable to enemy attacks before you can control them
        city.cityConstructions.constructIfEnough()

        city.tryUpdateRoadStatus()
        city.attackedThisTurn = false

        // The ordering is intentional - resolve end of resistance before updateCitizens
        nextTurnFlags()

        if (city.isPuppet) {
            city.setCityFocus(CityFocus.GoldFocus)
            city.reassignAllPopulation()
        } else if (city.shouldReassignPopulation || city.civ.isAI()) {
            city.reassignPopulation()  // includes cityStats.update
        } else
            city.cityStats.update()

        // Civ VI Loyalty (Rise and Fall — 6D): apply loyalty pressure; a city may fall here.
        city.loyalty.startTurn()
    }

    // cf DiplomacyManager nextTurnFlags
    private fun nextTurnFlags() {
        for (flag in city.flagsCountdown.keys.toList()) {
            if (city.flagsCountdown[flag]!! > 0)
                city.flagsCountdown[flag] = city.flagsCountdown[flag]!! - 1

            if (city.flagsCountdown[flag] == 0) {
                city.flagsCountdown.remove(flag)

                when (flag) {
                    CityFlags.ResourceDemand.name -> {
                        city.demandedResource = ""
                    }
                    CityFlags.Resistance.name -> {
                        city.shouldReassignPopulation = true
                        city.civ.addNotification(
                            "The resistance in [${city.name}] has ended!",
                            CityAction.withLocation(city), NotificationCategory.General, "StatIcons/Resistance")
                    }
                }
            }
        }
    }

    fun endTurn():Unit = timeThis("CityTurnManager.endTurn") {
        for (unique in city.getTriggeredUniques(UniqueType.TriggerUponTurnEnd, includeCivUniques = false).toList()) {
            UniqueTriggerActivation.triggerUnique(unique, city)
        }
        val stats = city.cityStats.currentCityStats

        city.cityConstructions.endTurn(stats)
        city.expansion.nextTurn(stats.culture)
        if (city.isBeingRazed) {
            val removedPopulation =
                    1 + city.civ.getMatchingUniques(UniqueType.CitiesAreRazedXTimesFaster)
                        .sumOf { it.params[0].toInt() - 1 }

            if (city.population.population <= removedPopulation) {
                city.espionage.removeAllPresentSpies(SpyFleeReason.Other)
                city.civ.addNotification(
                    "[${city.name}] has been razed to the ground!",
                    city.location, NotificationCategory.General,
                    "OtherIcons/Fire"
                )
                city.destroyCity()
            } else { //if not razed yet:
                city.population.addPopulation(-removedPopulation)
                if (city.population.foodStored >= city.population.getFoodToNextPopulation()) { //if surplus in the granary...
                    city.population.foodStored =
                            city.population.getFoodToNextPopulation() - 1 //...reduce below the new growth threshold
                }
            }
        } else city.population.nextTurn(city.foodForNextTurn())

        // This should go after the population change, as that might impact the amount of followers in this city
        if (city.civ.gameInfo.isReligionEnabled()) city.religion.endTurn()

        if (city in city.civ.cities) { // city was not destroyed
            // Civ VI: a city Under Siege (all adjacent tiles blocked by enemy ZoC) cannot heal
            if (!city.isUnderSiege())
                city.health = (city.health + 20).coerceAtMost(city.getMaxHealth())
            city.population.unassignExtraPopulation()
        }
        
        city.clearCaches()
    }

}
