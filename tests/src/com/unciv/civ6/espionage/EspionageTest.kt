package com.unciv.civ6.espionage

import com.unciv.civ6.domain.espionage.Spy
import com.unciv.civ6.domain.espionage.SpyMission
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EspionageTest {
    @Test
    fun spyMission() {
        val spy = Spy("S1")
        assertTrue(spy.startMission("CityA", SpyMission.StealTech, visibility = 0))
        assertEquals(8, spy.turnsRemaining)
        repeat(8) { spy.tick() }
        assertEquals(0, spy.turnsRemaining)
        assertEquals(com.unciv.civ6.domain.espionage.SpyRank.Agent, spy.rank)
    }

    @Test
    fun successChance() {
        val spy = Spy("S1", rank = com.unciv.civ6.domain.espionage.SpyRank.SpecialAgent)
        assertTrue(spy.successChance(0) > 80)
        assertTrue(spy.successChance(10) < 70)
    }
}
