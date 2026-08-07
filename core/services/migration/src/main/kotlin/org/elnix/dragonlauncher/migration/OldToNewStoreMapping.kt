package org.elnix.dragonlauncher.migration

import androidx.compose.ui.unit.Density
import org.elnix.dragonlauncher.migration.OldToNewStoreMapping.legacyAppVersionPrefix
import org.elnix.dragonlauncher.migration.OldToNewStoreMapping.mappings
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

/**
 * Context available to [StoreMapping.valueTransformers] during a migration run.
 *
 * @property density The screen density factor, used for pixel-to-DP conversions.
 * @property screenWidthDp The screen width in DP.
 * @property screenHeightDp The screen height in DP.
 */
internal data class MigrationContext(
    val density: Density,
    val screenWidthDp: Int,
    val screenHeightDp: Int
)

/**
 * Routes a single old key of a store into a *different* target store.
 *
 * Used when a key physically moved from one store in 3.2.2 to another store in
 * 4.0.0 (e.g. `selected_icon_pack` moved from `ui` to `icons`).
 *
 * @property targetStore The name of the new (4.0.0) store receiving the key.
 * @property newKey The key name to use inside the target store.
 * @property transform Optional value transformation applied before writing.
 */
internal data class KeyRoute(
    val targetStore: String,
    val newKey: String,
    val transform: (Any?) -> Any? = { it }
)

/**
 * Describes how a single old (3.2.2) backup store maps to the new (4.0.0) format.
 *
 * @param oldBackupKey The key used in the old backup JSON.
 * @param newBackupKey The target store name in the new system, or `null` if removed or handled via [splitInto].
 * @param keyMappings Renames for individual keys within the store (old -> new).
 * @param valueTransformers Transformers for specific key values (e.g. unit conversion). Keyed by the *old* key name.
 * @param skipKeys Keys to exclude entirely during migration.
 * @param splitInto When non-empty, the old store is split into multiple new stores.
 *   Each entry maps a new store name to a function that extracts the relevant data from the raw old value.
 * @param keyRoutes Old keys routed into a different target store than [newBackupKey].
 * @param handledExternally When `true`, the store is processed by a dedicated migrator
 *   (e.g. `new_actions` -> [PointsAndNestsMigrator]) and skipped by the generic loop.
 */
internal data class StoreMapping(
    val oldBackupKey: String,
    val newBackupKey: String?,
    val keyMappings: Map<String, String> = emptyMap(),
    val valueTransformers: Map<String, (Any?, MigrationContext) -> Any?> = emptyMap(),
    val skipKeys: Set<String> = emptySet(),
    val splitInto: Map<String, (Any) -> Any?> = emptyMap(),
    val keyRoutes: Map<String, KeyRoute> = emptyMap(),
    val handledExternally: Boolean = false
)

/**
 * Central registry of all store mappings from the old 3.2.2 backup format to the new 4.0.0 system.
 *
 * Each entry in [mappings] corresponds to a top-level key in the old backup JSON and defines
 * how its data should be transformed, renamed, split, routed, or dropped.
 *
 * The mapping also defines the [legacyAppVersionPrefix] used to detect old backups.
 */
internal object OldToNewStoreMapping {

