package org.elnix.dragonlauncher.base.model.models

import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.AdaptiveIconDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.icons.ColorLayer
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TintedIconLayer
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel

data class SystemApp(
    private val ctx: Context,
    private val applicationInfo: ApplicationInfo,
    override val versionName: String?,
    override val labelOverride: String? = null,
    override val profile: Profile,
    override val isSuspended: Boolean
) : Application() {

    override val isSystem: Boolean = true
    override val isLaunchable: Boolean = false

    override val label: String
        get() = labelOverride ?: applicationInfo.loadLabel(ctx.packageManager).toString()


    override fun overrideLabel(label: String): Application {
        return this.copy(labelOverride = label)
    }

    override val componentName: ComponentName
        get() = buildFakeComponentName(applicationInfo.packageName)

    /**
     * Cached result of the normalized label.
     * First string is the normalizer ID
     * Second string is the normalized label
     */
    override var cachedNormalizerResult: Pair<String, String>? = null


    override val packageName: String
        get() = applicationInfo.packageName

    override val category: AppCategory = AppCategory.Other


    override suspend fun loadIcon(themed: Boolean): LauncherIcon? {
        return try {
            val icon = withContext(Dispatchers.IO) {
                applicationInfo.loadIcon(ctx.packageManager)
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
        } catch (_: Exception) {
            null
        }
    }

    override fun getStoreDetails(ctx: Context): StoreLink? {
        // System apps don't have store links
        return null
    }
}

fun buildFakeComponentName(
    packageName: String
): ComponentName {
    // System apps don't have a launcher activity, create a synthetic one
    val packageName = packageName
    val shortClassName = ".MainActivity" // Fallback class name
    return ComponentName(packageName, packageName + shortClassName)
}