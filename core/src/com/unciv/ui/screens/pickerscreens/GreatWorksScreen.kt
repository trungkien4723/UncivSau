package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.GreatWorkType
import com.unciv.models.translations.tr
import com.unciv.ui.images.ImageGetter
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.screens.worldscreen.WorldScreen

class GreatWorksScreen(
    private val worldScreen: WorldScreen,
    private val civInfo: Civilization
) : PickerScreen() {

    init {
        setDefaultCloseAction()
        rightSideButton.isVisible = false
        descriptionLabel.setText("Great Works".tr())

        val greatWorks = civInfo.greatWorks

        for (type in GreatWorkType.entries) {
            val worksOfType = greatWorks.getWorksByType(type)
            val typeLabel = type.name.tr().toLabel()

            val header = Table()
            header.add(ImageGetter.getStatIcon(type.name)).size(24f).padRight(5f)
            header.add(typeLabel)
            header.add(" (${worksOfType.size})".toLabel())
            topTable.add(header).pad(5f).left().row()

            if (worksOfType.isEmpty()) {
                topTable.add("  No great works of this type".toLabel()).padLeft(30f).row()
            } else {
                for (city in civInfo.cities) {
                    val cityWorks = city.cityConstructions
                        .getBuiltBuildings()
                        .flatMap { building ->
                            greatWorks.getWorksInBuilding(city.id, building.name, type)
                                .map { work -> "  ${building.name}: ${work.name}" }
                        }
                    for (workText in cityWorks) {
                        topTable.add("  ").padLeft(30f)
                        topTable.add(workText.toLabel()).padLeft(5f).row()
                    }
                }
            }
            topTable.add().padBottom(8f).row()
        }

        val totalStats = greatWorks.getTotalStats()
        val totalLabel = "Total: +${totalStats.tourism.toInt()} Tourism, +${totalStats.culture.toInt()} Culture".toLabel()
        topTable.add(totalLabel).pad(10f).colspan(2).row()
    }
}