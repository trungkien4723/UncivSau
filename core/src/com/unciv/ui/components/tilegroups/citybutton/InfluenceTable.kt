package com.unciv.ui.components.tilegroups.citybutton

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.logic.civilization.diplomacy.RelationshipLevel
import com.unciv.ui.images.ImageGetter
import com.unciv.ui.screens.basescreen.BaseScreen

/**
 *  This is the "progress-bar" showing city-state envoy count.
 *  Scale: 0 to 6 envoys across 4 segments (Friend at 1, Ally at 3, Suzerain at 6).
 *  A negative value (e.g. -60 while at war) renders as a single hostile segment.
 *  Goes below the main Button with the name, before the statuses (puppet, resistance, WLTK).
 *  Not internal, it's reused in DiplomacyScreen.
 */
class InfluenceTable(
    influence: Float,
    relationshipLevel: RelationshipLevel,
    private val desiredWidth: Float = 100f, // shadowing Actor.width didn't work, needs differentiation
    private val desiredHeight: Float = 5f
) : Table() {
    init {
        defaults().pad(1f)
        setSize(desiredWidth, desiredHeight)
        background = BaseScreen.skinStrings.getUiBackground(
            "WorldScreen/CityButton/InfluenceBar",
            tintColor = ImageGetter.CHARCOAL)

        val warPenalty = influence < 0f
        val normalizedInfluence = influence.coerceIn(0f, 6f) / 1.5f

        val color = when (relationshipLevel) {
            RelationshipLevel.Unforgivable -> Color.RED
            RelationshipLevel.Enemy -> Color.ORANGE
            RelationshipLevel.Afraid -> Color.YELLOW
            RelationshipLevel.Neutral, RelationshipLevel.Friend -> Color.LIME
            RelationshipLevel.Ally -> Color.SKY
            else -> Color.DARK_GRAY
        }

        val percentages = arrayListOf(0f, 0f, 0f, 0f)
        if (warPenalty) {
            percentages[0] = 1f
        } else {
            for (i in 0..3) {
                val segmentStart = i
                percentages[i] = (normalizedInfluence - segmentStart).coerceIn(0f, 1f)
            }
        }

        for (i in 0..3)
            add(getBarPiece(percentages[i], color))
    }

    private fun getBarPiece(percentage: Float, color: Color) = Table().apply {
        val barPieceSize = desiredWidth / 4f
        val full = ImageGetter.getWhiteDot()
        val empty = ImageGetter.getWhiteDot()

        full.color = color
        empty.color = Color.DARK_GRAY

        add(full).size(percentage * barPieceSize, desiredHeight)
        add(empty).size((1f - percentage) * barPieceSize, desiredHeight)
    }

    override fun draw(batch: Batch?, parentAlpha: Float) { super.draw(batch, parentAlpha) }
}
