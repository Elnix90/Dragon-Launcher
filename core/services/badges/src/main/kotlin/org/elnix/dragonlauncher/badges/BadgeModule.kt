package org.elnix.dragonlauncher.badges

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
		@ApplicationContext ctx: Context,
		profileManager: ProfileManager,
		notificationRepository: NotificationRepository
	): BadgeService = BadgeServiceImpl(ctx, profileManager, notificationRepository)
}
