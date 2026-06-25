package org.elnix.dragonlauncher.appoverrides

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object AppOverridesModule {

    @Provides
    @Singleton
    public fun provideAppOverrides(@ApplicationContext ctx: Context): AppOverridesManager =
        AppOverridesManager(ctx)
}