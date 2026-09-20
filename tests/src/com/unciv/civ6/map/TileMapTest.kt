package com.unciv.civ6.map

import com.unciv.civ6.domain.city.HexCoord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TileMapTest {
    @Test
    fun placeDistrict() {
        val map = TileMapV2(10, 10)
        val coord = HexCoord(5, 5)
        assertTrue(map.placeDistrict(coord, "Campus"))
        assertFalse(map.placeDistrict(coord, "Holy Site")) // already occupied
        val tile = map.get(coord)!!
        assertEquals("Campus", tile.district)
    }

    @Test
    fun districtOnResourceLosesResource() {
        val map = TileMapV2(5, 5)
        val coord = HexCoord(2, 2)
        val tile = map.get(coord)!!
        tile.resource = "Iron"
        tile.resourceType = ResourceType.Strategic
        map.placeDistrict(coord, "Campus")
        assertEquals(null, tile.resource)
        assertEquals(ResourceType.None, tile.resourceType)
    }

    @Test
    fun harborRequiresCoast() {
        val map = TileMapV2(5, 5)
        val land = HexCoord(1, 1)
        map.get(land)!!.terrain = TerrainType.Grassland
        assertFalse(map.placeDistrict(land, "Harbor", setOf("Coast")))
        val coast = HexCoord(2, 2)
        map.get(coast)!!.terrain = TerrainType.Coast
        assertTrue(map.placeDistrict(coast, "Harbor", setOf("Coast")))
    }

    @Test
    fun cityTilesRadius() {
        val map = TileMapV2(20, 20)
        val center = HexCoord(10, 10)
        val tiles = map.cityTiles(center, 3)
        // Hex radius 3 should have 1+6+12+18=37 tiles
        assertEquals(37, tiles.size)
    }
}
