@file:Suppress("UNUSED_VARIABLE")  // These are tests and the names serve readability

package com.unciv.logic.map

import com.unciv.Constants
import com.unciv.logic.civilization.Civilization
import com.unciv.models.UnitActionType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import com.unciv.ui.screens.worldscreen.unit.actions.UnitActionsCombine
import com.unciv.utils.DebugUtils
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
internal class UnitActionsCombineTests {
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

    private fun giveCivicsAndGold(vararg civics: String) {
        civInfo.civics.civicsResearched.addAll(civics)
        civInfo.addGold(1000)
    }

    @Test
    fun `no form corps action without nationalism`() {
        setUp(3)
        val tile = testGame.getTile(0, 0)
        val partnerTile = tile.neighbors.first()
        val warrior = testGame.addUnit("Warrior", civInfo, tile)
        testGame.addUnit("Warrior", civInfo, partnerTile)
        civInfo.addGold(1000)

        val actions = UnitActionsCombine.getFormCorpsActions(warrior, tile).toList()
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `no form corps action without adjacent same-name partner`() {
        setUp(3)
        val tile = testGame.getTile(0, 0)
        val warrior = testGame.addUnit("Warrior", civInfo, tile)
        giveCivicsAndGold("Nationalism")

        assertTrue(UnitActionsCombine.getFormCorpsActions(warrior, tile).toList().isEmpty())
    }

    @Test
    fun `no form corps action when partner is a different unit`() {
        setUp(3)
        val tile = testGame.getTile(0, 0)
        val partnerTile = tile.neighbors.first()
        val warrior = testGame.addUnit("Warrior", civInfo, tile)
        testGame.addUnit("Archer", civInfo, partnerTile)
        giveCivicsAndGold("Nationalism")

        assertTrue(UnitActionsCombine.getFormCorpsActions(warrior, tile).toList().isEmpty())
    }

    @Test
    fun `form corps merges adjacent same-name military units`() {
        setUp(3)
        val tile = testGame.getTile(0, 0)
        val partnerTile = tile.neighbors.first()
        val warrior = testGame.addUnit("Warrior", civInfo, tile)
        val partner = testGame.addUnit("Warrior", civInfo, partnerTile)
        giveCivicsAndGold("Nationalism")

        val actions = UnitActionsCombine.getFormCorpsActions(warrior, tile).toList()
        assertEquals(1, actions.size)
        assertEquals(UnitActionType.FormCorps, actions[0].type)
        assertNotNull(actions[0].action)

        val goldBefore = civInfo.gold
        actions[0].action!!.invoke()

        assertEquals(1, warrior.formationLevel)
        assertTrue(partner.isDestroyed)
        assertEquals(goldBefore - 50, civInfo.gold)
    }

    @Test
    fun `form army requires two adjacent corps`() {
        setUp(3)
        val tileA = testGame.getTile(0, 0)
        val tileB = tileA.neighbors.first()
        giveCivicsAndGold("Nationalism", "Mobilization")

        val warriorA = testGame.addUnit("Warrior", civInfo, tileA)
        val warriorB = testGame.addUnit("Warrior", civInfo, tileB)
        UnitActionsCombine.getFormCorpsActions(warriorA, tileA).first().action!!.invoke()

        val warriorC = testGame.addUnit("Warrior", civInfo, tileB)
        val tileD = tileB.neighbors.first { it != tileA && it.militaryUnit == null }
        val warriorD = testGame.addUnit("Warrior", civInfo, tileD)
        UnitActionsCombine.getFormCorpsActions(warriorC, tileB).first().action!!.invoke()

        assertEquals(1, warriorA.formationLevel)
        assertEquals(1, warriorC.formationLevel)

        val actions = UnitActionsCombine.getFormArmyActions(warriorA, tileA).toList()
        assertEquals(1, actions.size)
        assertEquals(UnitActionType.FormArmy, actions[0].type)
        assertNotNull(actions[0].action)
        actions[0].action!!.invoke()

        assertEquals(2, warriorA.formationLevel)
        assertTrue(warriorC.isDestroyed)
    }

    @Test
    fun `form fleet merges water units`() {
        setUp(3)
        val tile = testGame.getTile(2, 2)
        val partnerTile = tile.neighbors.first()
        tile.baseTerrain = "Coast"
        partnerTile.baseTerrain = "Coast"
        tile.setTransients()
        partnerTile.setTransients()
        civInfo.tech.addTechnology("Shipbuilding")
        giveCivicsAndGold("Nationalism")

        val galley = testGame.addUnit("Galley", civInfo, tile)
        val partner = testGame.addUnit("Galley", civInfo, partnerTile)

        val actions = UnitActionsCombine.getFormFleetActions(galley, tile).toList()
        assertEquals(1, actions.size)
        assertEquals(UnitActionType.FormFleet, actions[0].type)
        assertNotNull(actions[0].action)
        actions[0].action!!.invoke()

        assertEquals(1, galley.formationLevel)
        assertTrue(partner.isDestroyed)
    }

    @Test
    fun `displayName appends formation suffix`() {
        setUp(3)
        val tile = testGame.getTile(0, 0)
        val warrior = testGame.addUnit("Warrior", civInfo, tile)
        val galley = testGame.addUnit("Galley", civInfo, null)

        assertEquals("[Warrior]", warrior.displayName())
        warrior.formationLevel = 1
        assertEquals("[Warrior] Corps", warrior.displayName())
        warrior.formationLevel = 2
        assertEquals("[Warrior] Army", warrior.displayName())

        galley.formationLevel = 1
        assertEquals("[Galley] Fleet", galley.displayName())
        galley.formationLevel = 2
        assertEquals("[Galley] Armada", galley.displayName())
    }
}
