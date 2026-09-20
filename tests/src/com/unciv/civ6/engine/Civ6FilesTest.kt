package com.unciv.civ6.engine

import com.unciv.civ6.Civ6GameStarter
import com.unciv.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Civ6FilesTest {
    @Test
    fun saveLoadRoundTrip() {
        val info = Civ6GameStarter.newGame()
        val files = Civ6Files()
        val jsonStr = files.gameInfoToString(info)
        val loaded = files.gameInfoFromString(jsonStr)
        assertEquals(info.gameId, loaded.gameId)
        assertEquals(10, loaded.version.number)
    }

    @Test
    fun incompatibleVersion() {
        val info = Civ6GameStarter.newGame()
        info.version = Civ6CompatibilityVersion(11, Civ6Version("future", 9999))
        val jsonStr = json().toJson(info)
        val files = Civ6Files()
        assertFailsWith<IncompatibleCiv6VersionException> {
            files.gameInfoFromString(jsonStr)
        }
    }

    @Test
    fun corruptedNotIncompatible() {
        val files = Civ6Files()
        val corrupted = "{ not valid json"
        // Should throw generic exception, not incompatible, because version can't be parsed
        var threwIncompatible = false
        try { files.gameInfoFromString(corrupted) } catch (e: IncompatibleCiv6VersionException) { threwIncompatible = true } catch (_: Exception) {}
        assertEquals(false, threwIncompatible)
    }
}
