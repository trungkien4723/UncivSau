package com.unciv.logic.city.managers

import com.unciv.Constants
import com.unciv.logic.map.HexCoord
import com.unciv.models.metadata.BaseRuleset
import com.unciv.models.ruleset.RulesetCache
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestCase
import com.unciv.testing.TestGame
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class CivVIDistrictIntegrationTest {

    private val testGame = TestGame()
    private lateinit var civ: com.unciv.logic.civilization.Civilization
    private lateinit var city: com.unciv.logic.city.City

    @Before
    fun setUp() {
        RulesetCache.loadRulesets(noMods = true)
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!
        testGame.makeHexagonalMap(3)
        // swap the test game ruleset to Civ VI
        testGame.ruleset // ensure initialized
        civ = testGame.addCiv()
        // load all techs so districts are buildable
        for (tech in ruleset.technologies.values)
            civ.tech.addTechnology(tech.name)
        city = testGame.addCity(civ, testGame.tileMap[0, 0])
        // own a nearby tile to place the district
        val tile = city.getTiles().first { !it.isCityCenter() }
        testGame.addTileToCity(city, tile)
    }

    @Test
    fun `Civ VI district can be placed and yields`() {
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!
        val campus = ruleset.buildings["Campus"]!!
        assertTrue("Campus placement building exists", campus.getMatchingUniques(
            com.unciv.models.ruleset.unique.UniqueType.CreatesOneDistrict).any())

        // Queue the Campus district placement building
        city.cityConstructions.addToQueue("Campus")
        city.cityConstructions.setCurrentConstruction("Campus")

        // Simulate production so the construction completes and the district is placed
        // (district cost is scaled up because all techs are researched)
        val constructionStats = com.unciv.models.stats.Stats()
        constructionStats.production += 2000
        repeat(3) {
            city.cityConstructions.endTurn(constructionStats)
            city.cityConstructions.constructIfEnough()
        }

        // The district should now exist in the city
        assertTrue("City should have a Campus district placed",
            city.districts.values.contains("Campus"))
        val districtTile = city.getTiles().firstOrNull { it.district == "Campus" }
        assertNotNull("A tile should host the Campus district", districtTile)
        assertFalse("Pillaged check: district should be active",
            districtTile!!.districtIsPillaged)
    }
}
