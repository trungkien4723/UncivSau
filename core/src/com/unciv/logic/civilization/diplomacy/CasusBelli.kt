package com.unciv.logic.civilization.diplomacy

import com.unciv.logic.civilization.Civilization
import yairm210.purity.annotations.Readonly

/**
 * Casus Belli (Justification for War) - Civ VI style
 * Determines the diplomatic penalty/warmonger penalty for declaring war
 */
enum class CasusBelli(
    val displayName: String, 
    val warmongerPenaltyPercent: Int, 
    val grievanceCost: Int, 
    val warSupportForAttacker: Int,
    val canUse: (Civilization, Civilization) -> Boolean = { attacker, defender -> true }
) {
    SurpriseWar("Surprise War", 100, 100, -3, { _, _ -> true }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Declaring war without a valid Casus Belli. Maximum warmonger penalty."
    },
    FormalWar("Formal War", 75, 75, 3, { attacker, defender -> attacker.diplomacyFunctions.canDeclareFormalWar(defender) }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Requires denouncing the target 5 turns ago. Moderate warmonger penalty."
    },
    HolyWar("Holy War", 50, 50, 2, { attacker, defender -> attacker.diplomacyFunctions.canDeclareHolyWar(defender) }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Target follows a different religion. Low warmonger penalty."
    },
    LiberationWar("Liberation War", 25, 25, 3, { attacker, defender -> attacker.diplomacyFunctions.canDeclareLiberationWar(defender) }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Target holds a city that originally belonged to you or an ally. Very low warmonger penalty."
    },
    ReconquestWar("Reconquest War", 50, 50, 2, { attacker, defender -> attacker.diplomacyFunctions.canDeclareReconquestWar(defender) }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Target holds a city that originally belonged to you. Moderate warmonger penalty."
    },
    ProtectorateWar("Protectorate War", 0, 0, 2, { attacker, defender -> attacker.diplomacyFunctions.canDeclareProtectorateWar(defender) }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Target attacked your city-state ally. No warmonger penalty."
    },
    ColonialWar("Colonial War", 50, 50, 1, { attacker, defender -> attacker.diplomacyFunctions.canDeclareColonialWar(defender) }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Target is on a different continent. Moderate warmonger penalty."
    },
    RetributionWar("Retribution War", 50, 50, 2, { attacker, defender -> attacker.diplomacyFunctions.canDeclareRetributionWar(defender) }) {
        override fun getDescription(attacker: Civilization, defender: Civilization): String = "Target has converted our cities to their religion. Moderate warmonger penalty."
    },
    ;

    abstract fun getDescription(attacker: Civilization, defender: Civilization): String

    companion object {
        @Readonly
        fun getAvailableCasusBelli(attacker: Civilization, defender: Civilization): List<CasusBelli> {
            return values().filter { it.canUse(attacker, defender) }
        }
    }
}