package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.unciv.GUI
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.civilization.managers.CivicManager
import com.unciv.models.UncivSound
import com.unciv.models.ruleset.civic.Civic
import com.unciv.models.ruleset.unique.UniqueType
import com.unciv.models.translations.tr
import com.unciv.ui.components.NonTransformGroup
import com.unciv.ui.components.extensions.colorFromRGB
import com.unciv.ui.components.extensions.darken
import com.unciv.ui.components.extensions.disable
import com.unciv.ui.components.extensions.surroundWithCircle
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.fonts.Fonts
import com.unciv.ui.components.input.onClick
import com.unciv.ui.components.input.onRightClick
import com.unciv.ui.components.input.onDoubleClick
import com.unciv.ui.images.ImageGetter
import com.unciv.ui.popups.ToastPopup
import com.unciv.utils.Concurrency
import yairm210.purity.annotations.Readonly
import kotlin.math.abs


class CivicPickerScreen(
    internal val civInfo: Civilization,
    centerOnCivic: Civic? = null,
) : PickerScreen() {

    private val freeCivicPick: Boolean = civInfo.civics.freeCivics != 0
    private val ruleset = civInfo.gameInfo.ruleset
    private var civicNameToButton = HashMap<String, CivicButton>()
    private var selectedCivic: Civic? = null
    private var civCivics: CivicManager = civInfo.civics
    private var tempCivicsToResearch: ArrayList<String>
    private var lines = NonTransformGroup()
    private var orderIndicators = NonTransformGroup()
    private var eraLabels = ArrayList<Label>()

    private val techTable = Table()

    private val currentCivicColor = skinStrings.getUIColor("CivicPickerScreen/CurrentCivicColor", colorFromRGB(72, 147, 175))
    private val researchedCivicColor = skinStrings.getUIColor("CivicPickerScreen/ResearchedCivicColor", colorFromRGB(255, 215, 0))
    private val researchableCivicColor = skinStrings.getUIColor("CivicPickerScreen/ResearchableCivicColor", colorFromRGB(28, 170, 0))
    private val queuedCivicColor = skinStrings.getUIColor("CivicPickerScreen/QueuedCivicColor", colorFromRGB(7*2, 46*2, 43*2))

    private val turnsToCivic = ruleset.civics.values.associateBy({ it.name }, { civCivics.turnsToCivic(it.name) })

    init {
        setDefaultCloseAction()
        scrollPane.setOverscroll(false, false)

        descriptionLabel.onClick {
            if (selectedCivic != null)
                openCivilopedia(selectedCivic!!.makeLink())
        }

        tempCivicsToResearch = ArrayList(civCivics.civicsToResearch)

        createCivicTable()
        setButtonsInfo()
        techTable.addActor(lines)
        techTable.addActor(orderIndicators)
        topTable.add(techTable)
        techTable.background = skinStrings.getUiBackground("CivicPickerScreen/Background", tintColor = skinStrings.skinConfig.clearColor)
        pickerPane.bottomTable.background = skinStrings.getUiBackground("CivicPickerScreen/BottomTable", tintColor = skinStrings.skinConfig.clearColor)

        rightSideButton.setText(if (freeCivicPick) "Pick a free civic".tr() else "Pick a civic".tr())
        rightSideButton.onClick(UncivSound.Paper) { tryExit() }

        val civic = centerOnCivic ?: civInfo.civics.currentCivic()
        if (civic != null) {
            if (civInfo.civics.isResearched(civic.name) || civInfo.civics.civicsToResearch.size <= 1)
                selectCivic(civic, queue = false, center = true)
            else centerOnCivic(civic)
        } else {
            val firstAvailable = ruleset.civics.keys.firstOrNull { civCivics.canBeResearched(it) }
            val firstAvailableCivic = ruleset.civics[firstAvailable]
            if (firstAvailableCivic != null)
                centerOnCivic(firstAvailableCivic)
        }
    }

    override fun getCivilopediaRuleset() = ruleset


    private fun tryExit() {
        if (freeCivicPick) {
            val freeCivic = selectedCivic!!.name
            if (!civCivics.canBeResearched(freeCivic)) return
            civCivics.getFreeCivic(selectedCivic!!.name)
        }
        else civCivics.civicsToResearch = tempCivicsToResearch

        civCivics.updateResearchProgress()

        game.popScreen()
    }

    private fun createCivicTable() {

        for (label in eraLabels) label.remove()
        eraLabels.clear()

        val allCivics = ruleset.civics.values
        if (allCivics.isEmpty()) return
        val columns = allCivics.maxOf { it.column!!.columnNumber } + 1
        val rows = allCivics.maxOf { it.row } + 1
        val civicMatrix = Array<Array<Civic?>>(columns) { arrayOfNulls(rows) }

        for (civic in allCivics) {
            civicMatrix[civic.column!!.columnNumber][civic.row - 1] = civic
        }

        val erasNamesToColumns = LinkedHashMap<String, ArrayList<Int>>()
        for (civic in allCivics) {
            val era = civic.era()
            if (!erasNamesToColumns.containsKey(era)) erasNamesToColumns[era] = ArrayList()
            val columnNumber = civic.column!!.columnNumber
            if (!erasNamesToColumns[era]!!.contains(columnNumber)) erasNamesToColumns[era]!!.add(columnNumber)
        }
        for ((era, eraColumns) in erasNamesToColumns) {
            val columnSpan = eraColumns.size
            val color = ImageGetter.CHARCOAL.cpy()

            val table1 = Table().pad(1f)
            val table2 = Table()

            table1.background = skinStrings.getUiBackground("General/Border", tintColor = Color.WHITE)
            table2.background = skinStrings.getUiBackground("General/Border", tintColor = color)

            val label = era.toLabel().apply {
                setAlignment(Align.center)
            }

            eraLabels.add(label)

            table2.add(label).growX()
            table1.add(table2).growX()

            techTable.add(table1).fill().colspan(columnSpan)
        }

        for (rowIndex in 0 until rows) {

            techTable.row()

            for (columnIndex in civicMatrix.indices) {
                val civic = civicMatrix[columnIndex][rowIndex]

                val table = Table().pad(2f).padRight(60f).padLeft(20f)
                if (rowIndex == 0)
                    table.padTop(7f)

                if (civic == null) {
                    techTable.add(table).fill()
                } else {
                    val civicButton = CivicButton(civic.name, civCivics, false)
                    table.add(civicButton)
                    civicNameToButton[civic.name] = civicButton
                    civicButton.onClick { selectCivic(civic, queue = false, center = false) }
                    civicButton.onRightClick { selectCivic(civic, queue = true, center = false) }
                    civicButton.onDoubleClick(UncivSound.Paper) { tryExit() }
                    techTable.add(table).fillX()
                }
            }
        }
    }

    private fun setButtonsInfo() {
        for ((civicName, civicButton) in civicNameToButton) {
            val isResearched = civCivics.isResearched(civicName)
            civicButton.setButtonColor(when {
                isResearched -> researchedCivicColor
                tempCivicsToResearch.firstOrNull() == civicName && !freeCivicPick -> currentCivicColor
                civCivics.canBeResearched(civicName) -> researchableCivicColor
                tempCivicsToResearch.contains(civicName) -> queuedCivicColor
                else -> ImageGetter.CHARCOAL.cpy()
            })

            if (!isResearched) {
                civicButton.turns.setText(turnsToCivic[civicName] + "${Fonts.turn}".tr())
            }

            civicButton.text.setText(civicName.tr(true))
        }

        addConnectingLines()

        addOrderIndicators()
    }

    private fun addConnectingLines() {
        techTable.pack()
        scrollPane.updateVisualScroll()

        lines.clear()

        for (eraLabel in eraLabels) {
            val coords = Vector2(0f, 0f)
            eraLabel.localToStageCoordinates(coords)
            techTable.stageToLocalCoordinates(coords)
            val line = ImageGetter.getLine(coords.x-1f, coords.y, coords.x-1f, coords.y - 1000f, 1f)
            line.color = Color.GRAY.cpy().apply { a = 0.6f }
            line.toBack()
            lines.addActor(line)
        }

        for (civic in ruleset.civics.values) {
            if (!civicNameToButton.containsKey(civic.name)) {
                ToastPopup("Civic ${civic.name} appears to be missing - perhaps two civics have the same row & column", this)
                continue
            }
            val civicButton = civicNameToButton[civic.name]!!
            for (prerequisite in civic.prerequisites) {
                if (!civicNameToButton.containsKey(prerequisite)) {
                    ToastPopup("Civic $prerequisite. prerequisite of ${civic.name}, appears to be missing - perhaps two civics have the same row & column", this)
                    continue
                }
                val prerequisiteButton = civicNameToButton[prerequisite]!!
                val civicButtonCoords = Vector2(0f, civicButton.height / 2)
                civicButton.localToStageCoordinates(civicButtonCoords)
                techTable.stageToLocalCoordinates(civicButtonCoords)

                val prerequisiteCoords = Vector2(prerequisiteButton.width, prerequisiteButton.height / 2)
                prerequisiteButton.localToStageCoordinates(prerequisiteCoords)
                techTable.stageToLocalCoordinates(prerequisiteCoords)

                val lineColor = when {
                    civCivics.isResearched(civic.name) && !civic.isContinuallyResearchable() -> Color.WHITE.cpy()
                    civCivics.isResearched(prerequisite) -> researchableCivicColor
                    tempCivicsToResearch.contains(civic.name) -> currentCivicColor
                    else -> Color.WHITE.cpy()
                }

                val lineSize = when {
                    tempCivicsToResearch.contains(civic.name) && !civCivics.isResearched(prerequisite) -> 4f
                    else -> 2f
                }

                if (civicButtonCoords.y != prerequisiteCoords.y) {

                    val r = 6f

                    val deltaX = civicButtonCoords.x - prerequisiteCoords.x
                    val deltaY = civicButtonCoords.y - prerequisiteCoords.y
                    val halfLength = deltaX / 2f

                    val line = ImageGetter.getWhiteDot().apply {
                        width = halfLength - r - lineSize/2
                        height = lineSize
                        x = prerequisiteCoords.x
                        y = prerequisiteCoords.y - lineSize / 2
                    }
                    val line1 = ImageGetter.getWhiteDot().apply {
                        width = halfLength - r - lineSize/2
                        height = lineSize
                        x = civicButtonCoords.x - width
                        y = civicButtonCoords.y - lineSize / 2
                    }
                    val line2 = ImageGetter.getWhiteDot().apply {
                        width = lineSize
                        height = abs(deltaY) - 2*r - lineSize
                        x = civicButtonCoords.x - halfLength - lineSize / 2
                        y = civicButtonCoords.y + (if (deltaY > 0f) -height-r-lineSize/2 else r+lineSize/2)
                    }

                    var line3: Image?
                    var line4: Image?

                    if (deltaY < 0) {
                        /* -\ */ line3 = ImageGetter.getLine(line2.x+lineSize/2+0.3f, line2.y + line2.height-lineSize/2,line.x + line.width-lineSize/2, line.y+lineSize/2+0.3f, lineSize)
                        /* \- */ line4 = ImageGetter.getLine(line2.x+lineSize/2-0.3f, line2.y+lineSize/2, line1.x+lineSize/2, line1.y+lineSize/2-0.3f, lineSize)
                    } else {
                        /* -/ */ line3 = ImageGetter.getLine(line2.x+lineSize/2+0.3f, line2.y+lineSize/2, line.x + line.width-lineSize/2, line.y+lineSize/2-0.3f, lineSize)
                        /* /- */ line4 = ImageGetter.getLine(line2.x+lineSize/2-0.3f, line2.y + line2.height-lineSize/2, line1.x+lineSize/2, line1.y+lineSize/2+0.3f, lineSize)
                    }

                    line.color = lineColor
                    line1.color = lineColor
                    line2.color = lineColor
                    line3.color = lineColor
                    line4.color = lineColor

                    lines.addActor(line)
                    lines.addActor(line1)
                    lines.addActor(line2)
                    lines.addActor(line3)
                    lines.addActor(line4)

                } else {

                    val line = ImageGetter.getWhiteDot().apply {
                        width = civicButtonCoords.x - prerequisiteCoords.x
                        height = lineSize
                        x = prerequisiteCoords.x
                        y = prerequisiteCoords.y - lineSize / 2
                    }
                    line.color = lineColor

                    lines.addActor(line)
                }
            }
        }

        lines.children.filter { it.color == currentCivicColor && it.color != Color.WHITE.cpy() }
            .forEach { it.toFront() }
    }

    private fun addOrderIndicators() {
        orderIndicators.clear()
        for ((civicName, civicButton) in civicNameToButton) {
            val civicButtonCoords = Vector2(0f, civicButton.height / 2)
            civicButton.localToStageCoordinates(civicButtonCoords)
            techTable.stageToLocalCoordinates(civicButtonCoords)
            if (tempCivicsToResearch.contains(civicName) && tempCivicsToResearch.size > 1) {
                val index = tempCivicsToResearch.indexOf(civicName) + 1
                val orderIndicator = index.tr().toLabel(fontSize = 18)
                    .apply { setAlignment(Align.center) }
                    .surroundWithCircle(28f, color = skinStrings.skinConfig.baseColor)
                    .surroundWithCircle(30f,false)
                    .apply { setPosition(civicButtonCoords.x - width, civicButtonCoords.y - height / 2) }
                orderIndicators.addActor(orderIndicator)
            }
        }
        orderIndicators.toFront()
    }

    private fun selectCivic(civic: Civic?, queue: Boolean = false, center: Boolean = false, switchFromWorldScreen: Boolean = true) {

        val previousSelectedCivic = selectedCivic
        selectedCivic = civic
        descriptionLabel.setText(civic?.getDescription(civInfo))

        if (!switchFromWorldScreen)
            return

        if (civic == null)
            return

        if (center) centerOnCivic(civic)

        if (freeCivicPick) {
            selectCivicForFreeCivic(civic)
            setButtonsInfo()
            return
        }

        if (civInfo.gameInfo.gameParameters.godMode && !civInfo.civics.isResearched(civic.name)
                && selectedCivic == previousSelectedCivic) {
            civInfo.civics.addCivic(civic.name)
        }

        if (civCivics.isResearched(civic.name) && !civic.isContinuallyResearchable()) {
            rightSideButton.setText("Pick a civic".tr())
            rightSideButton.disable()
            setButtonsInfo()
            return
        }

        if (!GUI.isAllowedChangeState()) {
            rightSideButton.disable()
            return
        }

        val pathToCivic = civCivics.getRequiredCivicsToDestination(civic)
        for (requiredCivic in pathToCivic) {
            val unavailableUniques = requiredCivic.uniqueObjects.filter {
                it.type == UniqueType.OnlyAvailable && !it.conditionalsApply(civInfo.state) ||
                    it.type == UniqueType.Unavailable && it.conditionalsApply(civInfo.state)
            }
            for (unique in unavailableUniques) {
                rightSideButton.setText(unique.getDisplayText().tr())
                rightSideButton.disable()
                return
            }
        }

        if (queue){
            for (pathCivic in pathToCivic) {
                if (pathCivic.name !in tempCivicsToResearch) {
                    tempCivicsToResearch.add(pathCivic.name)
                }
            }
        }else{
            tempCivicsToResearch.clear()
            tempCivicsToResearch.addAll(pathToCivic.map { it.name })
        }

        if (tempCivicsToResearch.any()) {
            val label = "Research [${tempCivicsToResearch[0]}]".tr()
            val civicProgression = getCivicProgressLabel(tempCivicsToResearch)
            pick("${label}\n${civicProgression}")
        } else {
            rightSideButton.setText("Unavailable".tr())
            rightSideButton.disable()
        }
        setButtonsInfo()
    }

    @Readonly
    private fun getCivicProgressLabel(civics: List<String>): String {
        val progress = civics.sumOf { civic -> civCivics.cultureSpentOnCivic(civic) }
        val civicCost = civics.sumOf { civic -> civInfo.civics.costOfCivic(civic) }
        return "(${progress}/${civicCost})"
    }

    private fun centerOnCivic(civic: Civic) {
        Concurrency.runOnGLThread {
            civicNameToButton[civic.name]?.parent?.let {
                scrollPane.scrollTo(it.x, it.y, it.width, it.height, true, true)
                scrollPane.updateVisualScroll()
            }
        }
    }

    private fun selectCivicForFreeCivic(civic: Civic) {
        if (civCivics.canBeResearched(civic.name)) {
            val label = "Pick [${civic.name}] as free civic".tr()
            val civicProgression = getCivicProgressLabel(listOf(civic.name))
            pick("${label}\n${civicProgression}")
        } else {
            rightSideButton.setText("Pick a free civic".tr())
            rightSideButton.disable()
        }
    }
}
