package com.unciv.civ6.tech

import com.unciv.civ6.domain.tech.Civic
import com.unciv.civ6.domain.tech.CivicManager
import com.unciv.civ6.domain.tech.Tech
import com.unciv.civ6.domain.tech.TechManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TechCivicTest {
    private val techs = mapOf(
        "Pottery" to Tech("Pottery", 25, 1, "Ancient", emptyList()),
        "Writing" to Tech("Writing", 35, 2, "Ancient", listOf("Pottery")),
        "Mining" to Tech("Mining", 25, 1, "Ancient", emptyList())
    )

    @Test
    fun canResearchRequiresPrereq() {
        val mgr = TechManager(techs)
        assertTrue(mgr.canResearch("Pottery", emptySet()))
        assertFalse(mgr.canResearch("Writing", emptySet()))
        assertTrue(mgr.canResearch("Writing", setOf("Pottery")))
    }

    @Test
    fun eurekaBoostHalfCost() {
        val mgr = TechManager(techs)
        mgr.triggerEureka("Writing") // 35/2=17
        assertEquals(17, mgr.progress("Writing"))
        mgr.addProgress("Writing", 18)
        assertTrue(mgr.isCompleted("Writing"))
        // second eureka no double
        mgr.triggerEureka("Writing")
        assertEquals(35, mgr.progress("Writing"))
    }

    @Test
    fun civicInspiration() {
        val civics = mapOf(
            "Code of Laws" to Civic("Code of Laws", 20, 1, "Ancient", emptyList()),
            "Craftsmanship" to Civic("Craftsmanship", 25, 1, "Ancient", listOf("Code of Laws"))
        )
        val mgr = CivicManager(civics)
        assertTrue(mgr.canResearch("Code of Laws", emptySet()))
        assertFalse(mgr.canResearch("Craftsmanship", emptySet()))
        mgr.triggerInspiration("Code of Laws")
        assertEquals(10, mgr.progress("Code of Laws"))
        mgr.addProgress("Code of Laws", 10)
        assertTrue(mgr.isCompleted("Code of Laws"))
        assertTrue(mgr.canResearch("Craftsmanship", setOf("Code of Laws")))
    }
}