    /**
     * Complete set of store mappings from old backup keys to new store definitions.
     *
     * - Stores with `newBackupKey == null` are removed or handled separately.
     * - Stores with `splitInto` entries are decomposed into multiple new stores.
     * - Stores with `keyMappings` have individual keys renamed.
     * - Stores with `keyRoutes` have individual keys moved to another store.
     * - Stores with `valueTransformers` have specific values transformed.
     * - Stores with `handledExternally` are processed by a dedicated migrator.
     */
    val mappings: Map<String, StoreMapping> = mapOf(
        "ui" to StoreMapping(
            oldBackupKey = "ui",
            newBackupKey = "ui",
            keyMappings = mapOf(
                "showAppLaunchPreview" to "showAppLaunchingPreview",
                "fullscreen" to "fullScreen",
                "showAppPreviewIconCenterStartPosition" to "showPointPreviewCenterStartPosition",
                "showCirclePreview" to "showCurrentShape",
                "showAllActionsOnCurrentCircle" to "showAllPointsInCurrentShape",
                "showAllActionsOnCurrentNest" to "showAllPointsInCurrentNest",
                "freeMoveDraggedPoint" to "allowFreePoints",
                "cellSizeDp" to "pointsCellSizeDp"
            ),
            skipKeys = setOf(
                "mainScreenLayers"
            ),
            keyRoutes = mapOf(
                "selected_icon_pack" to KeyRoute("icons", "selectedIconPack"),
                "icon_pack_tint" to KeyRoute("icons", "iconsTint"),
                "useCustomColorChannels" to KeyRoute("color_modes", "useCustomColorChannels"),
                "rgbLine" to KeyRoute("angle_line", "rgbLine"),
                "rgbLoading" to KeyRoute("hold_to_activate_arc", "holdRgbLoading"),
                "mainScreenLayers" to KeyRoute(
                    targetStore = "main_screen_layers",
                    newKey = "mainScreenLayers",
                    transform = { value ->
                        (value as? String)?.let(::migrateMainScreenLayersString) ?: value
                    }
                )
            )
        ),
        "color_mode" to StoreMapping(
            oldBackupKey = "color_mode",
            newBackupKey = "color_modes",
            keyMappings = mapOf(
                "dynamicColor" to "dynamicColors",
                "colorPickerButton" to "colorPickerButtonOne"
            )
        ),
        "color" to StoreMapping(
            oldBackupKey = "color",
            newBackupKey = "color",
            keyMappings = mapOf(
                "primary_color" to "primaryColor",
                "on_primary_color" to "onPrimaryColor",
                "primary_container_color" to "primaryContainerColor",
                "on_primary_container_color" to "onPrimaryContainerColor",
                "inverse_primary_color" to "inversePrimaryColor",
                "secondary_color" to "secondaryColor",
                "on_secondary_color" to "onSecondaryColor",
                "secondary_container_color" to "secondaryContainerColor",
                "on_secondary_container_color" to "onSecondaryContainerColor",
                "tertiary_color" to "tertiaryColor",
                "on_tertiary_color" to "onTertiaryColor",
                "tertiary_container_color" to "tertiaryContainerColor",
                "on_tertiary_container_color" to "onTertiaryContainerColor",
                "background_color" to "backgroundColor",
                "on_background_color" to "onBackgroundColor",
                "surface_color" to "surfaceColor",
                "on_surface_color" to "onSurfaceColor",
                "surface_variant_color" to "surfaceVariantColor",
                "on_surface_variant_color" to "onSurfaceVariantColor",
                "surface_tint_color" to "surfaceTintColor",
                "inverse_surface_color" to "inverseSurfaceColor",
                "inverse_on_surface_color" to "inverseOnSurfaceColor",
                "error_color" to "errorColor",
                "on_error_color" to "onErrorColor",
                "error_container_color" to "errorContainerColor",
                "on_error_container_color" to "onErrorContainerColor",
                "outline_color" to "outlineColor",
                "outline_variant_color" to "outlineVariantColor",
                "scrim_color" to "scrimColor",
                "surface_bright_color" to "surfaceBrightColor",
                "surface_container_color" to "surfaceContainerColor",
                "surface_container_high_color" to "surfaceContainerHighColor",
                "surface_container_highest_color" to "surfaceContainerHighestColor",
                "surface_container_low_color" to "surfaceContainerLowColor",
                "surface_container_lowest_color" to "surfaceContainerLowestColor",
                "surface_dim_color" to "surfaceDimColor",
                "primary_fixed_color" to "primaryFixedColor",
                "primary_fixed_dim_color" to "primaryFixedDimColor",
                "on_primary_fixed_color" to "onPrimaryFixedColor",
                "on_primary_fixed_variant_color" to "onPrimaryFixedVariantColor",
                "secondary_fixed_color" to "secondaryFixedColor",
                "secondary_fixed_dim_color" to "secondaryFixedDimColor",
                "on_secondary_fixed_color" to "onSecondaryFixedColor",
                "on_secondary_fixed_variant_color" to "onSecondaryFixedVariantColor",
                "tertiary_fixed_color" to "tertiaryFixedColor",
                "tertiary_fixed_dim_color" to "tertiaryFixedDimColor",
                "on_tertiary_fixed_color" to "onTertiaryFixedColor",
                "on_tertiary_fixed_variant_color" to "onTertiaryFixedVariantColor",
                "angle_line_color" to "angleLineColor",
                "circle_color" to "shapesColor",
                "launch_app_color" to "launchAppColor",
                "open_url_color" to "openUrlColor",
                "notification_shade_color" to "notificationShadeColor",
                "control_panel_color" to "controlPanelColor",
                "open_app_drawer_color" to "openAppDrawerColor",
                "launcher_settings_color" to "launcherSettingsColor",
                "lock_color" to "lockColor",
                "open_file_color" to "openFileColor",
                "reload_color" to "reloadColor",
                "open_recent_apps" to "openRecentAppsColor",
                "open_circle_nest" to "openCircleNestColor",
                "go_parent_nest" to "goParentNestColor"
            )
        ),
        "language" to StoreMapping(
            oldBackupKey = "language",
            newBackupKey = "language",
            keyMappings = mapOf(
                "pref_app_language" to "keyLang"
            )
        ),
        "drawer" to StoreMapping(
            oldBackupKey = "drawer",
            newBackupKey = "drawer",
            keyMappings = mapOf(
                "showAppLabelInDrawer" to "showAppLabelsInDrawer",
                "categoryGridWidth" to "categoryGridCells",
                "iconsShape" to "iconShape",
                "scrollDownDrawerAction" to "drawerScrollDownAction",
                "scrollUpDrawerAction" to "drawerScrollUpAction",
                "backDrawerAction" to "drawerBackAction",
                "tabEmptySpaceToRaiseKeyboard" to "tapEmptySpaceAction",
                "recentlyUsedPackagesSet" to "recentlyUsedPackages",
                "iconsSpacingHorizontal\u00B2" to "iconsSpacingHorizontal"
            ),
            valueTransformers = mapOf(
                "leftDrawerWidth" to { value, ctx ->
                    if (value is Number) {
                        (value.toFloat() * ctx.screenWidthDp).roundToInt().coerceAtLeast(0)
                    } else value
                },
                "rightDrawerWidth" to { value, ctx ->
                    if (value is Number) {
                        (value.toFloat() * ctx.screenWidthDp).roundToInt().coerceAtLeast(0)
                    } else value
                }
            )
        ),
        "debug" to StoreMapping(
            oldBackupKey = "debug",
            newBackupKey = "debug",
            keyMappings = mapOf(
                "debugInfos" to "mainScreenDebugInfos"
            )
        ),
        "workspaces" to StoreMapping(
            oldBackupKey = "workspaces",
            newBackupKey = null,
            splitInto = mapOf(
                "workspaces" to { value ->
                    (value as? JSONObject)?.optJSONArray("workspaces")?.let(::migrateWorkspacesArray)
                },
                "app_overrides" to { value ->
                    migrateAppOverrides((value as? JSONObject)?.opt("appOverrides"))
                }
            )
        ),
        "behavior" to StoreMapping(
            oldBackupKey = "behavior",
            newBackupKey = "behavior",
            keyMappings = mapOf(
                "upPadding" to "topPadding",
                "downPadding" to "bottomPadding"
            )
        ),
        "backup" to StoreMapping(
            oldBackupKey = "backup",
            newBackupKey = "backup"
        ),
        "widgets" to StoreMapping(
            oldBackupKey = "widgets",
            newBackupKey = null,
            splitInto = mapOf(
                "widgets" to { value ->
                    (value as? JSONArray)?.let(::migrateWidgetsArray)
                }
            )
        ),
        "wellbeing" to StoreMapping(
            oldBackupKey = "wellbeing",
            newBackupKey = "wellbeing",
            keyMappings = mapOf(
                "GUILT_MODE_ENABLED" to "guiltModeEnabled",
                "SOCIAL_MEDIA_PAUSE_ENABLED" to "socialMediaPauseEnabled",
                "SHOW_USAGE_STATS" to "showUsageStats",
                "PAUSE_DURATION_SECONDS" to "pauseDurationSeconds",
                "REMINDER_ENABLED" to "reminderEnabled",
                "REMINDER_INTERVAL_MINUTES" to "reminderIntervalMinutes",
                "RETURN_TO_LAUNCHER_ENABLED" to "returnToLauncherEnabled",
                "PAUSED_APPS_LIST" to "pausedApps",
                "POPUP_SHOW_SESSION_TIME" to "popupShowSessionTime",
                "POPUP_SHOW_TODAY_TIME" to "popupShowTodayTime",
                "POPUP_SHOW_REMAINING_TIME" to "popupShowRemainingTime"
            ),
            valueTransformers = mapOf(
                "PAUSED_APPS_LIST" to { value, _ ->
                    when (value) {
                        is JSONArray -> value
                        is List<*> -> JSONArray(value)
                        is String -> try {
                            JSONArray(value)
                        } catch (_: Exception) {
                            JSONArray()
                        }

                        else -> JSONArray()
                    }
                }
            )
        ),
        "swipe_map" to StoreMapping(
            oldBackupKey = "swipe_map",
            newBackupKey = null,
            keyRoutes = mapOf(
                "isInDragAroundMode" to KeyRoute("private", "isInDragAroundMode")
            )
        ),
        "status_bar" to StoreMapping(
            oldBackupKey = "status_bar",
            newBackupKey = "status_bar"
        ),
        "status_bar_json" to StoreMapping(
            oldBackupKey = "status_bar_json",
            newBackupKey = null,
            splitInto = mapOf(
                "status_bar_json" to { value ->
                    (value as? JSONArray)?.let(::migrateStatusBarJsonArray)
                }
            )
        ),
        "angle_line" to StoreMapping(
            oldBackupKey = "angle_line",
            newBackupKey = null,
            skipKeys = setOf("lineJson", "angleLineJson", "startLineJson", "endLineJson"),
            splitInto = mapOf(
                "angle_line" to { value ->
                    (value as? JSONObject)?.let { extractAngleLineSettings(it) }
                },
                "angle_object_setting_store" to { value ->
                    (value as? JSONObject)?.let { parseCustomObject(it.optString("angleLineJson", "")) }
                },
                "line_object_setting_store" to { value ->
                    (value as? JSONObject)?.let { parseCustomObject(it.optString("lineJson", "")) }
                },
                "start_object_setting_store" to { value ->
                    (value as? JSONObject)?.let { parseCustomObject(it.optString("startLineJson", "")) }
                },
                "end_object_setting_store" to { value ->
                    (value as? JSONObject)?.let { parseCustomObject(it.optString("endLineJson", "")) }
                }
            )
        ),
        "hold_to_activate" to StoreMapping(
            oldBackupKey = "hold_to_activate",
            newBackupKey = null,
            splitInto = mapOf(
                "hold_to_activate_arc" to { value ->
                    (value as? JSONObject)?.let { extractHoldToActivateArcSettings(it) }
                },
                "hold_to_activate_object" to { value ->
                    (value as? JSONObject)?.let { parseCustomObject(it.optString("holdToActivateArcCustomObject", "")) }
                }
            )
        ),
        "new_actions" to StoreMapping(
            oldBackupKey = "new_actions",
            newBackupKey = null,
            handledExternally = true
        ),
        "private" to StoreMapping(
            oldBackupKey = "private",
            newBackupKey = "private",
            keyMappings = mapOf(
                "lastSeenVersionCode" to "lastSeenVersionCodeWhatsNew",
                "lockPinHash" to "lockHash"
            ),
            skipKeys = setOf(
                "lastBackupTime",
                "lastSeenVersionCodeDoABackup"
            )
        ),
        "private_apps" to StoreMapping(
            oldBackupKey = "private_apps",
            newBackupKey = null
        ),
        "floating_apps" to StoreMapping(
            oldBackupKey = "floating_apps",
            newBackupKey = null
        )
    )

