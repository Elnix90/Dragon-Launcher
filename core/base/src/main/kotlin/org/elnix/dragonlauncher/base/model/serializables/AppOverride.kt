package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("AppOverride")
public data class AppOverride(
    val customName: String? = null,
    val customIcon: CustomIcon? = null,
    val customCategory: String? = null,
    val aliases: Set<String>? = null
) {
    public val isNotNullOrEmpty: Boolean
        get() = !customName.isNullOrEmpty() ||
                customIcon != null ||
                !customCategory.isNullOrEmpty() ||
                !aliases.isNullOrEmpty()

    public companion object {
        public val defaultAppOverrides: AppOverrideState = emptyMap()
    }
}

public typealias AppOverrideState = Map<CacheKey, AppOverride>
