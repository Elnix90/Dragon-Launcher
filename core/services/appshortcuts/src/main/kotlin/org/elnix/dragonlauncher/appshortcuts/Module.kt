package org.elnix.dragonlauncher.appshortcuts

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.StringNormalizer
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.profiles.ProfileManager


@Module
@InstallIn(SingletonComponent::class)
internal object AppShortcutsModule {

    @Provides
    @Singleton
    fun provideAppShortcutRepository(
        @ApplicationContext ctx: Context,
        permissionManager: PermissionsManager,
        profileManager: ProfileManager,
        stringNormalizer: StringNormalizer
    ): AppShortcutRepository {
        return AppShortcutRepositoryImpl(
            ctx = ctx,
            permissionsManager = permissionManager,
            profileManager = profileManager,
            stringNormalizer = stringNormalizer
        )
    }
}