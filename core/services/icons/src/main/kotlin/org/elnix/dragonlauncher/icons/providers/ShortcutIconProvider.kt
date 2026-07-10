package org.elnix.dragonlauncher.icons.providers

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.AdaptiveIconDrawable
import io.github.elnix90.logging.ICONS_TAG
import io.github.elnix90.logging.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.appshortcuts.AppShortcutRepository
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel

internal class ShortcutIconProvider(
    private val ctx: Context,
    private val shortcutRepository: AppShortcutRepository,
    private val themed: Boolean,
    private val tint: Int?
) : IconProvider {
    override suspend fun getIcon(action: Action, size: Int): LauncherIcon? {
        if (action !is Action.LaunchShortcut) return null

        val shortcutInfo = shortcutRepository.fromAction(action) ?: return null

        val launcherApps = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val icon = withContext(Dispatchers.IO) {
            try {
                launcherApps.getShortcutIconDrawable(
                    shortcutInfo,
                    ctx.resources.displayMetrics.densityDpi
                )
            } catch (e: SecurityException) {
                logE(ICONS_TAG, e) { "Security Exception when getting shortcut icon; Dragon is probably not the default launcher" }
                null
            } catch (e: NullPointerException) {
                logE(ICONS_TAG, e) { "Failed to get shortcut icon for ${shortcutInfo.`package`}" }
                null
            }
        } ?: return null
        if (icon is AdaptiveIconDrawable) {
            if (themed && isAtLeastApiLevel(33) && icon.monochrome != null) {
                return StaticLauncherIcon(
                    foregroundLayer = StaticIconLayer(
                        scale = 1f,
                        icon = icon.monochrome!!,
                        tint = tint
                    ),
                    backgroundLayer = ColorLayer()
                )
            }
            return StaticLauncherIcon(
                foregroundLayer = icon.foreground?.let {
                    StaticIconLayer(
                        icon = it,
                        scale = 1.5f,
                        tint = tint
                    )
                } ?: TransparentLayer,
                backgroundLayer = icon.background?.let {
                    StaticIconLayer(
                        icon = it,
                        scale = 1.5f,
                        tint = tint
                    )
                } ?: TransparentLayer,
            )
        }
        return StaticLauncherIcon(
            foregroundLayer = StaticIconLayer(
                icon = icon,
                scale = 1f,
                tint = tint
            ),
            backgroundLayer = TransparentLayer
        )
    }
}