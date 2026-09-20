package com.unciv.civ6.city

import com.unciv.civ6.domain.city.AmenityCalculator
import com.unciv.civ6.domain.city.HousingCalculator
import kotlin.test.Test
import kotlin.test.assertEquals

class CityStatsTest {
    @Test
    fun housingFreshWater() {
        assertEquals(5, HousingCalculator.calculateHousing(true, false, 0, 0, 0)) // 2+3
        assertEquals(3, HousingCalculator.calculateHousing(false, true, 0, 0, 0)) // 2+1
        assertEquals(2, HousingCalculator.calculateHousing(false, false, 0, 0, 0))
        assertEquals(7, HousingCalculator.calculateHousing(true, false, 2, 0, 0)) // + Granary
    }

    @Test
    fun housingGrowthModifier() {
        assertEquals(1f, HousingCalculator.growthModifier(3, 5))
        assertEquals(0.75f, HousingCalculator.growthModifier(4, 5))
        assertEquals(0.5f, HousingCalculator.growthModifier(5, 5))
        assertEquals(0f, HousingCalculator.growthModifier(6, 5))
    }

    @Test
    fun amenitiesNeeded() {
        assertEquals(0, AmenityCalculator.amenitiesNeeded(1))
        assertEquals(0, AmenityCalculator.amenitiesNeeded(2))
        assertEquals(1, AmenityCalculator.amenitiesNeeded(3))
        assertEquals(1, AmenityCalculator.amenitiesNeeded(4))
        assertEquals(2, AmenityCalculator.amenitiesNeeded(5))
        assertEquals(2, AmenityCalculator.amenitiesNeeded(6))
        assertEquals(3, AmenityCalculator.amenitiesNeeded(7))
    }

    @Test
    fun amenityLevel() {
        assertEquals(AmenityCalculator.AmenityLevel.Content, AmenityCalculator.level(1, 1))
        assertEquals(AmenityCalculator.AmenityLevel.Happy, AmenityCalculator.level(2, 1))
        assertEquals(AmenityCalculator.AmenityLevel.Ecstatic, AmenityCalculator.level(4, 1))
        assertEquals(AmenityCalculator.AmenityLevel.Unhappy, AmenityCalculator.level(0, 1))
        assertEquals(AmenityCalculator.AmenityLevel.Revolt, AmenityCalculator.level(0, 5))
    }
}
