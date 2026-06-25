package org.elnix.dragonlauncher.badges

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.notifications.NotificationRepository
import org.elnix.dragonlauncher.profiles.ProfileManager


@Module
@InstallIn(SingletonComponent::class)
internal object BadgeModule {

    @Provides
    @Singleton
    fun provideBadgeService(
        profileManager: ProfileManager,
        notificationRepository: NotificationRepository
    ): BadgeService {
        return BadgeServiceImpl(profileManager, notificationRepository)
    }
}