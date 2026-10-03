package org.elnix.dragonlauncher.applaunch

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.profiles.ProfileManager
import org.elnix.dragonlauncher.recents.RecentsService

@Module
@InstallIn(SingletonComponent::class)
internal object AppLaunchModule {
	@Provides
	@Singleton
	fun provideLifecycleService(
		@ApplicationContext ctx: Context,
		permissionsManager: PermissionsManager,
		recentsService: RecentsService,
		profileManager: ProfileManager,
		packageManagerCompat: PackageManagerCompat,
		appRepository: AppRepository
	): AppLaunchService =
		AppLaunchServiceImpl(
			ctx = ctx,
			permissionsManager = permissionsManager,
			recentsService = recentsService,
			profileManager = profileManager,
			packageManagerCompat = packageManagerCompat,
			appRepository = appRepository
		)
}
