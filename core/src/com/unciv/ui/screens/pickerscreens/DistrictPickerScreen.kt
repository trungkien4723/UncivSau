package com.unciv.ui.screens.pickerscreens

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.logic.map.tile.Tile
import com.unciv.models.ruleset.District
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.fonts.Fonts
import com.unciv.ui.components.input.ActivationTypes
import com.unciv.ui.components.input.onActivation
import com.unciv.ui.images.ImageGetter

class DistrictPickerScreen(
    private val tile: Tile,
    private val unit: MapUnit,
    private val onAccept: (District, Tile) -> Unit,
) : PickerScreen() {

    private val ruleset = tile.tileMap.gameInfo.ruleset
    private val currentPlayerCiv = tile.tileMap.gameInfo.getCurrentPlayerCivilization()

    init {
        setDefaultCloseAction()

        rightSideButton.setText("Build district".tr())
        rightSideButton.onActivation {
            game.popScreen()
        }

        descriptionLabel.setText("Select a district to build on this tile.".tr())

        val districtsTable = Table()
        districtsTable.defaults().pad(5f)

        val city = tile.getCity()
        if (city == null) {
            topTable.add("This tile is not within a city's workable area.".tr().toLabel()).row()
        } else if (city.civ != currentPlayerCiv) {
            topTable.add("This tile belongs to another civilization.".tr().toLabel()).row()
        } else {
            for (district in ruleset.districts.values) {
                if (!canBuildDistrict(district)) continue

                val image = ImageGetter.getConstructionPortrait(district.name, 30f)
                val labelText = district.name.tr()
                val cost = district.cost
                val turnsToBuild = city.cityConstructions.getTurnsToBuildDistrict(district.name)

                val fullLabel = "$labelText - $cost${Fonts.turn} ($turnsToBuild ${Fonts.turn})"
                val button = PickerPane.getPickerOptionButton(image, fullLabel)

                button.onActivation(type = ActivationTypes.Tap, noEquivalence = true) {
                    pick(district.name.tr())
                    onAccept(district, tile)
                    game.popScreen()
                }

                districtsTable.add(button).row()
            }
        }

        topTable.add(districtsTable)
    }

    private fun canBuildDistrict(district: District): Boolean {
        if (tile.isCityCenter()) return false
        if (tile.district != null) return false

        val city = tile.getCity() ?: return false
        if (city.civ != currentPlayerCiv) return false

        if (tile !in city.tilesInRange) return false
        if (tile.districtToCreate != null) return false
        if (city.getDistrictsCount() >= city.getDistrictCapacity()) return false
        if (district.onlyBuildableOn.isNotEmpty() && !tile.matchesFilter(district.onlyBuildableOn, currentPlayerCiv)) return false

        val requiredTech = district.requiredTech
        if (requiredTech != null && !currentPlayerCiv.tech.isResearched(requiredTech)) return false
        val requiredCivic = district.requiredCivic
        if (requiredCivic != null && !currentPlayerCiv.civics.isResearched(requiredCivic)) return false

        return true
    }
}