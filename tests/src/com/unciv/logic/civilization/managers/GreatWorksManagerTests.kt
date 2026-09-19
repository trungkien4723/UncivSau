package com.unciv.logic.civilization.managers

import com.unciv.models.Counter
import com.unciv.models.ruleset.Building
import com.unciv.models.ruleset.GreatWorkType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class GreatWorksManagerTest {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: com.unciv.logic.city.City

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(3)
        civ = testGame.addCiv()
        for (tech in testGame.ruleset.technologies.values)
            civ.tech.addTechnology(tech.name)
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
    }

    private fun addGreatWorkSlotBuilding(name: String, type: GreatWorkType, slots: Int): Building {
        val building = testGame.createBuilding()
        building.name = name
        building.greatWorkSlots = Counter(mapOf(type.name to slots))
        testGame.ruleset.buildings[name] = building
        city.cityConstructions.addBuilding(building)
        return building
    }

    @Test
    fun newWorkIsPlacedIntoBuildingWithSlot() {
        addGreatWorkSlotBuilding("Test Museum", GreatWorkType.Art, 3)
        val works = civ.greatWorks.addGreatWork(GreatWorkType.Art, "Mona Lisa", "Leonardo", "Renaissance")

        assertEquals(city.id, works.cityId)
        assertEquals("Test Museum", works.building)
        assertEquals(2, civ.greatWorks.getAvailableSlots(GreatWorkType.Art))
        assertEquals(1, civ.greatWorks.getWorksInBuilding(city.id, "Test Museum", GreatWorkType.Art).size)
    }

    @Test
    fun availableSlotsCountsPerBuildingCapacity() {
        addGreatWorkSlotBuilding("Gallery A", GreatWorkType.Art, 2)
        addGreatWorkSlotBuilding("Gallery B", GreatWorkType.Art, 1)

        assertEquals(3, civ.greatWorks.getAvailableSlots(GreatWorkType.Art))

        civ.greatWorks.addGreatWork(GreatWorkType.Art, "Work 1")
        civ.greatWorks.addGreatWork(GreatWorkType.Art, "Work 2")

        // Both fit naturally under one app, but placement may split across the two buildings.
        val totalPlaced = civ.greatWorks.getWorksByType(GreatWorkType.Art).size
        assertEquals(2, totalPlaced)
        assertEquals(1, civ.greatWorks.getAvailableSlots(GreatWorkType.Art))

        assertTrue(civ.greatWorks.hasAvailableSlot(GreatWorkType.Art))
    }

    @Test
    fun buildingsNotFullyFilledGiveNoTheming() {
        addGreatWorkSlotBuilding("Half Museum", GreatWorkType.Art, 2)
        civ.greatWorks.addGreatWork(GreatWorkType.Art, "Only Work")

        val theming = civ.greatWorks.getThemingStats(GreatWorkType.Art)
        assertEquals(0f, theming.culture, 0.001f)
        assertEquals(0f, theming.tourism, 0.001f)
    }

    @Test
    fun fullyFilledBuildingGivesThemingBonus() {
        addGreatWorkSlotBuilding("Full Museum", GreatWorkType.Art, 2)
        civ.greatWorks.addGreatWork(GreatWorkType.Art, "Work A")
        civ.greatWorks.addGreatWork(GreatWorkType.Art, "Work B")

        val theming = civ.greatWorks.getThemingStats(GreatWorkType.Art)
        assertEquals(2f, theming.culture, 0.001f)
        assertEquals(2f, theming.tourism, 0.001f)
    }

    @Test
    fun themingIsPerBuildingNotPerType() {
        addGreatWorkSlotBuilding("Museum A", GreatWorkType.Art, 2)
        addGreatWorkSlotBuilding("Museum B", GreatWorkType.Art, 2)

        civ.greatWorks.addGreatWork(GreatWorkType.Art, "A1")
        civ.greatWorks.addGreatWork(GreatWorkType.Art, "A2")
        civ.greatWorks.addGreatWork(GreatWorkType.Art, "B1")

        // Museum A is full (2/2), Museum B is not (1/2) -> only A theming counts
        val theming = civ.greatWorks.getThemingStats(GreatWorkType.Art)
        assertEquals(2f, theming.culture, 0.001f)
        assertEquals(2f, theming.tourism, 0.001f)
    }
}