package com.unciv.logic.automation

import com.unciv.logic.automation.civilization.GamePhase
import com.unciv.logic.automation.civilization.getGamePhase
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
}
