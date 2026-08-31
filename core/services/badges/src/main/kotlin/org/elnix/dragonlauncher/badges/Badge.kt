package org.elnix.dragonlauncher.badges

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

public sealed interface BadgeIcon {
    @JvmInline
    public value class Drawable(
        public val drawable: android.graphics.drawable.Drawable
    ) : BadgeIcon

    @JvmInline
    public value class Vector(
        @param:DrawableRes public val iconRes: Int
    ) : BadgeIcon
}

public fun BadgeIcon(drawable: Drawable): BadgeIcon = BadgeIcon.Drawable(drawable)

public fun BadgeIcon(
    @DrawableRes iconRes: Int
): BadgeIcon = BadgeIcon.Vector(iconRes)

public interface Badge {
    public val number: Int?
    public val progress: Float?
    public val icon: BadgeIcon?
}

public fun Badge(
    number: Int? = null,
    progress: Float? = null,
    icon: BadgeIcon? = null
): Badge = MutableBadge(number, progress, icon)

internal data class MutableBadge(
    override var number: Int? = null,
    override var progress: Float? = null,
    override var icon: BadgeIcon? = null
) : Badge

public fun Collection<Badge>.combine(): Badge? {
    if (isEmpty()) return null
    val badge = MutableBadge()
    var progresses = 0
    forEach {
        if (it.icon != null && badge.icon == null) badge.icon = it.icon
        it.number?.let { a ->
            badge.number?.let { b -> badge.number = a + b } ?: run {
                badge.number = a
            }
        }
        it.progress?.let { a ->
            badge.progress?.let { b ->
                badge.progress = a + b
            } ?: run {
                badge.progress = a
            }
            progresses++
        }
    }
    if (progresses > 0) {
        badge.progress?.let { badge.progress = it / progresses }
    }
    return badge
}
