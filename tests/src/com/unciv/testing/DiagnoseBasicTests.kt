package com.unciv.testing

import com.unciv.models.metadata.BaseRuleset
import com.unciv.models.ruleset.RulesetCache
import com.unciv.models.ruleset.unique.UniqueParameterType
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.translations.getPlaceholderParameters
import com.unciv.models.translations.getPlaceholderText
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(GdxTestRunner::class)
class DiagnoseBasicTests {
    private val tmpDir = File("C:/Users/84384/AppData/Local/Temp/opencode")

    @Before
    fun loadRulesets() {
        if (RulesetCache.isEmpty())
            RulesetCache.loadRulesets(noMods = true)
    }

    private fun List<String>.dump(name: String) {
        File(tmpDir, name).writeText(sorted().distinct().joinToString("\n"), Charsets.UTF_8)
        println("$name: ${sorted().distinct().size}")
    }

    @Test
    fun diagnose() {
        val ruleset = RulesetCache[BaseRuleset.Civ_VI.fullName]!!

        // 1. Unknown placeholder parameters in UniqueType
        val unknownParams = mutableListOf<String>()
        for (uniqueType in UniqueType.entries) {
            if (uniqueType.getDeprecationAnnotation() != null) continue
            val actualParameters = uniqueType.text.getPlaceholderParameters()
            for ((index, parameterName) in actualParameters.withIndex()) {
                if (uniqueType.parameterTypeMap[index].isEmpty()) {
                    unknownParams.add("${uniqueType.name}: param[$index] \"$parameterName\" text=\"${uniqueType.text}\"")
                }
            }
        }
        unknownParams.dump("unknownParams.txt")

        // 2. Unit uniques missing from UniqueType.entries
        val unitUniques = mutableListOf<String>()
        for (unit in ruleset.units.values) {
            for (unique in unit.uniques) {
                if (!UniqueType.entries.any { it.placeholderText == unique.getPlaceholderText() }) {
                    unitUniques.add("${unit.name} :: ${unique.getPlaceholderText()} :: $unique")
                }
            }
        }
        unitUniques.dump("unitUniques.txt")

        // 3. Building uniques missing from UniqueType.entries
        val buildingUniques = mutableListOf<String>()
        for (building in ruleset.buildings.values) {
            for (unique in building.uniques) {
                if (!UniqueType.entries.any { it.placeholderText == unique.getPlaceholderText() }) {
                    buildingUniques.add("${building.name} :: ${unique.getPlaceholderText()} :: $unique")
                }
            }
        }
        buildingUniques.dump("buildingUniques.txt")

        // 4. Error list for baseRulesetHasNoBugs
        val modCheck = ruleset.getErrorList()
        File(tmpDir, "errors.txt").writeText(modCheck.getErrorText(unfiltered = true), Charsets.UTF_8)
        println("errorCount: ${modCheck.count { it.errorSeverityToReport == com.unciv.models.ruleset.validation.RulesetErrorSeverity.Error }}, severity: ${modCheck.getFinalSeverity()}")
    }
}
