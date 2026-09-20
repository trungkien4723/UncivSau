package com.unciv.civ6.victory

import com.unciv.civ6.domain.victory.VictoryManager
import com.unciv.civ6.domain.victory.VictoryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VictoryTest {
    @Test
    fun domination() {
        val mgr = VictoryManager()
        assertTrue(!mgr.updateDomination(1, 3))
        assertTrue(mgr.updateDomination(3, 3))
        assertEquals(VictoryType.Domination, mgr.winner())
    }

    @Test
    fun science() {
        val mgr = VictoryManager()
        repeat(4) { mgr.addScienceProject() }
        assertEquals(null, mgr.winner())
        mgr.addScienceProject()
        assertEquals(VictoryType.Science, mgr.winner())
    }

    @Test
    fun diplomacy() {
        val mgr = VictoryManager()
        assertTrue(mgr.updateDiplomacy(10))
        assertEquals(VictoryType.Diplomacy, mgr.winner())
    }

    @Test
    fun culture() {
        val mgr = VictoryManager()
        assertTrue(mgr.updateCulture(150, 100))
        assertEquals(VictoryType.Culture, mgr.winner())
    }
}
