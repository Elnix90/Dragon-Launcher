package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.serializables.AdaptifiedLegacyIcon.Companion.ThemeColor
import org.elnix.dragonlauncher.base.model.serializables.AdaptifiedLegacyIcon.Companion.UnspecifiedColor


@Immutable
@Serializable
sealed class CustomIcon {
    companion object {
        fun CustomIcon.getProperties(): CustomIconProperties = when(this) {
            is AdaptifiedLegacyIcon -> this.properties
            is CustomActionIcon -> this.properties
            is CustomIconPackIcon -> this.properties
            is CustomTextIcon -> this.properties
            is CustomThemedIcon -> this.properties
            is DefaultPlaceholderIcon -> this.properties
            is ForceThemedIcon -> this.properties
            is UnmodifiedSystemDefaultIcon -> this.properties
        }

        fun CustomIcon.setProperties(properties: CustomIconProperties): CustomIcon = when (this) {
            is AdaptifiedLegacyIcon -> this.copy(properties = properties)
            is CustomActionIcon -> this.copy(properties = properties)
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
data class CustomActionIcon(
    val action: Action,
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

@Immutable
@Serializable
@SerialName("CustomIconPackIcon")
data class CustomIconPackIcon(
    val iconPackPackage: String,
    val type: String,
    val drawable: String?,
    val extras: String?,
    val allowThemed: Boolean,
    val properties: CustomIconProperties = CustomIconProperties()
): CustomIcon()

@Immutable
@Serializable
@SerialName("AdaptifiedLegacyIcon")
data class AdaptifiedLegacyIcon(
    val fgScale: Float,
    /**
     * The background color in ARGB format or [UnspecifiedColor] or [ThemeColor]
     */
    val bgColor: Int = UnspecifiedColor,
    val properties: CustomIconProperties = CustomIconProperties()
): CustomIcon() {

    companion object {
        /**
         * Extract color from foreground icon
         */
        const val UnspecifiedColor = 1

        /**
         * Use color from theme
         */
        const val ThemeColor = 0
    }
}

@Immutable
@Serializable
@SerialName("CustomThemedIcon")
data class CustomThemedIcon(
    val iconPackageName: String,
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

@Immutable
@Serializable
@SerialName("ForceThemedIcon")
data class ForceThemedIcon(
    val properties: CustomIconProperties = CustomIconProperties()
) : CustomIcon()

/**
 * Use default icon, ignore any icon pack, themed icon or force adaptive settings.
 */
@Immutable
@Serializable
@SerialName("UnmodifiedSystemDefaultIcon")
data class UnmodifiedSystemDefaultIcon(
    val properties: CustomIconProperties = CustomIconProperties()
): CustomIcon()

@Immutable
@Serializable
@SerialName("CustomTextIcon")
data class CustomTextIcon(
    val text: String,
    val color: Int = 0,
    val properties: CustomIconProperties = CustomIconProperties()
): CustomIcon()

/**
 * Use the default placeholder icon
 */
@Immutable
@Serializable
@SerialName("DefaultPlaceholderIcon")
data class DefaultPlaceholderIcon(
    val properties: CustomIconProperties = CustomIconProperties()
): CustomIcon()


@Immutable
@Serializable
data class CustomIconProperties(
    /** Tint color (ARGB) applied after rendering. */
    val tint: Int? = null,

    /** Icon opacity multiplier (0.0 – 1.0). */
    val opacity: Float? = null,

    /** Per-corner radius override for icon clipping. */
    val shape: IconShape? = null,


    /** Rotation applied to the icon in degrees. */
    val rotationDeg: Float? = null,

    /** Horizontal scale multiplier. */
    val scaleX: Float? = null,

    /** Vertical scale multiplier. */
    val scaleY: Float? = null,
)