    @Suppress("ConstPropertyName")
    const val legacyAppVersionPrefix: String = "3.2.2"

    private const val ICON_SHAPE_PREFIX = "org.elnix.dragonlauncher.common.serializables.IconShape."
    private const val STATUS_BAR_PREFIX = "org.elnix.dragonlauncher.common.serializables.StatusBarSerializable."
    private const val MAIN_SCREEN_LAYER_PREFIX = "org.elnix.dragonlauncher.common.serializables.MainScreenLayer."
    private const val NAVIGATION_ROUTE_PREFIX = "org.elnix.dragonlauncher.common.navigaton.NavigationRoute."

    private const val ROUTE_TYPE_KEY = "type"

    private val VALID_CUSTOM_ICON_TYPES = setOf(
        "CustomIconPackIcon",
        "AdaptifiedLegacyIcon",
        "CustomTextIcon",
        "CustomThemedIcon",
        "DefaultPlaceholderIcon",
        "ForceThemedIcon",
        "UnmodifiedSystemDefaultIcon"
    )

    private fun stripPrefix(value: String, prefix: String): String =
        if (value.startsWith(prefix)) value.removePrefix(prefix) else value

    internal fun parseCustomObject(raw: String): JSONObject? {
        if (raw.isBlank()) return null
        val obj = try {
            JSONObject(raw)
        } catch (_: Exception) {
            return null
        }
        obj.optJSONObject("shape")?.let { shape ->
            val type = shape.optString(ROUTE_TYPE_KEY, "")
            if (type.isNotEmpty()) shape.put(ROUTE_TYPE_KEY, stripPrefix(type, ICON_SHAPE_PREFIX))
        }
        if (obj.has("color") && obj.get("color") is Number) {
            obj.put("color", toHexColor(obj.getInt("color")))
        }
        obj.optJSONObject("glow")?.let { glow ->
            if (glow.has("color") && glow.get("color") is Number) {
                glow.put("color", toHexColor(glow.getInt("color")))
            }
        }
        // The 4.0.0 CustomObject requires `shape`, `size` and `rotation`; old line/arc
        // objects may omit them, so inject sensible defaults (lines don't use size).
        if (!obj.has("shape")) {
            obj.put("shape", JSONObject().put(ROUTE_TYPE_KEY, "Circle"))
        }
        if (!obj.has("size")) {
            obj.put("size", 0.0)
        }
        if (!obj.has("rotation")) {
            obj.put("rotation", 0)
        }
        return obj
    }

