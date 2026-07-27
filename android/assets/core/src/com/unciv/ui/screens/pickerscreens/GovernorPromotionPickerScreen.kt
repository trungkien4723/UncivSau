package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.Constants
import com.unciv.GUI
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.managers.GovernorManager
import com.unciv.models.ruleset.Governor
import com.unciv.models.ruleset.unique.Unique
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.extensions.toTextButton
import com.unciv.ui.components.input.onClick
import com.unciv.ui.popups.Popup
import com.unciv.ui.screens.basescreen.BaseScreen

class GovernorPromotionPickerScreen(
    internal val civInfo: Civilization,
    private val city: com.unciv.logic.city.City,
    private val onPromote: (String) -> Unit,  // promotion name selected
) : PickerScreen() {

    private val ruleset = civInfo.gameInfo.ruleset
    private val governorManager = civInfo.governorManager
    private val governorName = city.governor!!
    private val governor = ruleset.governors[governorName]!!
    private val currentLevel = governorManager.getGovernorPromotionLevel(city)

    init {
        setDefaultCloseAction()
        buildPromotionList()
    }

    private fun buildPromotionList() {
        val promotionList = Table().apply { defaults().pad(5f) }

// Current level info
        val currentLevelLabel = "Current Level: ${currentLevel + 1}".toLabel()
        currentLevelLabel.color = BaseScreen.skinStrings.skinConfig.baseColor
        promotionList.add(currentLevelLabel).colspan(2).padBottom(10f).row()

        // Show promotion options for next level
        val nextLevel = currentLevel
        if (nextLevel < governor.promotionObjects.size) {
            val levelPromotions = governor.promotionObjects[nextLevel]
            val label = "Choose a Level ${nextLevel + 1} Promotion:".toLabel()
            label.color = BaseScreen.skinStrings.skinConfig.baseColor
            promotionList.add(label).colspan(2).padBottom(5f).row()

            for (promotionUnique in levelPromotions) {
                val promoName = promotionUnique.placeholderText.tr()
                val button = promoName.toTextButton().apply {
                    onClick { selectPromotion(promotionUnique) }
                }
                promotionList.add(button).colspan(2).row()

                // Description
                val desc = promotionUnique.getDisplayText()
                val descLabel = desc.toLabel()
                descLabel.wrap = true
                promotionList.add(descLabel).colspan(2).padBottom(10f).row()
            }
        } else {
            promotionList.add("Max level reached!".toLabel()).colspan(2).row()
        }

        topTable.add(promotionList)
    }

    private fun selectPromotion(promotionUnique: Unique) {
        // The promotion name is the first part of the unique text
        val promotionName = promotionUnique.placeholderText
        onPromote(promotionName)
        game.popScreen()
    }
}