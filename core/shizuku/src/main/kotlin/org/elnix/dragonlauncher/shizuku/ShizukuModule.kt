package org.elnix.dragonlauncher.shizuku

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ShizukuModule {
	@Provides
	@Singleton
	fun provideShizukuService(
		@ApplicationContext ctx: Context
	): ShizukuService = ShizukuServiceImpl(ctx)
}
