package com.unciv.ui.screens.overviewscreen

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.GreatWorkType
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.images.ImageGetter

class GreatWorksOverviewTab(
    viewingPlayer: Civilization,
    overviewScreen: EmpireOverviewScreen
) : EmpireOverviewTab(viewingPlayer, overviewScreen) {

    init {
        pad(10f)
        val greatWorks = viewingPlayer.greatWorks

        for (type in GreatWorkType.entries) {
            val worksOfType = greatWorks.getGreatWorksByType(type)
            val icon = ImageGetter.getStatIcon(type.name)
            val typeLabel = type.name.tr().toLabel()
            val slotInfo = greatWorks.getAvailableSlots(type)
            val totalSlots = worksOfType.size + slotInfo

            val header = Table()
            header.add(icon).size(24f).padRight(5f)
            header.add(typeLabel)
            header.add(" ($totalSlots slots, ${worksOfType.size} filled)".toLabel())
            add(header).pad(5f).left().row()

            if (worksOfType.isEmpty()) {
                add("  No great works of this type".toLabel()).padLeft(30f).row()
            } else {
                for (work in worksOfType) {
                    val stats = work.getStats()
                    val statsText = buildString {
                        if (stats.tourism > 0) append("+${stats.tourism.toInt()} Tourism ")
                        if (stats.culture > 0) append("+${stats.culture.toInt()} Culture")
                    }
                    val workTable = Table()
                    workTable.add("  ".toLabel()).padLeft(30f)
                    workTable.add("${work.name} ($statsText)".toLabel()).padLeft(5f)
                    add(workTable).row()
                }
            }
            add(Table().apply { padBottom(8f) }).row()
        }

        val totalStats = viewingPlayer.greatWorks.getTotalStats()
        val totalLabel = "Total: +${totalStats.tourism.toInt()} Tourism, +${totalStats.culture.toInt()} Culture".toLabel()
        add(totalLabel).pad(10f).colspan(2).row()
    }
}
