package com.unciv.ui.screens.cityscreen

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.unciv.logic.city.City
import com.unciv.logic.map.tile.Tile
import com.unciv.models.ruleset.Building
import com.unciv.models.ruleset.District
import com.unciv.models.stats.Stat
import com.unciv.models.translations.tr
import com.unciv.ui.components.extensions.toLabel
import com.unciv.ui.components.extensions.toTextButton
import com.unciv.ui.components.widgets.ExpanderTab
import com.unciv.ui.images.ImageGetter
import com.unciv.ui.screens.basescreen.BaseScreen


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

            val availableDistricts = getAvailableDistricts()
            for (building in availableDistricts) {
                val districtName = building.getDistrictToCreate(cityScreen.city.getRuleset())?.name ?: continue
                val button = "New [${districtName}]".toTextButton()
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        cityScreen.startPickTileForCreatesOneDistrict(building, Stat.Gold, false)
                    }
                })
                table.add(button).padTop(5f)
                table.row()
            }

            it.add(table).growX()
        }
    }

    private fun getAvailableDistricts(): List<Building> {
        val builtDistrictNames = city.getDistricts().map { it.second.name }.toSet()
        return city.getRuleset().buildings.values.asSequence()
            .filter { it.hasCreateOneDistrictUnique() }
            .filter { building ->
                val district = building.getDistrictToCreate(city.getRuleset()) ?: return@filter false
                district.name !in builtDistrictNames
            }
            .toList()
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
