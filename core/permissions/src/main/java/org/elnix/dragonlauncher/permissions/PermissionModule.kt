package org.elnix.dragonlauncher.permissions

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {

    @Provides
    @Singleton
    fun providePermissionManager(ctx: Context): PermissionsManager {
        return PermissionsManagerImpl(ctx)
    }
}