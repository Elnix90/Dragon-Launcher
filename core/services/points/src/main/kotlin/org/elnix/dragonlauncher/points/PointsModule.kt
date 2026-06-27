package org.elnix.dragonlauncher.points

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object PointsModule {
    @Provides
    @Singleton
    fun providePointsService(@ApplicationContext ctx: Context, ): PointsService = PointsServiceImpl(ctx)
}