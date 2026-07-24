package com.unciv.ui.screens.worldscreen.unit.actions

import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.logic.map.tile.Tile
import com.unciv.models.UnitAction
import com.unciv.models.UnitActionType
import com.unciv.models.translations.tr

object UnitActionsCombine {

    internal fun getFormCorpsActions(unit: MapUnit, tile: Tile) = sequence {
        if (unit.formationLevel != 0) return@sequence
        if (!unit.isMilitary()) return@sequence
        if (!unit.civ.civics.isResearched("Nationalism")) return@sequence
        if (unit.baseUnit.isWaterUnit) return@sequence

        val partner = findCombinePartner(unit, tile) ?: return@sequence
        val goldCost = getCombineGoldCost(unit)

        yield(UnitAction(
            UnitActionType.FormCorps, 70f,
            title = "Form Corps (${goldCost.tr()} Gold)",
            action = {
                unit.civ.addGold(-goldCost)
                unit.formationLevel = 1
                unit.attacksThisTurn = 0
                unit.currentMovement = unit.currentMovement.coerceAtLeast(unit.getMaxMovement().toFloat() / 2)
                partner.destroy()
                unit.civ.addNotification("${unit.name} formed into a Corps!",
                    NotificationCategory.General)
            }.takeIf {
                unit.hasMovement() && unit.civ.gold >= goldCost && partner.hasMovement()
                    && !partner.isEmbarked() && !unit.isEmbarked()
            }
        ))
    }

    internal fun getFormArmyActions(unit: MapUnit, tile: Tile) = sequence {
        if (unit.formationLevel != 1) return@sequence
        if (!unit.isMilitary()) return@sequence
        if (!unit.civ.civics.isResearched("Mobilization")) return@sequence
        if (unit.baseUnit.isWaterUnit) return@sequence

        val partner = findCombinePartner(unit, tile) ?: return@sequence
        if (partner.formationLevel != 1) return@sequence
        val goldCost = getCombineGoldCost(unit) * 2

        yield(UnitAction(
            UnitActionType.FormArmy, 65f,
            title = "Form Army (${goldCost.tr()} Gold)",
            action = {
                unit.civ.addGold(-goldCost)
                unit.formationLevel = 2
                unit.attacksThisTurn = 0
                unit.currentMovement = unit.currentMovement.coerceAtLeast(unit.getMaxMovement().toFloat() / 2)
                partner.destroy()
                unit.civ.addNotification("${unit.name} formed into an Army!",
                    NotificationCategory.General)
            }.takeIf {
                unit.hasMovement() && unit.civ.gold >= goldCost && partner.hasMovement()
                    && !partner.isEmbarked() && !unit.isEmbarked()
            }
        ))
    }

    private fun findCombinePartner(unit: MapUnit, tile: Tile): MapUnit? {
        val allUnitsOnTile = sequence {
            if (tile.militaryUnit != null && tile.militaryUnit != unit) yield(tile.militaryUnit!!)
            if (tile.civilianUnit != null && tile.civilianUnit != unit) yield(tile.civilianUnit!!)
            yieldAll(tile.airUnits.filter { it != unit })
        }
        return allUnitsOnTile.firstOrNull {
            it.owner == unit.owner
                && it.isMilitary()
                && it.name == unit.name
                && it.formationLevel == unit.formationLevel
        }
    }

    private fun getCombineGoldCost(unit: MapUnit): Int {
        val eraNumber = unit.civ.getEra().eraNumber
        return 50 + eraNumber * 20
    }
}
