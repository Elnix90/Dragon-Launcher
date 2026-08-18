package org.elnix.dragonlauncher.notifications

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object NotificationsModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository()
    }
}