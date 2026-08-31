package org.elnix.dragonlauncher.base.model.models

import android.content.ComponentName
import android.content.Context
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.Profile

/**
 * Dummy app
 *
 * This is a class, as it has imported some public properties across the app and therefore made some core icons system completely destroyed
 */
public class DummyApp : Application() {
    override val isSystem: Boolean = false
    override val isLaunchable: Boolean = true
    override val isSuspended: Boolean = false
    override val packageName: String = ""
    override val label: String = ""
    override val defaultLabel: String = ""
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

    override suspend fun loadIcon(themed: Boolean, tint: Int?): LauncherIcon? = null

    override fun getStoreDetails(ctx: Context): StoreLink? = null
}
