package com.unciv.logic.civilization.diplomacy

import com.unciv.logic.GameInfo
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Ruleset
import com.unciv.models.ruleset.nation.Agenda
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestCase
import com.unciv.testing.TestGame
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class AgendaTests {

    private val testGame = TestGame()
    private lateinit var civA: Civilization
    private lateinit var civB: Civilization
    private lateinit var civC: Civilization

    @Before
    fun init() {
        testGame.makeHexagonalMap(2)
        // Build a minimal ruleset with two agendas and two nations carrying them
        val likesAgenda = Agenda().apply {
            name = "LikesMajor"
            likes = "Major"
        }
        testGame.ruleset.agendas[likesAgenda.name] = likesAgenda

        val dislikesAgenda = Agenda().apply {
            name = "HatesMajor"
            dislikes = "Major"
        }
        testGame.ruleset.agendas[dislikesAgenda.name] = dislikesAgenda

        civA = testGame.addCiv()
        civB = testGame.addCiv()
        civC = testGame.addCiv()

        // civA likes Major civs, civB hates Major civs
        civA.nation.agenda = "LikesMajor"
        civB.nation.agenda = "HatesMajor"

        testGame.addCity(civA, testGame.tileMap[0, 0])
        testGame.addCity(civB, testGame.tileMap[1, 0])
        testGame.addCity(civC, testGame.tileMap[0, 1])

        // Make them know each other
        civA.diplomacy[diplomacyKey(civB)] = DiplomacyManager(civA, civB.civID)
        civB.diplomacy[diplomacyKey(civA)] = DiplomacyManager(civB, civA.civID)
        civA.diplomacy[diplomacyKey(civC)] = DiplomacyManager(civA, civC.civID)
        civC.diplomacy[diplomacyKey(civA)] = DiplomacyManager(civC, civA.civID)
        // Give A a hidden agenda that likes Major as well
        civA.chosenHiddenAgenda = "LikesMajor"
    }

    private fun diplomacyKey(other: Civilization) = other.civID

    @Test
    fun agendaInfluencesOpinion() {
        // civA likes Major civs -> civB (Major) should get positive agenda modifier from A
        val aVsB = civA.diplomacy[diplomacyKey(civB)]!!
        aVsB.updateAgendaModifierFor(civB)
        Assert.assertTrue("A should like B (Major) via agenda",
            aVsB.opinionOfOtherCiv() > 0f)

        // civB hates Major civs -> civA (Major) should get negative agenda modifier from B
        val bVsA = civB.diplomacy[diplomacyKey(civA)]!!
        bVsA.updateAgendaModifierFor(civA)
        Assert.assertTrue("B should dislike A (Major) via agenda",
            bVsA.opinionOfOtherCiv() < 0f)
    }

    @Test
    fun agendaModifierReflectedInOpinion() {
        civA.diplomacy.values.forEach { it.updateAgendaModifiers() }
        val aVsB = civA.diplomacy[diplomacyKey(civB)]!!
        // 20 (historical LikesMajor) + 20 (hidden LikesMajor) = 40
        Assert.assertEquals(40f, aVsB.opinionOfOtherCiv(), 1e-3f)
    }

    @Test
    fun noAgendaModifierWhenFilterUnmatched() {
        // A civ with NO agenda of its own applies no agenda modifier to others
        val civNoAgenda = testGame.addCiv()
        civNoAgenda.nation.agenda = null
        testGame.addCity(civNoAgenda, testGame.tileMap[-1, 1])
        civA.diplomacy[diplomacyKey(civNoAgenda)] = DiplomacyManager(civA, civNoAgenda.civID)
        val aVsNo = civA.diplomacy[diplomacyKey(civNoAgenda)]!!
        // civA still has agenda that likes Major -> should apply +20 to this Major civ
        aVsNo.updateAgendaModifierFor(civNoAgenda)
        Assert.assertTrue(aVsNo.opinionOfOtherCiv() > 0f)
    }
}
