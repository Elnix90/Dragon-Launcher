package org.elnix.dragonlauncher.profiles

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.elnix.dragonlauncher.permissions.PermissionsManager
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object ProfileManagerModule {

    @Provides
    @Singleton
    fun provideProfileManager(
        @ApplicationContext ctx: Context,
        permissionsManager: PermissionsManager
    ): ProfileManager  {
        return ProfileManager(ctx, permissionsManager)
    }
}