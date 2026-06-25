package org.elnix.dragonlauncher.badges.providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeIcon
import org.elnix.dragonlauncher.badges.MutableBadge
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.i18n.R

internal class SuspendedAppsBadgeProvider : BadgeProvider {

    override fun getBadge(application: Application): Flow<Badge?> =
        if (application.isSuspended) {
            flowOf(MutableBadge(icon = BadgeIcon(R.drawable.hourglass_bottom)))
        } else {
            flowOf(null)
        }
}