package org.elnix.dragonlauncher.applaunch

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.backup.BackupService
import org.elnix.dragonlauncher.backup.BackupServiceIml

@Module
@InstallIn(SingletonComponent::class)
internal object BackupModule {
	@Provides
	@Singleton
	fun provideBackupModule(
		@ApplicationContext ctx: Context
	): BackupService =
		BackupServiceIml(ctx)
}
