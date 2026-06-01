package org.elnix.dragonlauncher.badges.providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.MutableBadge
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.notifications.NotificationRepository

class NotificationBadgeProvider(
    private val notificationRepository: NotificationRepository
) : BadgeProvider {

    override fun getBadge(application: Application): Flow<Badge?> {
        val packageName = application.componentName.packageName
        return notificationRepository.notifications.map { notifs ->
            notifs.filter { it.packageName == packageName && it.canShowBadge }
        }.map { notifs ->
            if (notifs.isEmpty()) {
                return@map null
            } else {
                val badge = MutableBadge(
                    number = notifs.sumOf {
                        if (it.canShowBadge && !it.isGroupSummary) it.number
                        else 0
                    },
                    progress = notifs.mapNotNull {
                        val progress = it.progress ?: return@mapNotNull null
                        val progressMax = it.progressMax ?: return@mapNotNull null
                        return@mapNotNull progress.toFloat() / progressMax.toFloat()
                    }
                        .takeIf { it.isNotEmpty() }
                        ?.let { notif ->
                            notif.sumOf { it.toDouble() }.toFloat() / notif.size
                        }
                )
                return@map badge
            }
        }
    }
}