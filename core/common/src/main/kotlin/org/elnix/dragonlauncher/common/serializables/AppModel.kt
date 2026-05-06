package org.elnix.dragonlauncher.common.serializables

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.WORKSPACES_TAG
import org.elnix.dragonlauncher.logging.logE

@Immutable
data class AppModel(
    val name: String,
    val packageName: String,
    val isEnabled: Boolean,
    val isSystem: Boolean,
    val isWorkProfile: Boolean,
    val isLaunchable: Boolean?,
    val userId: Int? = 0,
    val category: AppCategory,
    val isPrivateProfile: Boolean = false // Android 15+ Private Space
) {
    val action = SwipeActionSerializable.LaunchApp(packageName,isPrivateProfile , userId ?: 0)

    /**
     * Builds a stable cache key for an app icon entry.
     *
     * The key is composed of:
     * - the application package name
     * - a user identifier suffix
     *
     * Format:
     * `packageName#userId`
     *
     * If [userId] is null, `0` is used as a fallback. This ensures:
     * - consistent keys for primary user apps
     * - separation between the same package installed for different users/profiles
     *
     * Example:
     * - `com.example.app#0`
     * - `com.example.app#10`
     *
     * @param packageName The application package name.
     * @param userId The Android user/profile id. Null defaults to 0.
     * @return A unique and deterministic cache key for icon storage.
     */
    val iconCacheKey: CacheKey
        get() = CacheKey("$packageName#${userId ?: 0}")
}


fun CacheKey.splitCacheKey(): Pair<String, Int> {

    try{
        if ("#" in cacheKey) {
            val split = cacheKey.split("#", limit = 2)
            return Pair(split.first(), split.last().toInt())
        }
    } catch (e: Throwable) {
        logE(WORKSPACES_TAG, e) { "Error splitting cacheKey" }
    }
    // Fallback if user has still the old storage way, with no cacheKey
    return Pair(cacheKey, 0)
}

@Serializable
data class CacheKey (
    val cacheKey: String
)

fun SwipeActionSerializable.LaunchApp.toAppModel() =
    AppModel(
        name = packageName,
        packageName = packageName,
        isEnabled = true, // Unknown
        isSystem = false, // Unknown
        isWorkProfile = false, // Unknown
        isLaunchable = true, // Unknown
        userId = userId,
        category = AppCategory.Other,
        isPrivateProfile = false, // Unknown
    )

fun dummyAppModel(
    packageName: String,
    userId: Int = 0
) =
    AppModel(
        name = packageName,
        packageName = packageName,
        isEnabled = true, // Dummy
        isSystem = false, // Dummy
        isWorkProfile = false, // Dummy
        isLaunchable = true, // Dummy
        userId = userId,
        category = AppCategory.Other,
        isPrivateProfile = false, // Dummy
    )

enum class AppCategory {
    Games,
    Audio,
    Video,
    Images,
    Social,
    News,
    Maps,
    Productivity,
    Accessibility,
    Other
}



fun mapSystemCategoryToSection(category: Int): AppCategory {
    return when (category) {
        ApplicationInfo.CATEGORY_GAME -> AppCategory.Games

        ApplicationInfo.CATEGORY_AUDIO -> AppCategory.Audio
        ApplicationInfo.CATEGORY_VIDEO -> AppCategory.Video
        ApplicationInfo.CATEGORY_IMAGE -> AppCategory.Images

        ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.Social
        ApplicationInfo.CATEGORY_NEWS -> AppCategory.News
        ApplicationInfo.CATEGORY_MAPS -> AppCategory.Maps

        ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.Productivity
        ApplicationInfo.CATEGORY_ACCESSIBILITY -> AppCategory.Accessibility

        ApplicationInfo.CATEGORY_UNDEFINED -> AppCategory.Other

        else -> AppCategory.Other
    }
}

fun mapAppToSection(app: ApplicationInfo): AppCategory =
     mapSystemCategoryToSection(app.category)




enum class WorkspaceType {
    ALL,
    USER,
    SYSTEM,
    WORK,
    PRIVATE,  // Android 15+ Private Space
    CUSTOM
}

@Serializable
data class Workspace(
    val id: String,
    val name: String,
    val type: WorkspaceType,
    val appIds: Set<CacheKey>,
    val removedAppIds: Set<CacheKey>?, // Nullable cause I added it in 1.2.2, so if you were on previous versions, it'll cause crash
    val enabled: Boolean
)

@Serializable
data class AppOverride(
    val customName: String? = null,
    val customIcon: CustomIconSerializable? = null,
    val customCategory: String? = null
)



@Serializable
data class WorkspaceState(
    val workspaces: List<Workspace> = defaultWorkspaces,
    val appOverrides: Map<CacheKey, AppOverride> = emptyMap(),
    val appAliases: Map<CacheKey, Set<String>> = emptyMap()
)

fun resolveApp(
    app: AppModel,
    overrides: Map<CacheKey, AppOverride>
): AppModel {
    val o = overrides[app.iconCacheKey] ?: return app
    return app.copy(name = o.customName ?: app.name)
}


// I disable non-user workspaces by default, enable it if you need it (only used for nerds) (those who download my app are btw :) )
val defaultWorkspaces = listOf(
    Workspace("user", "User", WorkspaceType.USER, emptySet(), setOf(CacheKey("org.elnix.dragonlauncher#0")), true),
    Workspace("system", "System", WorkspaceType.SYSTEM, emptySet(), emptySet(), false),
    Workspace("all", "All", WorkspaceType.ALL, emptySet(), emptySet(),  false),
    Workspace("work", "Work", WorkspaceType.WORK, emptySet(), emptySet(),  false),
    Workspace("private", "Private Space", WorkspaceType.PRIVATE, emptySet(), emptySet(), false) // Android 15+ only
)



data class IconPackInfo(
    val packageName: String,
    val name: String,
//    val isManualOnly: Boolean
)
data class IconMapping(val component: String, val drawable: String)
