package com.unciv.civ6.city

import com.unciv.civ6.data.AdjacencyBonus
import com.unciv.civ6.domain.city.AdjacencyCalculator
import com.unciv.civ6.domain.city.AdjacencyContext
import com.unciv.civ6.domain.city.AdjacencyType
import kotlin.test.Test
import kotlin.test.assertEquals

class AdjacencyTest {
    @Test
    fun campusMountain() {
        val bonuses = listOf(AdjacencyBonus(AdjacencyType.Mountain, "Science", 1f))
        val ctx = AdjacencyContext(mapOf(AdjacencyType.Mountain to 2), 0, 0)
        assertEquals(2f, AdjacencyCalculator.calculate("Campus", bonuses, ctx))
    }

    @Test
    fun campusDistrictHalf() {
        val bonuses = listOf(AdjacencyBonus(AdjacencyType.District, "Science", 0.5f, perCount = 2))
        val ctx = AdjacencyContext(emptyMap(), 2, 0)
        assertEquals(0.5f, AdjacencyCalculator.calculate("Campus", bonuses, ctx))
        val ctx2 = AdjacencyContext(emptyMap(), 4, 0)
        assertEquals(1f, AdjacencyCalculator.calculate("Campus", bonuses, ctx2))
    }

    @Test
    fun commercialHubRiver() {
        val bonuses = listOf(AdjacencyBonus(AdjacencyType.River, "Gold", 2f))
        val ctx = AdjacencyContext(mapOf(AdjacencyType.River to 1), 0, 0)
        assertEquals(2f, AdjacencyCalculator.calculate("Commercial Hub", bonuses, ctx))
        // Scripture doubles Holy Site
        assertEquals(4f, AdjacencyCalculator.withPolicyMultiplier(2f, 2f))
    }

    @Test
    fun industrialMine() {
        val bonuses = listOf(AdjacencyBonus(AdjacencyType.MineOrQuarry, "Production", 1f))
        val ctx = AdjacencyContext(mapOf(AdjacencyType.MineOrQuarry to 3), 0, 0)
        assertEquals(3f, AdjacencyCalculator.calculate("Industrial Zone", bonuses, ctx))
    }
}
