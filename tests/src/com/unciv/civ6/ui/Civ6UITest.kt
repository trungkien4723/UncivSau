package com.unciv.civ6.ui

import com.unciv.civ6.Civ6GameStarter
import com.unciv.civ6.data.DistrictData
import com.unciv.civ6.domain.city.HexCoord
import kotlin.test.Test
import kotlin.test.assertTrue

class Civ6UITest {
    @Test
    fun worldScreenNextTurn() {
        val info = Civ6GameStarter.newGame()
        val screen = Civ6WorldScreen(info)
        assertTrue(screen.cityInfo().first().contains("Capital"))
        screen.nextTurn()
        assertTrue(info.turns == 1)
    }

    @Test
    fun cityScreenHousing() {
        val info = Civ6GameStarter.newGame()
        val city = info.cities.first()
        val cityScreen = Civ6CityScreen(city)
        assertTrue(cityScreen.housingStatus().contains("Housing"))
        assertTrue(cityScreen.districtSlots().contains("Districts"))
    }

    @Test
    fun addDistrictViaStarter() {
        val info = Civ6GameStarter.newGame()
        val campus = DistrictData("Campus", 60)
        assertTrue(Civ6GameStarter.addDistrictToCity(info, "Capital", campus, HexCoord(1, 0)))
        assertTrue(info.cities.first().districts.size == 1)
    }
}
