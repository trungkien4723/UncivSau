package com.unciv.logic.civilization.managers

import com.unciv.logic.GameInfo
import com.unciv.logic.civilization.Civilization
import com.unciv.models.metadata.BaseRuleset
import com.unciv.models.ruleset.RulesetCache
import com.unciv.testing.GdxTestRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GdxTestRunner::class)
class GovernmentManagerTest {

    private fun makeManager(): GovernmentManager {
        RulesetCache.loadRulesets(noMods = true)
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!
        val gameInfo = GameInfo()
        gameInfo.ruleset = ruleset
        val civ = Civilization("Test")
        civ.gameInfo = gameInfo
        return civ.government.also { it.setTransients(civ) }
    }

    @Test
    fun adoptGovernmentClearsPendingPickerFlag() {
        val manager = makeManager()
        manager.shouldOpenGovernmentPicker = true
        manager.adoptGovernment("Autocracy")
        assertFalse(manager.shouldOpenGovernmentPicker)
    }

    @Test
    fun assignCardKeepsSlotAlignment() {
        val manager = makeManager()
        manager.adoptGovernment("Autocracy") // slots: Military, Military, Economic, Wildcard
        manager.assignCard(0, "Discipline")
        manager.assignCard(1, "Military Tradition")
        manager.assignCard(2, "God King")
        assertEquals(listOf("Discipline", "Military Tradition", "God King", ""), manager.assignedCards)

        // Clearing a middle slot must not shift or misalign the other cards
        manager.assignCard(1, null)
        assertEquals(listOf("Discipline", "", "God King", ""), manager.assignedCards)

        // Reassigning a card that already occupies another slot moves it instead of duplicating
        manager.assignCard(1, "Discipline")
        assertEquals(listOf("", "Discipline", "God King", ""), manager.assignedCards)
    }

    @Test
    fun fullyFilledGovernmentIsValid() {
        val manager = makeManager()
        manager.adoptGovernment("Chiefdom") // slots: Military, Economic
        manager.assignCard(0, "Survey")     // Military
        manager.assignCard(1, "God King")   // Economic
        assertEquals(listOf("Survey", "God King"), manager.assignedCards)
        assertTrue(manager.isValid())
    }

    @Test
    fun cardAssignedToASlotIsNotOfferedForOtherSlots() {
        val manager = makeManager()
        manager.adoptGovernment("Autocracy") // slots: Military, Military, Economic, Wildcard
        // Research every civic so all policy cards become available
        for (civic in manager.civInfo.gameInfo.ruleset.civics.values)
            manager.civInfo.civics.civicsResearched.add(civic.name)

        manager.assignCard(0, "Discipline") // Military
        val forSlot1 = manager.getAvailableCardsForSlot(1)
        assertFalse("A card already assigned to another slot must not be offered again",
            forSlot1.any { it.name == "Discipline" })
        assertTrue("The slot that already holds the card must still offer it",
            manager.getAvailableCardsForSlot(0).any { it.name == "Discipline" })

        // A card in an Economic slot must not be offered for a Military slot either
        manager.assignCard(2, "God King")
        assertFalse("A card assigned to an Economic slot must not be offered for a Military slot",
            manager.getAvailableCardsForSlot(1).any { it.name == "God King" })
    }

    @Test
    fun cardPickedInProgressIsNotOfferedForOtherSlots() {
        val manager = makeManager()
        manager.adoptGovernment("Autocracy") // slots: Military, Military, Economic, Wildcard
        for (civic in manager.civInfo.gameInfo.ruleset.civics.values)
            manager.civInfo.civics.civicsResearched.add(civic.name)

        // Simulate the picker's in-progress selection: "Discipline" picked for slot 0, not yet committed
        val inProgress = ArrayList(listOf("Discipline", "", "", ""))
        assertFalse("A card picked for one slot must not be offered for another slot while the selection is in progress",
            manager.getAvailableCardsForSlot(1, manager.getGovernment()!!, inProgress).any { it.name == "Discipline" })
        assertFalse("A card picked for a Military slot must not be offered for an Economic slot either",
            manager.getAvailableCardsForSlot(2, manager.getGovernment()!!, inProgress).any { it.name == "Discipline" })
        assertTrue("The slot holding the card must still offer it",
            manager.getAvailableCardsForSlot(0, manager.getGovernment()!!, inProgress).any { it.name == "Discipline" })
    }

    @Test
    fun previewingAnotherGovernmentFiltersCardsByItsOwnSlots() {
        val manager = makeManager()
        manager.adoptGovernment("Autocracy") // slots: Military, Military, Economic, Wildcard
        for (civic in manager.civInfo.gameInfo.ruleset.civics.values)
            manager.civInfo.civics.civicsResearched.add(civic.name)

        // Player is previewing Theocracy (slots: Military, Economic, Diplomatic, Diplomatic, Wildcard)
        val theocracy = manager.civInfo.gameInfo.ruleset.governments["Theocracy"]!!
        val inProgress = ArrayList(listOf("", "", "", "", ""))
        val diploSlot = manager.getAvailableCardsForSlot(2, theocracy, inProgress)
        assertTrue("The previewed government's Diplomatic slot must offer diplomatic cards - "
                + "filtering with the adopted government's (Economic) slot instead would hide them",
            diploSlot.any { it.slotType == "Diplomatic" })
    }
}
