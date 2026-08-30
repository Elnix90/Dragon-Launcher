package org.elnix.dragonlauncher.fonts

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object FontModule {
    @Provides
    @Singleton
    fun provideFontService(
        @ApplicationContext ctx: Context
    ): FontService = FontServiceImpl(ctx)
}
