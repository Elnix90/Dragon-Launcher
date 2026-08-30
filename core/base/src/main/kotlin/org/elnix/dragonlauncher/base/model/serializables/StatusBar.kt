@file:Suppress("ConstPropertyName")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer

@Stable
@Serializable
@SerialName("StatusBar")
public sealed class StatusBar {
    @Stable
    @Serializable
    @SerialName("Time")
    public data class Time(
        val formatter: String = "HH:mm:ss",
        val action: Action? = null,
        val fontSize: Int = 16,
        val isBold: Boolean = false,
        val colorHex: String? = null
    ) : StatusBar()

    @Stable
    @Serializable
    @SerialName("Date")
    public data class Date(
        val formatter: String = "MMM dd",
        val action: Action? = null,
        val fontSize: Int = 14,
        val isBold: Boolean = false,
        val colorHex: String? = null
    ) : StatusBar()

    @Stable
    @Serializable
    @SerialName("Bandwidth")
    public data class Bandwidth(
        val merge: Boolean = false,
        val fontSize: Int = 12,
        val colorHex: String? = null
    ) : StatusBar()

    @Stable
    @Serializable
    @SerialName("Notifications")
    public data class Notifications(
        val maxIcons: Int = defaultMaxIcons,
        @Serializable(with = DpSerializer::class)
        val iconSize: Dp = defaultIconSize
    ) : StatusBar() {
        public companion object {
            public const val defaultMaxIcons: Int = 8
            public val defaultIconSize: Dp = 18.dp
        }
    }

    @Stable
    @Serializable
    @SerialName("Connectivity")
    public data class Connectivity(
        val showAirplaneMode: Boolean = defaultAirplaneMode,
        val showWifi: Boolean = defaultShowWifi,
        val showBluetooth: Boolean = defaultShowBluetooth,
        val showVpn: Boolean = defaultShowVpn,
        val showMobileData: Boolean = defaultShowMobileData,
        val showHotspot: Boolean = defaultShowHotspot,
        val showUsb: Boolean = defaultShowUsb,
        val updateFrequency: Int = defaultUpdateFrequency,
        @Serializable(with = DpSerializer::class)
        val iconSize: Dp = defaultIconSize
    ) : StatusBar() {
        public companion object {
            public const val defaultAirplaneMode: Boolean = true
            public const val defaultShowWifi: Boolean = true
            public const val defaultShowBluetooth: Boolean = true
            public const val defaultShowVpn: Boolean = true
            public const val defaultShowMobileData: Boolean = true
            public const val defaultShowHotspot: Boolean = true
            public const val defaultShowUsb: Boolean = true
            public const val defaultUpdateFrequency: Int = 5
            public val defaultIconSize: Dp = 18.dp
        }
    }

    @Stable
    @Serializable
    @SerialName("Spacer")
    public data class Spacer(
        @Serializable(with = DpSerializer::class)
        val width: Dp = defaultWidth,
        val mode: SpacerMode = defaultSpacerMode
    ) : StatusBar() {
        public enum class SpacerMode {
            Width,
            Fill,
            Cutout
        }

        public companion object {
            public val defaultWidth: Dp = 0.dp
            public val defaultSpacerMode: SpacerMode = SpacerMode.Cutout
        }
    }

    @Stable
    @Serializable
    @SerialName("Battery")
    public data class Battery(
        val showIcon: Boolean = false,
        val showPercentage: Boolean = true,
        val fontSize: Int = 14,
        val colorHex: String? = null
    ) : StatusBar()

    @Stable
    @Serializable
    @SerialName("NextAlarm")
    public data class NextAlarm(
        val formatter: String = "HH:mm",
        val fontSize: Int = 12,
        val colorHex: String? = null
    ) : StatusBar()
}

public val allStatusBars: List<StatusBar> =
    listOf(
        StatusBar.Time(),
        StatusBar.Date(),
        StatusBar.Bandwidth(),
        StatusBar.Notifications(),
        StatusBar.Connectivity(),
        StatusBar.Battery(),
        StatusBar.NextAlarm(),
        StatusBar.Spacer()
    )

public object StatusBarJson : DragonJson<List<StatusBar>>()
