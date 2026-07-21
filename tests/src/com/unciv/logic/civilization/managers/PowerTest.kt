package com.unciv.logic.civilization.managers

import com.unciv.logic.map.HexCoord
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class PowerTest {

    val testGame = TestGame()
    val civ = testGame.addCiv()
    val powerManager = PowerManager()

    @Before
    fun setUp() {
        powerManager.civInfo = civ
    }

    @Test
    fun `should calculate power deficit when consumption exceeds production`() {
        testGame.makeHexagonalMap(1)
        val city = testGame.addCity(civ, testGame.getTile(HexCoord.Zero))
        city.cityConstructions.addBuilding("Wind Farm")
        city.cityConstructions.addBuilding("Coal Power Plant")

        powerManager.calculatePower()

        assertTrue(powerManager.isPowerDeficit())
        assertTrue(powerManager.getPowerDeficit() > 0)
    }

    @Test
    fun `should calculate no power deficit when production meets consumption`() {
        testGame.makeHexagonalMap(1)
        val city = testGame.addCity(civ, testGame.getTile(HexCoord.Zero))
        city.cityConstructions.addBuilding("Wind Farm")
        city.cityConstructions.addBuilding("Coal Power Plant")
    }

    @Test
    fun `should track CO2 accumulation`() {
        powerManager.addCO2(10)
        powerManager.addCO2(5)

        assertEquals(15, powerManager.getCO2Level())
    }

    @Test
    fun `should reduce CO2`() {
        powerManager.totalCO2 = 20
        powerManager.reduceCO2(5)

        assertEquals(15, powerManager.totalCO2)
    }

    @Test
    fun `should not reduce CO2 below zero`() {
        powerManager.totalCO2 = 5
        powerManager.reduceCO2(10)

        assertEquals(0, powerManager.totalCO2)
    }
}