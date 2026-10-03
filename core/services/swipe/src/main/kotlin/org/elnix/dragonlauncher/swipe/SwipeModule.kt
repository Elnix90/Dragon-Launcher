package org.elnix.dragonlauncher.swipe

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.applaunch.AppLaunchService
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.lifecycle.LifecycleService
import org.elnix.dragonlauncher.points.NestsNavigationService
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.shizuku.ShizukuService
import org.elnix.dragonlauncher.widgets.WidgetsService

@Module
@InstallIn(SingletonComponent::class)
internal object SwipeModule {
	@Provides
	@Singleton
	fun provideSwipeService(
		@ApplicationContext ctx: Context,
		widgetsService: WidgetsService,
		nestsNavigationService: NestsNavigationService,
		pointsService: PointsService,
		appLaunchService: AppLaunchService,
		appRepository: AppRepository,
		shizukuService: ShizukuService,
		lifecycleService: LifecycleService
	): SwipeService = SwipeServiceImpl(
		ctx = ctx,
		nestsNavigationService = nestsNavigationService,
		widgetsService = widgetsService,
		pointsService = pointsService,
		appLaunchService = appLaunchService,
		appRepository = appRepository,
		shizukuService = shizukuService,
		lifecycleService = lifecycleService
	)
}
