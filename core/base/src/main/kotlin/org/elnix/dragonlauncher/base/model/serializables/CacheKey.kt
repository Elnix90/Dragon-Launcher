package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.models.Application

/**
 * Builds a stable cache key for an app, a point or a shortcut, or anything
 */
@JvmInline
@Serializable
@SerialName("CacheKey")
value class CacheKey private constructor(
    val cacheKey: String
) {
    /**
     * Icon Service constructors, to retrieve icons and invalide cache when the providers or transformations changes
     */
    constructor(
        data: Any,
        customIconHashCode: Int,
        providersHashCode: Int,
        transformationsHashcode: Int
    ) : this(cacheKey = data.toString() + customIconHashCode + providersHashCode + transformationsHashcode)

    /**
     * Point constructor, in order to store the key in the cache for points too
     * @param point Takes the point id, which is a [java.util.UUID] in [String] format
     */
    constructor(
        point: Point
    ) : this(cacheKey = point.id)

    /**
     * Application constructor
     */
    constructor(
        app: Application
    ) : this(cacheKey = "${app.packageName}#${app.user.hashCode()}")

    constructor(
        packageName: String,
        userId: Int
    ) : this(cacheKey = "$packageName#$userId")


    /**
     * Shortcut constructor
     */
    constructor(
        shortcut: Action.LaunchShortcut
    ) : this(cacheKey = "${shortcut.packageName}#${shortcut.user.hashCode()}#${shortcut.shortcutId}")
}