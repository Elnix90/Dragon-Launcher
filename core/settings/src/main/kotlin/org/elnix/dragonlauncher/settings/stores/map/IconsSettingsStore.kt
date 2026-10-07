package org.elnix.dragonlauncher.settings.stores.map

import androidx.compose.ui.graphics.Color
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object IconsSettingsStore : MapSettingsStore() {
	@SettingKey
	val selectedIconPack =
		string(
			title = R.string.icon_packs,
			default = ""
		)

	@SettingKey
	val useIconTint =
		boolean(
			title = R.string.use_icon_tint,
			description = R.string.use_icon_tint_desc,
			icon = R.drawable.colorize,
			default = false
		)

	@SettingKey
	val iconsTint =
		color(
			title = R.string.icons_tint,
			default = Color.Unspecified
		)

	@SettingKey
	val onlyTintIconPack =
		boolean(
			title = R.string.only_tint_icon_packs,
			description = R.string.only_tint_icon_packs_desc,
			icon = R.drawable.apps,
			default = true
		)

	@SettingKey
	val renderForeground =
		boolean(
			title = R.string.render_foreground,
			description = R.string.render_foreground_desc,
			icon = R.drawable.wallpaper,
			default = true
		)

	@SettingKey
	val renderBackground =
		boolean(
			title = R.string.render_background,
			description = R.string.render_background_desc,
			icon = R.drawable.wallpaper,
			default = true
		)

	@SettingKey
	val themedIcons =
		boolean(
			title = R.string.themed_icons,
			description = R.string.themed_icons_desc,
			icon = R.drawable.wallpaper,
			default = false
		)

	@SettingKey
	val forceThemed =
		boolean(
			title = R.string.force_themed,
			description = R.string.force_themed_icons_desc,
			icon = R.drawable.wallpaper,
			default = false
		)

	@SettingKey
	val adaptify =
		boolean(
			title = R.string.adaptify,
			description = R.string.adaptify_icons_desc,
			icon = R.drawable.question_mark,
			default = false
		)
}
