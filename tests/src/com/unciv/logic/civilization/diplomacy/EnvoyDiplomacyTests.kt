package com.unciv.logic.civilization.diplomacy

import com.unciv.logic.civilization.Civilization
import com.unciv.testing.GdxTestRunner
import com.unciv.testing.TestGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class EnvoyDiplomacyTests {

    private val testGame = TestGame()

    fun addCiv(cityStateType: String? = null) = testGame.addCiv(cityStateType = cityStateType).apply {
        testGame.addUnit("Warrior", this, null)
    }

    private val a = addCiv()

    private fun meet(civilization: Civilization, otherCivilization: Civilization) {
        civilization.diplomacyFunctions.makeCivilizationsMeet(otherCivilization)
    }

    @Before
    fun setUp() {
        testGame.makeHexagonalMap(4)
    }

    private fun cityStateBonusTexts(major: Civilization): List<String> =
        major.cache.cityStateBonusUniqueMaps.flatMap { it.getAllUniques().map { u -> u.text } }

    @Test
    fun `no bonuses without envoys`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)
        cityState.getDiplomacyManager(a)!!.setEnvoys(0) // remove the free first-contact envoy

        a.cache.updateCivResources()

        assertTrue(cityStateBonusTexts(a).isEmpty())
        assertEquals(
            RelationshipLevel.Neutral,
            cityState.getDiplomacyManager(a)!!.relationshipIgnoreAfraid()
        )
    }

    @Test
    fun `friend bonus applies at one envoy`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)
        val csDiplomacy = cityState.getDiplomacyManager(a)!!

        csDiplomacy.addEnvoys(DiplomacyManager.friendThreshold)
        a.cache.updateCivResources()

        assertEquals(
            RelationshipLevel.Friend,
            csDiplomacy.relationshipIgnoreAfraid()
        )
        assertTrue(
            "Friend bonus (20 turns) should apply at ${DiplomacyManager.friendThreshold} envoy",
            cityStateBonusTexts(a).any { it.contains("[20]") }
        )
    }

    @Test
    fun `ally suzerain granted at three envoys`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)
        val csDiplomacy = cityState.getDiplomacyManager(a)!!

        csDiplomacy.addEnvoys(DiplomacyManager.allyThreshold)
        a.cache.updateCivResources()

        assertEquals(a, cityState.allyCiv)
        assertEquals(
            RelationshipLevel.Ally,
            csDiplomacy.relationshipIgnoreAfraid()
        )
        assertTrue(
            "Ally (suzerain) bonus (17 turns) should apply at ${DiplomacyManager.allyThreshold} envoys",
            cityStateBonusTexts(a).any { it.contains("[17]") }
        )
    }

    @Test
    fun `war removes friendship and bonus`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)
        val csDiplomacy = cityState.getDiplomacyManager(a)!!
        csDiplomacy.addEnvoys(1)
        a.cache.updateCivResources()
        assertTrue(cityStateBonusTexts(a).isNotEmpty())

        a.getDiplomacyManager(cityState)!!.declareWar()
        a.cache.updateCivResources()

        assertEquals(RelationshipLevel.Unforgivable, csDiplomacy.relationshipLevel())
        assertTrue("No friend bonus while at war", cityStateBonusTexts(a).isEmpty())
        assertEquals(null, cityState.allyCiv)
    }

    @Test
    fun `gold gift grants single digit envoys`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)

        for (giftAmount in listOf(250, 500, 1000, 2000)) {
            val envoysGained = cityState.cityStateFunctions.influenceGainedByGift(a, giftAmount)
            assertTrue("Gift of [$giftAmount] should grant 1-3 Envoys, got [$envoysGained]", envoysGained in 1..3)
        }
    }

    @Test
    fun `barbarian kill grants one envoy`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)
        val csDiplomacy = cityState.getDiplomacyManager(a)!!
        csDiplomacy.setEnvoys(0) // remove the free first-contact envoy

        cityState.cityStateFunctions.threateningBarbarianKilledBy(a)

        assertEquals(1, csDiplomacy.getEnvoys())
    }

    @Test
    fun `every city state nation has a suzerain bonus`() {
        val cityStatesWithoutBonus = testGame.ruleset.nations.values
            .filter { it.cityStateType != null && it.uniqueMap.getAllUniques().none() }
            .map { it.name }
        assertTrue(
            "City-states missing a suzerain (nation uniques) bonus: $cityStatesWithoutBonus",
            cityStatesWithoutBonus.isEmpty()
        )
    }

    @Test
    fun `no unassigned envoys accrue per turn`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)

        assertEquals(0, a.unassignedEnvoys)
        a.cityStateFunctions.gainEnvoysPerTurn()
        assertEquals(0, a.unassignedEnvoys)
    }

    @Test
    fun `AI sends unassigned envoys to city-states`() {
        val cityState = addCiv(cityStateType = "Militaristic")
        meet(a, cityState)
        val csDiplomacy = cityState.getDiplomacyManager(a)!!
        csDiplomacy.setEnvoys(0) // remove the free first-contact envoy

        a.unassignedEnvoys = 3
        a.cityStateFunctions.aiSendEnvoys()
        assertEquals(2, a.unassignedEnvoys)
        assertEquals(1, csDiplomacy.getEnvoys())
    }

    @Test
    fun `policy and society uniques grant envoys into the pool`() {
        val b = testGame.addCiv("Gain [1] Envoys per turn")
        assertEquals(0, b.unassignedEnvoys)
        b.cityStateFunctions.gainEnvoysPerTurn()
        assertEquals(1, b.unassignedEnvoys)
    }

    @Test
    fun `seoul suzerain bonus applies at three envoys`() {
        val seoulNation = testGame.ruleset.nations["Seoul"]!!
        val seoul = testGame.addCiv(seoulNation).apply {
            testGame.addUnit("Warrior", this, null)
        }
        meet(a, seoul)
        val csDiplomacy = seoul.getDiplomacyManager(a)!!

        csDiplomacy.addEnvoys(DiplomacyManager.allyThreshold)
        a.cache.updateCivResources()

        assertEquals(a, seoul.allyCiv)
        val bonusTexts = cityStateBonusTexts(a)
        assertTrue(
            "Seoul's suzerain bonus ([+2 Science] in all cities) should apply at ${DiplomacyManager.allyThreshold} envoys. Got: $bonusTexts",
            bonusTexts.any { it.contains("+2 Science") }
        )
    }
}
