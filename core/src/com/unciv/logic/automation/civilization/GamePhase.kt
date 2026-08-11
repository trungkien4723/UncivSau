package com.unciv.logic.automation.civilization

import com.unciv.logic.civilization.Civilization
import yairm210.purity.annotations.Readonly

enum class GamePhase {
    Early,
    Mid,
    Late;

    val isEarly: Boolean get() = this == Early
    val isMid: Boolean get() = this == Mid
    val isLate: Boolean get() = this == Late
}

/**
 * Divides the game into three AI strategy phases based on the current era:
 * - [GamePhase.Early]: Ancient / Classical
 * - [GamePhase.Mid]: Medieval / Renaissance
 * - [GamePhase.Late]: Industrial and onwards
 */
@Readonly
fun Civilization.getGamePhase(): GamePhase {
    val eraNumber = getEraNumber()
    return when {
        eraNumber <= 1 -> GamePhase.Early
        eraNumber <= 3 -> GamePhase.Mid
        else -> GamePhase.Late
    }
}
