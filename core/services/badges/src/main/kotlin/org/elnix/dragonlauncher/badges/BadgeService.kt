package org.elnix.dragonlauncher.badges

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.badges.providers.NotificationBadgeProvider
import org.elnix.dragonlauncher.badges.providers.ProfileBadgeProvider
import org.elnix.dragonlauncher.badges.providers.SuspendedAppsBadgeProvider
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.notifications.NotificationRepository
import org.elnix.dragonlauncher.profiles.ProfileManager


interface BadgeService {
    fun getBadge(application: Application): Flow<Badge?>
}


internal class BadgeServiceImpl(
    profileManager: ProfileManager,
    notificationRepository: NotificationRepository
) : BadgeService {

    private val badgeProviders = listOf(
        ProfileBadgeProvider(profileManager),
        NotificationBadgeProvider(notificationRepository),
        SuspendedAppsBadgeProvider()
    )


    override fun getBadge(application: Application): Flow<Badge?> {
        return combine(badgeProviders.map { it.getBadge(application) }) { it.filterNotNull() }
            .map { it.combine() }
            .flowOn(Dispatchers.Default)
    }
}