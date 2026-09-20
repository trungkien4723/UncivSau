package com.unciv.civ6.balancing

import kotlin.test.Test
import kotlin.test.assertTrue

class BalancingTest {
    @Test
    fun validate() {
        val errors = Balancing.validate()
        assertTrue(errors.isEmpty(), errors.joinToString())
    }

    @Test
    fun civ6Constants() {
        assertTrue(Balancing.EUREKA_BOOST_PERCENT == 50)
        assertTrue(Balancing.SIEGE_VS_LAND_PENALTY == -17)
        assertTrue(Balancing.DIPLOMATIC_POINTS_NEEDED == 10)
        assertTrue(Balancing.FAITH_PANTHEON == 25)
    }
}
