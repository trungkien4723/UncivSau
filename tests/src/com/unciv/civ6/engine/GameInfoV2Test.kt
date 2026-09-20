package com.unciv.civ6.engine

import com.unciv.civ6.domain.city.City
import com.unciv.civ6.domain.tech.Civic
import com.unciv.civ6.domain.tech.CivicManager
import com.unciv.civ6.domain.tech.Tech
import com.unciv.civ6.domain.tech.TechManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameInfoV2Test {
    @Test
    fun versionCompatibility() {
        val info = GameInfoV2()
        assertTrue(info.isCompatible())
        info.version = Civ6CompatibilityVersion(11, Civ6Version("future", 3000))
        assertFalse(info.isCompatible())
    }

    @Test
    fun cityAndLoyalty() {
        val info = GameInfoV2()
        val city = City("Capital", 4, hasFreshWater = true)
        info.addCity(city)
        assertEquals(1, info.cities.size)
        assertEquals(100f, info.cityLoyalties["Capital"]?.loyalty)
        val city2 = City("Border", 2)
        info.addCity(city2)
        assertEquals(2, info.cities.size)
        assertFalse(info.cityLoyalties["Border"]?.isCapital ?: true)
    }

    @Test
    fun turnManagerIntegration() {
        val techs = mapOf("Pottery" to Tech("Pottery", 25, 1, "Ancient", emptyList()))
        val civics = mapOf("Code of Laws" to Civic("Code of Laws", 20, 1, "Ancient", emptyList()))
        val info = GameInfoV2(techManager = TechManager(techs), civicManager = CivicManager(civics))
        info.addCity(City("Cap", 1, hasFreshWater = true))
        val tm = TurnManager(info)
        tm.addScience(10, "Pottery")
        tm.triggerEureka("Pottery") // +12
        assertEquals(22, info.techManager?.progress("Pottery"))
        tm.nextTurn()
        assertEquals(1, info.turns)
        assertEquals(100f, info.cityLoyalties["Cap"]?.loyalty)
    }
}
