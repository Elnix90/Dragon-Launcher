package org.elnix.dragonlauncher.compat

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object Module {

    @Provides
    @Singleton
    fun providePackageManagerCompat(@ApplicationContext ctx: Context): PackageManagerCompat =
        PackageManagerCompatImpl(ctx)
}