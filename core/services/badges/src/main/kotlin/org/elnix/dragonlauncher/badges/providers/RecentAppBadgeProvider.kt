package org.elnix.dragonlauncher.badges.providers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeIcon
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.i18n.R

internal class RecentAppBadgeProvider(
	private val daysToKeep: Int
) : BadgeProvider {
	override fun getBadge(application: Application): Flow<Badge?> {
		val isRecent = application.isRecent(daysToKeep)

		if (!isRecent) return flowOf(null)
		return flow {
			emit(
				Badge(
					icon = BadgeIcon(R.drawable.recent)
				)
			)
		}
	}
}
