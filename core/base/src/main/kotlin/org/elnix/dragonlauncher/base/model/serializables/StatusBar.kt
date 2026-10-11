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
sealed class StatusBar {
	@Stable
	@Serializable
	@SerialName("Time")
	data class Time(
		val formatter: String = "HH:mm:ss",
		val action: Action? = null,
		val fontSize: Int = 16,
		val isBold: Boolean = false,
		val colorHex: String? = null
	) : StatusBar()

	@Stable
	@Serializable
	@SerialName("Date")
	data class Date(
		val formatter: String = "MMM dd",
		val action: Action? = null,
		val fontSize: Int = 14,
		val isBold: Boolean = false,
		val colorHex: String? = null
	) : StatusBar()

	@Stable
	@Serializable
	@SerialName("Bandwidth")
	data class Bandwidth(
		val merge: Boolean = false,
		val fontSize: Int = 12,
		val colorHex: String? = null
	) : StatusBar()

	@Stable
	@Serializable
	@SerialName("Notifications")
	data class Notifications(
		val maxIcons: Int = defaultMaxIcons,
		@Serializable(with = DpSerializer::class)
		val iconSize: Dp = defaultIconSize
	) : StatusBar() {
		companion object {
			const val defaultMaxIcons: Int = 8
			val defaultIconSize: Dp = 18.dp
		}
	}

	@Stable
	@Serializable
	@SerialName("Connectivity")
	data class Connectivity(
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
		companion object {
			const val defaultAirplaneMode: Boolean = true
			const val defaultShowWifi: Boolean = true
			const val defaultShowBluetooth: Boolean = true
			const val defaultShowVpn: Boolean = true
			const val defaultShowMobileData: Boolean = true
			const val defaultShowHotspot: Boolean = true
			const val defaultShowUsb: Boolean = true
			const val defaultUpdateFrequency: Int = 5
			val defaultIconSize: Dp = 18.dp
		}
	}

	@Stable
	@Serializable
	@SerialName("Spacer")
	data class Spacer(
		@Serializable(with = DpSerializer::class)
		val width: Dp = defaultWidth,
		val mode: SpacerMode = defaultSpacerMode
	) : StatusBar() {
		enum class SpacerMode {
			Width,
			Fill,
			Cutout
		}

		companion object {
			val defaultWidth: Dp = 0.dp
			val defaultSpacerMode: SpacerMode = SpacerMode.Cutout
		}
	}

	@Stable
	@Serializable
	@SerialName("Battery")
	data class Battery(
		val showIcon: Boolean = false,
		val showPercentage: Boolean = true,
		val fontSize: Int = 14,
		val colorHex: String? = null
	) : StatusBar()

	@Stable
	@Serializable
	@SerialName("NextAlarm")
	data class NextAlarm(
		val formatter: String = "HH:mm",
		val fontSize: Int = 12,
		val colorHex: String? = null
	) : StatusBar()
}

val allStatusBars: List<StatusBar> =
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

object StatusBarJson : DragonJson<List<StatusBar>>()
