package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.common.navigaton.SETTINGS
import org.elnix.dragonlauncher.common.utils.BluetoothADBCommands
import org.elnix.dragonlauncher.common.utils.Constants.Logging.SWIPE_TAG
import org.elnix.dragonlauncher.common.utils.DataADBCommands
import org.elnix.dragonlauncher.common.utils.WifiADBCommands
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logV
import java.util.UUID


fun dummySwipePoint(
    action: SwipeActionSerializable? = null,
    id: String? = null
) =
    SwipePointSerializable(
        circleNumber = 0,
        angleDeg = 0.0,
        action = action ?: SwipeActionSerializable.OpenDragonLauncherSettings(),
        id = id ?: UUID.randomUUID().toString(),
        nestId = 0
    )

val defaultSwipePointsValues = dummySwipePoint(null).copy(
    borderStroke = 4f,
    borderStrokeSelected = 8f,
    opacity = 1f,
    cornerRadius = null,
    innerPadding = 5,
    size = 22,
    borderShape = IconShape.Circle,
    borderShapeSelected = IconShape.Circle,
    liveNestPreviewDelayMs = 500,
    liveNestScale = 0.65f,
    liveNestGraceDistancePx = 50,
    liveNestSnapsToFingerPosition = true,
    holdAndRunDelayMs = 500,
    cycleActionsLoopDelayMs = 500, // -1 = No loop
    cycleActionStageDefaultDelay = 500,
    liveNestMainNestOpacityPercent = 50
)


/**
 * Swipe Actions Serializable, the core of the main gesture idea
 * Holds all the different actions the user can do
 *
 * Here are all the related settings in which you have to add logic in order to correctly add a new action:
 *  - `actionLabel`
 *  - `launchSwipeAction`
 *  - `actionColor`
 *  - `actionLabel`
 *  - [org.elnix.dragonlauncher.common.utils.ImageUtils.createUntintedBitmap]
 */
@Serializable
sealed class SwipeActionSerializable {
    @Serializable
    data class LaunchApp(
        val packageName: String,
        val isPrivateSpace: Boolean,
        val userId: Int?
    ) : SwipeActionSerializable()

    @Serializable
    data class LaunchShortcut(
        val packageName: String,
        val shortcutId: String
    ) : SwipeActionSerializable()

    @Serializable
    data class OpenUrl(val url: String) : SwipeActionSerializable()

    @Serializable
    data class OpenFile(
        val uri: String,
        val mimeType: String? = null
    ) : SwipeActionSerializable()

    @Serializable
    object NotificationShade : SwipeActionSerializable()

    @Serializable
    object ControlPanel : SwipeActionSerializable()

    @Serializable
    data class OpenAppDrawer(val workspaceId: String? = null) : SwipeActionSerializable()

    @Serializable
    data class OpenDragonLauncherSettings(val route: String = SETTINGS.ROOT) : SwipeActionSerializable()

    @Serializable
    object Lock : SwipeActionSerializable()

    @Serializable
    object ReloadApps : SwipeActionSerializable()

    @Serializable
    object OpenRecentApps : SwipeActionSerializable()

    @Serializable
    data class OpenCircleNest(val nestId: Int) : SwipeActionSerializable()

    @Serializable
    object GoParentNest : SwipeActionSerializable()

    @Serializable
    data class OpenWidget(
        val widgetId: Int,
        val providerPackage: String,
        val providerClass: String
    ) : SwipeActionSerializable()

    @Serializable
    data class ToggleWifi(
        val command: WifiADBCommands = WifiADBCommands.Svc,
        val toast: Boolean? = false
    ) : SwipeActionSerializable()

    @Serializable
    data class ToggleBluetooth(
        val command: BluetoothADBCommands = BluetoothADBCommands.Cmd,
        val toast: Boolean? = false
    ) : SwipeActionSerializable()

    @Serializable
    data class ToggleData(
        val command: DataADBCommands = DataADBCommands.Svc,
        val toast: Boolean? = false
    ) : SwipeActionSerializable()

    @Serializable
    data class RunAdbCommand(
        val command: String,
        val toast: Boolean? = false
    ) : SwipeActionSerializable()

    @Serializable
    data object KillLauncher : SwipeActionSerializable()
    @Serializable
    object None : SwipeActionSerializable()
}

object SwipeJson {
    private val jsonConfig = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    private val jsonPretty = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
        prettyPrint = true
    }


    /* ───────────── Points ───────────── */

    fun encodePoints(points: List<SwipePointSerializable>): String {
        return jsonConfig.encodeToString(points)
    }

    fun encodePointsPretty(points: List<SwipePointSerializable>): String =
        jsonPretty.encodeToString(points)

    fun decodePoints(json: String): List<SwipePointSerializable> {
        return try {
            jsonConfig
                .decodeFromString<List<SwipePointSerializable>>(json)
                .takeIf { it.isNotEmpty() }
                .also {
                    logV(SWIPE_TAG) { "Decoded points value:\n$it" }
                }

                // If empty, but no errors reported, try the old method anyway
                ?: LegacySwipeJson.decodePoints(json)

        } catch (e: Exception) {
            logE(SWIPE_TAG, e) { "Modern points decode failed, trying legacy method" }

            // Decode the ol' way, returns emptyList if fails. (Skill issue if it happens)
            LegacySwipeJson.decodePoints(json)
        }
    }

    /* ───────────── Nests ───────────── */

    fun encodeNests(nests: List<CircleNest>): String =
        jsonConfig.encodeToString(nests)

    fun encodeNestsPretty(nests: List<CircleNest>): String =
        jsonPretty.encodeToString(nests)

    fun decodeNests(json: String): List<CircleNest> {
        if (json.isBlank()) return emptyList()
        return try {
            jsonConfig.decodeFromString(json)
        } catch (e: Exception) {
            logE(SWIPE_TAG, e) { "Modern nest decode failed, trying legacy" }

            // Decode the ol' way, returns emptyList if fails. (Skill issue if it happens)
            LegacySwipeJson.decodeNests(json)
        }
    }

    fun encodeAction(action: SwipeActionSerializable?): String? =
        action?.let {
            jsonConfig.encodeToString(it)
        }

    fun decodeAction(jsonString: String): SwipeActionSerializable? {
        if (jsonString.isBlank() || jsonString == "{}") return null
        return try {
            jsonConfig.decodeFromString<SwipeActionSerializable>(jsonString)
        } catch (_: Throwable) {
            LegacySwipeJson.decodeAction(jsonString)
        }
    }
}
