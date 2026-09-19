package com.unciv.civ6.loyalty

import com.unciv.civ6.domain.loyalty.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoyaltyTest {
    @Test
    fun pressureCalculation() {
        val p = LoyaltyPressure(10, 2, isGoldenAge = false).pressure()
        assertEquals(8f, p) // 10*(10-2)/10=8
        val pGolden = LoyaltyPressure(10, 2, isGoldenAge = true).pressure()
        assertEquals(12f, pGolden) // 8*1.5
        val pGov = LoyaltyPressure(10, 2, hasGovernor = true).pressure()
        assertEquals(16f, pGov) // 8+8
    }

    @Test
    fun cityLoyaltyUpdate() {
        val city = CityLoyalty("Border", 50f, 6)
        city.update(ownPressure = 20f, foreignPressure = 10f) // net +10 -> +1 loyalty
        assertEquals(51f, city.loyalty)
        city.update(ownPressure = 5f, foreignPressure = 20f) // net -15 -> -1.5
        assertEquals(49.5f, city.loyalty)
    }

    @Test
    fun freeCity() {
        val city = CityLoyalty("Lost", 1f, 1)
        city.update(0f, 20f) // large foreign pressure
        // 1 + (0-20)*0.1 = -1 -> clamped 0
        assertTrue(city.isFreeCity())
        assertTrue(city.isRevoltRisk())
        val capital = CityLoyalty("Capital", 100f, 10, isCapital = true)
        capital.update(0f, 100f)
        assertEquals(100f, capital.loyalty)
    }

    @Test
    fun eraScore() {
        assertEquals(AgeType.GoldenAge, EraScoreManager.ageType(EraType.Ancient, 15, false)) // threshold 10+5
        assertEquals(AgeType.DarkAge, EraScoreManager.ageType(EraType.Ancient, 4, false))
        assertEquals(AgeType.HeroicAge, EraScoreManager.ageType(EraType.Ancient, 22, true)) // 10+12
        assertEquals(4, EraScoreManager.pointsForWonder(true))
        assertEquals(3, EraScoreManager.pointsForNaturalWonder())
    }
}
