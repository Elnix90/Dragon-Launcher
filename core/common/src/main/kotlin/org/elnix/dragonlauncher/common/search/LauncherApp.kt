package org.elnix.dragonlauncher.common.search

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TintedIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.profiles.Profile
import org.elnix.dragonlauncher.ktx.getInstallSource
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel

data class LauncherApp(
    private val launcherActivityInfo: LauncherActivityInfo,
    override val versionName: String?,
    override val labelOverride: String? = null,
    override val profile: Profile,
    override val category: AppCategory
) : Application() {
    override val isSystem: Boolean = false
    override val isLaunchable: Boolean = true

    override val label: String
        get() = labelOverride ?: launcherActivityInfo.label.toString()


    override fun overrideLabel(label: String): Application {
        return this.copy(labelOverride = label)
    }


    override val componentName: ComponentName
        get() = launcherActivityInfo.componentName


    /**
     * Cached result of the normalized label.
     * First string is the normalizer ID
     * Second string is the normalized label
     */
    override var cachedNormalizerResult: Pair<String, String>? = null

    override val packageName: String
        get() = componentName.packageName


    override suspend fun loadIcon(themed: Boolean): LauncherIcon? {
        return try {
            val icon = withContext(Dispatchers.IO) {
                launcherActivityInfo.getIcon(0)
            } ?: return null

            when (icon) {
                is AdaptiveIconDrawable -> {
                    if (themed && isAtLeastApiLevel(33) && icon.monochrome != null) {
                        StaticLauncherIcon(
                            foregroundLayer = TintedIconLayer(
                                scale = 1.5f,
                                icon = icon.monochrome!!,
                            ),
                            backgroundLayer = ColorLayer()
                        )
                    } else {
                        StaticLauncherIcon(
                            foregroundLayer = icon.foreground?.let {
                                StaticIconLayer(
                                    icon = it,
                                    scale = 1.5f,
                                )
                            } ?: TransparentLayer,
                            backgroundLayer = icon.background?.let {
                                StaticIconLayer(
                                    icon = it,
                                    scale = 1.5f,
                                )
                            } ?: TransparentLayer,
                        )
                    }
                }

                else -> {
                    StaticLauncherIcon(
                        foregroundLayer = StaticIconLayer(
                            icon = icon,
                            scale = 1f,
                        ),
                        backgroundLayer = TransparentLayer
                    )
                }
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun getStoreDetails(ctx: Context): StoreLink? {
        return try {
            val installSourceInfo = ctx.getInstallSource(componentName.packageName)

            getStoreLinkForInstaller(
                installSourceInfo.initiatingPackageName,
                componentName.packageName
            )
        } catch (_: PackageManager.NameNotFoundException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}