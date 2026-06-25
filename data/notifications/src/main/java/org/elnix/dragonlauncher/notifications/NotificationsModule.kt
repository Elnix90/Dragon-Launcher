package org.elnix.dragonlauncher.notifications

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.elnix.dragonlauncher.permissions.PermissionsManager
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object NotificationsModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository()
    }

    @Provides
    @Singleton
    fun provideNotificationService(
        notificationRepository: NotificationRepository,
        permissionManager: PermissionsManager
    ): NotificationService {
        return NotificationService(
            notificationRepository = notificationRepository,
            permissionsManager = permissionManager
        )
    }
}