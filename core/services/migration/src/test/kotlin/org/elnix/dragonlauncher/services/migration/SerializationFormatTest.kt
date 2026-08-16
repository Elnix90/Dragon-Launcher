package org.elnix.dragonlauncher.services.migration

import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.model.serializables.AppOverride
import org.elnix.dragonlauncher.base.model.serializables.AppOverrideState
import org.elnix.dragonlauncher.base.model.serializables.CacheKey
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.base.model.serializables.WorkspaceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SerializationFormatTest {

    @Test
    fun `inspect serialization formats`() {
        val overrides: AppOverrideState = mapOf(
            CacheKey("app.morphe.android.youtube", 0) to AppOverride(customName = "YouTube"),
            CacheKey("com.aeroinsta.android", 0) to AppOverride(customName = "AeroInsta")
        )
        println("APP_OVERRIDES ENCODED")
        println(json.encodeToString(overrides))

        val ws = Workspace(
            id = "user",
            type = WorkspaceType.User,
            appIds = setOf(CacheKey("org.elnix.dragonlauncher", 0)),
            removedAppIds = setOf(CacheKey("org.elnix.dragonlauncher", 0)),
            enabled = true
        )
        println("WORKSPACE ENCODED")
        println(json.encodeToString(ws))

        val oldFlat = """[{"cacheKey":"app.morphe.android.youtube#0"},{"customName":"YouTube"},{"cacheKey":"com.aeroinsta.android#0"},{"customName":"AeroInsta"}]"""
        println("OLD FLAT APP_OVERRIDES (decode should FAIL):")
        try {
            json.decodeFromString<AppOverrideState>(oldFlat)
            println("DECODED (unexpected)")
        } catch (e: Exception) {
            println("DECODE FAILED as expected: ${e.message?.take(120)}")
        }

        val objFormat = """{"app.morphe.android.youtube#0":{"customName":"YouTube"},"com.aeroinsta.android#0":{"customName":"AeroInsta"}}"""
        println("OBJECT-KEYED APP_OVERRIDES DECODED: ${json.decodeFromString<AppOverrideState>(objFormat)}")

        val wsOldObjects = """{"id":"user","name":"User","type":"USER","appIds":[],"removedAppIds":[{"cacheKey":"org.elnix.dragonlauncher#0"}],"enabled":true}"""
        println("WS REMOVED-AS-OBJECTS (decode should FAIL):")
        try {
            json.decodeFromString<Workspace>(wsOldObjects)
            println("DECODED (unexpected)")
        } catch (e: Exception) {
            println("DECODE FAILED as expected: ${e.message?.take(120)}")
        }

        val wsOldStrings = """{"id":"user","name":"User","type":"USER","appIds":[],"removedAppIds":["org.elnix.dragonlauncher"],"enabled":true}"""
        println("WS REMOVED-AS-STRINGS DECODED: ${json.decodeFromString<Workspace>(wsOldStrings)}")

        val objHex = """{"stroke":2.0,"color":"FFFF0000","glow":{"color":"FFFF7283","radius":12.0},"rotation":90,"shape":{"type":"Circle"},"size":50.0,"eraseBackground":false}"""
        println("OBJ HEX-COLOR DECODED: ${json.decodeFromString<CustomObject>(objHex)}")

        val objOld = """{"stroke":2.0,"color":-65536,"glow":{"color":-36797,"radius":12.0},"rotation":90,"shape":{"type":"Circle"},"size":50.0,"eraseBackground":false}"""
        println("OBJ OLD INT-COLOR (decode should FAIL):")
        try {
            json.decodeFromString<CustomObject>(objOld)
            println("DECODED (unexpected)")
        } catch (e: Exception) {
            println("DECODE FAILED as expected: ${e.message?.take(120)}")
        }
    }

    @Test
    fun `object-keyed app overrides round-trip`() {
        val decoded: AppOverrideState = json.decodeFromString(
            """{"app.morphe.android.youtube#0":{"customName":"YouTube"}}"""
        )
        assertEquals(1, decoded.size)
        assertEquals("YouTube", decoded[CacheKey("app.morphe.android.youtube", 0)]?.customName)
    }

    @Test
    fun `workspace with migrated removed-app-ids strings`() {
        val decoded: Workspace = json.decodeFromString(
            """{"id":"user","name":"User","type":"User","appIds":[],"removedAppIds":["org.elnix.dragonlauncher#0"],"enabled":true}"""
        )
        assertTrue(decoded.removedAppIds.orEmpty().any { it.cacheKey == "org.elnix.dragonlauncher#0" })
    }
}