    private fun toHexColor(color: Int): String =
        "%08X".format(color.toLong() and 0xFFFFFFFFL)

    internal fun extractAngleLineSettings(obj: JSONObject): JSONObject {
        val result = JSONObject()
        for (key in listOf(
            "showLineObjectPreview",
            "showAngleLineObjectPreview",
            "showStartObjectPreview",
            "showEndObjectPreview",
            "angleLineObjectsOrder"
        )) {
            if (obj.has(key)) result.put(key, obj.get(key))
        }
        return result
    }

    internal fun extractHoldToActivateArcSettings(obj: JSONObject): JSONObject {
        val result = JSONObject()
        val renames = mapOf(
            "rotationPerSecond" to "rotationsPerSecond",
            "holdMenuEntries2" to "holdMenuEntriesJson"
        )
        for (key in listOf(
            "holdDelayBeforeStartingLongClickSettings",
            "longCLickSettingsDuration",
            "holdToActivateSettingsTolerance",
            "showToleranceOnMainScreen",
            "rotationPerSecond",
            "holdMenuEntries2"
        )) {
            if (!obj.has(key)) continue
            val value = obj.get(key)
            val newKey = renames[key] ?: key
            result.put(newKey, when (key) {
                "holdToActivateSettingsTolerance" ->
                    (value as? Number)?.toFloat()?.roundToInt() ?: value

                "holdMenuEntries2" -> migrateHoldMenuEntriesString(value.toString()) ?: value
                else -> value
            })
        }
        return result
    }

