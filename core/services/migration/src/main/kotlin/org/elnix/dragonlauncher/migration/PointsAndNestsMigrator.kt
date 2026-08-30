package org.elnix.dragonlauncher.migration

import androidx.compose.ui.unit.Density
import org.elnix.dragonlauncher.migration.PointsAndNestsMigrator.migrateAction
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Converts points, nests, and the default point from the old 3.2.2 format
 * (using UUIDs, circle numbers, angle degrees) to the new 4.0.0 format
 * (using integer IDs, shape IDs, and offset strings).
 *
 * This migrator is invoked as a pre-processing step by [LegacyBackupJsonMigrator]
 * when it encounters a `new_actions` store in the old backup JSON.
 */
internal object PointsAndNestsMigrator {
    /**
     * Migrates all points, nests, and the default point from a single
     * `new_actions` JSON object.
     *
     * @param newActions The `new_actions` section of the legacy backup.
     * @param density The screen density factor for pixel-to-DP conversions.
     * @return [MigrationOutput] containing the migrated arrays/objects.
     */
    fun migrate(
        newActions: JSONObject,
        density: Density
    ): MigrationOutput {
        val oldPoints = newActions.optJSONArray("points") ?: JSONArray()
        val oldNests = newActions.optJSONArray("nests") ?: JSONArray()
        val oldDefaultPoint = newActions.optJSONArray("default_point")?.optJSONObject(0)

        val idMap = buildIdMap(oldPoints)

        val newNests = migrateNests(oldNests, density)
        val newPoints = migratePoints(oldPoints, idMap, density)
        val newDefaultPoint =
            if (oldDefaultPoint != null) {
                migrateSinglePoint(oldDefaultPoint, emptyMap(), density)
            } else {
                null
            }

        return MigrationOutput(
            newPoints = newPoints,
            newNests = newNests,
            newDefaultPoint = newDefaultPoint
        )
    }

    /**
     * The output of a single migration run: re-indexed points, nests,
     * and an optional default point.
     *
     * @property newPoints Migrated points array.
     * @property newNests Migrated nests array.
     * @property newDefaultPoint Migrated default point, or `null` if no default existed.
     */
    data class MigrationOutput(
        val newPoints: JSONArray,
        val newNests: JSONArray,
        val newDefaultPoint: JSONObject?
    )

    /**
     * Assigns a sequential integer ID to each unique point UUID found in
     * the old points array. UUIDs are replaced by integer IDs in the new format.
     *
     * @param oldPoints The old points array (each entry has a `"id"` string UUID).
     * @return Map from UUID -> new integer ID.
     */
    private fun buildIdMap(
        oldPoints: JSONArray
    ): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        var nextId = 0

