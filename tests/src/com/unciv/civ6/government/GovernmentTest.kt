package com.unciv.civ6.government

import com.unciv.civ6.domain.government.Government
import com.unciv.civ6.domain.government.GovernmentManager
import com.unciv.civ6.domain.government.PolicyCard
import com.unciv.civ6.domain.government.PolicySlotType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GovernmentTest {
    private val governments = mapOf(
        "Chiefdom" to Government("Chiefdom", "Ancient", mapOf(PolicySlotType.Military to 1, PolicySlotType.Economic to 1)),
        "Classical Republic" to Government("Classical Republic", "Classical", mapOf(PolicySlotType.Military to 1, PolicySlotType.Economic to 2, PolicySlotType.Diplomatic to 1, PolicySlotType.Wildcard to 1), requiredCivic = "Political Philosophy")
    )
    private val cards = mapOf(
        "Discipline" to PolicyCard("Discipline", PolicySlotType.Military),
        "Urban Planning" to PolicyCard("Urban Planning", PolicySlotType.Economic),
        "Caravansaries" to PolicyCard("Caravansaries", PolicySlotType.Economic),
        "Raj" to PolicyCard("Raj", PolicySlotType.Diplomatic)
    )

    @Test
    fun availableGovernments() {
        val mgr = GovernmentManager(governments, cards)
        assertEquals(1, mgr.availableGovernments(emptySet()).size) // only Chiefdom
        assertEquals(2, mgr.availableGovernments(setOf("Political Philosophy")).size)
    }

    @Test
    fun switchGovernmentClearsSlots() {
        val mgr = GovernmentManager(governments, cards)
        mgr.switchGovernment("Chiefdom", emptySet())
        mgr.slotPolicy("Discipline")
        assertEquals(1, mgr.slottedPolicies().size)
        mgr.switchGovernment("Classical Republic", setOf("Political Philosophy"))
        assertEquals(0, mgr.slottedPolicies().size) // cleared on switch
        assertEquals("Classical Republic", mgr.currentGovernment)
    }

    @Test
    fun slottingRespectsCapacityAndWildcard() {
        val mgr = GovernmentManager(governments, cards)
        mgr.switchGovernment("Chiefdom", emptySet())
        assertTrue(mgr.slotPolicy("Discipline")) // military 1/1
        assertTrue(mgr.slotPolicy("Urban Planning")) // economic 1/1
        assertFalse(mgr.slotPolicy("Caravansaries")) // economic full, no wildcard
        // Republic has wildcard
        mgr.switchGovernment("Classical Republic", setOf("Political Philosophy"))
        assertTrue(mgr.slotPolicy("Discipline"))
        assertTrue(mgr.slotPolicy("Urban Planning"))
        assertTrue(mgr.slotPolicy("Caravansaries")) // second economic slot
        assertTrue(mgr.slotPolicy("Raj")) // diplomatic slot via wildcard? actually diplomatic 1, so direct
        assertEquals(4, mgr.slottedPolicies().size)
    }

    @Test
    fun activeBonuses() {
        val cardsWithBonus = mapOf(
            "Discipline" to PolicyCard("Discipline", PolicySlotType.Military, bonus = mapOf("VsBarbarian" to 5f)),
            "Urban Planning" to PolicyCard("Urban Planning", PolicySlotType.Economic, bonus = mapOf("ProductionAllCities" to 1f))
        )
        val mgr = GovernmentManager(governments, cardsWithBonus)
        mgr.switchGovernment("Chiefdom", emptySet())
        mgr.slotPolicy("Discipline")
        mgr.slotPolicy("Urban Planning")
        val bonuses = mgr.activeBonuses()
        assertEquals(5f, bonuses["VsBarbarian"])
        assertEquals(1f, bonuses["ProductionAllCities"])
    }
}
