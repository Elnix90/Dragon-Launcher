package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object AngleLineSettingsStore : MapSettingsStore() {
	/** Use the computing of HSV color to produce a color that depends on the angle / progress */
	@SettingKey
	val rgbLine =
		boolean(
			title = R.string.rgb_line_selector,
			description = R.string.rgb_line_selector_description,
			icon = R.drawable.palette,
			default = true
		)

	@SettingKey
	val startAndAngleShareSameRandomAngle =
		boolean(
			title = R.string.start_ang_angle_share_same_random_angle,
			description = R.string.start_ang_angle_share_same_random_angle_desc,
			icon = R.drawable.share,
			default = false
		)

	@SettingKey
	val useSnappedAngleOrRealAngle =
		boolean(
			title = R.string.use_snapped_angle_or_real_angle,
			description = R.string.use_snapped_angle_or_real_angle_desc,
			icon = R.drawable.call_missed,
			default = true
		)

	@SettingKey
	val showLineObjectPreview =
		boolean(
			title = R.string.show_app_line_preview,
			description = R.string.show_app_line_preview_desc,
			icon = R.drawable.polyline,
			default = true
		)

	@SettingKey
	val showAngleLineObjectPreview =
		boolean(
			title = R.string.show_angle_preview,
			description = R.string.show_app_angle_preview_description,
			icon = R.drawable.polyline,
			default = false
		)

	@SettingKey
	val showStartObjectPreview =
		boolean(
			title = R.string.show_start_object_preview,
			description = R.string.show_start_object_preview_desc,
			icon = R.drawable.polyline,
			default = true
		)

	@SettingKey
	val showEndObjectPreview =
		boolean(
			title = R.string.show_end_object_preview,
			description = R.string.show_end_object_preview_desc,
			icon = R.drawable.polyline,
			default = true
		)

	@SettingKey
	val angleLineObjectsOrder =
		string(AngleLineObjects.entries.joinToString(",") { it.name })
}
