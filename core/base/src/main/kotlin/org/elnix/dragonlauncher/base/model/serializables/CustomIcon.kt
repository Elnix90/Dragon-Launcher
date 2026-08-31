@file:Suppress("ConstPropertyName")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.AdaptifiedLegacyIcon.Companion.ThemeColor
import org.elnix.dragonlauncher.base.model.serializables.AdaptifiedLegacyIcon.Companion.UnspecifiedColor
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer

@Immutable
@Serializable
@SerialName("CustomIcon")
public sealed class CustomIcon {
    public companion object {
        public fun CustomIcon.getProperties(): CustomIconProperties =
            when (this) {
                is AdaptifiedLegacyIcon -> this.properties
                is CustomIconPackIcon -> this.properties
                is CustomTextIcon -> this.properties
                is CustomThemedIcon -> this.properties
                is DefaultPlaceholderIcon -> this.properties
                is ForceThemedIcon -> this.properties
                is UnmodifiedSystemDefaultIcon -> this.properties
            }

        public fun CustomIcon.setProperties(properties: CustomIconProperties): CustomIcon =
            when (this) {
                is AdaptifiedLegacyIcon -> this.copy(properties = properties)
                is CustomIconPackIcon -> this.copy(properties = properties)
                is CustomTextIcon -> this.copy(properties = properties)
                is CustomThemedIcon -> this.copy(properties = properties)
                is DefaultPlaceholderIcon -> this.copy(properties = properties)
                is ForceThemedIcon -> this.copy(properties = properties)
                is UnmodifiedSystemDefaultIcon -> this.copy(properties = properties)
            }
    }
}

@Immutable
@Serializable
@SerialName("CustomIconPackIcon")
public data class CustomIconPackIcon(
    val iconPackPackage: String,
    val packType: String,
    val drawable: String?,
    val extras: String?,
    val allowThemed: Boolean,
    val tint: Int?,
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

@Immutable
@Serializable
@SerialName("AdaptifiedLegacyIcon")
public data class AdaptifiedLegacyIcon(
    val fgScale: Float,
    /**
     * The background color in ARGB format or [UnspecifiedColor] or [ThemeColor]
     */
    val bgColor: Int = UnspecifiedColor,
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon() {
    public companion object {
        /**
         * Extract color from foreground icon
         */
        public const val UnspecifiedColor: Int = 1

        /**
         * Use color from theme
         */
        public const val ThemeColor: Int = 0
    }
}

@Immutable
@Serializable
@SerialName("CustomThemedIcon")
public data class CustomThemedIcon(
    val iconPackageName: String,
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

@Immutable
@Serializable
@SerialName("ForceThemedIcon")
public data class ForceThemedIcon(
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

/**
 * Use default icon, ignore any icon pack, themed icon or force adaptive settings.
 */
@Immutable
@Serializable
@SerialName("UnmodifiedSystemDefaultIcon")
public data class UnmodifiedSystemDefaultIcon(
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

@Immutable
@Serializable
@SerialName("CustomTextIcon")
public data class CustomTextIcon(
    val text: String,
    val color: Int = 0,
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

/**
 * Use the default placeholder icon
 */
@Immutable
@Serializable
@SerialName("DefaultPlaceholderIcon")
public data class DefaultPlaceholderIcon(
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

@Immutable
@Serializable
public data class CustomIconProperties(
    /** Tint color (ARGB) applied after rendering. */
    @Serializable(with = ColorSerializer::class)
    val tint: Color? = null,
    /** Icon opacity multiplier (0.0 – 1.0). */
    @FloatRange(0.0, 1.0)
    val opacity: Float? = null,
    /** Per-corner radius override for icon clipping. */
    val shape: IconShape? = null,
    /** Rotation applied to the icon in degrees. */
    @IntRange(-180, 180)
    val rotationDeg: Int? = null,
    /** Horizontal scale multiplier. */
    val scaleX: Float? = null,
    /** Vertical scale multiplier. */
    val scaleY: Float? = null
) {
    public val isNotEmpty: Boolean
        get() =
            tint != null ||
                opacity != null ||
                shape != null ||
                rotationDeg != null ||
                scaleX != null ||
                scaleY != null

    public companion object {
        public const val defaultOpacity: Float = 1f
        public const val defaultRotationDeg: Int = 0
        public const val defaultScaleX: Float = 1f
        public const val defaultScaleY: Float = 1f
    }
}
