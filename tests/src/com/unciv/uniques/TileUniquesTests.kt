package com.unciv.uniques

import com.unciv.Constants
import com.unciv.logic.map.HexCoord
import com.unciv.logic.map.tile.TileAppeal
import com.unciv.models.UnitActionType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import com.unciv.ui.screens.worldscreen.unit.actions.UnitActions
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class TileUniquesTests {
    private lateinit var game: TestGame

    @Before
    fun initTheWorld() {
        game = TestGame()
    }

    @Test
    fun pillageYieldTest() {
        game.makeHexagonalMap(2)
        val civInfo = game.addCiv()

        val tile = game.setTileTerrain(HexCoord.Zero, Constants.grassland)
        val cityTile = game.setTileTerrain(HexCoord(2,0), Constants.grassland)
        val city = game.addCity(civInfo, cityTile, true)
        city.population.foodStored = 0 // just to be sure
        civInfo.addGold(-civInfo.gold) // reset gold just to be sure

        val testImprovement = game.createTileImprovement("Pillaging this improvement yields [+20 Gold, +11 Food]")
        tile.setImprovement(testImprovement)
        val unit = game.addUnit("Warrior", civInfo, tile)
        unit.currentMovement = 2f

        UnitActions.invokeUnitAction(unit, UnitActionType.Pillage)
        Assert.assertTrue("Pillaging should transfer gold to the civ", civInfo.gold == 20)
        Assert.assertTrue("Pillaging should transfer food to the nearest city", city.population.foodStored == 11)
    }

    @Test
    fun `improvement with tourism based on tile appeal generates tourism equal to appeal`() {
        game.makeHexagonalMap(3)
        val civInfo = game.addCiv()
        val city = game.addCity(civInfo, game.tileMap[0, 0], true)

        val resortTile = city.getTiles().first { !it.isCityCenter() }
        game.addTileToCity(city, resortTile)
        resortTile.setOwningCity(city)
        // Forest (+1 each) gives a deterministic positive appeal for the test
        game.setTileFeatures(resortTile.position, "Forest", "Jungle")

        val improvement = game.createTileImprovement("Tourism based on tile appeal")
        improvement.name = "Seaside Resort Test"
        game.ruleset.tileImprovements[improvement.name] = improvement
        resortTile.setImprovement(improvement)

        val appeal = TileAppeal.getAppeal(resortTile, civInfo)
        civInfo.updateStatsForNextTurn()

        Assert.assertTrue("Appeal should be positive for the test to be meaningful", appeal > 0)
        Assert.assertEquals("Tourism from appeal-based improvement should equal the tile appeal",
            appeal.toFloat(), civInfo.stats.statsForNextTurn.tourism)
    }
}
