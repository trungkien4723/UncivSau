package com.unciv.ui.objectdescriptions

import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.Building
import com.unciv.models.ruleset.Ruleset
import com.unciv.models.ruleset.civic.Civic
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.translations.tr
import com.unciv.ui.components.fonts.Fonts
import com.unciv.ui.screens.civilopediascreen.FormattedLine

object CivicDescriptions {
    fun getDescription(civic: Civic, viewingCiv: Civilization): String = civic.run {
        val ruleset = viewingCiv.gameInfo.ruleset
        val lineList = ArrayList<String>()

        for (pediaText in civic.civilopediaText) {
            if (pediaText.text.isEmpty() || pediaText.header != 0) continue
            lineList += pediaText.text
        }

        uniquesToDescription(lineList)

        val enabledUnits = getEnabledUnits(name, ruleset, viewingCiv)
        if (enabledUnits.any()) {
            lineList += "{Units enabled}: "
            for (unit in enabledUnits)
                lineList += " • ${unit.name.tr()} (${unit.getShortDescription()})\n"
        }

        val (wonders, regularBuildings) = getEnabledBuildings(name, ruleset, viewingCiv)
            .partition { it.isAnyWonder() }

        if (regularBuildings.isNotEmpty()) {
            lineList += "{Buildings enabled}: "
            for (building in regularBuildings)
                lineList += " • ${building.name.tr()} (${building.getShortDescription()})\n"
        }

        if (wonders.isNotEmpty()) {
            lineList += "{Wonders enabled}: "
            for (wonder in wonders)
                lineList += " • ${wonder.name.tr()} (${wonder.getShortDescription()})\n"
        }

        val enabledDistricts = ruleset.districts.values.asSequence()
            .filter { it.requiredCivic == name }
            .toList()
        if (enabledDistricts.isNotEmpty()) {
            lineList += "{Districts enabled}: " + enabledDistricts.joinToString { it.name.tr() }
        }

        val enabledGovernments = ruleset.governments.values.asSequence()
            .filter { it.requiredCivic == name }
            .toList()
        if (enabledGovernments.isNotEmpty()) {
            lineList += "{Governments enabled}: " + enabledGovernments.joinToString { it.name.tr() }
        }

        // Civ VI Inspiration status
        if (civic.hasUnique(UniqueType.Inspiration)) {
            lineList += if (name in viewingCiv.civics.inspirationsTriggered)
                "Inspiration! achieved (research boost already gained)"
            else "Inspiration available (complete its condition for a research boost)"
        }

        return lineList.joinToString("\n") { it.tr() }
    }

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

        val enabledUnits = getEnabledUnits(name, ruleset, null)
        if (enabledUnits.any()) {
            lineList += FormattedLine()
            lineList += FormattedLine("{Units enabled}:")
            for (unit in enabledUnits)
                lineList += FormattedLine(unit.name.tr(true) + " (" + unit.getShortDescription() + ")", link = unit.makeLink())
        }

        val (wonders, regularBuildings) = getEnabledBuildings(name, ruleset, null)
            .partition { it.isAnyWonder() }

        if (regularBuildings.isNotEmpty()) {
            lineList += FormattedLine()
            lineList += FormattedLine("{Buildings enabled}:")
            for (building in regularBuildings)
                lineList += FormattedLine(building.name.tr(true) + " (" + building.getShortDescription() + ")", link = building.makeLink())
        }

        if (wonders.isNotEmpty()) {
            lineList += FormattedLine()
            lineList += FormattedLine("{Wonders enabled}:")
            for (wonder in wonders)
                lineList += FormattedLine(wonder.name.tr(true) + " (" + wonder.getShortDescription() + ")", link = wonder.makeLink())
        }

        val enabledDistricts = ruleset.districts.values.asSequence()
            .filter { it.requiredCivic == name }
            .toList()
        if (enabledDistricts.isNotEmpty()) {
            lineList += FormattedLine()
            lineList += FormattedLine("{Districts enabled}:")
            for (district in enabledDistricts)
                lineList += FormattedLine(district.name, link = district.makeLink())
        }

        val enabledGovernments = ruleset.governments.values.asSequence()
            .filter { it.requiredCivic == name }
            .toList()
        if (enabledGovernments.isNotEmpty()) {
            lineList += FormattedLine()
            lineList += FormattedLine("{Governments enabled}:")
            for (government in enabledGovernments)
                lineList += FormattedLine(government.name, link = government.makeLink())
        }

        return lineList
    }

    private fun getEnabledBuildings(civicName: String, ruleset: Ruleset, civInfo: Civilization?): Sequence<Building> {
        return ruleset.buildings.values.asSequence()
            .filter {
                it.requiredCivic == civicName
                && (it.uniqueTo == null || civInfo?.matchesFilter(it.uniqueTo!!) == true)
            }
    }

    private fun getEnabledUnits(civicName: String, ruleset: Ruleset, civInfo: Civilization?): Sequence<com.unciv.models.ruleset.unit.BaseUnit> {
        return ruleset.units.values.asSequence()
            .filter {
                it.requiredCivic == civicName
                && (it.uniqueTo == null || civInfo?.matchesFilter(it.uniqueTo!!) == true)
            }
    }
}
