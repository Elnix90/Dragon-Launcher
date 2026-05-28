package org.elnix.dragonlauncher.database
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule{
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context) : AppDatabase =
        AppDatabase.getInstance(ctx)
}