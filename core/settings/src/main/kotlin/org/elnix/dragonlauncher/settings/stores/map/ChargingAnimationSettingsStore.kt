package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.objects.float
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object ChargingAnimationSettingsStore : MapSettingsStore() {
	@SettingKey
	val color = color(
		default = Color.White,
		title = R.string.charging_animation_color
	)

	@SettingKey
	val sizeMultiplier = float(
		default = 1f,
		title = R.string.charging_animation_size_multiplier,
		description = R.string.charging_animation_size_multiplier_desc,
		allowedRange = 0.5f..5f
	)

	@SettingKey
	val amountMultiplier = int(
		default = 1,
		title = R.string.charging_animation_amount_multiplier,
		description = R.string.charging_animation_amount_multiplier_desc,
		allowedRange = 1..20
	)
}
