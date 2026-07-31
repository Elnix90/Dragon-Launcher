package org.elnix.dragonlauncher.migration

import org.elnix.dragonlauncher.migration.OldToNewStoreMapping.legacyAppVersionPrefix
import org.elnix.dragonlauncher.migration.OldToNewStoreMapping.mappings
import org.json.JSONArray
import org.json.JSONObject

/**
 * Describes how a single old (3.2.2) backup store maps to the new (4.0.0) format.
 *
 * @param oldBackupKey The key used in the old backup JSON.
 * @param newBackupKey The target store name in the new system, or `null` if removed or handled via [splitInto].
 * @param keyMappings Renames for individual keys within the store (old -> new).
 * @param valueTransformers Transformers for specific key values (e.g., enum casing).
 * @param skipKeys Keys to exclude entirely during migration.
 * @param splitInto When non-empty, the old store is split into multiple new stores.
 *   Each entry maps a new store name to a function that extracts the relevant data from the old JSON.
 */
internal data class StoreMapping(
    val oldBackupKey: String,
    val newBackupKey: String?,
    val keyMappings: Map<String, String> = emptyMap(),
    val valueTransformers: Map<String, (Any?) -> Any?> = emptyMap(),
    val skipKeys: Set<String> = emptySet(),
    val splitInto: Map<String, (JSONObject) -> Any?> = emptyMap()
)

