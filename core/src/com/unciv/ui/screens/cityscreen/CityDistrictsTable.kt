package com.unciv.ui.screens.cityscreen

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.unciv.logic.city.City
import com.unciv.logic.civilization.Civilization
import com.unciv.logic.map.tile.Tile
import com.unciv.models.ruleset.Building
import com.unciv.models.ruleset.District
import com.unciv.models.ruleset.Ruleset
import com.unciv.models.stats.Stat
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.extensions.toTextButton
import com.unciv.ui.components.input.onClick
import com.unciv.ui.components.widgets.ExpanderTab
import com.unciv.ui.images.ImageGetter
import com.unciv.ui.popups.Popup
import com.unciv.ui.screens.basescreen.BaseScreen
import com.unciv.ui.screens.pickerscreens.PickerPane


class CityDistrictsTable(private val cityScreen: CityScreen) {
    private val city: City = cityScreen.city

    fun asExpander(): ExpanderTab {
        return ExpanderTab(
            "Districts".tr(),
            startsOutOpened = true,
            persistenceID = "CityDistrictsTable",
            defaultPad = 7f,
            onChange = { cityScreen.updateWithoutConstructionAndMap() }
        ) {
            val table = Table()
            table.defaults().pad(3f)
            val districts = city.getDistricts().toList()
            if (districts.isEmpty()) {
                table.add("No districts built yet".tr().toLabel())
                table.row()
            } else {
                for ((tile, district) in districts) {
                    table.add(buildDistrictRow(district, tile))
                    table.row()
                }
            }

            table.add(createAddDistrictButton()).padTop(3f)
            table.row()

            it.add(table).growX()
        }
    }

    private fun createAddDistrictButton(): Table {
        val button = "+".toTextButton()
        button.onClick {
            showDistrictPicker()
        }
        return button
    }

    private fun showDistrictPicker() {
        val popup = object : Popup(cityScreen, scrollable = Popup.Scrollability.All) {
            init {
                defaults().fillX()
                addCloseButton()

                val ruleset = city.getRuleset()
                val civ = city.civ

                for (district in ruleset.districts.values) {
                    if (!canBuildDistrict(district, ruleset, civ)) continue

                    val image = ImageGetter.getConstructionPortrait(district.name, 30f)
                    val button = PickerPane.getPickerOptionButton(image, district.name.tr())
                    button.onClick {
                        close()
                        val building = findBuildingForDistrict(district.name, ruleset)
                        if (building != null) {
                            cityScreen.startPickTileForCreatesOneDistrict(building, Stat.Gold, false)
                        }
                    }
                    add(button).row()
                }

                pack()
                open()
            }
        }
    }

    private fun canBuildDistrict(district: District, ruleset: Ruleset, civ: Civilization): Boolean {
        if (city.getDistrictsCount() >= city.getDistrictCapacity()) return false
        if (district.name == "City Center") return false
        if (district.name in city.districts.values) return false

        val requiredTech = district.requiredTech
        if (requiredTech != null && !civ.tech.isResearched(requiredTech)) return false
        val requiredCivic = district.requiredCivic
        if (requiredCivic != null && !civ.civics.isResearched(requiredCivic)) return false

        val building = findBuildingForDistrict(district.name, ruleset)
        if (building == null) return false
        if (!building.isBuildable(city.cityConstructions, true)) return false

        return true
    }

    private fun findBuildingForDistrict(districtName: String, ruleset: Ruleset): Building? {
        val building = ruleset.buildings[districtName]
        return if (building != null && building.hasCreateOneDistrictUnique()) building else null
    }

    private fun buildDistrictRow(district: District, tile: Tile): Table {
        val row = Table()
        val nameText = if (tile.districtIsPillaged) "[${district.name}] (Pillaged)" else district.name
        val nameLabel = nameText.toLabel()
        if (tile.districtIsPillaged) nameLabel.color = BaseScreen.skinStrings.skinConfig.baseColor
        row.add(nameLabel).padRight(6f)
        val buildingsInDistrict = city.cityConstructions.getBuiltBuildings()
            .filter { it.district == district.name }
        if (buildingsInDistrict.none())
            row.add("[${district.name}] (empty)".toLabel().apply { color = BaseScreen.skinStrings.skinConfig.baseColor })
        else
            for (building in buildingsInDistrict)
                row.add(ImageGetter.getConstructionPortrait(building.name, 24f)).size(24f).padRight(3f)
        return row
    }
}
