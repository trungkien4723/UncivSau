package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.Constants
import com.unciv.GUI
import com.unciv.UncivGame
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.managers.GovernmentManager
import com.unciv.models.UncivSound
import com.unciv.models.ruleset.government.Government
import com.unciv.models.ruleset.government.PolicyCard
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.disable
import com.unciv.ui.components.extensions.enable
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.extensions.toTextButton
import com.unciv.ui.components.input.onClick
import com.unciv.ui.popups.Popup
import com.unciv.ui.screens.basescreen.BaseScreen
import com.unciv.ui.screens.civilopediascreen.FormattedLine
import com.unciv.ui.screens.civilopediascreen.MarkupRenderer

class GovernmentPickerScreen(
    internal val civInfo: Civilization,
    private val isPickGovernment: Boolean = true,
) : PickerScreen() {

    private val ruleset = civInfo.gameInfo.ruleset
    private val manager = civInfo.government

    // In-progress selection (only meaningful when picking a new government)
    private var selectedGovernment: String = manager.currentGovernment
    private var selectedCards = ArrayList<String>(manager.assignedCards)

    private lateinit var governmentListTable: Table
    private lateinit var slotTable: Table

    init {
        setDefaultCloseAction()

        governmentListTable = Table().apply { defaults().pad(5f) }
        slotTable = Table()

        topTable.defaults().pad(10f).top()
        topTable.add(governmentListTable)
        topTable.add(slotTable).padLeft(20f)

        rightSideButton.setText("Adopt [${selectedGovernment}]".tr())
        rightSideButton.onClick(UncivSound.Paper) { adopt() }
        rightSideButton.enable()

        selectGovernment(selectedGovernment)
    }

    private fun selectGovernment(governmentName: String) {
        selectedGovernment = governmentName
        val government = ruleset.governments[governmentName]!!
        if (governmentName == manager.currentGovernment) {
            // Preserve the cards currently assigned to the adopted government
            selectedCards = ArrayList(manager.assignedCards)
            while (selectedCards.size < government.totalSlots()) selectedCards.add("")
        } else {
            selectedCards = ArrayList()
            repeat(government.totalSlots()) { selectedCards.add("") }
        }

        descriptionLabel.setText(getGovernmentDescriptionLines(government)
            .joinToString("\n") { it.text })
        rebuildGovernmentList()
        rebuildSlotTable()
        rightSideButton.setText("Adopt [${governmentName}]".tr())
    }

    private fun rebuildGovernmentList() {
        governmentListTable.clear()
        governmentListTable.defaults().pad(5f)
        val governmentList = governmentListTable
        // Civ 6: only governments whose required civic has been researched can be adopted.
        // The current government is always shown, so there is always at least one entry.
        for (government in ruleset.governments.values.sortedBy { it.name }) {
            val available = manager.isGovernmentAvailable(government)
            if (!available && government.name != manager.currentGovernment)
                continue
            val button = government.name.toTextButton()
                .apply {
                    if (available) {
                        onClick { selectGovernment(government.name) }
                        if (government.name == selectedGovernment) isDisabled = true
                    } else isDisabled = true
                }
            governmentList.add(button).row()
        }
    }

    private fun rebuildSlotTable() {
        val government = ruleset.governments[selectedGovernment] ?: return
        val slots = government.getSlots()
        val table = Table()
        for (i in slots.indices) {
            val slotType = slots[i]
            val cardName = selectedCards.getOrNull(i) ?: ""
            val card = if (cardName.isEmpty()) null else ruleset.policyCards[cardName]
            val cell = Table().apply {
                background = skinStrings.getUiBackground("General/Border", tintColor = BaseScreen.skinStrings.skinConfig.baseColor)
                if (card == null) {
                    add("($slotType)".toLabel(fontSize = 20)).pad(10f).row()
                    add("empty".toLabel(fontSize = 16)).pad(10f)
                } else {
                    add(card.name.toLabel(fontSize = 20)).pad(10f).row()
                    val descLines = getCardDescriptionLines(card)
                    if (descLines.isNotEmpty()) {
                        add(MarkupRenderer.render(descLines, labelWidth = SLOT_DESCRIPTION_WIDTH)).pad(10f)
                    } else {
                        add("($slotType slot)".toLabel(fontSize = 16)).pad(10f)
                    }
                }
            }
            cell.onClick { openCardChooser(i, slotType) }
            table.add(cell).pad(4f)
            if (i % 2 == 1) table.row()
        }
        slotTable.clear()
        slotTable.add(table)
    }

    private fun openCardChooser(slotIndex: Int, slotType: String) {
        val popup = Popup(this)
        popup.add("Choose a [${slotType}] policy card".tr()).row()
        // Civ 6: each policy card is unique - a card already assigned to another slot is not offered again
        val available = manager.getAvailableCardsForSlot(slotIndex)
        if (available.isEmpty()) popup.add("No available cards".toLabel()).row()
        for (card in available) {
            val descLines = getCardDescriptionLines(card)
            popup.add(card.name.toTextButton().apply {
                onClick {
                    selectedCards.addOrReplaceAt(slotIndex, card.name)
                    popup.close()
                    rebuildSlotTable()
                }
            }).row()
            if (descLines.isNotEmpty()) {
                popup.add(MarkupRenderer.render(descLines, labelWidth = POPUP_DESCRIPTION_WIDTH)).padLeft(30f).padBottom(10f).row()
            }
        }
        popup.addCloseButton()
        popup.open()
    }

    private fun ArrayList<String>.addOrReplaceAt(index: Int, value: String) {
        // ensure a placeholder exists for every slot
        while (this.size <= index) this.add("")
        this[index] = value
    }

    /** Card description for the picker: skips the "Slot" header and the (redundant) "Requires [civic]" line,
     *  since only available cards are ever shown. */
    private fun getCardDescriptionLines(card: PolicyCard): List<FormattedLine> =
        card.getCivilopediaTextLines(ruleset)
            .drop(1)
            .filterNot { it.text.startsWith("Requires [") }

    /** Government description for the picker: skips the "Requires [civic]" line, as only available
     *  governments are listed and the requirement is implied. */
    private fun getGovernmentDescriptionLines(government: Government): List<FormattedLine> =
        government.getCivilopediaTextLines(ruleset)
            .filterNot { it.text.startsWith("Requires [") }

    private fun adopt() {
        if (!GUI.isAllowedChangeState()) return
        // If changing government, clear old cards first (cards are government-specific)
        if (selectedGovernment != manager.currentGovernment) {
            manager.adoptGovernment(selectedGovernment)
        }
        // Assign the chosen cards into slots
        val government = ruleset.governments[selectedGovernment]!!
        for (i in government.getSlots().indices) {
            val cardName = selectedCards.getOrNull(i) ?: ""
            manager.assignCard(i, if (cardName.isEmpty()) null else cardName)
        }
        manager.shouldOpenGovernmentPicker = false
        game.popScreen()
    }

    companion object {
        private const val SLOT_DESCRIPTION_WIDTH = 150f
        private const val POPUP_DESCRIPTION_WIDTH = 320f
    }
}
