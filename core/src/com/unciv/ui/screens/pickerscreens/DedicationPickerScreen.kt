package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.Constants
import com.unciv.GUI
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.managers.GoldenAgeManager
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.extensions.toTextButton
import com.unciv.ui.components.input.onClick
import com.unciv.ui.popups.Popup
import com.unciv.ui.screens.basescreen.BaseScreen

class DedicationPickerScreen(
    internal val civInfo: Civilization,
    private val onDedicationSelected: (String) -> Unit,
) : PickerScreen() {

    private val goldenAgeManager = civInfo.goldenAges

    init {
        setDefaultCloseAction()
        buildDedicationList()
    }

    private fun buildDedicationList() {
        val dedicationList = Table().apply { defaults().pad(5f) }

        val age = goldenAgeManager.currentAge
        val titleLabel = "Select Dedication for ${age} Age:".tr().toLabel()
        titleLabel.color = BaseScreen.skinStrings.skinConfig.baseColor
        dedicationList.add(titleLabel).colspan(2).padBottom(15f).row()

        val goldenThreshold = goldenAgeManager.getGoldenThreshold(civInfo.getEra().eraNumber)
        val darkThreshold = goldenAgeManager.getDarkThreshold(civInfo.getEra().eraNumber)
        val thresholdsLabel = "Golden threshold: ${goldenThreshold} | Dark threshold: ${darkThreshold}".tr().toLabel()
        thresholdsLabel.color = BaseScreen.skinStrings.skinConfig.baseColor
        dedicationList.add(thresholdsLabel).colspan(2).padBottom(10f).row()

        val availableDedications = goldenAgeManager.getDedicationsForAge()
        if (availableDedications.isEmpty()) {
            dedicationList.add("No dedications available".toLabel()).colspan(2).row()
        } else {
            for (dedicationName in availableDedications) {
                val button = dedicationName.tr().toTextButton().apply {
                    onClick { selectDedication(dedicationName) }
                }
                dedicationList.add(button).colspan(2).padBottom(8f).row()

                // Show description of the dedication
                val desc = getDedicationDescription(dedicationName)
                val descLabel = desc.toLabel()
                descLabel.wrap = true
                dedicationList.add(descLabel).colspan(2).width(400f).padBottom(12f).row()
            }
        }

        topTable.add(dedicationList)
    }

    private fun selectDedication(dedicationName: String) {
        onDedicationSelected(dedicationName)
        game.popScreen()
    }

    private fun getDedicationDescription(dedicationName: String): String {
        // Return description based on dedication name (simplified)
        return when (dedicationName) {
            "Exodus of the Evangelists" -> "Religious units gain [+2] Movement and [+25]% Religious Strength."
            "Penitent" -> "Holy Sites and Shrines provide [+2] Faith."
            "Commune" -> "Districts provide [+1] Housing."
            "Inquisition" -> "Inquisitors gain [+1] Spread Religion charge."
            "Heartbeat of Steam" -> "Industrial Zones provide [+2] Production."
            "To Arms!" -> "Encampments provide [+2] Production and [+1] Great General points."
            "Monumentality" -> "Faith purchasing of civilian units [+50]% cheaper."
            "Free Inquiry" -> "Campus and Library provide [+2] Science."
            "Bodyguard of Lies" -> "Spies gain [+2] levels and [+20]% mission success chance."
            "Hic Sunt Dracones" -> "Natural Wonders provide [+2] Science and [+2] Culture."
            "Wonders of the Ancient World" -> "Wonder production [+15]%."
            "Civic Pride" -> "Monuments provide [+2] Culture and [+1] Loyalty."
            "Heroic Epic" -> "Military units gain [+1] Movement and [+5] Combat Strength."
            "Age of Discovery" -> "Naval units gain [+2] Movement and [+1] Sight."
            else -> "Special dedication bonus for ${goldenAgeManager.currentAge} Age."
        }
    }
}