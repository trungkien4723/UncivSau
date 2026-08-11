package com.unciv.logic.automation

import com.unciv.models.ruleset.unique.GameContext
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class AiWeightsTests {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(3)
        civ = testGame.addCiv()
    }

    @Test
    fun aiChoiceWeightOnStrategicBuildingsIsApplied() {
        val campus = testGame.ruleset.buildings["Campus"]!!
        val weight = campus.getWeightForAiDecision(GameContext(civ))
        assertEquals(1.5f, weight, 0.001f)
    }

    @Test
    fun buildingAiChoiceWeightIsUntouchedByNeutralPersonality() {
        // An AI civ without a named personality gets neutral personality (no weights)
        val monument = testGame.ruleset.buildings["Monument"]!!
        val weight = monument.getWeightForAiDecision(GameContext(civ))
        assertEquals(1.25f, weight, 0.001f)
    }

    @Test
    fun personalityFaithWeightBoostsFaithBuildings() {
        civ.nation.personality = "Gandhi"
        val shrine = testGame.ruleset.buildings["Shrine"]!!
        // 1.25 (own AiChoiceWeight) * 1.5 (personality [+50]% Faith) = 1.875
        val weight = shrine.getWeightForAiDecision(GameContext(civ))
        assertEquals(1.875f, weight, 0.001f)
    }

    @Test
    fun personalityMilitaryWeightReducesMilitaryUnits() {
        civ.nation.personality = "Gandhi"
        val warrior = testGame.ruleset.units["Warrior"]!!
        // 1.0 * 0.75 (personality [-25]% Military) = 0.75
        val weight = warrior.getWeightForAiDecision(GameContext(civ))
        assertEquals(0.75f, weight, 0.001f)
    }

    @Test
    fun personalityWaterWeightBoostsNavalUnits() {
        civ.nation.personality = "Elizabeth"
        val galley = testGame.ruleset.units["Galley"]!!
        // 1.0 * 1.75 (personality [+75]% Water) = 1.75
        val weight = galley.getWeightForAiDecision(GameContext(civ))
        assertEquals(1.75f, weight, 0.001f)
    }

    @Test
    fun personalityWillNotBuildListsNuclearWeapons() {
        civ.nation.personality = "Gandhi"
        val toAvoid = civ.getPersonality().getMatchingUniques(UniqueType.WillNotBuild, GameContext(civ))
            .map { it.params[0] }
        assertTrue("Gandhi should refuse to build nuclear weapons", "Nuclear Weapon" in toAvoid)
    }

    @Test
    fun nuclearWeaponMatchesWillNotBuildFilter() {
        val atomicBomb = testGame.ruleset.units["Atomic Bomb"]!!
        assertTrue(atomicBomb.matchesFilter("Nuclear Weapon"))
    }

    @Test
    fun nonNuclearUnitDoesNotMatchWillNotBuildFilter() {
        val warrior = testGame.ruleset.units["Warrior"]!!
        assertFalse(warrior.matchesFilter("Nuclear Weapon"))
    }
}
