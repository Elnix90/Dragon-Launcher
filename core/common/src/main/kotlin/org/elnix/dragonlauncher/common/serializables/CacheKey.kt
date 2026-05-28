package org.elnix.dragonlauncher.common.serializables

import android.content.ComponentName
import android.os.UserHandle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Builds a stable cache key for an app icon entry.
 *
 * The key is composed of:
 * - the application [packageName]
 * - a [userId] identifier suffix
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
 */
@JvmInline
@Serializable
@SerialName("CacheKey")
value class CacheKey private constructor(
    val cacheKey: String
) {
    init {
        require(cacheKey.contains('#')) {
            "Cache key needs to contain a '#' separator"
        }
    }

    val packageName: String
        get() = splitCacheKey().packageName
    val userId: Int
        get() = splitCacheKey().userId


    constructor(
        componentName: ComponentName,
        userHandle: UserHandle
    ) : this(cacheKey = "${componentName.packageName}#${userHandle.hashCode()}")

    constructor(
        packageName: String,
        userId: Int?
    ) : this(cacheKey = "${packageName}#${userId ?: 0}")

    constructor(
        cacheKey: CacheKey,
        customIconHashCode: Int,
        providersHashCode: Int,
        transformationsHashcode: Int
    ) : this(cacheKey = cacheKey.cacheKey + customIconHashCode + providersHashCode + transformationsHashcode)

    /**
     * Point constructor, in order to store the key in the cache for points too
     * @param point Takes the point id, which is a [java.util.UUID] in [String] format
     */
    constructor(
        point: Point
    ) : this(cacheKey = "${point.id}#")


    private fun splitCacheKey(): SplitCacheKey {
        return runCatching {
            val (first, second) = cacheKey.split("#", limit = 2)
            SplitCacheKey(first, second.toInt())

        }.getOrElse {
            // Fallback if user has still the old storage way, with no cacheKey
            SplitCacheKey(packageName, 0)
        }
    }


}



private data class SplitCacheKey(
    val packageName: String,
    val userId: Int
)