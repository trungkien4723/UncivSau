package com.unciv.ui.components.tilegroups.layers

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.unciv.logic.civilization.Civilization
import com.unciv.ui.components.tilegroups.TileGroup
import com.unciv.ui.images.ImageGetter

class TileLayerPin(tileGroup: TileGroup, size: Float) : TileLayer(tileGroup, size) {
    private var pinIcon: Image? = null

    override fun doUpdate(viewingCiv: Civilization?) {
        val pinLabel = tile.mapPin
        if (pinLabel.isNullOrEmpty()) {
            if (pinIcon != null) {
                removeOwnedActor(pinIcon!!)
                pinIcon = null
            }
            return
        }
        if (pinIcon != null) return

        val color = getPinColor(pinLabel)
        pinIcon = ImageGetter.getCircle(color).apply {
            setSize(12f, 12f)
            x = tileX + (tileGroup.width - 12f) / 2f
            y = tileY + tileGroup.height * 0.32f
        }
        addOwnedActor(pinIcon!!)
    }

    override fun determineVisibility() {
        isVisible = pinIcon != null && tile.mapPin != null
    }

    companion object {
        private val pinColors = listOf(
            Color(0.2f, 0.5f, 1.0f, 0.9f),
            Color(1.0f, 0.3f, 0.3f, 0.9f),
            Color(0.2f, 0.8f, 0.3f, 0.9f),
            Color(1.0f, 0.8f, 0.0f, 0.9f),
            Color(1.0f, 0.5f, 0.0f, 0.9f),
            Color(0.7f, 0.3f, 1.0f, 0.9f),
            Color(1.0f, 0.3f, 0.7f, 0.9f),
            Color(0.5f, 0.5f, 0.5f, 0.9f),
        )

        fun getPinColor(pinLabel: String): Color {
            val index = ((pinLabel.hashCode() % pinColors.size) + pinColors.size) % pinColors.size
            return pinColors[index]
        }
    }
}
