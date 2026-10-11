package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer
import org.elnix.dragonlauncher.ktx.round
import org.elnix.dragonlauncher.ktx.toHexWithAlpha

@Immutable
@Serializable
@SerialName("CustomGlow")
data class CustomGlow(
	@Serializable(with = DpSerializer::class)
	val radius: Dp? = null,
	@Serializable(with = ColorSerializer::class)
	val color: Color? = null
) {
	override fun toString(): String =
		"CustomGlow(\n" +
			"    radius = ${radius?.value?.round(2)}.dp,\n" +
			"    color = ${color?.toHexWithAlpha}\n" +
			")"

	companion object {
		@Stable
		val Unspecified: CustomGlow = CustomGlow(null, null)
	}
}

fun CustomGlow?.takeDefaults(default: CustomGlow?, constant: CustomGlow): CustomGlow =
	CustomGlow(
		radius = this?.radius ?: default?.radius ?: constant.radius,
		color = this?.color ?: default?.color ?: constant.color
	)

val CustomGlow?.isUnSpecified: Boolean
	get() = this == null || this == CustomGlow.Unspecified

val CustomGlow?.isSpecified: Boolean
	get() = !this.isUnSpecified
