package com.unciv.logic.automation

import com.unciv.logic.automation.civilization.getAiVictoryFocus
import com.unciv.logic.automation.civilization.getAiVictoryStatModifiers
import com.unciv.logic.automation.civilization.getCivicFocusMultiplier
import com.unciv.logic.automation.civilization.getTechFocusMultiplier
import com.unciv.models.ruleset.Victory
import com.unciv.models.ruleset.tech.Era
import com.unciv.models.stats.Stat
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class AiVictoryFocusTests {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(3)
        civ = testGame.addCiv()
    }

    private fun setEra(eraNumber: Int) {
        civ.tech.era = Era().apply { this.eraNumber = eraNumber }
    }

    private fun addAliveEnemy(): com.unciv.logic.civilization.Civilization {
        val enemy = testGame.addCiv()
        testGame.addUnit("Warrior", enemy, testGame.getTile(3, 3)) // so enemy is alive (not defeated)
        return enemy
    }

    @Test
    fun earlyGameHasNoSpecialization() {
        setEra(0)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 50f
        enemy.stats.statsForNextTurn[Stat.Science] = 10f

        assertTrue(civ.getAiVictoryStatModifiers().isEmpty())
    }

    @Test
    fun humanCivHasNoSpecialization() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 50f
        enemy.stats.statsForNextTurn[Stat.Science] = 10f
        civ.playerType = com.unciv.logic.civilization.PlayerType.Human

        assertTrue(civ.getAiVictoryStatModifiers().isEmpty())
    }

    @Test
    fun noSpecializationWithoutOutput() {
        setEra(4)
        addAliveEnemy() // field exists, but we produce nothing yet

        assertTrue(civ.getAiVictoryStatModifiers().isEmpty())
    }

    @Test
    fun midGameFavorsStrongestStat() {
        setEra(2)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 50f
        civ.stats.statsForNextTurn[Stat.Culture] = 10f
        civ.stats.statsForNextTurn[Stat.Gold] = 10f
        enemy.stats.statsForNextTurn[Stat.Science] = 10f
        enemy.stats.statsForNextTurn[Stat.Culture] = 10f
        enemy.stats.statsForNextTurn[Stat.Gold] = 10f

        val modifiers = civ.getAiVictoryStatModifiers()
        assertEquals(1, modifiers.size)
        assertEquals(1.3f, modifiers[Stat.Science]!!, 0.0001f)
    }

    @Test
    fun lateGameBoostIsStrongerThanMid() {
        fun boostForEra(era: Int): Float {
            val game = TestGame()
            game.makeHexagonalMap(3)
            val civ = game.addCiv()
            civ.tech.era = Era().apply { this.eraNumber = era }
            val enemy = game.addCiv()
            game.addUnit("Warrior", enemy, game.getTile(3, 3)) // so enemy is alive
            civ.stats.statsForNextTurn[Stat.Faith] = 30f
            enemy.stats.statsForNextTurn[Stat.Faith] = 5f
            return civ.getAiVictoryStatModifiers().values.first()
        }

        val midBoost = boostForEra(2)
        val lateBoost = boostForEra(4)
        assertTrue(lateBoost > midBoost)
        assertEquals(1.3f, midBoost, 0.0001f)
        assertEquals(1.8f, lateBoost, 0.0001f)
    }

    @Test
    fun focusFollowsFieldStrength() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 10f
        civ.stats.statsForNextTurn[Stat.Gold] = 100f
        enemy.stats.statsForNextTurn[Stat.Science] = 10f
        enemy.stats.statsForNextTurn[Stat.Gold] = 10f

        val modifiers = civ.getAiVictoryStatModifiers()
        assertEquals(1, modifiers.size)
        assertEquals(1.8f, modifiers[Stat.Gold]!!, 0.0001f)
    }

    @Test
    fun militaryFocusWhenDominant() {
        setEra(4)
        addAliveEnemy()
        // many warriors but no stat output: might is our only advantage
        for (i in 0..5) testGame.addUnit("Warrior", civ, testGame.getTile(i % 3, i / 3))

        assertEquals(Victory.Focus.Military, civ.getAiVictoryFocus())
    }

    @Test
    fun scienceFocusWhenDominant() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 50f
        civ.stats.statsForNextTurn[Stat.Culture] = 20f
        enemy.stats.statsForNextTurn[Stat.Science] = 10f
        enemy.stats.statsForNextTurn[Stat.Culture] = 10f

        // strongest relative stat is Science (Apollo Program not researched -> Scientific victory = Science focus)
        assertEquals(Victory.Focus.Science, civ.getAiVictoryFocus())
    }

    @Test
    fun cultureFocusWhenTourismDominant() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Culture] = 50f
        enemy.stats.statsForNextTurn[Stat.Culture] = 10f

        assertEquals(Victory.Focus.Culture, civ.getAiVictoryFocus())
    }

    @Test
    fun noFocusWhenBehind() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 10f
        enemy.stats.statsForNextTurn[Stat.Science] = 100f

        assertEquals(null, civ.getAiVictoryFocus())
    }

    @Test
    fun earlyGameHasNoFocus() {
        setEra(0)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 50f
        enemy.stats.statsForNextTurn[Stat.Science] = 10f

        assertEquals(null, civ.getAiVictoryFocus())
    }

    @Test
    fun scienceFocusResearchesApolloTech() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 50f
        enemy.stats.statsForNextTurn[Stat.Science] = 10f
        assertEquals(Victory.Focus.Science, civ.getAiVictoryFocus())

        // Rocketry unlocks Apollo Program (Enables construction of Spaceship parts) -> strongly boosted
        assertTrue(civ.getTechFocusMultiplier(testGame.ruleset.technologies["Rocketry"]!!) > 1f)
    }

    @Test
    fun militaryFocusResearchesMilitaryTechs() {
        setEra(4)
        addAliveEnemy()
        for (i in 0..5) testGame.addUnit("Warrior", civ, testGame.getTile(i % 3, i / 3))
        assertEquals(Victory.Focus.Military, civ.getAiVictoryFocus())

        // Bronze Working unlocks Spearman, a military unit
        assertTrue(civ.getTechFocusMultiplier(testGame.ruleset.technologies["Bronze Working"]!!) > 1f)
    }

    @Test
    fun noFocusLeavesResearchWeightUntouched() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 10f
        enemy.stats.statsForNextTurn[Stat.Science] = 100f

        assertEquals(null, civ.getAiVictoryFocus())
        assertEquals(1f, civ.getTechFocusMultiplier(testGame.ruleset.technologies["Rocketry"]!!), 0.0001f)
    }

    @Test
    fun cultureFocusResearchesCultureCivics() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Culture] = 50f
        enemy.stats.statsForNextTurn[Stat.Culture] = 10f
        assertEquals(Victory.Focus.Culture, civ.getAiVictoryFocus())

        // Drama and Poetry unlocks the Amphitheater (Great Work slot) -> culture path
        assertTrue(civ.getCivicFocusMultiplier(testGame.ruleset.civics["Drama and Poetry"]!!) > 1f)
    }

    @Test
    fun faithFocusResearchesFaithCivics() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Faith] = 50f
        enemy.stats.statsForNextTurn[Stat.Faith] = 10f
        assertEquals(Victory.Focus.Faith, civ.getAiVictoryFocus())

        // Political Philosophy unlocks the Temple (Faith yield) -> religion path
        assertTrue(civ.getCivicFocusMultiplier(testGame.ruleset.civics["Political Philosophy"]!!) > 1f)
    }

    @Test
    fun noFocusLeavesCivicWeightUntouched() {
        setEra(4)
        val enemy = addAliveEnemy()
        civ.stats.statsForNextTurn[Stat.Science] = 10f
        enemy.stats.statsForNextTurn[Stat.Science] = 100f

        assertEquals(null, civ.getAiVictoryFocus())
        assertEquals(1f, civ.getCivicFocusMultiplier(testGame.ruleset.civics["Political Philosophy"]!!), 0.0001f)
    }
}
