package org.elnix.dragonlauncher.badges.providers

import kotlinx.coroutines.flow.Flow
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.base.model.models.Application

internal interface BadgeProvider {
    /**
     * This must emit a value as soon as possible because the
     * BadgeRepository is waiting for values from every provider.
     * null must be emitted if no badge should be shown.
     */
    fun getBadge(application: Application): Flow<Badge?>
}