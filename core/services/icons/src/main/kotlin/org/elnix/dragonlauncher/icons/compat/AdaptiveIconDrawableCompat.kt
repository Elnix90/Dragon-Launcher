package org.elnix.dragonlauncher.icons.compat

import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.Xml
import android.view.InflateException
import androidx.core.content.res.ResourcesCompat
import org.elnix.dragonlauncher.ICONS_TAG
import io.github.elnix90.logging.logE
import org.elnix.dragonlauncher.base.icons.ClockLayer
import org.elnix.dragonlauncher.base.icons.ClockSublayer
import org.elnix.dragonlauncher.base.icons.ClockSublayerRole
import org.elnix.dragonlauncher.base.icons.StaticIconLayer
import org.elnix.dragonlauncher.base.icons.StaticLauncherIcon
import org.elnix.dragonlauncher.base.icons.TransparentLayer
import org.elnix.dragonlauncher.ktx.isAtLeastApiLevel
import org.elnix.dragonlauncher.ktx.skipToNextTag
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException


internal data class AdaptiveIconDrawableCompat(
    val background: Drawable,
    val foreground: Drawable,
    val monochrome: Drawable?,
) {

    companion object {
        fun from(adaptiveIconDrawable: AdaptiveIconDrawable): AdaptiveIconDrawableCompat {
            return AdaptiveIconDrawableCompat(
                background = adaptiveIconDrawable.background,
                foreground = adaptiveIconDrawable.foreground,
                monochrome = if (isAtLeastApiLevel(33)) adaptiveIconDrawable.monochrome else null,
            )
        }

        fun from(resources: Resources, resId: Int): AdaptiveIconDrawableCompat? {
            if (isAtLeastApiLevel(33)) {
                return try {
                    val drawable = ResourcesCompat.getDrawable(resources, resId, null)
                    if (drawable is AdaptiveIconDrawable) {
                        from(drawable)
                    } else {
                        null
                    }
                } catch (_: Resources.NotFoundException) {
                    null
                }
            }

            var xmlParser: XmlResourceParser? = null

            try {
                xmlParser = resources.getXml(resId)
                val attrs = Xml.asAttributeSet(xmlParser)
                if (!xmlParser.skipToNextTag()) return null

                if (xmlParser.name != "adaptive-icon") {
                    return null
                }

                var background: Drawable? = null
                var foreground: Drawable? = null
                var monochrome: Drawable? = null

                while (xmlParser.skipToNextTag()) {
                    when (xmlParser.name) {
                        "monochrome" -> {
                            monochrome = parseLayer(resources, xmlParser, attrs)
                        }

                        "background" -> {
                            background = parseLayer(resources, xmlParser, attrs)
                        }

                        "foreground" -> {
                            foreground = parseLayer(resources, xmlParser, attrs)
                        }
                    }
                }
                if (foreground != null && background != null) {
                    return AdaptiveIconDrawableCompat(
                        background = background,
                        foreground = foreground,
                        monochrome = monochrome,
                    )
                }
            } catch (_: Resources.NotFoundException) {
                return null
            } catch (_: IOException) {
                return null
            } catch (_: XmlPullParserException) {
                return null
            } finally {
                xmlParser?.close()
            }
            return null
        }

        @Throws(
            XmlPullParserException::class,
            IOException::class,
            Resources.NotFoundException::class
        )
        private fun parseLayer(
            resources: Resources,
            parser: XmlResourceParser,
            attrs: AttributeSet
        ): Drawable? {
            val drawableId = parser.getAttributeResourceValue(
                "http://schemas.android.com/apk/res/android",
                "drawable",
                0
            )

            if (drawableId != 0) {
                return ResourcesCompat.getDrawable(resources, drawableId, null)
            }
            if (!parser.skipToNextTag()) return null
            return try {
                Drawable.createFromXmlInner(resources, parser, attrs)
            } catch (e: InflateException) {
                logE(ICONS_TAG, e) { "Error parsing icon layer "}
                null
            }
        }
    }
}

internal fun AdaptiveIconDrawableCompat.toLauncherIcon(
    themed: Boolean,
    tint: Int?,
    clock: ClockIconConfig? = null,
): StaticLauncherIcon {
    val clockForeground = (if (themed) monochrome else foreground) as? LayerDrawable
    if (clock != null && clockForeground != null) {
        val clockLayers = (0 until clockForeground.numberOfLayers).map {
            val drw = clockForeground.getDrawable(it)
            ClockSublayer(
                drawable = drw,
                role = when (it) {
                    clock.hourLayer -> ClockSublayerRole.Hour
                    clock.minuteLayer -> ClockSublayerRole.Minute
                    clock.secondLayer -> ClockSublayerRole.Second
                    else -> ClockSublayerRole.Static
                }
            )
        }
        if (themed) {
            return StaticLauncherIcon(
                foregroundLayer = ClockLayer(
                    defaultHour = clock.defaultHour,
                    defaultMinute = clock.defaultMinute,
                    defaultSecond = clock.defaultSecond,
                    sublayers = clockLayers,
                    scale = 1.5f,
                    tint = tint
                ),
                backgroundLayer = TransparentLayer,
            )
        }
        return StaticLauncherIcon(
            foregroundLayer = ClockLayer(
                defaultHour = clock.defaultHour,
                defaultMinute = clock.defaultMinute,
                defaultSecond = clock.defaultSecond,
                sublayers = clockLayers,
                scale = 1.5f,
                tint = tint
            ),
            backgroundLayer = StaticIconLayer(
                icon = this.background,
                scale = 1.5f,
                tint = null
            )
        )
    }

    if (themed && this.monochrome != null) {
        return StaticLauncherIcon(
            foregroundLayer = StaticIconLayer(
                scale = 1.5f,
                icon = this.monochrome,
                tint = tint
            ),
            backgroundLayer = TransparentLayer
        )
    } else {
        return StaticLauncherIcon(
            foregroundLayer = StaticIconLayer(
                scale = 1.5f,
                icon = this.foreground,
                tint = tint
            ),
            backgroundLayer = StaticIconLayer(
                scale = 1.5f,
                icon = this.background,
                tint = null
            )
        )
    }
}

public data class ClockIconConfig(
    val hourLayer: Int,
    val minuteLayer: Int,
    val secondLayer: Int,
    val defaultHour: Int,
    val defaultMinute: Int,
    val defaultSecond: Int,
)