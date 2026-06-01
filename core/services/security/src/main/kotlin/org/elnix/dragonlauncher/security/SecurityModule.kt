package org.elnix.dragonlauncher.security

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    fun provideSecurityService(): SecurityService = SecurityServiceImpl()
}