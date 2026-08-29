package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer

@Immutable
@Serializable
@SerialName("CustomGlow")
public data class CustomGlow(
    @Serializable(with = DpSerializer::class)
    val radius: Dp? = null,
    @Serializable(with = ColorSerializer::class)
    val color: Color? = null
) {
    public companion object {
        @Stable
        public val Unspecified: CustomGlow = CustomGlow(null, null)
    }
}

public fun CustomGlow?.takeDefaults(default: CustomGlow?, constant: CustomGlow): CustomGlow {
    return CustomGlow(
        radius = this?.radius ?: default?.radius ?: constant.radius,
        color = this?.color ?: default?.color ?: constant.color,
    )
}

public val CustomGlow?.isUnSpecified: Boolean
    get() =  this == null || this == CustomGlow.Unspecified

public val CustomGlow?.isSpecified: Boolean
    get() = !this.isUnSpecified