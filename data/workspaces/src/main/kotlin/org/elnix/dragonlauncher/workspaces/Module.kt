package org.elnix.dragonlauncher.workspaces

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkspaceManagerModule {

    @Provides
    @Singleton
    fun provideWorkspaceManager(@ApplicationContext ctx: Context): WorkspacesManager =
        WorkspacesManager(ctx)
}