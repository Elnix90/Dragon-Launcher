package org.elnix.dragonlauncher.base.model.models

import android.content.ComponentName
import android.content.Context
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Profile

data class PointApp(
    private val point: Point
) : Application() {

    override val isSystem: Boolean = false
    override val isLaunchable: Boolean = true

    override val packageName: String
        get() = when (val action = point.action) {

            is Action.LaunchApp -> action.packageName
            is Action.LaunchShortcut -> action.packageName
            else -> ""
        }

    // TODO
    override val isSuspended: Boolean = false
    override val label: String = ""
    override val labelOverride: String = ""
    override val profile: Profile = Profile.dummy()
    override val versionName: String = ""
    override val category: AppCategory = AppCategory.Other

    override fun overrideLabel(label: String): Application = this

    override val componentName: ComponentName
        get() = buildFakeComponentName(packageName)

    /**
     * Cached result of the normalized label.
     * First string is the normalizer ID
     * Second string is the normalized label
     */
    override var cachedNormalizerResult: Pair<String, String>? = null


    override suspend fun loadIcon(themed: Boolean): LauncherIcon? {
        return null
    }

    override fun getStoreDetails(ctx: Context): StoreLink? {
        // System apps don't have store links
        return null
    }
}