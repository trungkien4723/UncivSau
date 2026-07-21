package com.unciv.uniques

import com.unciv.models.metadata.BaseRuleset
import com.unciv.models.ruleset.RulesetCache
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestCase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class CivVIRulesetLoadTest {

    @Test
    fun `Civ VI ruleset loads with districts and agendas`() {
        RulesetCache.loadRulesets(noMods = true)
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!

        // 6 districts defined
        Assert.assertTrue("Campus district present", ruleset.districts.containsKey("Campus"))
        Assert.assertTrue("Theater Square district present", ruleset.districts.containsKey("Theater Square"))
        Assert.assertTrue("Holy Site district present", ruleset.districts.containsKey("Holy Site"))
        Assert.assertTrue("Commercial Hub district present", ruleset.districts.containsKey("Commercial Hub"))
        Assert.assertTrue("Industrial Zone district present", ruleset.districts.containsKey("Industrial Zone"))
        Assert.assertTrue("Neighborhood district present", ruleset.districts.containsKey("Neighborhood"))

        // Placement buildings create districts
        val campus = ruleset.buildings["Campus"]
        val matches = campus?.getMatchingUniques(UniqueType.CreatesOneDistrict)?.toList()
        println("DEBUG matches=$matches objs=${campus?.uniqueObjects?.map { it.type }}")
        Assert.assertTrue("Campus building creates a district", matches?.any() ?: false)

        // District buildings are gated to their district
        val library = ruleset.buildings["Library"]!!
        Assert.assertEquals("Library requires Campus district", "Campus", library.district)

        // Agendas load
        Assert.assertTrue("Warmonger Hater agenda present", ruleset.agendas.containsKey("Warmonger Hater"))
    }

    @Test
    fun `Civ VI Era Score decides Age on era transition`() {
        RulesetCache.loadRulesets(noMods = true)
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!

        val testGame = com.unciv.testing.TestGame()
        testGame.makeHexagonalMap(2)
        val civ = testGame.addCiv()

        // High Era Score in a later era -> Golden Age
        civ.goldenAges.eraScore = 20
        val golden = civ.goldenAges.onEraTransition(4) // goldenThreshold = 8
        Assert.assertEquals("High Era Score yields Golden Age", "Golden", golden)

        // Reset and low Era Score -> Dark Age
        civ.goldenAges.eraScore = 1
        val dark = civ.goldenAges.onEraTransition(4) // darkThreshold = 4
        Assert.assertEquals("Low Era Score yields Dark Age", "Dark", dark)

        // Middle -> Normal Age
        civ.goldenAges.eraScore = 5
        val normal = civ.goldenAges.onEraTransition(4)
        Assert.assertEquals("Mid Era Score yields Normal Age", "Normal", normal)
    }
}
