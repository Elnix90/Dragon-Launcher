package org.elnix.dragonlauncher.widgets

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object WidgetsModule {

    @Provides
    @Singleton
    fun provideWidgetService(
        @ApplicationContext ctx: Context,
    ): WidgetsService = WidgetServiceImpl(ctx)
}