package org.elnix.dragonlauncher.applications

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.StringNormalizer
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.profiles.ProfileManager
import org.elnix.dragonlauncher.workspaces.WorkspacesManager

@Module
@InstallIn(SingletonComponent::class)
internal object ApplicationsModule {
	@Provides
	@Singleton
	fun provideAppRepository(
		@ApplicationContext ctx: Context,
		profileManager: ProfileManager,
		pointsService: PointsService,
		packageManagerCompat: PackageManagerCompat,
		appOverridesManager: AppOverridesManager,
		workspacesManager: WorkspacesManager,
		stringNormalizer: StringNormalizer
	): AppRepository =
		AppRepositoryImpl(
			ctx = ctx,
			profileManager = profileManager,
			pointService = pointsService,
			packageManagerCompat = packageManagerCompat,
			appOverridesManager = appOverridesManager,
			workspacesManager = workspacesManager,
			stringNormalizer = stringNormalizer
		)
}
