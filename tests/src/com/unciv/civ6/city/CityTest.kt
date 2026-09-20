package com.unciv.civ6.city

import com.unciv.civ6.data.BuildingData
import com.unciv.civ6.data.DistrictData
import com.unciv.civ6.domain.city.City
import com.unciv.civ6.domain.city.HexCoord
import com.unciv.civ6.domain.city.PlacedDistrict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CityTest {
    @Test
    fun maxDistrictSlots() {
        assertEquals(1, City("A", 1).maxDistrictSlots())
        assertEquals(1, City("A", 3).maxDistrictSlots())
        assertEquals(2, City("A", 4).maxDistrictSlots())
        assertEquals(2, City("A", 6).maxDistrictSlots())
        assertEquals(3, City("A", 7).maxDistrictSlots())
        assertEquals(4, City("A", 10).maxDistrictSlots())
        assertEquals(5, City("A", 13).maxDistrictSlots())
    }

    @Test
    fun canBuildDistrict() {
        val campus = DistrictData("Campus", 60, populationRequirement = 1)
        val cityPop1 = City("A", 1)
        assertTrue(cityPop1.canBuildDistrict(campus))
        val cityWithCampus = cityPop1.copy(districts = listOf(PlacedDistrict(campus, HexCoord(0,0))))
        assertFalse(cityWithCampus.canBuildDistrict(campus)) // 1 per type
        val cityPop10 = City("Huge", 10, districts = listOf(PlacedDistrict(campus, HexCoord(0,0))))
        val holy = DistrictData("Holy Site", 60)
        assertTrue(cityPop10.canBuildDistrict(holy))
    }

    @Test
    fun buildingRequiresDistrict() {
        val campus = DistrictData("Campus", 60)
        val library = BuildingData("Library", 90, requiredDistrict = "Campus")
        val city = City("A", 4, districts = listOf(PlacedDistrict(campus, HexCoord(0,0))))
        assertTrue(city.districts.first().canAddBuilding(library))
        val market = BuildingData("Market", 120, requiredDistrict = "Commercial Hub")
        assertFalse(city.districts.first().canAddBuilding(market))
    }

    @Test
    fun housingCalculation() {
        val granary = BuildingData("Granary", 65, requiredDistrict = "City Center", housing = 2)
        val city = City("A", 4, hasFreshWater = true, buildings = listOf(com.unciv.civ6.domain.city.PlacedBuilding(granary, "City Center")))
        assertEquals(7, city.housing()) // 2+3+2
    }
}