        for (i in 0 until oldPoints.length()) {
            val uuid = oldPoints.getJSONObject(i).optString("id", "")
            if (uuid.isBlank() || uuid in map) continue
            val id = nextId++
            map[uuid] = id
        }
        return map
    }

    /**
     * Migrates every nest in the old nests array.
     *
     * @param oldNests The old nests array.
     * @param density Screen density for pixel-to-DP conversion.
     * @return Migrated nests array.
     */
    private fun migrateNests(oldNests: JSONArray, density: Density): JSONArray {
        val result = JSONArray()
        for (i in 0 until oldNests.length()) {
            result.put(migrateNest(oldNests.getJSONObject(i), density))
        }
        return result
    }

    /**
     * Converts a single old nest to the new format.
     *
     * Transformations:
     * - `showCircle` -> `showCurrentShape`
     * - `dragDistances[-1]` -> `cancelZone` (pixels -> DP)
     * - `dragDistances[0..N]` -> `intersectionShapes` with ratio-based scales
     *
     * @param oldNest The old nest JSON object.
     * @param density Screen density for pixel-to-DP conversion.
     * @return Migrated nest JSON object.
     */
    private fun migrateNest(oldNest: JSONObject, density: Density): JSONObject {
        val newNest = JSONObject()

        val id = oldNest.optInt("id", 0)
        if (id != -1) newNest.put("id", id)

        val name = oldNest.optString("name", "")
        if (name.isNotBlank()) newNest.put("name", name)

        migrateDragDistances(oldNest, newNest, density)

        if (oldNest.has("showCircle")) {
            newNest.put("showCurrentShape", oldNest.getBoolean("showCircle"))
        }

        return newNest
    }

    /**
     * Converts old `dragDistances` map to new `cancelZone` and `intersectionShapes`.
     *
     * - Key `-1` becomes `cancelZone` (converted from pixels to DP).
     * - Keys `0..N` become `intersectionShapes` with a `scale` ratio relative to the first circle.
     *
     * @param oldNest The old nest JSON object (source of `dragDistances`).
     * @param newNest The new nest JSON object (receives `cancelZone` and `intersectionShapes`).
     * @param density Screen density for pixel-to-DP conversion.
     */
    private fun migrateDragDistances(oldNest: JSONObject, newNest: JSONObject, density: Density) {
        val dragDistances = oldNest.optJSONObject("dragDistances") ?: return

        val cancelZonePx = dragDistances.optInt("-1", -1)
        if (cancelZonePx >= 0) {
            newNest.put("cancelZone", pixelsToDpValue(cancelZonePx.toFloat(), density).roundToInt())
        }

        val circleKeys =
            dragDistances
                .keys()
                .asSequence()
                .filter { it != "-1" }
                .mapNotNull { it.toIntOrNull() }
                .sorted()
                .toList()

        if (circleKeys.isEmpty()) return

        val shapes = JSONArray()
        for (circleKey in circleKeys) {
            val dist = dragDistances.optInt(circleKey.toString())
            val shape = JSONObject()
            shape.put("id", circleKey)

            val scale = dist / 300f // 300 is the default size of the previous version
            shape.put("scale", scale)
            shapes.put(shape)
        }
        newNest.put("intersectionShapes", shapes)
    }

    /**
     * Migrates every point in the old points array.
     *
     * @param oldPoints The old points array.
     * @param idMap UUID-to-integer-ID mapping.
     * @param density Screen density for pixel-to-DP conversion.
     * @return Migrated points array.
     */
    private fun migratePoints(
        oldPoints: JSONArray,
        idMap: Map<String, Int>,
        density: Density
    ): JSONArray {
        val result = JSONArray()
        for (i in 0 until oldPoints.length()) {
            result.put(migrateSinglePoint(oldPoints.getJSONObject(i), idMap, density))
        }
        return result
    }

    /**
     * Converts a single old point to the new format.
     *
     * Key transformations:
     * - UUID -> integer ID via [idMap]
     * - `circleNumber` -> `shapeId`
     * - `angleDeg` (with −90° offset) -> `offset` string `"x,y"`
     * - `action` -> migrated via [migrateAction]
     * - Colors: int ARGB -> hex `"AARRGGBB"`
     * - Dp values (`borderStroke`, `size`, `innerPadding`): pixels -> DP
     * - `haptic`/`hapticFeedback` -> new haptic format
     * - `liveNestGraceDistancePx` -> `liveNestGraceDistance` (pixels -> DP)
     * - `liveNestMainNestOpacityPercent` -> `liveNestSubNestOpacityPercent`
     * - `showCircle` -> `showCurrentShape`
     * - `customIcon`: only passes through if it matches a known `@SerialName`
     * - Old-only fields (`cornerRadius`, `resolution`, `cycleActionStageDefaultDelay`, `minAngleActivation`, `nestRadius`) are dropped
     *
     * @param oldPoint The old point JSON object.
     * @param idMap UUID-to-integer-ID mapping.
     * @param density Screen density for pixel-to-DP conversion.
     * @return Migrated point JSON object.
     */
    private fun migrateSinglePoint(
        oldPoint: JSONObject,
        idMap: Map<String, Int>,
        density: Density
    ): JSONObject {
        val newPoint = JSONObject()

        val uuid = oldPoint.optString("id", "")
        newPoint.put("id", if (uuid.isNotBlank()) (idMap[uuid] ?: -1) else -1)

        val circleNumber = oldPoint.optInt("circleNumber", 0)
        newPoint.put("shapeId", circleNumber)

        val angleDeg = oldPoint.optDouble("angleDeg", 0.0)
        newPoint.put("offset", computeOffsetString(angleDeg))

        val nestId = oldPoint.optInt("nestId", 0)
        newPoint.put("nestId", nestId)

        val action = oldPoint.optJSONObject("action")
        if (action != null) {
            newPoint.put("action", migrateAction(action))
        }

        copyIfValidCustomIcon(oldPoint, newPoint)
        copyIfPresent(oldPoint, newPoint, "customName", null)
        copyIfPresent(oldPoint, newPoint, "borderStroke", density)
        copyIfPresent(oldPoint, newPoint, "borderStrokeSelected", density)
        copyIfPresent(oldPoint, newPoint, "opacity", null)
        copyIfPresent(oldPoint, newPoint, "size", null)
        copyIfPresent(oldPoint, newPoint, "innerPadding", null)
        copyIfPresent(oldPoint, newPoint, "liveNestTargetNestId", null)
        copyIfPresent(oldPoint, newPoint, "liveNestPreviewDelayMs", null)
        copyIfPresent(oldPoint, newPoint, "liveNestScale", null)
        copyIfPresent(oldPoint, newPoint, "liveNestSnapsToFingerPosition", null)
        copyIfPresent(oldPoint, newPoint, "cycleActionsLoopDelayMs", null)
        copyIfPresent(oldPoint, newPoint, "cycleActionsLoop", null)
        copyIfPresent(oldPoint, newPoint, "holdAndRunDelayMs", null)
        copyIfPresent(oldPoint, newPoint, "fastActivation", null)
        copyIfPresent(oldPoint, newPoint, "glow", null)
        copyIfPresent(oldPoint, newPoint, "glowSelected", null)
        copyIntColor(oldPoint, newPoint, "borderColor")
        copyIntColor(oldPoint, newPoint, "borderColorSelected")
        copyIntColor(oldPoint, newPoint, "backgroundColor")
        copyIntColor(oldPoint, newPoint, "backgroundColorSelected")
        copyIntColor(oldPoint, newPoint, "customActionColor")

        if (oldPoint.has("liveNestGraceDistancePx")) {
            val px =
                when (val raw = oldPoint.get("liveNestGraceDistancePx")) {
                    is Number -> raw.toFloat()
                    else -> null
                }
            if (px != null) {
                newPoint.put("liveNestGraceDistance", pixelsToDpValue(px, density))
            }
        }
        if (oldPoint.has("liveNestMainNestOpacityPercent")) {
            newPoint.put("liveNestSubNestOpacityPercent", oldPoint.getInt("liveNestMainNestOpacityPercent"))
        }

        val borderShape = oldPoint.optJSONObject("borderShape")
        if (borderShape != null) {
            newPoint.put("borderShape", migrateIconShape(borderShape))
        }
        val borderShapeSelected = oldPoint.optJSONObject("borderShapeSelected")
        if (borderShapeSelected != null) {
            newPoint.put("borderShapeSelected", migrateIconShape(borderShapeSelected))
        }

        val haptic =
            oldPoint.optJSONObject("haptic")
                ?: oldPoint.optJSONObject("hapticFeedback")
        if (haptic != null) {
            newPoint.put("haptic", migrateHaptic(haptic))
        }

        val cycleActions = oldPoint.optJSONArray("cycleActions")
        if (cycleActions != null) {
            newPoint.put("cycleActions", migrateCycleActions(cycleActions))
        }

        val holdAndRunAction = oldPoint.optJSONObject("holdAndRunAction")
        if (holdAndRunAction != null) {
            newPoint.put("holdAndRunAction", migrateAction(holdAndRunAction))
        }

        return newPoint
    }

    /**
     * Computes the `"x,y"` offset string from an angle in degrees.
     *
     * The offset is calculated on a circle of radius 100.
     *
     * @param angleDeg The angle in degrees (0° = right, trigonometric).
     * @return String in the format `"x,y"` with two decimal places.
     */
    private fun computeOffsetString(angleDeg: Double): String {
        // need to subtract 90 because the new angle calculation starts at the 0 of the trigonometric circle
        val rad = Math.toRadians(angleDeg - 90.0)
        val x = cos(rad) * 100.0
        val y = sin(rad) * 100.0
        return "%.2f,%.2f".format(x, y)
    }

    /**
     * Strips the full class path prefix from an old `IconShape` type string.
     *
     * Example: `"org.elnix.dragonlauncher.common.serializables.IconShape.Circle"`
     * becomes `"Circle"`.
     *
     * @param oldShape The old shape JSON object.
     * @return New shape JSON object with stripped type.
     */
    private fun migrateIconShape(oldShape: JSONObject): JSONObject {
        val newShape = JSONObject()
        val oldType = oldShape.optString("type", "Circle")
        val newType = oldType.removePrefix("org.elnix.dragonlauncher.common.serializables.IconShape.")
        newShape.put("type", newType)
        return newShape
    }

    /**
     * Converts an old action JSON to the new format.
     *
     * Transformations:
     * - Strips `SwipeActionSerializable.` prefix from the type.
     * - `LaunchApp`: adds a `profile` object with `type`, `userHandle`, `serial`.
     * - `LaunchShortcut`: adds a `user` field.
     * - `ToggleWifi`/`ToggleBluetooth`/`ToggleData`/`RunAdbCommand`: copies `command` and `toast`.
     *
     * @param oldAction The old action JSON object.
     * @return Migrated action JSON object.
     */
    internal fun migrateAction(oldAction: JSONObject): JSONObject {
        val newAction = JSONObject()
        val oldType = oldAction.optString("type", "None")
        val newType = oldType.removePrefix("org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable.")
        newAction.put("type", newType)

        when (newType) {
            "LaunchApp" -> {
                copyIfPresent(oldAction, newAction, "packageName", null)
                val isPrivate = oldAction.optBoolean("isPrivateSpace", false)
                val userId = oldAction.optInt("userId", 0)
                val profile = JSONObject()
                profile.put("type", if (isPrivate) "Private" else "Personal")
                profile.put("userHandle", userId)
                profile.put("serial", userId.toLong())
                newAction.put("profile", profile)
                copyIfPresent(oldAction, newAction, "timerDuration", null)
            }

            "LaunchShortcut" -> {
                copyIfPresent(oldAction, newAction, "packageName", null)
                copyIfPresent(oldAction, newAction, "shortcutId", null)
                val userId = oldAction.optInt("userId", 0)
                newAction.put("user", userId)
            }

            "OpenCircleNest" -> {
                if (oldAction.has("nestId")) {
                    newAction.put("nestId", oldAction.getInt("nestId"))
                }
            }

            "OpenFile" -> {
                copyIfPresent(oldAction, newAction, "uri", null)
                copyIfPresent(oldAction, newAction, "mimeType", null)
            }

            "OpenUrl" -> {
                copyIfPresent(oldAction, newAction, "url", null)
            }

            "OpenWidget" -> {
                copyIfPresent(oldAction, newAction, "widgetId", null)
                copyIfPresent(oldAction, newAction, "providerPackage", null)
                copyIfPresent(oldAction, newAction, "providerClass", null)
            }

            "OpenAppDrawer" -> {
                copyIfPresent(oldAction, newAction, "workspaceId", null)
            }

            "OpenDragonLauncherSettings" -> {
                val oldRoute = oldAction.optJSONObject("route")
                if (oldRoute != null) {
                    newAction.put("route", migrateRoute(oldRoute))
                }
            }

            "ToggleWifi", "ToggleBluetooth", "ToggleData" -> {
                copyIfPresent(oldAction, newAction, "command", null)
                copyIfPresent(oldAction, newAction, "toast", null)
            }

            "RunAdbCommand" -> {
                copyIfPresent(oldAction, newAction, "command", null)
                copyIfPresent(oldAction, newAction, "toast", null)
            }
        }

        return newAction
    }

    /**
     * Strips the full class path prefix from an old `NavigationRoute` type string.
     *
     * Example: `"org.elnix.dragonlauncher.common.navigaton.NavigationRoute.Settings"`
     * becomes `"Settings"`.
     *
     * Also preserves additional fields (e.g. `initialNestId` for `PointsSettings`).
     *
     * @param oldRoute The old route JSON object.
     * @return New route JSON object with stripped type.
     */
    private fun migrateRoute(oldRoute: JSONObject): JSONObject {
        val newRoute = JSONObject()
        val oldType = oldRoute.optString("type", "PointsSettings")
        val newType = oldType.substringAfterLast("NavigationRoute.")
        newRoute.put("type", newType)
        for (key in oldRoute.keys()) {
            if (key != "type") {
                newRoute.put(key, oldRoute.get(key))
            }
        }
        return newRoute
    }

    /**
     * Converts old haptic feedback JSON to the new format.
     *
     * The old format uses `"first"`/`"second"` (Pair serialization from kotlinx),
     * while the new format uses `"isVibration"`/`"durationMs"`.
     *
     * @param oldHaptic The old haptic JSON object.
     * @return Migrated haptic JSON object.
     */
    private fun migrateHaptic(oldHaptic: JSONObject): JSONObject {
        val newHaptic = JSONObject()
        val oldHaptics = oldHaptic.optJSONArray("haptics") ?: return newHaptic
        val newHaptics = JSONArray()
        for (i in 0 until oldHaptics.length()) {
            val oldEntry = oldHaptics.getJSONObject(i)
            val newEntry = JSONObject()
            newEntry.put("isVibration", oldEntry.optBoolean("first", true))
            newEntry.put("durationMs", oldEntry.optInt("second", 100))
            newHaptics.put(newEntry)
        }
        newHaptic.put("haptics", newHaptics)
        return newHaptic
    }

    /**
     * Migrates the cycle actions array by converting each stage.
     *
     * Each stage's action and haptic feedback are individually migrated.
     *
     * @param oldCycleActions The old cycle actions array.
     * @return Migrated cycle actions array.
     */
    private fun migrateCycleActions(oldCycleActions: JSONArray): JSONArray {
        val newCycleActions = JSONArray()
        for (i in 0 until oldCycleActions.length()) {
            val oldStage = oldCycleActions.getJSONObject(i)
            val newStage = JSONObject()
            copyIfPresent(oldStage, newStage, "triggerTimeMs", null)
            val action = oldStage.optJSONObject("action")
            if (action != null) {
                newStage.put("action", migrateAction(action))
            }
            val haptic = oldStage.optJSONObject("hapticFeedback")
            if (haptic != null) {
                newStage.put("hapticFeedback", migrateHaptic(haptic))
            }
            newCycleActions.put(newStage)
        }
        return newCycleActions
    }

    /**
     * Converts an integer ARGB color value to a hex string in `"AARRGGBB"` format.
     *
     * @param color The integer color.
     * @return Upper-case hex string.
     */
    private fun intToHexColor(color: Int): String = "%08X".format(color.toLong() and 0xFFFFFFFFL)

    /**
     * Copies a field from source to target, optionally converting pixel values to DP.
     *
     * When [useDensity] is non-null and the source value is a [Number], the value
     * is converted from pixels to density-independent pixels before writing.
     *
     * @param source Source JSON object.
     * @param target Target JSON object.
     * @param key The field name to copy.
     * @param useDensity Screen density for pixel-to-DP conversion, or `null` to copy verbatim.
     */
    private fun copyIfPresent(
        source: JSONObject,
        target: JSONObject,
        key: String,
        useDensity: Density?
    ) {
        if (source.has(key)) {
            val value = source.get(key)
            if (useDensity != null && value is Number) {
                target.put(key, pixelsToDpValue(value.toFloat(), useDensity))
            } else {
                target.put(key, value)
            }
        }
    }

    /**
     * Copies `customIcon` from source to target only if it is a valid
     * `CustomIcon` in the new format (i.e. a non-empty JSONObject whose
     * `type` matches one of the known `@SerialName` values).
     *
     * Empty objects `{}` and old-format icons (e.g., `{"type":"ICON_PACK",...}`)
     * are silently dropped to avoid deserialization failures.
     *
     * @param source Source JSON object.
     * @param target Target JSON object.
     */
    private fun copyIfValidCustomIcon(source: JSONObject, target: JSONObject) {
        if (!source.has("customIcon")) return
        val value = source.get("customIcon")
        if (value !is JSONObject) return
        if (value.length() == 0) return
        val type = value.optString("type", "")
        if (type != "CustomIconPackIcon" &&
            type != "AdaptifiedLegacyIcon" &&
            type != "CustomThemedIcon" &&
            type != "ForceThemedIcon" &&
            type != "UnmodifiedSystemDefaultIcon" &&
            type != "CustomTextIcon" &&
            type != "DefaultPlaceholderIcon"
        ) {
            return
        }
        target.put("customIcon", value)
    }

    /**
     * Copies a color field from source to target, converting from int ARGB
     * to hex `"AARRGGBB"` string format.
     *
     * @param source Source JSON object.
     * @param target Target JSON object.
     * @param key The color field name to copy.
     */
    private fun copyIntColor(source: JSONObject, target: JSONObject, key: String) {
        if (!source.has(key)) return
        val intValue =
            when (val raw = source.get(key)) {
                is Int -> raw
                is Number -> raw.toInt()
                else -> return
            }
        target.put(key, intToHexColor(intValue))
    }

    /**
     * Converts a pixel value to density-independent pixels (DP).
     *
     * @param pixels The value in pixels.
     * @param density The screen density (from [Density.density]).
     * @return The equivalent value in DP.
     */
    private fun pixelsToDpValue(pixels: Float, density: Density): Float = pixels / density.density
}
