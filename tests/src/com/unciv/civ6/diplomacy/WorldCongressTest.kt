package com.unciv.civ6.diplomacy

import com.unciv.civ6.domain.diplomacy.WorldCongress
import com.unciv.civ6.domain.climate.ClimateManager
import com.unciv.civ6.domain.climate.PowerPlant
import com.unciv.civ6.domain.climate.PowerType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorldCongressTest {
    @Test
    fun diplomaticVictory() {
        val wc = WorldCongress(favorPerTurn = 300, diplomaticPoints = 9)
        wc.voteForPoints(30)
        assertTrue(wc.hasWon()) // 9+1=10
    }

    @Test
    fun climateCO2() {
        val cm = ClimateManager()
        cm.addPlant(PowerPlant("Cap", PowerType.Coal))
        cm.tickTurn()
        assertEquals(3.28f, cm.co2)
        cm.addPlant(PowerPlant("City2", PowerType.Wind))
        cm.tickTurn()
        assertTrue(cm.co2 > 6f)
        cm.carbonRecapture()
        assertEquals(0f, cm.co2)
    }
}
