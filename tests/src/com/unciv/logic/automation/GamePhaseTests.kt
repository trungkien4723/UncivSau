package com.unciv.logic.automation

import com.unciv.logic.automation.civilization.GamePhase
import com.unciv.logic.automation.civilization.getGamePhase
import com.unciv.logic.automation.civilization.militaryBuildModifier
import com.unciv.logic.automation.civilization.minimumFreeLandForExpansion
import com.unciv.logic.automation.civilization.workerRatio
import com.unciv.logic.automation.civilization.wonderModifier
import com.unciv.logic.automation.unit.CivilianUnitAutomation
import com.unciv.models.ruleset.tech.Era
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class GamePhaseTests {

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

    @Test
    fun ancientEraIsEarlyGame() {
        setEra(0)
        assertEquals(GamePhase.Early, civ.getGamePhase())
    }

    @Test
    fun classicalEraIsEarlyGame() {
        setEra(1)
        assertEquals(GamePhase.Early, civ.getGamePhase())
    }

    @Test
    fun medievalAndRenaissanceAreMidGame() {
        setEra(2)
        assertEquals(GamePhase.Mid, civ.getGamePhase())
        setEra(3)
        assertEquals(GamePhase.Mid, civ.getGamePhase())
    }

    @Test
    fun industrialEraAndBeyondAreLateGame() {
        setEra(4)
        assertEquals(GamePhase.Late, civ.getGamePhase())
        setEra(8)
        assertEquals(GamePhase.Late, civ.getGamePhase())
    }

    @Test
    fun isLateGameMatchesLatePhase() {
        setEra(3)
        assertFalse(CivilianUnitAutomation.isLateGame(civ))
        setEra(4)
        assertTrue(CivilianUnitAutomation.isLateGame(civ))
    }

    @Test
    fun expansionIsEagerInEarlyAndMidGame() {
        assertTrue(GamePhase.Early.minimumFreeLandForExpansion(false) < GamePhase.Late.minimumFreeLandForExpansion(false))
        assertTrue(GamePhase.Mid.minimumFreeLandForExpansion(false) < GamePhase.Late.minimumFreeLandForExpansion(false))
        // Expansionist agendas expand more readily than defensive ones
        assertTrue(GamePhase.Late.minimumFreeLandForExpansion(true) < GamePhase.Late.minimumFreeLandForExpansion(false))
    }

    @Test
    fun workersArePrioritizedInEarlyGame() {
        assertTrue(GamePhase.Early.workerRatio() > GamePhase.Mid.workerRatio())
        assertTrue(GamePhase.Mid.workerRatio() > GamePhase.Late.workerRatio())
    }

    @Test
    fun militaryBuildsUpInLateGame() {
        assertEquals(1f, GamePhase.Mid.militaryBuildModifier(), 0f)
        assertTrue(GamePhase.Late.militaryBuildModifier() > GamePhase.Mid.militaryBuildModifier())
        assertTrue(GamePhase.Early.militaryBuildModifier() > GamePhase.Mid.militaryBuildModifier())
    }

    @Test
    fun wondersAreWeightedByPhase() {
        assertEquals(1f, GamePhase.Mid.wonderModifier(), 0f)
        assertTrue(GamePhase.Early.wonderModifier() > 1f)
        assertTrue(GamePhase.Late.wonderModifier() > GamePhase.Early.wonderModifier())
    }
}