    internal fun migrateHoldMenuEntriesString(raw: String): String? {
        if (raw.isBlank()) return null
        val arr = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return null
        }
        for (i in 0 until arr.length()) {
            val entry = arr.optJSONObject(i) ?: continue
            val type = entry.optString(ROUTE_TYPE_KEY, "")
            if (type.isNotEmpty()) entry.put(ROUTE_TYPE_KEY, stripPrefix(type, NAVIGATION_ROUTE_PREFIX))
        }
        return arr.toString()
    }

    internal fun migrateWidgetsArray(arr: JSONArray): JSONArray {
        for (i in 0 until arr.length()) {
            val widget = arr.optJSONObject(i) ?: continue
            widget.optJSONObject("action")?.let { action ->
                widget.put("action", PointsAndNestsMigrator.migrateAction(action))
            }
            widget.optJSONObject("shape")?.let { shape ->
                val type = shape.optString(ROUTE_TYPE_KEY, "")
                if (type.isNotEmpty()) shape.put(ROUTE_TYPE_KEY, stripPrefix(type, ICON_SHAPE_PREFIX))
            }
        }
        return arr
    }

    internal fun migrateStatusBarJsonArray(arr: JSONArray): JSONArray {
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            val type = item.optString(ROUTE_TYPE_KEY, "")
            if (type.isNotEmpty()) item.put(ROUTE_TYPE_KEY, stripPrefix(type, STATUS_BAR_PREFIX))
            item.optJSONObject("action")?.let { action ->
                item.put("action", PointsAndNestsMigrator.migrateAction(action))
            }
        }
        return arr
    }

    internal fun migrateMainScreenLayersString(raw: String): String? {
        if (raw.isBlank()) return null
        val arr = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return null
        }
        for (i in 0 until arr.length()) {
            val entry = arr.optJSONObject(i) ?: continue
            val type = entry.optString(ROUTE_TYPE_KEY, "")
            if (type.isNotEmpty()) entry.put(ROUTE_TYPE_KEY, stripPrefix(type, MAIN_SCREEN_LAYER_PREFIX))
        }
        return arr.toString()
    }

    /**
     * Migrates the old `appOverrides` value into the new `app_overrides` format.
     *
     * The new format is a JSON object keyed by cache key
     * (`{"<cacheKey>": {"customName": ...}, ...}`), matching how
     * [org.elnix.dragonlauncher.base.model.serializables.AppOverrideState] is serialized.
     *
     * The old value can be either:
     * - a flat interleaved JSON array (`[{"cacheKey": ...}, {"customIcon": ...}, {"cacheKey": ...}, ...]`)
     *   where every `cacheKey` object starts a new override and the following objects without a
     *   `cacheKey` merge into it, or
     * - a JSON object keyed by cache key.
     *
     * Old-format `customIcon` values (e.g. `{"type": "ICON_PACK", ...}`) cannot be decoded by the
     * new [CustomIcon][org.elnix.dragonlauncher.base.model.serializables.CustomIcon] sealed class
     * and would break the whole import, so they are dropped. Entries left empty after cleaning
     * are omitted entirely.
     */
    internal fun migrateAppOverrides(value: Any?): JSONObject? {
        val result = JSONObject()
        when (value) {
            is JSONArray -> {
                var currentKey: String? = null
                var currentEntry: JSONObject? = null
                for (i in 0 until value.length()) {
                    val entry = value.optJSONObject(i) ?: continue
                    val cacheKey = entry.optString("cacheKey", "").takeIf { it.isNotEmpty() }
                    if (cacheKey != null) {
                        currentEntry?.let { if (it.length() > 0) result.put(currentKey, it) }
                        currentKey = cacheKey
                        currentEntry = JSONObject()
                    } else {
                        currentEntry?.let { target ->
                            val cleaned = cleanAppOverrideEntry(entry)
                            for (k in cleaned.keys()) {
                                if (!target.has(k)) target.put(k, cleaned.get(k))
                            }
                        }
                    }
                }
                currentEntry?.let { if (it.length() > 0) result.put(currentKey, it) }
            }

            is JSONObject -> {
                for (key in value.keys()) {
                    val entry = value.optJSONObject(key) ?: continue
                    result.put(key, cleanAppOverrideEntry(entry))
                }
            }

            else -> return null
        }
        return if (result.length() > 0) result else null
    }

    /**
     * Converts old `workspaces` entries into the new 4.0.0 format.
     *
     * In 3.2.2 each workspace stored `removedAppIds` as an array of `{"cacheKey": ...}` objects,
     * while 4.0.0 expects a plain array of cache-key strings. Enum values are also normalized to
     * the new title-cased names (e.g. `USER` -> `User`) for readability.
     */
    internal fun migrateWorkspacesArray(arr: JSONArray): JSONArray {
        for (i in 0 until arr.length()) {
            val workspace = arr.optJSONObject(i) ?: continue
            workspace.optJSONArray("removedAppIds")?.let { removed ->
                val converted = JSONArray()
                for (j in 0 until removed.length()) {
                    val obj = removed.optJSONObject(j)
                    val cacheKey = obj?.optString("cacheKey", "")
                    if (!cacheKey.isNullOrBlank()) {
                        converted.put(cacheKey)
                    } else {
                        removed.opt(j)?.let { converted.put(it) }
                    }
                }
                workspace.put("removedAppIds", converted)
            }
            val type = workspace.optString("type", "")
            val normalized = when (type.uppercase()) {
                "USER" -> "User"
                "SYSTEM" -> "System"
                "ALL" -> "All"
                "WORK" -> "Work"
                "PRIVATE" -> "Private"
                else -> type
            }
            if (normalized.isNotEmpty()) workspace.put("type", normalized)
        }
        return arr
    }

    internal fun cleanAppOverrideEntry(entry: JSONObject): JSONObject {
        val cleaned = JSONObject()
        for (key in entry.keys()) {
            when (key) {
                "customIcon" -> {
                    val icon = entry.optJSONObject(key)
                    if (icon != null && VALID_CUSTOM_ICON_TYPES.contains(icon.optString(ROUTE_TYPE_KEY, ""))) {
                        cleaned.put(key, icon)
                    }
                }

                else -> cleaned.put(key, entry.get(key))
            }
        }
        return cleaned
    }
}
