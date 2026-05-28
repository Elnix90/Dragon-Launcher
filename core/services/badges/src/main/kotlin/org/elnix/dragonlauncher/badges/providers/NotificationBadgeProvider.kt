package org.elnix.dragonlauncher.badges.providers

import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.MutableBadge
import org.elnix.dragonlauncher.notifications.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.common.search.Application
import org.koin.core.component.inject

class NotificationBadgeProvider : BadgeProvider {
    private val notificationRepository: NotificationRepository by inject()

    override fun getBadge(application: Application): Flow<Badge?> {
        if (searchable !is Application) return flowOf(null)

        val packageName = searchable.componentName.packageName
        return notificationRepository.notifications.map {
            it.filter { it.packageName == packageName && it.canShowBadge }
        }.map {
            if (it.isEmpty()) {
                return@map null
            } else {
                val badge = MutableBadge(
                    number = it.sumOf {
                        if (it.canShowBadge && !it.isGroupSummary) it.number
                        else 0
                    },
                    progress = it.mapNotNull {
                        val progress = it.progress ?: return@mapNotNull null
                        val progressMax = it.progressMax ?: return@mapNotNull null
                        return@mapNotNull progress.toFloat() / progressMax.toFloat()
                    }
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            it.sumOf { it.toDouble() }.toFloat() / it.size
                        }
                )
                return@map badge
            }
        }
    }
}