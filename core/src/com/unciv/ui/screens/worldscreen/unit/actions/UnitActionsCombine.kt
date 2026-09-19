package com.unciv.ui.screens.worldscreen.unit.actions

import com.unciv.logic.civilization.NotificationCategory
import com.unciv.logic.map.mapunit.MapUnit
import com.unciv.logic.map.tile.Tile
import com.unciv.models.UnitAction
import com.unciv.models.UnitActionType
import com.unciv.models.translations.tr

object UnitActionsCombine {

    fun getFormCorpsActions(unit: MapUnit, tile: Tile) = sequence {
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

    fun getFormArmyActions(unit: MapUnit, tile: Tile) = sequence {
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

    // Naval formations: Fleet (Corps equivalent) and Armada (Army equivalent)
    fun getFormFleetActions(unit: MapUnit, tile: Tile) = sequence {
        if (unit.formationLevel != 0) return@sequence
        if (!unit.isMilitary()) return@sequence
        if (!unit.civ.civics.isResearched("Nationalism")) return@sequence
        if (!unit.baseUnit.isWaterUnit) return@sequence

        val partner = findCombinePartner(unit, tile) ?: return@sequence
        val goldCost = getCombineGoldCost(unit)

        yield(UnitAction(
            UnitActionType.FormFleet, 70f,
            title = "Form Fleet (${goldCost.tr()} Gold)",
            action = {
                unit.civ.addGold(-goldCost)
                unit.formationLevel = 1
                unit.attacksThisTurn = 0
                unit.currentMovement = unit.currentMovement.coerceAtLeast(unit.getMaxMovement().toFloat() / 2)
                partner.destroy()
                unit.civ.addNotification("${unit.name} formed into a Fleet!",
                    NotificationCategory.General)
            }.takeIf {
                unit.hasMovement() && unit.civ.gold >= goldCost && partner.hasMovement()
                    && !partner.isEmbarked() && !unit.isEmbarked()
            }
        ))
    }

    fun getFormArmadaActions(unit: MapUnit, tile: Tile) = sequence {
        if (unit.formationLevel != 1) return@sequence
        if (!unit.isMilitary()) return@sequence
        if (!unit.civ.civics.isResearched("Mobilization")) return@sequence
        if (!unit.baseUnit.isWaterUnit) return@sequence

        val partner = findCombinePartner(unit, tile) ?: return@sequence
        if (partner.formationLevel != 1) return@sequence
        val goldCost = getCombineGoldCost(unit) * 2

        yield(UnitAction(
            UnitActionType.FormArmada, 65f,
            title = "Form Armada (${goldCost.tr()} Gold)",
            action = {
                unit.civ.addGold(-goldCost)
                unit.formationLevel = 2
                unit.attacksThisTurn = 0
                unit.currentMovement = unit.currentMovement.coerceAtLeast(unit.getMaxMovement().toFloat() / 2)
                partner.destroy()
                unit.civ.addNotification("${unit.name} formed into an Armada!",
                    NotificationCategory.General)
            }.takeIf {
                unit.hasMovement() && unit.civ.gold >= goldCost && partner.hasMovement()
                    && !partner.isEmbarked() && !unit.isEmbarked()
            }
        ))
    }

    /**
     * Civ 6 forms corps from units stacked on one tile, but this engine enforces 1 military
     * unit per tile ([com.unciv.logic.map.mapunit.movement.UnitMovement] `TileIsNotEmpty`).
     * So we look for a matching partner on the unit's own tile **or any adjacent tile** - the
     * two units "merge" and the partner is consumed.
     */
    fun findCombinePartner(unit: MapUnit, tile: Tile): MapUnit? {
        val candidateTiles = sequence {
            yield(tile)
            yieldAll(tile.neighbors)
        }
        return candidateTiles.mapNotNull { it.militaryUnit }
            .firstOrNull {
                it != unit
                    && it.owner == unit.owner
                    && it.isMilitary()
                    && it.name == unit.name
                    && it.formationLevel == unit.formationLevel
            }
    }

    fun getCombineGoldCost(unit: MapUnit): Int {
        val eraNumber = unit.civ.getEra().eraNumber
        return 50 + eraNumber * 20
    }
}
