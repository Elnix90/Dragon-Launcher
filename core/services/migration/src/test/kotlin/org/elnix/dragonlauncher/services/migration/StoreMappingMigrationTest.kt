package org.elnix.dragonlauncher.services.migration

import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.model.serializables.AppOverrideState
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.migration.OldToNewStoreMapping
import org.elnix.dragonlauncher.migration.PointsAndNestsMigrator
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

/**
 * Exercises the mapping helper functions against real 3.2.2 fixture data and
 * verifies the migrated output decodes with the new 4.0.0 models.
 */
class StoreMappingMigrationTest {

    companion object {
        private val fixtures: MutableMap<String, JSONObject> = mutableMapOf()

        @BeforeClass
        @JvmStatic
        fun loadFixtures() {
            for (name in listOf("alyon", "polaris", "sandsalamand", "red_velvet", "witness-1", "witness-2")) {
                val json = StoreMappingMigrationTest::class.java.classLoader
                    ?.getResourceAsStream("backup-3.2.2-$name.json")
                    ?.bufferedReader()?.readText()
                if (json != null) {
                    fixtures[name] = JSONObject(json)
                }
            }
        }
    }

    @Test
    fun `widgets actions and shapes are migrated and decode`() {
        val polaris = fixtures["polaris"] ?: return
        val widgets = OldToNewStoreMapping.migrateWidgetsArray(
            polaris.optJSONArray("widgets") ?: return
        )
        assertTrue(widgets.length() > 0)
        for (i in 0 until widgets.length()) {
            val widget = widgets.getJSONObject(i)
            val action = widget.optJSONObject("action") ?: continue
            val type = action.getString("type")
            assertFalse("action type must not contain class prefix: $type", type.contains("org.elnix.dragonlauncher"))
            assertFalse("action type must not contain SwipeActionSerializable: $type", type.contains("SwipeActionSerializable"))
        }
        // Polaris widgets embed IconShape.RoundedSquare / IconShape.Square shapes.
        for (i in 0 until widgets.length()) {
            val shape = widgets.getJSONObject(i).optJSONObject("shape") ?: continue
            val type = shape.getString("type")
            assertFalse("shape type must not contain IconShape prefix: $type", type.contains("IconShape."))
        }
    }

    @Test
    fun `embedded LaunchApp action gets a profile`() {
        val oldAction = JSONObject(
            """{"type":"org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable.LaunchApp","packageName":"com.example.app","isPrivateSpace":false,"userId":0}"""
        )
        val migrated = PointsAndNestsMigrator.migrateAction(oldAction)
        assertEquals("LaunchApp", migrated.getString("type"))
        val profile = migrated.optJSONObject("profile") ?:  throw AssertionError("LaunchApp must have a profile")
        assertEquals("Personal", profile.getString("type"))
        assertTrue(profile.has("userHandle"))
        assertTrue(profile.has("serial"))
    }

    @Test
    fun `workspaces removedAppIds and types are migrated and decode`() {
        val witness = fixtures["witness-1"] ?: return
        val migrated = OldToNewStoreMapping.migrateWorkspacesArray(
            witness.optJSONObject("workspaces")!!.optJSONArray("workspaces")!!
        )
        val decoded: List<Workspace> = json.decodeFromString(migrated.toString())
        assertEquals(5, decoded.size)
        val user = decoded.first { it.id == "user" }
        assertTrue(
            "removedAppIds must be plain cache-key strings",
            user.removedAppIds.orEmpty().any { it.cacheKey == "org.elnix.dragonlauncher#0" }
        )
        assertFalse(
            "workspace types must be title-cased",
            migrated.toString().contains("\"USER\"") || migrated.toString().contains("\"PRIVATE\"")
        )
    }

    @Test
    fun `old appOverrides become a cache-key object and drop un-migratable icons`() {
        val witness = fixtures["witness-1"] ?: return
        val migrated = OldToNewStoreMapping.migrateAppOverrides(
            witness.optJSONObject("workspaces")!!.opt("appOverrides")
        ) ?: return
        // witness-1 only contains old-style ICON_PACK customIcons, which are dropped,
        // so no overrides should survive.
        assertNull(
            "old-style ICON_PACK overrides must be dropped",
            migrated.takeIf { it.length() > 0 }
        )

        // A mixed entry with a customName must survive, keyed by cache key.
        val mixed = OldToNewStoreMapping.migrateAppOverrides(
            JSONObject(
                """[{"cacheKey":"app.morphe.android.youtube#0"},{"customName":"YouTube"},{"cacheKey":"com.example.app#0"},{"customIcon":{"type":"ICON_PACK","source":"x,com.y"}}]"""
            )
        )
        assertNotNull(mixed)
        val decoded: AppOverrideState = json.decodeFromString(mixed.toString())
        assertEquals(1, decoded.size)
        assertEquals("YouTube", decoded[org.elnix.dragonlauncher.base.model.serializables.CacheKey("app.morphe.android.youtube", 0)]?.customName)
    }

    @Test
    fun `angle line objects are extracted and decode as CustomObject`() {
        val witness = fixtures["witness-1"] ?: return
        val angleLine = witness.optJSONObject("angle_line") ?: return

        val settings = OldToNewStoreMapping.extractAngleLineSettings(angleLine)
        assertFalse("extracted settings must not contain lineJson", settings.has("lineJson"))
        assertFalse("extracted settings must not contain angleLineJson", settings.has("angleLineJson"))

        for (key in listOf("lineJson", "angleLineJson", "startLineJson", "endLineJson")) {
            val obj = OldToNewStoreMapping.parseCustomObject(angleLine.getString(key))
            assertNotNull("$key should parse", obj)
            val decoded = json.decodeFromString<CustomObject>(obj.toString())
            assertNotNull(decoded)
        }
    }

    @Test
    fun `hold to activate arc is extracted with renames and object decodes`() {
        val witness = fixtures["witness-1"] ?: return
        val hold = witness.optJSONObject("hold_to_activate") ?: return

        val arc = OldToNewStoreMapping.extractHoldToActivateArcSettings(hold)
        assertTrue("rotationPerSecond renamed to rotationsPerSecond", arc.has("rotationsPerSecond"))
        assertTrue("holdMenuEntries2 renamed to holdMenuEntriesJson", arc.has("holdMenuEntriesJson"))
        assertFalse("old key must not survive", arc.has("rotationPerSecond") && arc.has("rotationPerSecond"))

        val customObject = OldToNewStoreMapping.parseCustomObject(hold.getString("holdToActivateArcCustomObject"))
        assertNotNull(customObject)
        val decoded = json.decodeFromString<CustomObject>(customObject.toString())
        assertNotNull(decoded)

        val menu = OldToNewStoreMapping.migrateHoldMenuEntriesString(hold.getString("holdMenuEntries2"))
        assertNotNull(menu)
        assertTrue(
            "hold menu entry must have NavigationRoute prefix stripped",
            menu != null && !menu.contains("NavigationRoute.")
        )
    }

    @Test
    fun `main screen layers string has prefix stripped`() {
        val witness = fixtures["witness-1"] ?: return
        val ui = witness.optJSONObject("ui") ?: return
        val raw = ui.optString("mainScreenLayers", "") ?: return
        if (raw.isEmpty()) return
        val migrated = OldToNewStoreMapping.migrateMainScreenLayersString(raw)
        assertNotNull(migrated)
        assertFalse(
            "main screen layer types must have MainScreenLayer prefix stripped",
            migrated != null && migrated.contains("MainScreenLayer.")
        )
    }
}
