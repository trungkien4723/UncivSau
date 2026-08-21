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
        // Return description based on dedication name
        return when (dedicationName) {
            "Monumentality" -> "+1 Era Score for each specialty District built. Golden Age: civilian units can be bought with Faith at a discount."
            "Free Inquiry" -> "+1 Era Score per Eureka, +2 for Campus and Commercial Hub buildings. Golden Age: +10% Science in all cities."
            "Pen, Brush, and Voice" -> "+1 Era Score per Inspiration, +2 for Theater Square buildings. Golden Age: +10% Culture and +1 Gold per citizen."
            "Exodus of the Evangelists" -> "+2 Era Score for converting a foreign city. Golden Age: religious units gain Movement and Religious Strength."
            "Reform the Coinage" -> "+1 Era Score for completing an international Trade Route. Golden Age: bonus Gold from Trade Routes and pillaging."
            "To Arms!" -> "+1 Era Score for killing an enemy military unit or conquering a city. Golden Age: military production bonus."
            else -> "Special dedication bonus for ${goldenAgeManager.currentAge} Age."
        }
    }
}