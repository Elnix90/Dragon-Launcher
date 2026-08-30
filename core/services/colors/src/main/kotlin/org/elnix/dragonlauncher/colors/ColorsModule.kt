package org.elnix.dragonlauncher.colors

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ColorsModule {
    @Provides
    @Singleton
    fun provideColorService(
        @ApplicationContext ctx: Context
    ): ColorService = ColorServiceImpl(ctx)
}
