package org.elnix.dragonlauncher.recents

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.applications.AppRepository


@Module
@InstallIn(SingletonComponent::class)
object RecentsModule {

    @Provides
    @Singleton
    fun provideRecentsService(
        @ApplicationContext ctx: Context,
        applicationRepository: AppRepository
    ): RecentsService {
        return RecentsServiceImpl(ctx, applicationRepository)
    }
}