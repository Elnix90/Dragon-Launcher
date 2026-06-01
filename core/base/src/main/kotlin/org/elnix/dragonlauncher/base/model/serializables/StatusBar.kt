package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson

@Serializable
@SerialName("StatusBar")
sealed class StatusBar {

    @Serializable
    @SerialName("Time")
    data class Time(
        val formatter: String = "HH:mm:ss",
        val action: Action? = null,
        val fontSize: Int = 16,
        val isBold: Boolean = false,
        val colorHex: String? = null
    ) : StatusBar()

    @Serializable
    @SerialName("Date")
    data class Date(
        val formatter: String = "MMM dd",
        val action: Action? = null,
        val fontSize: Int = 14,
        val isBold: Boolean = false,
        val colorHex: String? = null
    ) : StatusBar()

    @Serializable
    @SerialName("Bandwidth")
    data class Bandwidth(
        val merge: Boolean = false,
        val fontSize: Int = 12,
        val colorHex: String? = null
    ) : StatusBar()

    @Serializable
    @SerialName("Notifications")
    data class Notifications(
        val maxIcons: Int = 8,
        val iconSize: Int = 18
    ) : StatusBar()

    @Serializable
    @SerialName("Connectivity")
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
    ) : StatusBar()

    @Serializable
    @SerialName("Spacer")
    data class Spacer(
        val width: Int = -1
    ) : StatusBar()

    @Serializable
    @SerialName("Battery")
    data class Battery(
        val showIcon: Boolean = false,
        val showPercentage: Boolean = true,
        val fontSize: Int = 14,
        val colorHex: String? = null
    ) : StatusBar()

    @Serializable
    @SerialName("NextAlarm")
    data class NextAlarm(
        val formatter: String = "HH:mm",
        val fontSize: Int = 12,
        val colorHex: String? = null
    ) : StatusBar()
}


val allStatusBars = listOf(
    StatusBar.Time(),
    StatusBar.Date(),
    StatusBar.Bandwidth(),
    StatusBar.Notifications(),
    StatusBar.Connectivity(),
    StatusBar.Battery(),
    StatusBar.NextAlarm(),
    StatusBar.Spacer()
)

object StatusBarJson : DragonJson<List<StatusBar>>()
