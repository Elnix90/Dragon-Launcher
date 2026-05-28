package org.elnix.dragonlauncher.icons

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.database.AppDatabase
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager


@Module
@InstallIn(SingletonComponent::class)
object IconsModule {

    @Provides
    @Singleton
    fun provideIconPackManager(
        @ApplicationContext ctx: Context,
        appDatabase: AppDatabase
    ): IconPackManager =
        IconPackManager(ctx, appDatabase)

    @Provides
    @Singleton
    fun provideIconsService(
        @ApplicationContext ctx: Context,
        iconsPackManager: IconPackManager,
        appOverridesManager: AppOverridesManager,
        iconSettingsRepository: IconSettingsRepository
    ): IconService {
        return IconService(
            ctx = ctx,
            iconPackManager = iconsPackManager,
            iconSettingsRepository = iconSettingsRepository,
            appOverrideManager = appOverridesManager,
        )
    }
}