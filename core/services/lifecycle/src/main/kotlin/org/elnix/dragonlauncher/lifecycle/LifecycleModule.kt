package org.elnix.dragonlauncher.lifecycle

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LifecycleModule {
	@Provides
	@Singleton
	fun provideAppLaunchService(): LifecycleService = LifecycleServiceImpl()
}
