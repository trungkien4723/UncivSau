package com.unciv.logic.map.tile

import com.unciv.logic.civilization.Civilization
import com.unciv.models.ruleset.tile.TerrainType

object TileAppeal {

    fun getAppeal(tile: Tile, civInfo: Civilization? = null): Int {
        var appeal = 0

        // Base terrain
        appeal += getBaseTerrainAppeal(tile)

        // Terrain features
        appeal += getTerrainFeatureAppeal(tile)

        // Improvements
        appeal += getImprovementAppeal(tile)

        // Districts
        appeal += getDistrictAppeal(tile)

        // Adjacent tiles
        for (neighbor in tile.neighbors) {
            if (neighbor.isLand) {
                val adjacentAppeal = getBaseTerrainAppeal(neighbor)
                    + getTerrainFeatureAppeal(neighbor)
                    + getImprovementAppeal(neighbor)
                    + getDistrictAppeal(neighbor)
                    + getNaturalWonderAppeal(neighbor)
                appeal += (adjacentAppeal / 2)
            }
        }

        return appeal.coerceIn(-5, 5)
    }

    private fun getBaseTerrainAppeal(tile: Tile): Int {
        if (tile.naturalWonder != null) return 3
        if (tile.matchesTerrainFilter("Mountain", null)) return 2
        if (tile.matchesTerrainFilter("Coast", null)) return 1
        if (tile.matchesTerrainFilter("Ocean", null)) return 0
        if (tile.isLand) return 0
        return 0
    }

    private fun getNaturalWonderAppeal(tile: Tile): Int {
        return if (tile.naturalWonder != null) 2 else 0
    }

    private fun getTerrainFeatureAppeal(tile: Tile): Int {
        if (tile.terrainFeatures.isEmpty()) return 0
        var appeal = 0
        for (feature in tile.terrainFeatures) {
            appeal += when (feature) {
                "Forest" -> 1
                "Jungle" -> 1
                "Marsh" -> -1
                "Flood plains" -> 0
                "Oasis" -> 1
                "Ice" -> 0
                "Reef" -> 0
                "Atoll" -> 1
                else -> 0
            }
        }
        return appeal
    }

    private fun getImprovementAppeal(tile: Tile): Int {
        val improvement = tile.getUnpillagedTileImprovement() ?: return 0
        return when (improvement.name) {
            "Mine" -> -1
            "Quarry" -> -1
            "Oil well" -> -1
            "Offshore Platform" -> -1
            "Fort" -> -1
            "Seaside Resort" -> 0
            "National Park" -> 2
            else -> 0
        }
    }

    private fun getDistrictAppeal(tile: Tile): Int {
        if (tile.district == null) return 0
        return when (tile.district) {
            "Holy Site" -> 1
            "Theater Square" -> 1
            "Entertainment Complex" -> 1
            "Water Park" -> 1
            "Encampment" -> -1
            "Industrial Zone" -> -1
            "Aerodrome" -> -1
            "Spaceport" -> -1
            else -> 0
        }
    }

    fun getLabel(appeal: Int): String = when {
        appeal >= 4 -> "Breathtaking"
        appeal >= 2 -> "Charming"
        appeal >= 0 -> "Average"
        appeal >= -2 -> "Unappealing"
        else -> "Disgusting"
    }
}
