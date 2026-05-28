package org.elnix.dragonlauncher

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object I18nModule {
    @Provides
    @Singleton
    fun provideCompatStringNormalizer(): StringNormalizer =
        CompatStringNormalizer()
}