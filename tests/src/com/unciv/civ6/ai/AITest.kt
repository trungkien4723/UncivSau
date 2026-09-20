package com.unciv.civ6.ai

import com.unciv.civ6.data.DistrictData
import com.unciv.civ6.domain.city.City
import com.unciv.civ6.domain.city.HexCoord
import com.unciv.civ6.map.TileMapV2
import com.unciv.civ6.domain.government.Government
import com.unciv.civ6.domain.government.GovernmentManager
import com.unciv.civ6.domain.government.PolicyCard
import com.unciv.civ6.domain.government.PolicySlotType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AITest {
    @Test
    fun cityAutomationChooses() {
        val campus = DistrictData("Campus", 60)
        val holy = DistrictData("Holy Site", 60)
        val city = City("A", 4)
        val chosen = CityAutomation.chooseDistrict(city, listOf(holy, campus), CityAutomation.VictoryFocus.Science)
        assertEquals("Campus", chosen?.name)
    }

    @Test
    fun bestPlacement() {
        val map = TileMapV2(10, 10)
        map.get(HexCoord(5, 5))!!.isCityCenter = true
        val campus = DistrictData("Campus", 60)
        val best = CityAutomation.bestPlacement(HexCoord(5, 5), campus, map)
        assertNotNull(best)
    }

    @Test
    fun governmentAutomationSwitch() {
        val govs = mapOf(
            "Chiefdom" to Government("Chiefdom", "Ancient", mapOf(PolicySlotType.Military to 1, PolicySlotType.Economic to 1)),
            "Republic" to Government("Republic", "Classical", mapOf(PolicySlotType.Military to 1, PolicySlotType.Economic to 2, PolicySlotType.Wildcard to 1), requiredCivic = "Civic1")
        )
        val mgr = GovernmentManager(govs, emptyMap(), currentGovernment = "Chiefdom")
        val newGov = GovernmentAutomation.shouldSwitchGovernment(mgr, setOf("Civic1"))
        assertEquals("Republic", newGov)
    }
}
