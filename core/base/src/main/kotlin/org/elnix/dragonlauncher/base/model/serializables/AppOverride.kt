package org.elnix.dragonlauncher.base.model.serializables

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("AppOverride")
public data class AppOverride(
	val customName: String? = null,
	val customIcon: CustomIcon? = null,
	/**
	 * Properties applied to the default (null [customIcon]) icon.
	 * When [customIcon] is set, its own [CustomIconProperties] are used instead.
	 */
	val iconProperties: CustomIconProperties? = null,
	val customCategory: String? = null,
	val aliases: Set<String>? = null
) {
	val isNotNullOrEmpty: Boolean
		get() =
			!customName.isNullOrEmpty() ||
				customIcon != null ||
				iconProperties?.isNotEmpty == true ||
				!customCategory.isNullOrEmpty() ||
				!aliases.isNullOrEmpty()

	companion object {
		val defaultAppOverrides: AppOverrideState = emptyMap()
	}
}

public typealias AppOverrideState = Map<CacheKey, AppOverride>
