package org.elnix.dragonlauncher.security

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SecurityModule {
    @Provides
    @Singleton
    fun provideSecurityService(@ApplicationContext ctx: Context): SecurityService = SecurityServiceImpl(ctx)
}