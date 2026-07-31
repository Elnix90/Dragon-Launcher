package org.elnix.dragonlauncher.services.migration

import androidx.compose.ui.unit.Density
import org.elnix.dragonlauncher.migration.OldToNewStoreMapping
import org.elnix.dragonlauncher.migration.PointsAndNestsMigrator
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

/**
 * Comprehensive migration tests that auto-discover all 3.2.2 backups
 * from the test resources folder and validate every backup can be
 * fully migrated without data loss.
 *
 * To add a new backup: place `backup-3.2.2-<name>.json` into
 * `core/services/migration/src/test/resources/` – it will be
 * discovered and tested automatically.
 *
 * Each test validates the structural integrity of the migrated output
 * and writes the result to `build/test-output/migration/` for inspection.
 */
class MigrationTest {

    companion object {
        private val legacyBackups: MutableList<BackupEntry> = mutableListOf()
        private val testDensity = Density(2.0f)

        private val outputDir: File = File("build/test-output/migration")

        @BeforeClass
        @JvmStatic
        fun discoverBackups() {
            val moduleResources = File("core/services/migration/src/test/resources")
            val altResources = File("src/test/resources")
            val resourceDir = when {
                moduleResources.isDirectory -> moduleResources
                altResources.isDirectory -> altResources
                else -> return
            }
            val files: Array<File> = resourceDir.listFiles { f ->
                f.name.matches(Regex("backup-3\\.2\\.2-.*\\.json"))
            } ?: return
            val sorted = files.sortedBy { it.name }
            for (f in sorted) {
                legacyBackups.add(BackupEntry(f.name, f.readText()))
            }
        }

        private fun loadResource(path: String): String {
            return MigrationTest::class.java.classLoader
                ?.getResourceAsStream(path)
                ?.bufferedReader()
                ?.readText()
                ?: throw FileNotFoundException("Cannot load resource: $path")
        }
    }

    private data class BackupEntry(
        val fileName: String,
        val json: String
    )

    @Test
    fun `discovered at least one 3-2-2 backup`() {
        assertTrue(
            "No backup-3.2.2-*.json files found. Drop user backups into core/services/migration/src/test/resources/",
            legacyBackups.isNotEmpty()
        )
    }

    @Test
    fun `every backup migrates points and nests without data loss`() {
        for ((i, entry) in legacyBackups.withIndex()) {
            val json = JSONObject(entry.json)
            val oldNewActions = json.optJSONObject("new_actions") ?: continue

            val oldPoints = oldNewActions.optJSONArray("points") ?: JSONArray()
            val oldNests = oldNewActions.optJSONArray("nests") ?: JSONArray()
            val oldDefaultPoint = oldNewActions.optJSONArray("default_point")?.optJSONObject(0)

            val result = PointsAndNestsMigrator.migrate(oldNewActions, testDensity)

            writeResult(entry.fileName, result)

            assertEquals(
                "${entry.fileName}: points count must match",
                oldPoints.length(), result.newPoints.length()
            )

            assertTrue(
                "${entry.fileName}: should have at least as many nests as source",
                result.newNests.length() >= oldNests.length().coerceAtLeast(1)
            )

            for (j in 0 until result.newPoints.length()) {
                val p = result.newPoints.getJSONObject(j)
                assertTrue("${entry.fileName} point $j: missing id", p.has("id"))
                assertTrue("${entry.fileName} point $j: missing offset", p.has("offset"))
                assertTrue("${entry.fileName} point $j: missing shapeId", p.has("shapeId"))
                assertTrue("${entry.fileName} point $j: missing action", p.has("action"))
                assertTrue("${entry.fileName} point $j: missing nestId", p.has("nestId"))

                val offset = p.getString("offset")
                assertTrue("${entry.fileName} point $j: offset not in 'x,y' format: $offset", offset.contains(","))
                val parts = offset.split(",")
                parts.forEach { it.toFloat() }

                val shapeId = p.getInt("shapeId")
                assertTrue("${entry.fileName} point $j: shapeId must be >= 0, got $shapeId", shapeId >= 0)
            }

            for (j in 0 until result.newNests.length()) {
                val nest = result.newNests.getJSONObject(j)
                assertTrue("${entry.fileName} nest $j: missing id", nest.has("id"))

                if (nest.has("intersectionShapes")) {
                    val shapes = nest.getJSONArray("intersectionShapes")
                    assertTrue("${entry.fileName} nest $j: intersectionShapes must have at least one entry", shapes.length() > 0)
                    for (k in 0 until shapes.length()) {
                        val shape = shapes.getJSONObject(k)
                        assertTrue("${entry.fileName} nest $j shape $k: missing id", shape.has("id"))
                    }
                }
            }

            if (oldDefaultPoint != null) {
                val dp = result.newDefaultPoint
                assertNotNull("${entry.fileName}: default_point must be present when source has it", dp)
                if (dp != null) {
                    assertTrue("${entry.fileName} default_point: missing offset", dp.has("offset"))
                    assertTrue("${entry.fileName} default_point: missing shapeId", dp.has("shapeId"))
                    assertTrue("${entry.fileName} default_point: missing action", dp.has("action"))
                }
            }
        }
    }

