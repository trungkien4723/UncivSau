package com.unciv.civ6.religion

import com.unciv.civ6.domain.government.Governor
import com.unciv.civ6.domain.government.GovernorManager
import com.unciv.civ6.domain.religion.Belief
import com.unciv.civ6.domain.religion.BeliefType
import com.unciv.civ6.domain.religion.ReligionManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReligionTest {
    @Test
    fun pantheon() {
        val pantheons = listOf(Belief("Divine Spark", BeliefType.Pantheon), Belief("God of War", BeliefType.Pantheon))
        val mgr = ReligionManager(pantheons)
        mgr.addFaith(25)
        assertTrue(mgr.canFoundPantheon())
        assertTrue(mgr.foundPantheon("Divine Spark"))
        assertEquals("Divine Spark", mgr.pantheon?.name)
        assertFalse(mgr.canFoundPantheon())
    }

    @Test
    fun religionFound() {
        val pantheons = listOf(Belief("Divine Spark", BeliefType.Pantheon))
        val beliefs = mutableListOf(
            Belief("Choral Music", BeliefType.Follower),
            Belief("Feed the World", BeliefType.Follower),
            Belief("Tithe", BeliefType.Founder),
            Belief("Reliquaries", BeliefType.Enhancer)
        )
        val mgr = ReligionManager(pantheons, beliefs)
        mgr.addFaith(25)
        mgr.foundPantheon("Divine Spark")
        mgr.addProphetPoints(60)
        assertTrue(mgr.canFoundReligion())
        assertTrue(mgr.foundReligion("MyReligion", "Choral Music", "Tithe"))
        assertEquals(2, mgr.religion?.beliefs?.size)
        assertFalse(beliefs.any { it.name == "Choral Music" }) // removed from pool
    }

    @Test
    fun governorCCEFix() {
        val gov = Governor("Magnus")
        gov.promotions.add(arrayListOf("Groundbreaker"))
        gov.promotions.add(arrayListOf("Surplus Logistics"))
        assertEquals(2, gov.level())
        assertEquals("Groundbreaker", gov.promotionObjects[0][0])
        val mgr = GovernorManager()
        mgr.addGovernor(gov)
        assertTrue(mgr.assign("Magnus", "CityA"))
        assertEquals(5, gov.turnsToEstablish)
        gov.tickTurn()
        assertEquals(4, gov.turnsToEstablish)
        assertFalse(gov.isEstablished())
    }
}
