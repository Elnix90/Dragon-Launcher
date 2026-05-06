package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.STATUS_BAR_TAG
import org.elnix.dragonlauncher.common.messyfolder.isBlankJson

@Serializable
sealed class StatusBarSerializable {

    @Serializable
    data class Time(
        val formatter: String = "HH:mm:ss",
        val action: SwipeActionSerializable? = null,
        val fontSize: Int = 16,
        val isBold: Boolean = false,
        val colorHex: String? = null
    ) : StatusBarSerializable()

    @Serializable
    data class Date(
        val formatter: String = "MMM dd",
        val action: SwipeActionSerializable? = null,
        val fontSize: Int = 14,
        val isBold: Boolean = false,
        val colorHex: String? = null
    ) : StatusBarSerializable()

    @Serializable
    data class Bandwidth(
        val merge: Boolean = false,
        val fontSize: Int = 12,
        val colorHex: String? = null
    ) : StatusBarSerializable()

    @Serializable
    data class Notifications(
        val maxIcons: Int = 8,
        val iconSize: Int = 18
    ) : StatusBarSerializable()

    @Serializable
    data class Connectivity(
        val showAirplaneMode: Boolean = true,
        val showWifi: Boolean = true,
        val showBluetooth: Boolean = true,
        val showVpn: Boolean = true,
        val showMobileData: Boolean = true,
        val showHotspot: Boolean = true,
        val showUsb: Boolean = true,
        val updateFrequency: Int = 5,
        val iconSize: Int = 18
    ) : StatusBarSerializable()

    @Serializable
    data class Spacer(
        val width: Int = -1
    ) : StatusBarSerializable()

    @Serializable
    data class Battery(
        val showIcon: Boolean = false,
        val showPercentage: Boolean = true,
        val fontSize: Int = 14,
        val colorHex: String? = null
    ) : StatusBarSerializable()

    @Serializable
    data class NextAlarm(
        val formatter: String = "HH:mm",
        val fontSize: Int = 12,
        val colorHex: String? = null
    ) : StatusBarSerializable()
}


val allStatusBarSerializable = listOf(
    StatusBarSerializable.Time(),
    StatusBarSerializable.Date(),
    StatusBarSerializable.Bandwidth(),
    StatusBarSerializable.Notifications(),
    StatusBarSerializable.Connectivity(),
    StatusBarSerializable.Battery(),
    StatusBarSerializable.NextAlarm(),
    StatusBarSerializable.Spacer()
)

object StatusBarJson {
    private val jsonConfig = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /* ───────────── List encoders / decoders ───────────── */

    fun encodeStatusBarElements(elements: List<StatusBarSerializable>): String =
        jsonConfig.encodeToString(elements)


    fun decodeStatusBarElements(json: String?): List<StatusBarSerializable> {
        if (json == null || json.isBlankJson) return emptyList()

        logD(STATUS_BAR_TAG) { json }
        return try {
            jsonConfig.decodeFromString(json)
        } catch (e: Throwable) {
            logE(STATUS_BAR_TAG, e) { "Decode failed for JSON: $json - ${e.message}" }
            emptyList()
        }
    }
}
