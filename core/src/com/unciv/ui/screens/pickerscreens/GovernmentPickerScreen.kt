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

class GovernmentPickerScreen(
    internal val civInfo: Civilization,
    private val isPickGovernment: Boolean = true,
) : PickerScreen() {

    private val ruleset = civInfo.gameInfo.ruleset
    private val manager = civInfo.government

    // In-progress selection (only meaningful when picking a new government)
    private var selectedGovernment: String = manager.currentGovernment
    private var selectedCards = ArrayList<String>(manager.assignedCards)

    init {
        setDefaultCloseAction()

        val governmentList = Table().apply { defaults().pad(5f) }
        for (government in ruleset.governments.values.sortedBy { it.name }) {
            val available = manager.isGovernmentAvailable(government)
            val button = government.name.toTextButton()
                .apply {
                    if (available) onClick { selectGovernment(government.name) }
                    else isDisabled = true
                }
            governmentList.add(button).row()
            if (!available)
                governmentList.add("Requires [$government.requiredCivic]".tr().toLabel()).padBottom(4f).row()
        }

        topTable.add(governmentList)
        rightSideButton.setText("Adopt [${selectedGovernment}]".tr())
        rightSideButton.onClick(UncivSound.Paper) { adopt() }
        rightSideButton.enable()

        selectGovernment(selectedGovernment)
    }

    private fun selectGovernment(governmentName: String) {
        selectedGovernment = governmentName
        selectedCards = ArrayList()
        val government = ruleset.governments[governmentName]!!
        repeat(government.totalSlots()) { selectedCards.add("") }

        descriptionLabel.setText(government.getCivilopediaTextLines(ruleset)
            .joinToString("\n") { it.text })
        rebuildSlotTable()
        rightSideButton.setText("Adopt [${governmentName}]".tr())
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
                setSize(600f, 600f)
                if (card == null) {
                    add("($slotType)".toLabel(fontSize = 40)).pad(20f).row()
                    add("empty".toLabel(fontSize = 30)).pad(20f)
                } else {
                    add(card.name.toLabel(fontSize = 30)).pad(10f).row()
                    val descText = card.getCivilopediaTextLines(ruleset)
                        .drop(1)
                        .joinToString("\n") { it.text }
                    if (descText.isNotEmpty()) {
                        add(descText.toLabel(fontSize = 16)).pad(10f)
                    } else {
                        add("($slotType slot)".toLabel(fontSize = 20)).pad(10f)
                    }
                }
            }
            cell.onClick { openCardChooser(i, slotType) }
            table.add(cell).pad(4f)
            if (i % 2 == 1) table.row()
        }
        topTable.clear()
        topTable.add(table)
    }

    private fun openCardChooser(slotIndex: Int, slotType: String) {
        val popup = Popup(this)
        popup.add("Choose a [${slotType}] policy card".tr()).row()
        val available = ruleset.policyCards.values.filter {
            (slotType == "Wildcard" || it.slotType == "Wildcard" || it.slotType == slotType)
                    && manager.isCardAvailable(it)
        }.sortedBy { it.name }
        if (available.isEmpty()) popup.add("No available cards".toLabel()).row()
        for (card in available) {
            val descText = card.getCivilopediaTextLines(ruleset)
                .drop(1)
                .joinToString("\n") { it.text }
            popup.add(card.name.toTextButton().apply {
                onClick {
                    selectedCards.addOrReplaceAt(slotIndex, card.name)
                    popup.close()
                    rebuildSlotTable()
                }
            }).row()
            if (descText.isNotEmpty()) {
                popup.add(descText.toLabel(fontSize = 20)).padLeft(30f).padBottom(10f).row()
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
}
