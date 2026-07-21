package com.unciv.ui.objectdescriptions

import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Ruleset
import com.unciv.models.ruleset.civic.Civic
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.translations.tr
import com.unciv.ui.components.fonts.Fonts
import com.unciv.ui.screens.civilopediascreen.FormattedLine

object CivicDescriptions {
    //region Methods called from Civic

    /**
     * Textual description used in CivicPickerScreen and AlertPopup(AlertType.CivicResearched) -
     * Civilization always known and description tailored to it.
     */
    fun getDescription(civic: Civic, viewingCiv: Civilization): String = civic.run {
        val lineList = ArrayList<String>()

        for (pediaText in civic.civilopediaText) {
            if (pediaText.text.isEmpty() || pediaText.header != 0) continue
            lineList += pediaText.text
        }

        uniquesToDescription(lineList)

        // Civ VI Inspiration status
        if (civic.hasUnique(UniqueType.Inspiration)) {
            lineList += if (name in viewingCiv.civics.inspirationsTriggered)
                "Inspiration! achieved (research boost already gained)"
            else "Inspiration available (complete its condition for a research boost)"
        }

        return lineList.joinToString("\n") { it.tr() }
    }

    /**
     *  Implementation of ICivilopediaText.getCivilopediaTextLines
     */
    fun getCivilopediaTextLines(civic: Civic, ruleset: Ruleset): List<FormattedLine> = civic.run {
        val lineList = ArrayList<FormattedLine>()

        val eraColor = ruleset.eras[era()]?.getHexColor() ?: ""
        lineList += FormattedLine(era(), header = 3, color = eraColor)
        lineList += FormattedLine()
        lineList += FormattedLine("{Cost}: $cost${Fonts.culture}")

        if (prerequisites.isNotEmpty()) {
            lineList += FormattedLine()
            if (prerequisites.size == 1)
                prerequisites.first().let { lineList += FormattedLine("Required civic: [$it]", link = "Civic/$it") }
            else {
                lineList += FormattedLine("Requires all of the following:")
                prerequisites.forEach {
                    lineList += FormattedLine(it, link = "Civic/$it")
                }
            }
        }

        val leadsTo = ruleset.civics.values.filter { name in it.prerequisites }
        if (leadsTo.isNotEmpty()) {
            lineList += FormattedLine()
            if (leadsTo.size == 1)
                leadsTo.first().let { lineList += FormattedLine("Leads to [${it.name}]", link = it.makeLink()) }
            else {
                lineList += FormattedLine("Leads to:")
                leadsTo.forEach {
                    lineList += FormattedLine(it.name, link = it.makeLink())
                }
            }
        }

        uniquesToCivilopediaTextLines(lineList)

        return lineList
    }

    //endregion
}
