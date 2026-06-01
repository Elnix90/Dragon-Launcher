package org.elnix.dragonlauncher.applications

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.StringNormalizer
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.compat.PackageManagerCompat
import org.elnix.dragonlauncher.profiles.ProfileManager

@Module
@InstallIn(SingletonComponent::class)
object ApplicationsModule {
    @Provides
    @Singleton
    @Named("Application")
    fun provideAppRepository(
        ctx: Context,
        profileManager: ProfileManager,
        packageManagerCompat: PackageManagerCompat,
        appOverridesManager: AppOverridesManager,
        stringNormalizer: StringNormalizer,
    ): AppRepository {
        return AppRepositoryImpl(ctx, profileManager, packageManagerCompat, appOverridesManager, stringNormalizer)
    }
}