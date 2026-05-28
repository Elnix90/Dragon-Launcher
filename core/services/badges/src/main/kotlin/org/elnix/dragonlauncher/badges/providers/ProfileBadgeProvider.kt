package org.elnix.dragonlauncher.badges.providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeIcon
import org.elnix.dragonlauncher.base.profiles.Profile
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.i18n.R.drawable
import org.elnix.dragonlauncher.profiles.ProfileManager

class ProfileBadgeProvider(
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
            icon = BadgeIcon(drawable.enterprise_20px)
        )

        private val PrivateProfile = Badge(
            icon = BadgeIcon(drawable.encrypted_20px)
        )
    }
}