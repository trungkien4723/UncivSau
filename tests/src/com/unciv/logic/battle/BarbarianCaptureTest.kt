package com.unciv.logic.battle

import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class BarbarianCaptureTest {
    private lateinit var game: TestGame

    @Before
    fun init() {
        game = TestGame()
        game.makeHexagonalMap(4)
        game.addBarbarianCiv()
    }

    /** Civ VI: barbarians converting a captured Settler into a Builder, which stays a
     *  Builder when recaptured - with its build charges intact. */
    @Test
    fun barbarianCapturedSettlerBecomesBuilderAndStaysBuilderOnRecapture() {
        val civ = game.addCiv(isPlayer = true)
        val barbs = game.gameInfo.getBarbarianCivilization()
        val settlerTile = game.getTile(com.unciv.logic.map.HexCoord(0, 0))
        val settler = game.addUnit("Settler", civ, settlerTile)

        // Barbarians capture the Settler
        val barbUnit = game.addUnit("Warrior", barbs, game.getTile(com.unciv.logic.map.HexCoord(1, 1)))
        BattleUnitCapture.captureCivilianUnit(
            MapUnitCombatant(barbUnit), MapUnitCombatant(settler), checkDefeat = false)

        Assert.assertTrue("Barbarians should hold the captured unit",
            settler.civ == barbs || settler.isDestroyed)
        val allUnits = game.gameInfo.civilizations.flatMap { it.units.getCivUnits() }
        val heldUnit = allUnits.firstOrNull { it.name == "Builder" }
        Assert.assertNotNull("Captured Settler should have become a Builder", heldUnit)
        Assert.assertEquals("Builder is held by the Barbarians", barbs, heldUnit!!.civ)

        // Original owner recaptures it
        val recapturer = game.addUnit("Warrior", civ, game.getTile(com.unciv.logic.map.HexCoord(0, 1)))
        BattleUnitCapture.captureCivilianUnit(
            MapUnitCombatant(recapturer), MapUnitCombatant(heldUnit), checkDefeat = false)

        Assert.assertEquals("Recaptured unit returns to the original owner", civ, heldUnit.civ)
        Assert.assertEquals("Recaptured unit stays a Builder", "Builder", heldUnit.name)
        Assert.assertTrue("Build charges intact",
            heldUnit.hasUnique(com.unciv.models.ruleset.unique.UniqueType.BuildImprovements,
                checkCivInfoUniques = true) || heldUnit.baseUnit.hasUnique(
                com.unciv.models.ruleset.unique.UniqueType.BuildImprovements))
    }
}
