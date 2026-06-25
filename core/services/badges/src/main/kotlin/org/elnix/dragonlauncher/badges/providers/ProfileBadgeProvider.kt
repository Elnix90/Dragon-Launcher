package org.elnix.dragonlauncher.badges.providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeIcon
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.profiles.ProfileManager

internal class ProfileBadgeProvider(
    private val profileManager: ProfileManager
) : BadgeProvider {

    override fun getBadge(application: Application): Flow<Badge?> = flow {

            emitAll(
                profileManager.getProfile(application.user).map {
                    when (it?.type) {
                        Profile.Type.Work -> WorkProfile
                        Profile.Type.Private -> PrivateProfile
                        else -> null
                    }
                }
            )
    }

    companion object {
        private val WorkProfile = Badge(
            icon = BadgeIcon(R.drawable.enterprise)
        )

        private val PrivateProfile = Badge(
            icon = BadgeIcon(R.drawable.encrypted)
        )
    }
}