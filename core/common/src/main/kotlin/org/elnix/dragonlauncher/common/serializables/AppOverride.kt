package org.elnix.dragonlauncher.common.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@Serializable
@SerialName("AppOverride")
data class AppOverride(
    val customName: String? = null,
    val customIcon: CustomIcon? = null,
    val customCategory: String? = null,
    val aliases: List<String>? = null
) {
    companion object {
        @OptIn(ExperimentalContracts::class)
        val AppOverride?.isNotNullOrEmpty: Boolean
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
    }
}


@Serializable
data class AppOverrideState(
    val appOverrides: Map<CacheKey, AppOverride> = emptyMap(),
)