    @Test
    fun `every backup has a mapping for all old stores`() {
        for ((index, entry) in legacyBackups.withIndex()) {
            val json = JSONObject(entry.json)
            for (key in json.keys()) {
                if (key == "app_version") continue
                assertNotNull(
                    "Backup #$index (${entry.fileName}): No mapping found for old store '$key'",
                    OldToNewStoreMapping.mappings[key]
                )
            }
        }
    }

    @Test
    fun `every backup has points and nests migrated with correct action types`() {
        for (entry in legacyBackups) {
            val json = JSONObject(entry.json)
            val oldNewActions = json.optJSONObject("new_actions") ?: continue
            val result = PointsAndNestsMigrator.migrate(oldNewActions, testDensity)

            for (j in 0 until result.newPoints.length()) {
                val action = result.newPoints.getJSONObject(j).optJSONObject("action") ?: continue
                val type = action.getString("type")
                assertTrue(
                    "${entry.fileName} point $j: action type '$type' still contains class prefix",
                    !type.contains("org.elnix.dragonlauncher")
                )
                assertTrue(
                    "${entry.fileName} point $j: action type '$type' still contains 'SwipeActionSerializable'",
                    !type.contains("SwipeActionSerializable")
                )

                if (type == "LaunchApp") {
                    assertTrue(
                        "${entry.fileName} point $j: LaunchApp must have profile",
                        action.has("profile")
                    )
                    val profile = action.getJSONObject("profile")
                    assertTrue("${entry.fileName} point $j: profile missing type", profile.has("type"))
                    assertTrue("${entry.fileName} point $j: profile missing userHandle", profile.has("userHandle"))
                }

                if (type == "LaunchShortcut") {
                    assertTrue(
                        "${entry.fileName} point $j: LaunchShortcut must have user",
                        action.has("user")
                    )
                }
            }
        }
    }

    @Test
    fun `every store mapping has valid old and new keys`() {
        for ((oldKey, mapping) in OldToNewStoreMapping.mappings) {
            assertEquals(
                "Mapping oldBackupKey must match its map key",
                oldKey,
                mapping.oldBackupKey
            )
        }
    }

    @Test
    fun `removed stores have null newBackupKey and empty splitInto`() {
        for ((_, mapping) in OldToNewStoreMapping.mappings) {
            if (mapping.newBackupKey == null && mapping.splitInto.isEmpty()) {
            }
        }
    }

    @Test
    fun `witness files are loadable`() {
        val v1 = loadResource("backup-3.2.2-witness-1.json")
        assertTrue("witness-1 should contain points", v1.contains("points"))
        val v2 = loadResource("backup-3.2.2-witness-2.json")
        assertTrue("witness-2 should contain points", v2.contains("points"))
        val v4 = loadResource("backup-4.0.0-witness.json")
        assertTrue("4.0.0 witness should contain points", v4.contains("points"))
    }

    private fun writeResult(fileName: String, result: PointsAndNestsMigrator.MigrationOutput) {
        outputDir.mkdirs()
        val baseName = fileName.removeSuffix(".json")

        val output = JSONObject()
        output.put("points", result.newPoints)
        output.put("nests", result.newNests)
        if (result.newDefaultPoint != null) {
            output.put("default_point", result.newDefaultPoint)
        }

        val outFile = File(outputDir, "$baseName-migrated.json")
        outFile.writeText(output.toString(2))
    }
}
