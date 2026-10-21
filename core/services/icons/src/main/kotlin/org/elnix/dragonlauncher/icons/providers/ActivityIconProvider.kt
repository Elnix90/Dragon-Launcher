package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.AdaptiveIconDrawable
import io.github.elnix90.logging.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.ICONS_TAG
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel

/**
 * Resolves the icon of a specific launcher activity from an [Action.LaunchActivity].
 *
 * Apps such as the MIUI/HyperOS dialer expose more than one launcher activity, so the icon
 * must come from the activity itself and not from the package's default one (see #468).
 */
internal class ActivityIconProvider(
	private val ctx: Context,
	private val themed: Boolean,
	private val tint: Int?
) : IconProvider {
	override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
		if (action !is Action.LaunchActivity) return null

		val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

		val icon =
			withContext(Dispatchers.IO) {
				try {
					launcherApps
						.getActivityList(action.packageName, action.profile.userHandle)
						.firstOrNull { it.componentName.className == action.activityName }
						?.getIcon(ctx.resources.displayMetrics.densityDpi)
				} catch (e: SecurityException) {
					logE(ICONS_TAG, e) { "Security Exception when getting activity icon; Dragon is probably not the default launcher" }
					null
				} catch (e: Exception) {
					logE(ICONS_TAG, e) { "Failed to get activity icon for ${action.packageName}/${action.activityName}" }
					null
				}
			} ?: return null

		if (icon is AdaptiveIconDrawable) {
			if (themed && isAtLeastApiLevel(33) && icon.monochrome != null) {
				return StaticLauncherIcon(
					foregroundLayer =
						StaticIconLayer(
							icon = icon.monochrome!!,
							scale = 1f,
							tint = tint
						),
					backgroundLayer = ColorLayer()
				)
			}
			return StaticLauncherIcon(
				foregroundLayer =
					icon.foreground?.let {
						StaticIconLayer(
							icon = it,
							scale = 1.5f,
							tint = tint
						)
					} ?: TransparentLayer,
				backgroundLayer =
					icon.background?.let {
						StaticIconLayer(
							icon = it,
							scale = 1.5f,
							tint = tint
						)
					} ?: TransparentLayer
			)
		}

		return StaticLauncherIcon(
			foregroundLayer =
				StaticIconLayer(
					icon = icon,
					scale = 1f,
					tint = tint
				),
			backgroundLayer = TransparentLayer
		)
	}
}