/**
 * Central registry of all store mappings from the old 3.2.2 backup format to the new 4.0.0 system.
 *
 * Each entry in [mappings] corresponds to a top-level key in the old backup JSON and defines
 * how its data should be transformed, renamed, split, or dropped.
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
     * - Stores with `valueTransformers` have specific values transformed.
     */
    val mappings: Map<String, StoreMapping> = mapOf(
        "ui" to StoreMapping(
            oldBackupKey = "ui",
            newBackupKey = "ui",
            keyMappings = mapOf(
                "selected_icon_pack" to "selectedIconPack",
                "icon_pack_tint" to "iconsTint",
                "showAppPreviewIconCenterStartPosition" to "showPointPreviewCenterStartPosition",
                "showCirclePreview" to "showCurrentShape",
                "showAllActionsOnCurrentCircle" to "showAllPointsInCurrentShape",
                "showAllActionsOnCurrentNest" to "showAllPointsInCurrentNest",
                "showLaunchingAppIcon" to "showPreviewPoint",
                "cellSizeDp" to "nestsCellSizeDp"
            ),
            skipKeys = setOf(
                "rgbLoading",
                "rgbLine",
                "mainScreenLayers",
                "useCustomColorChannels"
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
                "tertiary_color" to "tertiaryColor",
                "on_tertiary_color" to "onTertiaryColor",
                "background_color" to "backgroundColor",
                "on_background_color" to "onBackgroundColor",
                "surface_color" to "surfaceColor",
                "on_surface_color" to "onSurfaceColor",
                "surface_variant_color" to "surfaceVariantColor",
                "error_color" to "errorColor",
                "on_error_color" to "onErrorColor",
                "outline_color" to "outlineColor",
                "outline_variant_color" to "outlineVariantColor",
                "surface_container_low_color" to "surfaceContainerLowColor",
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
                "go_parent_nest" to "goParentNestColor",
                "toggleWifi" to "toggleWifi",
                "toggleData" to "toggleData",
                "toggleBluetooth" to "toggleBluetooth",
                "runAdbCommand" to "runAdbCommand"
            )
        ),
//        "private" to StoreMapping(
//            oldBackupKey = "private",
//            newBackupKey = null
//        ),
//        "new_actions" to StoreMapping(
//            oldBackupKey = "new_actions",
//            newBackupKey = null
//        ),
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
                "iconsSpacingHorizontal" to "iconsSpacingHorizontal",
                "iconsSpacingVertical" to "iconsSpacingVertical"
            ),
            valueTransformers = mapOf(
                "leftDrawerWidth" to { value ->
                    if (value is Number) (value.toFloat() * 1000).toInt() else value
                },
                "rightDrawerWidth" to { value ->
                    if (value is Number) (value.toFloat() * 1000).toInt() else value
                }
            )
        ),
        "debug" to StoreMapping(
            oldBackupKey = "debug",
            newBackupKey = "debug",
            skipKeys = setOf("lastSeenVersionCode")
        ),
        "workspaces" to StoreMapping(
            oldBackupKey = "workspaces",
            newBackupKey = null,
            splitInto = mapOf(
                "workspaces" to { obj -> obj.optJSONArray("workspaces") },
                "app_overrides" to { obj -> obj.optJSONArray("appOverrides") }
            ),
            skipKeys = setOf("appOverrides")
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
            newBackupKey = "widgets",
            keyMappings = mapOf(
                "widgets" to "_unused"
            ),
            skipKeys = setOf("widgets"),
            splitInto = mapOf(
                "widgets" to { obj ->
                    val raw = obj.optString("widgets", "")
                    if (raw.isNotBlank()) {
                        try { JSONArray(raw) } catch (_: Exception) { null }
                    } else null
                }
            )
        ),
        "wellbeing" to StoreMapping(
            oldBackupKey = "wellbeing",
            newBackupKey = "wellbeing",
            keyMappings = mapOf(
                "GUILT_MODE_ENABLED" to "guiltModeEnabled",
                "SOCIAL_MEDIA_PAUSE_ENABLED" to "socialMediaPauseEnabled",
                "PAUSE_DURATION_SECONDS" to "pauseDurationSeconds",
                "REMINDER_ENABLED" to "reminderEnabled",
                "REMINDER_INTERVAL_MINUTES" to "reminderIntervalMinutes",
                "REMINDER_MODE" to "reminderMode",
                "RETURN_TO_LAUNCHER_ENABLED" to "returnToLauncherEnabled",
                "PAUSED_APPS_LIST" to "pausedApps"
            ),
            valueTransformers = mapOf(
                "PAUSED_APPS_LIST" to { value ->
                    when (value) {
                        is JSONArray -> value
                        is List<*> -> JSONArray(value)
                        is String -> try { JSONArray(value) } catch (_: Exception) { JSONArray() }
                        else -> JSONArray()
                    }
                }
            )
        ),
        "swipe_map" to StoreMapping(
            oldBackupKey = "swipe_map",
            newBackupKey = null,
        ),
        "status_bar" to StoreMapping(
            oldBackupKey = "status_bar",
            newBackupKey = "status_bar"
        ),
        "status_bar_json" to StoreMapping(
            oldBackupKey = "status_bar_json",
            newBackupKey = "status_bar_json",
            splitInto = mapOf(
                "status_bar_json" to { obj ->
                    val raw = obj.optString("statusBarData", "")
                    if (raw.isNotBlank()) {
                        try {
                            JSONArray(raw)
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                }
            ),
            skipKeys = setOf("statusBarData")
        ),
        "angle_line" to StoreMapping(
            oldBackupKey = "angle_line",
            newBackupKey = null,
            keyMappings = mapOf(
                "rgbLine" to "rgbLine",
                "rgbAngle" to "rgbAngle",
                "linePreviewSnapToAction" to "linePreviewSnapToAction",
            ),
            skipKeys = setOf("lineJson", "angleLineJson", "startLineJson", "endLineJson"),
            splitInto = mapOf(
                "angle_line" to { obj ->
                    val result = JSONObject()
                    for (key in listOf("rgbLine", "rgbAngle", "linePreviewSnapToAction", "showCirclePreview", "showAppPreviewIconCenterStartPosition")) {
                        if (obj.has(key)) {
                            result.put(key, obj.get(key))
                        }
                    }
                    result
                },
                "angle_object" to { obj ->
                    val raw = obj.optString("angleLineJson", "")
                    if (raw.isNotBlank()) {
                        try { JSONObject(raw) } catch (_: Exception) { null }
                    } else null
                },
                "line_object" to { obj ->
                    val raw = obj.optString("lineJson", "")
                    if (raw.isNotBlank()) {
                        try { JSONObject(raw) } catch (_: Exception) { null }
                    } else null
                },
                "start_object" to { obj ->
                    val raw = obj.optString("startLineJson", "")
                    if (raw.isNotBlank()) {
                        try { JSONObject(raw) } catch (_: Exception) { null }
                    } else null
                },
                "end_object" to { obj ->
                    val raw = obj.optString("endLineJson", "")
                    if (raw.isNotBlank()) {
                        try { JSONObject(raw) } catch (_: Exception) { null }
                    } else null
                }
            )
        ),
        "hold_to_activate" to StoreMapping(
            oldBackupKey = "hold_to_activate",
            newBackupKey = null,
            keyMappings = mapOf(
                "rotationPerSecond" to "rotationsPerSecond",
                "holdMenuEntries2" to "holdMenuEntriesJson"
            ),
            skipKeys = setOf("holdToActivateArcCustomObject"),
            splitInto = mapOf(
                "hold_to_activate_arc" to { obj ->
                    val result = JSONObject()
                    if (obj.has("rotationPerSecond")) {
                        result.put("rotationsPerSecond", obj.get("rotationPerSecond"))
                    }
                    result
                },
                "hold_to_activate_object" to { obj ->
                    val raw = obj.optString("holdMenuEntries2", "")
                    if (raw.isNotBlank()) {
                        try {
                            JSONArray(raw)
                        } catch (_: Exception) {
                            try { JSONObject(raw) } catch (_: Exception) { null }
                        }
                    } else null
                }
            )
        )
    )

    @Suppress("ConstPropertyName")
    const val legacyAppVersionPrefix: String = "3.2.2"
}
