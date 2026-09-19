package com.unciv.logic.automation.civilization

import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.diplomacy.CasusBelli
import com.unciv.logic.civilization.diplomacy.DiplomacyFlags
import com.unciv.logic.civilization.diplomacy.DiplomaticModifiers
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class DeclareWarPlanEvaluatorTests {

    private val testGame = TestGame()

    private fun addCiv() = testGame.addCiv().apply { testGame.addUnit("Warrior", this, null) }

    private val attacker = addCiv()
    private val defender = addCiv()

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(4)
        attacker.diplomacyFunctions.makeCivilizationsMeet(defender)
        // tests may have set a denouncement flag in a previous run - start clean
        defender.getDiplomacyManager(attacker)!!.removeFlag(DiplomacyFlags.Denunciation)
    }

    @Test
    fun `chooseCasusBelli returns null when only surprise war is available`() {
        assertNull(DeclareWarPlanEvaluator.chooseCasusBelli(attacker, defender))
    }

    @Test
    fun `chooseCasusBelli picks the casus belli with the lowest grievance cost`() {
        // Formal War becomes available once the target was denounced 5 turns ago (flag <= 25)
        defender.getDiplomacyManager(attacker)!!.setFlag(DiplomacyFlags.Denunciation, 10)

        assertTrue(attacker.diplomacyFunctions.canDeclareFormalWar(defender))
        val chosen = DeclareWarPlanEvaluator.chooseCasusBelli(attacker, defender)

        assertEquals(CasusBelli.FormalWar, chosen)
        assertEquals(CasusBelli.FormalWar.grievanceCost,
            CasusBelli.getAvailableCasusBelli(attacker, defender)
                .filter { it != CasusBelli.SurpriseWar }
                .minOf { it.grievanceCost })
    }

    @Test
    fun `grievances increase the motivation to declare war`() {
        val baseline = DeclareWarPlanEvaluator.evaluateDeclareWarPlan(attacker, defender, givenMotivation = 60f)

        attacker.getDiplomacyManager(defender)!!.addGrievances(100)

        val withGrievances = DeclareWarPlanEvaluator.evaluateDeclareWarPlan(attacker, defender, givenMotivation = 60f)
        assertEquals(baseline + 5f, withGrievances, 0.001f)
        assertTrue(withGrievances > baseline)
    }

    @Test
    fun `grievances increase the motivation to prepare for war`() {
        val baseline = DeclareWarPlanEvaluator.evaluateStartPreparingWarPlan(attacker, defender, givenMotivation = 60f)

        attacker.getDiplomacyManager(defender)!!.addGrievances(100)

        val withGrievances = DeclareWarPlanEvaluator.evaluateStartPreparingWarPlan(attacker, defender, givenMotivation = 60f)
        assertEquals(baseline + 5f, withGrievances, 0.001f)
        assertTrue(withGrievances > baseline)
    }
}
