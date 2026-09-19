package com.unciv.logic.city

import com.unciv.logic.GameInfo
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Governor
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestCase
import com.unciv.testing.TestGame
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class LoyaltyTests {
    private val testGame = TestGame()
    private lateinit var civ: Civilization
    private lateinit var city: City

    @Before
    fun init() {
        testGame.makeHexagonalMap(2)
        // Enable the Civ VI loyalty mechanic by providing at least one governor.
        testGame.ruleset.governors["Victor"] = Governor().apply {
            name = "Victor"
            loyaltyBonus = 5
        }
        civ = testGame.addCiv()
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
    }

    @Test
    fun `loyalty is active and grows for a stable city`() {
        city.loyalty.loyalty = 50
        city.loyalty.startTurn()
        Assert.assertTrue(
            "Loyalty should have increased for a stable city",
            city.loyalty.loyalty > 50
        )
    }

    @Test
    fun `assigned governor increases loyalty more than without`() {
        // Baseline without a governor
        city.loyalty.loyalty = 50
        city.loyalty.startTurn()
        val withoutGovernor = city.loyalty.loyalty

        // Reset and assign a governor that grants +5 loyalty per turn
        city.loyalty.loyalty = 50
        civ.governorManager.assignGovernor(city, "Victor")
        city.loyalty.startTurn()
        val withGovernor = city.loyalty.loyalty

        Assert.assertTrue(
            "A governor should provide more loyalty pressure than no governor",
            withGovernor > withoutGovernor
        )
    }
}
