package org.elnix.dragonlauncher.common.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.common.serializables.AdaptifiedLegacyIcon.Companion.ThemeColor
import org.elnix.dragonlauncher.common.serializables.AdaptifiedLegacyIcon.Companion.UnspecifiedColor


@Immutable
@Serializable
sealed class CustomIcon {
}


@Immutable
@Serializable
@SerialName("CustomIconPackIcon")
data class CustomActionIcon(
    val action: SwipeAction,
    val properties: CustomIconProperties
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
    val properties: CustomIconProperties
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
    val properties: CustomIconProperties
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
    val properties: CustomIconProperties
) : CustomIcon()

@Immutable
@Serializable
@SerialName("ForceThemedIcon")
data class ForceThemedIcon(
    val properties: CustomIconProperties
) : CustomIcon()

/**
 * Use default icon, ignore any icon pack, themed icon or force adaptive settings.
 */
@Immutable
@Serializable
@SerialName("UnmodifiedSystemDefaultIcon")
data class UnmodifiedSystemDefaultIcon(
    val properties: CustomIconProperties
): CustomIcon()

@Immutable
@Serializable
@SerialName("CustomTextIcon")
data class CustomTextIcon(
    val text: String,
    val color: Int = 0,
    val properties: CustomIconProperties
): CustomIcon()

/**
 * Use the default placeholder icon
 */
@Immutable
@Serializable
@SerialName("DefaultPlaceholderIcon")
data object DefaultPlaceholderIcon: CustomIcon()


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
