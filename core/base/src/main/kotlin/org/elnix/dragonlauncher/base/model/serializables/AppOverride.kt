package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@Serializable
@SerialName("AppOverride")
public data class AppOverride(
    val customName: String? = null,
    val customIcon: CustomIcon? = null,
    val customCategory: String? = null,
    val aliases: List<String>? = null
) {
    public companion object {
        @OptIn(ExperimentalContracts::class)
        public val AppOverride?.isNotNullOrEmpty: Boolean
            get() {
                contract {
                    returns(true) implies (this@isNotNullOrEmpty != null)
                }

                return this != null &&
                        customName?.takeIf { it.isNotEmpty() } != null &&
                        customIcon != null &&
                        customCategory?.takeIf { it.isNotEmpty() } != null &&
                        aliases?.takeIf { it.isNotEmpty() } != null
            }

        public val defaultAppOverrides: AppOverrideState = emptyMap()
    }
}

public typealias AppOverrideState = Map<CacheKey, AppOverride>
