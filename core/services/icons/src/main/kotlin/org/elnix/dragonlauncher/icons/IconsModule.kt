package org.elnix.dragonlauncher.icons

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.appoverrides.AppOverridesManager
import org.elnix.dragonlauncher.appshortcuts.AppShortcutRepository
import org.elnix.dragonlauncher.colors.ColorService
import org.elnix.dragonlauncher.database.AppDatabase
import org.elnix.dragonlauncher.recents.PointsService


@Module
@InstallIn(SingletonComponent::class)
object IconsModule {

    @Provides
    @Singleton
    fun provideIconSettingsRepository(@ApplicationContext ctx: Context): IconSettingsRepository = IconSettingsRepository(ctx)

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
        iconPackManager: IconPackManager,
        iconSettingsRepository: IconSettingsRepository,
        appRepository: AppRepository,
        appOverridesManager: AppOverridesManager,
        shortcutRepository: AppShortcutRepository,
        pointsService: PointsService,
        colorService: ColorService
    ): IconService {
        return IconService(
            ctx = ctx,
            iconPackManager = iconPackManager,
            iconSettingsRepository = iconSettingsRepository,
            appRepository = appRepository,
            shortcutRepository = shortcutRepository,
            appOverrideManager = appOverridesManager,
            pointService = pointsService,
            colorService = colorService
        )
    }
}