package com.unciv.logic.civilization.managers

import com.unciv.logic.map.HexCoord
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class GoldenAgeManagerTest {

    val testGame = TestGame()
    val civ = testGame.addCiv()
    val goldenAgeManager = GoldenAgeManager()

    @Before
    fun setUp() {
        goldenAgeManager.civInfo = civ
    }

    @Test
    fun `starts in normal age`() {
        assertTrue(goldenAgeManager.isNormalAge())
        assertFalse(goldenAgeManager.isGoldenAge())
        assertFalse(goldenAgeManager.isDarkAge())
    }

    @Test
    fun `should accumulate era score`() {
        goldenAgeManager.addEraScore(10, "Test")

        assertEquals(10, goldenAgeManager.eraScore)
        assertEquals(10, goldenAgeManager.totalEraScore)
    }

    @Test
    fun `should enter golden age`() {
        goldenAgeManager.enterGoldenAge(10)

        assertTrue(goldenAgeManager.isGoldenAge())
        assertEquals(10, goldenAgeManager.turnsLeftForCurrentGoldenAge)
    }

    @Test
    fun `should decrease golden age duration on next turn`() {
        goldenAgeManager.enterGoldenAge(10)

        goldenAgeManager.endTurn()

        assertEquals(9, goldenAgeManager.turnsLeftForCurrentGoldenAge)
    }

    @Test
    fun `should enter dark age`() {
        goldenAgeManager.enterDarkAge()

        assertTrue(goldenAgeManager.isDarkAge())
    }

    @Test
    fun `should enter heroic age when golden follows dark`() {
        goldenAgeManager.enterDarkAge()
        goldenAgeManager.previousAge = "Dark"
        goldenAgeManager.eraScore = 100

        val age = goldenAgeManager.onEraTransition(2)

        assertEquals("Heroic", age)
        assertTrue(goldenAgeManager.isHeroicAge())
    }

    @Test
    fun `should go into golden age with enough stored era points`() {
        testGame.makeHexagonalMap(1)
        testGame.addCity(civ, testGame.getTile(HexCoord.Zero), initialPopulation = 3)
        // Leave plenty of headroom: negative amenities can consume 1 point per turn in endTurn()
        goldenAgeManager.storedEraPoints = goldenAgeManager.eraPointsRequiredForNextGoldenAge() + 100

        goldenAgeManager.endTurn()

        assertTrue(goldenAgeManager.isGoldenAge())
    }

    @Test
    fun `should increase golden age cost with more cities`() {
        testGame.makeHexagonalMap(1)
        testGame.addCity(civ, testGame.getTile(HexCoord.Zero), initialPopulation = 10)
        val eraPointsForOneCity = goldenAgeManager.eraPointsRequiredForNextGoldenAge()

        testGame.addCity(civ, testGame.getTile(1, 0), initialPopulation = 10)

        assertTrue(goldenAgeManager.eraPointsRequiredForNextGoldenAge() > eraPointsForOneCity)
    }

    @Test
    fun `loyalty modifier depends on age`() {
        assertEquals(0, goldenAgeManager.getLoyaltyModifier())

        goldenAgeManager.enterGoldenAge()
        assertEquals(3, goldenAgeManager.getLoyaltyModifier())

        goldenAgeManager.enterDarkAge()
        assertEquals(-4, goldenAgeManager.getLoyaltyModifier())
    }

    @Test
    fun `policy slot modifier depends on age`() {
        assertEquals(0, goldenAgeManager.getPolicySlotModifier())

        goldenAgeManager.enterGoldenAge()
        assertEquals(1, goldenAgeManager.getPolicySlotModifier())

        goldenAgeManager.enterDarkAge()
        assertEquals(-1, goldenAgeManager.getPolicySlotModifier())
    }

    @Test
    fun `should increase golden age length due to uniques`() {
        val civ = testGame.addCiv("[+50]% Golden Age length")
        goldenAgeManager.civInfo = civ

        goldenAgeManager.enterGoldenAge(10)

        assertEquals(15, goldenAgeManager.turnsLeftForCurrentGoldenAge)
    }

    @Test
    fun `age is decided by era score thresholds at era transition`() {
        testGame.makeHexagonalMap(2)
        val manager = testGame.addCiv().goldenAges

        // First real transition is into the Classical era (eraNumber = 1)
        val golden = manager.getGoldenThreshold(1)
        val dark = manager.getDarkThreshold(1)
        assertEquals(14, golden)
        assertEquals(4, dark)

        // A single ancient wonder ([4] Era Score) must NOT trigger a Golden Age early on
        manager.eraScore = 4
        assertEquals("Normal", manager.onEraTransition(1))

        // Scoring below the Dark threshold (e.g. no wonders at all) leads to a Dark Age
        manager.eraScore = 1
        assertEquals("Dark", manager.onEraTransition(1))

        // A solid accumulation of Era Score leads to a Golden Age
        manager.eraScore = golden
        assertEquals("Golden", manager.onEraTransition(1))
    }

    @Test
    fun `golden threshold scales with era`() {
        testGame.makeHexagonalMap(2)
        val manager = testGame.addCiv().goldenAges

        assertEquals(14, manager.getGoldenThreshold(1))
        assertEquals(18, manager.getGoldenThreshold(2))
        assertEquals(22, manager.getGoldenThreshold(3))
        assertEquals(4, manager.getDarkThreshold(1))
        assertEquals(6, manager.getDarkThreshold(2))
    }
}