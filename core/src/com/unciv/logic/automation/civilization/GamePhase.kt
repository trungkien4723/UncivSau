package com.unciv.logic.automation.civilization

import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Victory
import com.unciv.models.stats.Stat
import com.unciv.ui.screens.victoryscreen.RankingType
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

/**
 * Number of Builders/Workers per city the AI wants to maintain.
 * The young empire needs more builders, the late one already has its improvements in place.
 */
@Readonly
fun GamePhase.workerRatio(): Float = when (this) {
    GamePhase.Early -> 1.2f
    GamePhase.Mid -> 1f
    GamePhase.Late -> 0.8f
}

/**
 * Modifier for military unit production priority: the early empire defends itself
 * while it expands, the late one builds up for wars and territory defense.
 */
@Readonly
fun GamePhase.militaryBuildModifier(): Float = when (this) {
    GamePhase.Early -> 1.2f
    GamePhase.Mid -> 1f
    GamePhase.Late -> 1.3f
}

/**
 * Minimum amount of free, settleable tiles near the empire before the AI trains another Settler.
 * Expansion is an early/mid-game priority; late game only genuinely free land is settled.
 */
@Readonly
fun GamePhase.minimumFreeLandForExpansion(expansionist: Boolean): Int = when (this) {
    GamePhase.Early -> 1
    GamePhase.Mid -> 2
    GamePhase.Late -> if (expansionist) 3 else 6
}

/**
 * Extra weight put on constructing World Wonders, reflecting that an early-game wonder
 * lead can snowball while a late-game wonder directly pushes towards a victory condition.
 */
@Readonly
fun GamePhase.wonderModifier(): Float = when (this) {
    GamePhase.Early -> 1.3f
    GamePhase.Mid -> 1f
    GamePhase.Late -> 1.5f
}

/**
 * The major AI civs the civ compares itself against for victory decisions, excluding itself.
 */
@Readonly
private fun Civilization.getVictoryField(): List<Civilization> =
    gameInfo.civilizations.filter { it.isMajorCiv() && it.isAlive() && it != this }

/**
 * Returns the multiplier to put on the [Stat] the AI is strongest at **relative to the
 * rest of the field**, so it commits to its best victory path as the game progresses:
 * - [GamePhase.Early]: no specialization yet - the civ must grow first.
 * - [GamePhase.Mid]: mild boost (1.3x) towards the strongest stat.
 * - [GamePhase.Late]: strong boost (1.8x) - commit to the victory condition.
 *
 * Only major AI civs that actually produce the chosen stat specialize; returns an empty
 * map otherwise (human civs, city states, a civ with no output yet).
 */
@Readonly
fun Civilization.getAiVictoryStatModifiers(): Map<Stat, Float> {
    if (!isAI()) return emptyMap()
    val phase = getGamePhase()
    if (phase == GamePhase.Early) return emptyMap()

    val field = getVictoryField()
    if (field.isEmpty()) return emptyMap()

    fun ratio(stat: Stat): Float {
        val mine = stats.statsForNextTurn[stat]
        val fieldAverage = field.map { it.stats.statsForNextTurn[stat] }.average().toFloat()
        return if (fieldAverage <= 0f) 1.5f else mine / fieldAverage
    }

    val victoryStats = listOf(Stat.Science, Stat.Culture, Stat.Faith, Stat.Gold, Stat.Production)
    val focusStat = victoryStats.maxBy { ratio(it) }
    if (stats.statsForNextTurn[focusStat] <= 0f) return emptyMap()

    val boost = if (phase == GamePhase.Late) 1.8f else 1.3f
    return mapOf(focusStat to boost)
}

/**
 * Decides which victory type the AI is best positioned for and commits to it, based on the
 * victory conditions present in the ruleset:
 * - a [Victory.Focus.Military] (Domination) is measured by military might vs the field,
 * - stat focuses are measured by next-turn output vs the field.
 *
 * Returns null during [GamePhase.Early] or when the civ is not ahead of the field at anything
 * (it should keep growing instead of specializing).
 */
@Readonly
fun Civilization.getAiVictoryFocus(): Victory.Focus? {
    if (!isAI()) return null
    val phase = getGamePhase()
    if (phase == GamePhase.Early) return null

    val field = getVictoryField()
    if (field.isEmpty()) return null

    fun statRatio(stat: Stat): Float {
        val mine = stats.statsForNextTurn[stat]
        if (mine <= 0f) return 0f
        val fieldAverage = field.map { it.stats.statsForNextTurn[stat] }.average().toFloat()
        return if (fieldAverage <= 0f) 1.5f else mine / fieldAverage
    }
    fun mightRatio(): Float {
        val mine = getStatForRanking(RankingType.Force)
        if (mine <= 0) return 0f
        val fieldAverage = field.map { it.getStatForRanking(RankingType.Force) }.average().toFloat()
        return if (fieldAverage <= 0f) 1.5f else mine / fieldAverage
    }
    fun powerRatio(focus: Victory.Focus): Float = when (focus) {
        Victory.Focus.Military -> mightRatio()
        Victory.Focus.Science -> statRatio(Stat.Science)
        Victory.Focus.Culture -> statRatio(Stat.Culture)
        Victory.Focus.Faith -> statRatio(Stat.Faith)
        Victory.Focus.Gold -> statRatio(Stat.Gold)
        Victory.Focus.Production -> statRatio(Stat.Production)
        Victory.Focus.CityStates -> statRatio(Stat.Gold) // gold funds city-state courting
        Victory.Focus.Score -> statRatio(Stat.Production) // best proxy for overall growth
    }

    val focuses = gameInfo.ruleset.victories.values
        .flatMap { it.milestoneObjects }
        .map { milestone -> milestone.getFocus(this) }
        .distinct()
    val bestFocus = focuses.maxByOrNull { powerRatio(it) } ?: return null
    return bestFocus.takeIf { powerRatio(it) > 1f }
}
