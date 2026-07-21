package com.unciv.logic.civilization.managers

import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class GameModesTest {

    val testGame = TestGame()
    val civ = testGame.addCiv()
    val gameModesManager = GameModesManager()

    @Before
    fun setUp() {
        gameModesManager.civInfo = civ
    }

    @Test
    fun `should activate zombie mode`() {
        gameModesManager.setZombieMode(true)

        assertTrue(gameModesManager.isZombieModeActive())
    }

    @Test
    fun `should activate apocalypse mode`() {
        gameModesManager.setApocalypseMode(true)

        assertTrue(gameModesManager.isApocalypseModeActive())
    }

    @Test
    fun `should activate dramatic ages mode`() {
        gameModesManager.setDramaticAgesMode(true)

        assertTrue(gameModesManager.isDramaticAgesModeActive())
    }

    @Test
    fun `should join secret society`() {
        gameModesManager.joinSecretSociety("Oratory")

        assertTrue(gameModesManager.hasSecretSociety("Oratory"))
    }

    @Test
    fun `should spawn hero`() {
        gameModesManager.spawnHero("Sun Wukong")
        gameModesManager.spawnHero("Anansi")
        gameModesManager.spawnHero("Moses")

        assertEquals(3, gameModesManager.getHeroCount())
    }

    @Test
    fun `should not spawn more than 3 heroes`() {
        gameModesManager.spawnHero("Sun Wukong")
        gameModesManager.spawnHero("Anansi")
        gameModesManager.spawnHero("Moses")
        gameModesManager.spawnHero("Maui")

        assertEquals(3, gameModesManager.getHeroCount())
    }

    @Test
    fun `should start rock band`() {
        gameModesManager.startRockBand("Rock Band #1")

        assertTrue(gameModesManager.isRockBandActive("Rock Band #1"))
    }

    @Test
    fun `should stop rock band`() {
        gameModesManager.startRockBand("Rock Band #1")
        gameModesManager.stopRockBand("Rock Band #1")

        assertFalse(gameModesManager.isRockBandActive("Rock Band #1"))
    }

    @Test
    fun `should establish monopoly`() {
        gameModesManager.establishMonopoly("Spices")

        assertTrue(gameModesManager.hasMonopoly("Spices"))
    }

    @Test
    fun `should found corporation`() {
        gameModesManager.foundCorporation("Oil Company")

        assertTrue(gameModesManager.hasCorporation("Oil Company"))
    }

    @Test
    fun `should activate emergency`() {
        gameModesManager.activateEmergency("Natural Disaster")

        assertTrue(gameModesManager.hasEmergency("Natural Disaster"))
    }

    @Test
    fun `should deactivate emergency`() {
        gameModesManager.activateEmergency("Natural Disaster")
        gameModesManager.deactivateEmergency("Natural Disaster")

        assertFalse(gameModesManager.hasEmergency("Natural Disaster"))
    }

    @Test
    fun `should check if game mode is active`() {
        gameModesManager.setZombieMode(true)
        gameModesManager.setApocalypseMode(true)

        assertTrue(gameModesManager.isGameModeActive("Zombie"))
        assertTrue(gameModesManager.isGameModeActive("Apocalypse"))
        assertFalse(gameModesManager.isGameModeActive("SecretSocieties"))
    }

    @Test
    fun `should get all active game modes`() {
        gameModesManager.setZombieMode(true)
        gameModesManager.setApocalypseMode(true)
        gameModesManager.joinSecretSociety("Oratory")

        val activeModes = gameModesManager.getAllActiveGameModes()

        assertTrue(activeModes.contains("Zombie"))
        assertTrue(activeModes.contains("Apocalypse"))
        assertTrue(activeModes.contains("SecretSocieties"))
    }

    @Test
    fun `should activate all game modes`() {
        gameModesManager.activateAllGameModes()

        assertTrue(gameModesManager.isZombieModeActive())
        assertTrue(gameModesManager.isApocalypseModeActive())
        assertTrue(gameModesManager.isDramaticAgesModeActive())
    }

    @Test
    fun `should calculate culture from rock bands`() {
        gameModesManager.startRockBand("Rock Band #1")
        gameModesManager.startRockBand("Rock Band #2")

        assertEquals(60, gameModesManager.getCultureFromRockBand())
    }

    @Test
    fun `should calculate gold from rock band pillage`() {
        gameModesManager.startRockBand("Rock Band #1")
        gameModesManager.startRockBand("Rock Band #2")

        assertEquals(120, gameModesManager.getGoldFromRockBandPillage())
    }
}