package org.elnix.dragonlauncher.base.model.models

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.ktx.getInstallSource
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel

public data class LauncherApp(
    private val launcherActivityInfo: LauncherActivityInfo,
    override val versionName: String?,
    override val labelOverride: String? = null,
    override val profile: Profile,
    override val category: AppCategory,
    override val isSuspended: Boolean
) : Application() {
    override val isSystem: Boolean = false
    override val isLaunchable: Boolean = true

    override val label: String
        get() = labelOverride ?: defaultLabel

    override val defaultLabel: String by lazy {
        launcherActivityInfo.label.toString()
    }

    override fun overrideLabel(label: String): Application = this.copy(labelOverride = label)

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

    override suspend fun loadIcon(
        themed: Boolean,
        tint: Int?
    ): LauncherIcon? {
        return try {
            val icon =
                withContext(Dispatchers.IO) {
                    launcherActivityInfo.getIcon(0)
                } ?: return null

            when (icon) {
                is AdaptiveIconDrawable -> {
                    if (themed && isAtLeastApiLevel(33) && icon.monochrome != null) {
                        StaticLauncherIcon(
                            foregroundLayer =
                                StaticIconLayer(
                                    icon = icon.monochrome!!,
                                    tint = tint,
                                    scale = 1.5f
                                ),
                            backgroundLayer = TransparentLayer
                        )
                    } else {
                        StaticLauncherIcon(
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
                }

                else -> {
                    StaticLauncherIcon(
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
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun getStoreDetails(ctx: Context): StoreLink? =
        try {
            val installSourceInfo = ctx.getInstallSource(packageName)

            getStoreLinkForInstaller(installSourceInfo.initiatingPackageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
}
