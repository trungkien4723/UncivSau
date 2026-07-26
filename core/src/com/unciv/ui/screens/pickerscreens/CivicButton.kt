package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.unciv.logic.civilization.managers.CivicManager
import com.unciv.ui.images.ImageGetter
import com.unciv.ui.screens.basescreen.BaseScreen
import com.unciv.ui.components.extensions.addBorder
import com.unciv.ui.components.extensions.brighten
import com.unciv.ui.components.extensions.center
import com.unciv.ui.components.extensions.centerY
import com.unciv.ui.components.extensions.darken
import com.unciv.ui.components.extensions.setFontSize
import com.unciv.ui.components.extensions.toLabel

class CivicButton(
    private val civicName: String,
    private val civicManager: CivicManager,
    isWorldScreen: Boolean = true
) : Table(BaseScreen.skin) {

    internal val text = "".toLabel().apply {
        wrap = false
        setFontSize(16)
        setAlignment(Align.left)
        setEllipsis(true)
    }

    internal val turns = "".toLabel().apply {
        setFontSize(16)
        setAlignment(Align.right)
    }

    private val backgroundImage: Image  // Table.background is the border

    init {
        touchable = Touchable.enabled

        val path = "CivicPickerScreen/CivicButton"
        val default = BaseScreen.skinStrings.roundedEdgeRectangleMidShape
        backgroundImage = object : Image(BaseScreen.skinStrings.getUiBackground(path, default)){
            override fun draw(batch: Batch?, parentAlpha: Float) = super.draw(batch, parentAlpha)
        }
        background = BaseScreen.skinStrings.getUiBackground(path, default, Color.WHITE.darken(0.3f))

        addActor(backgroundImage)

        pad(5f, 5f, 5f, 0f)

        val iconStack = Table()
        iconStack.add(ImageGetter.getConstructionPortrait(civicName, 60f)).size(60f)
        if (civicName in civicManager.inspirationsTriggered) {
            val inspirationIcon = ImageGetter.getImage("OtherIcons/Star")
            inspirationIcon.setSize(22f, 22f)
            inspirationIcon.color = Color.YELLOW
            iconStack.addActor(inspirationIcon)
            inspirationIcon.setPosition(38f, 38f)
        }
        add(iconStack).padRight(5f).padLeft(2f).left()

        if (isWorldScreen) {
            val civicCost = civicManager.costOfCivic(civicName)
            val remainingCivic = civicManager.remainingCultureToCivic(civicName)
            val civicThisTurn = civicManager.civInfo.stats.statsForNextTurn.culture

            val percentComplete = if (civicCost > 0) (civicCost - remainingCivic) / civicCost.toFloat() else 0f
            val percentWillBeComplete = if (civicCost > 0) (civicCost - (remainingCivic - civicThisTurn)) / civicCost.toFloat() else 0f
            val progressBar = ImageGetter.ProgressBar(2f, 48f, true)
                .setBackground(Color.WHITE)
                .setSemiProgress(Color.BLUE.cpy().brighten(0.3f), percentWillBeComplete)
                .setProgress(Color.BLUE.cpy().darken(0.5f), percentComplete)
            add(progressBar.addBorder(1f, Color.GRAY.cpy())).padLeft(0f).padRight(5f)
        }

        val rightSide = object : Table() {
            override fun draw(batch: Batch?, parentAlpha: Float) = super.draw(batch, parentAlpha)
        }

        rightSide.add(text).width(220f).top().left().padRight(15f)
        rightSide.add(turns).width(50f).top().left().padRight(10f).row()

        rightSide.centerY(this)
        add(rightSide).expandX().left()

        rightSide.toBack()
        backgroundImage.toBack()
        pack()

        backgroundImage.setSize(width - 3f, height - 3f)
        backgroundImage.align = Align.center
        backgroundImage.center(this)

        pack()
    }

    fun setButtonColor(color: Color) {
        backgroundImage.color = color
        pack()
    }

    override fun draw(batch: Batch?, parentAlpha: Float) = super.draw(batch, parentAlpha)
}
