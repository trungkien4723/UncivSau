package com.unciv.logic

import com.badlogic.gdx.Gdx
import com.unciv.UncivGame
import com.unciv.logic.files.UncivFiles
import com.unciv.logic.map.MapParameters
import com.unciv.logic.map.MapSize
import com.unciv.models.metadata.GameParameters
import com.unciv.models.metadata.GameSettings
import com.unciv.models.metadata.GameSetupInfo
import com.unciv.models.metadata.Player
import com.unciv.models.ruleset.RulesetCache
import com.unciv.testing.GdxTestRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class RemovedContentSaveTest {
    @Before
    fun setup() {
        RulesetCache.loadRulesets(noMods = true)
        val game = UncivGame()
        UncivGame.Current = game
        game.settings = GameSettings()
        game.files = UncivFiles(Gdx.files)
    }

    /** Old saves can reference content that a later update removed from the ruleset
     *  (e.g. the Calendar tech or the Stone Works building). Loading must not crash -
     *  the removed entries are silently purged instead. */
    @Test
    fun saveContainingRemovedTechAndBuildingStillLoads() {
        val param = GameParameters().apply {
            numberOfCityStates = 0
            players.clear()
            players.add(Player("Rome", com.unciv.logic.civilization.PlayerType.Human))
        }
        val mapParameters = MapParameters().apply { mapSize = MapSize.Tiny; seed = 7L }
        val game = GameStarter.startNewGame(GameSetupInfo(param, mapParameters))
        val civ = game.getCurrentPlayerCivilization()
        val unit = civ.units.getCivUnits().first()
        civ.addCity(unit.getTile().position)
        unit.destroy()

        // Simulate an old save that references removed content
        civ.tech.techsResearched.add("Calendar")          // removed tech
        Assert.assertFalse(game.ruleset.technologies.containsKey("Calendar"))
        val city = civ.cities.first()
        city.cityConstructions.builtBuildings.add("Stone Works")      // removed building
        city.cityConstructions.constructionQueue.add("Stone Works")   // queued too
        Assert.assertFalse(game.ruleset.buildings.containsKey("Stone Works"))

        // Round-trip like UncivFiles.loadGameFromFile -> gameInfoFromString -> setTransients
        val serialized = UncivFiles.gameInfoToString(game)
        val reloaded = UncivFiles.gameInfoFromString(serialized)

        val reloadedCiv = reloaded.getCurrentPlayerCivilization()
        Assert.assertTrue(reloadedCiv.tech.techsResearched.none { it == "Calendar" })
        Assert.assertEquals(1, reloadedCiv.cities.size)
        val reloadedCity = reloadedCiv.cities.first()
        Assert.assertTrue(reloadedCity.cityConstructions.builtBuildings.none { it == "Stone Works" })
        Assert.assertTrue(reloadedCity.cityConstructions.constructionQueue.none { it == "Stone Works" })
    }
}
