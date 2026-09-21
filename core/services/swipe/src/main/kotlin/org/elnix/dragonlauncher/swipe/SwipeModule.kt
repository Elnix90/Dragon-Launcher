package org.elnix.dragonlauncher.swipe

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.points.NestsNavigationService
import org.elnix.dragonlauncher.widgets.WidgetsService

@Module
@InstallIn(SingletonComponent::class)
internal object SwipeModule {
	@Provides
	@Singleton
	fun provideSwipeService(
		@ApplicationContext ctx: Context,
		widgetsService: WidgetsService,
		nestsNavigationService: NestsNavigationService
	): SwipeService = SwipeServiceImpl(ctx, nestsNavigationService, widgetsService)
}
