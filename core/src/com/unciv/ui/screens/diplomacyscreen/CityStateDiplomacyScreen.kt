package com.unciv.ui.screens.diplomacyscreen

import com.unciv.logic.civilization.Civilization
import com.unciv.ui.screens.basescreen.BaseScreen
import com.unciv.ui.screens.basescreen.RecreateOnResize

/**
 * Civ VI-style City-States screen: lists only the known city-states in the left sidebar and
 * shows the city-state management panel (envoys, quests, bonuses, declare war, etc.) on the right.
 *
 * Opened from the City-States banner button in the top bar, or when the player selects a
 * city-state from the map / notifications / global politics overview.
 */
class CityStateDiplomacyScreen(
    viewingCiv: Civilization,
    selectCiv: Civilization? = null,
): DiplomacyScreen(viewingCiv, selectCiv), RecreateOnResize {

    override fun getKnownCivsForLeftSide(): List<Civilization> =
        viewingCiv.diplomacyFunctions.getKnownCivsSorted(includeCityStates = true)
            .filter { it.isCityState }.toList()

    override fun recreate(): BaseScreen = CityStateDiplomacyScreen(viewingCiv, selectCiv)
}