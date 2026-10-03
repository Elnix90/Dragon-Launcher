package org.elnix.dragonlauncher.logs

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LogsModule {
	@Provides
	@Singleton
	fun provideLogsModule(
		@ApplicationContext ctx: Context
	): LogsService = LogsServiceIml(ctx)
}
