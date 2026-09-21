package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore

data class HoldToActivateSettings(
	val holdDelayBeforeStartingLongClickSettings: Int,
	val longCLickSettingsDuration: Int,
	val holdToActivateSettingsTolerance: Dp,
	val showToleranceOnMainScreen: Boolean,
	val rotationsPerSecond: Float,
	val holdRgbLoading: Boolean,
	val pulsingRadius: Float,
	val pulsingRDuration: Int
)

val LocalHoldToActivateSettings: ProvidableCompositionLocal<HoldToActivateSettings> =
	compositionLocalOf { error("No HoldToActivateSettings provided") }

@Composable
fun ProvideHoldToActivateSettings(
	content: @Composable () -> Unit
) {
	val holdDelayBeforeStartingLongClickSettings by HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings.asState()
	val longCLickSettingsDuration by HoldToActivateArcSettingsStore.longCLickSettingsDuration.asState()
	val holdToActivateSettingsTolerance by HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance.asState()
	val rotationsPerSecond by HoldToActivateArcSettingsStore.rotationsPerSecond.asState()
	val holdRgbLoading by HoldToActivateArcSettingsStore.holdRgbLoading.asState()
	val showToleranceOnMainScreen by HoldToActivateArcSettingsStore.showToleranceOnMainScreen.asState()
	val pulsingRadius by HoldToActivateArcSettingsStore.pulsingRadius.asState()
	val pulsingDuration by HoldToActivateArcSettingsStore.pulsingRDuration.asState()

	CompositionLocalProvider(
		LocalHoldToActivateSettings provides HoldToActivateSettings(
			holdDelayBeforeStartingLongClickSettings = holdDelayBeforeStartingLongClickSettings,
			longCLickSettingsDuration = longCLickSettingsDuration,
			holdToActivateSettingsTolerance = holdToActivateSettingsTolerance,
			showToleranceOnMainScreen = showToleranceOnMainScreen,
			rotationsPerSecond = rotationsPerSecond,
			holdRgbLoading = holdRgbLoading,
			pulsingRadius = pulsingRadius,
			pulsingRDuration = pulsingDuration
		),
		content = content
	)
}
