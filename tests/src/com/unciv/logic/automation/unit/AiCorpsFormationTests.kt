@file:Suppress("UNUSED_VARIABLE")  // These are tests and the names serve readability

package com.unciv.logic.automation.unit

import com.unciv.Constants
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import com.unciv.utils.DebugUtils
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
internal class AiCorpsFormationTests {
    private lateinit var civInfo: Civilization

    val testGame = TestGame()

    fun setUp(size: Int, baseTerrain: String = Constants.desert) {
        testGame.makeHexagonalMap(size, baseTerrain)
        civInfo = testGame.addCiv()
    }

    @After
    fun wrapUp() {
        DebugUtils.VISIBLE_MAP = false
    }

    private fun addAdjacentWarriors(): Pair<MapUnit, MapUnit> {
        val tile = testGame.getTile(0, 0)
        val partnerTile = tile.neighbors.first()
        val warrior = testGame.addUnit("Warrior", civInfo, tile)
        val partner = testGame.addUnit("Warrior", civInfo, partnerTile)
        return warrior to partner
    }

    @Test
    fun `AI forms corps from adjacent same-name units during peacetime`() {
        setUp(3)
        val (warrior, partner) = addAdjacentWarriors()
        civInfo.civics.civicsResearched.add("Nationalism")
        civInfo.addGold(1000)

        UnitAutomation.automateUnitMoves(warrior)

        assertEquals(1, warrior.formationLevel)
        assertTrue(partner.isDestroyed)
        assertTrue(civInfo.gold < 1000)
    }

    @Test
    fun `AI does not form corps while at war`() {
        setUp(3)
        val enemyCiv = testGame.addCiv()
        testGame.addUnit("Warrior", enemyCiv, testGame.getTile(3, 3)) // so enemyCiv is not defeated
        civInfo.diplomacyFunctions.makeCivilizationsMeet(enemyCiv)
        civInfo.getDiplomacyManager(enemyCiv)!!.declareWar()
        val (warrior, partner) = addAdjacentWarriors()
        civInfo.civics.civicsResearched.add("Nationalism")
        civInfo.addGold(1000)

        UnitAutomation.automateUnitMoves(warrior)

        assertEquals(0, warrior.formationLevel)
        assertFalse(partner.isDestroyed)
    }

    @Test
    fun `AI does not form corps without nationalism`() {
        setUp(3)
        val (warrior, partner) = addAdjacentWarriors()
        civInfo.addGold(1000)

        UnitAutomation.automateUnitMoves(warrior)

        assertEquals(0, warrior.formationLevel)
        assertFalse(partner.isDestroyed)
    }

    @Test
    fun `AI does not form corps with a damaged unit`() {
        setUp(3)
        val (warrior, partner) = addAdjacentWarriors()
        warrior.health = 50
        civInfo.civics.civicsResearched.add("Nationalism")
        civInfo.addGold(1000)

        UnitAutomation.automateUnitMoves(warrior)

        assertEquals(0, warrior.formationLevel)
        assertFalse(partner.isDestroyed)
    }

    @Test
    fun `AI skips already destroyed units`() {
        setUp(3)
        val (warrior, partner) = addAdjacentWarriors()
        partner.destroy()

        UnitAutomation.automateUnitMoves(partner) // must not throw

        assertTrue(partner.isDestroyed)
        assertEquals(0, warrior.formationLevel)
    }

    @Test
    fun `AI forms army from adjacent corps units`() {
        setUp(3)
        val tileA = testGame.getTile(0, 0)
        val tileB = tileA.neighbors.first()
        civInfo.civics.civicsResearched.addAll(listOf("Nationalism", "Mobilization"))
        civInfo.addGold(1000)

        val warriorA = testGame.addUnit("Warrior", civInfo, tileA)
        val warriorB = testGame.addUnit("Warrior", civInfo, tileB)
        UnitAutomation.automateUnitMoves(warriorA)
        assertEquals(1, warriorA.formationLevel)

        val warriorC = testGame.addUnit("Warrior", civInfo, tileB)
        val tileD = tileB.neighbors.first { it != tileA && it.militaryUnit == null }
        val warriorD = testGame.addUnit("Warrior", civInfo, tileD)
        UnitAutomation.automateUnitMoves(warriorC)
        assertEquals(1, warriorC.formationLevel)

        UnitAutomation.automateUnitMoves(warriorA)

        assertEquals(2, warriorA.formationLevel)
        assertTrue(warriorC.isDestroyed)
    }
}
