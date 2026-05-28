package org.elnix.dragonlauncher.badges

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import org.elnix.dragonlauncher.profiles.ProfileManager


@Module
@InstallIn(SingletonComponent::class)
object BadgeModule {

    @Provides
    @Singleton
    fun provideBadgeService(profileManager: ProfileManager): BadgeService {
        return BadgeServiceImpl(profileManager)
    }
}