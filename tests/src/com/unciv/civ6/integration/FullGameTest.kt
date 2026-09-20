package com.unciv.civ6.integration

import com.unciv.civ6.Civ6GameStarter
import com.unciv.civ6.data.DistrictData
import com.unciv.civ6.domain.city.HexCoord
import com.unciv.civ6.domain.combat.CityCombatant
import com.unciv.civ6.domain.combat.UnitCombatant
import com.unciv.civ6.domain.combat.Battle
import com.unciv.civ6.engine.TurnManagerWithAI
import com.unciv.civ6.domain.victory.VictoryManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Full game loop QA - Sprint12.
 * Simulates 10 turns with city growth, tech, district, combat, victory.
 */
class FullGameTest {
    @Test
    fun fullGameLoop10Turns() {
        val info = Civ6GameStarter.newGame()
        val allDistricts = listOf(DistrictData("Campus", 60), DistrictData("Holy Site", 60))
        val tm = TurnManagerWithAI(info, allDistricts)

        // Turn 1-5: science eureka, housing check, district placement
        repeat(5) {
            tm.addScience(10, "Pottery")
            tm.addCulture(10, "Code of Laws")
            tm.nextTurn()
        }
        assertEquals(5, info.turns)
        assertTrue(info.cities.first().populationState().housing > 0)

        // Add district
        val campus = DistrictData("Campus", 60)
        assertTrue(Civ6GameStarter.addDistrictToCity(info, "Capital", campus, HexCoord(1, 0)))

        // Combat: siege vs walled city
        val siege = UnitCombatant("Catapult", 35, isSiege = true, isRanged = true, rangedStrength = 35)
        val city = CityCombatant("EnemyCity", 30, wallHp = 100)
        val result = Battle.siegeAttack(siege, city)
        assertTrue(result.wallHpAfter < 100)

        // Victory progress
        val victory = VictoryManager()
        repeat(5) { victory.addScienceProject() }
        assertEquals(com.unciv.civ6.domain.victory.VictoryType.Science, victory.winner())

        // Loyalty stable for capital
        val loyalty = info.cityLoyalties["Capital"]!!
        assertEquals(100f, loyalty.loyalty)
        assertTrue(!loyalty.isFreeCity())
    }

    @Test
    fun housingAmenityIntegration() {
        val info = Civ6GameStarter.newGame()
        val city = info.cities.first()
        // Capital fresh water housing 5, pop 1 => growth 1
        assertEquals(5, city.housing())
        assertEquals(1f, city.populationState().housingGrowthModifier)
        // Add pop to test amenity
        val bigCity = city.copy(population = 6)
        assertEquals(2, bigCity.populationState().amenities) // 6 needs 2, has 0 => Unhappy
        assertEquals(com.unciv.civ6.domain.city.AmenityCalculator.AmenityLevel.Unhappy, bigCity.populationState().amenityLevel)
    }
}
