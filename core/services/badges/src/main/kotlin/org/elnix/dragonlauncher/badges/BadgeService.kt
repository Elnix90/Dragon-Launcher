package org.elnix.dragonlauncher.badges

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.badges.providers.BadgeProvider
import org.elnix.dragonlauncher.badges.providers.NotificationBadgeProvider
import org.elnix.dragonlauncher.badges.providers.ProfileBadgeProvider
import org.elnix.dragonlauncher.badges.providers.RecentAppBadgeProvider
import org.elnix.dragonlauncher.badges.providers.SuspendedAppsBadgeProvider
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.notifications.NotificationRepository
import org.elnix.dragonlauncher.profiles.ProfileManager
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore

public interface BadgeService {
	public fun getBadge(application: Application): Flow<Badge?>
}

internal class BadgeServiceImpl(
	ctx: Context,
	profileManager: ProfileManager,
	notificationRepository: NotificationRepository
) : BadgeService {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	private val badgeProviders: MutableStateFlow<List<BadgeProvider>> = MutableStateFlow(
		listOf(
			ProfileBadgeProvider(profileManager),
			NotificationBadgeProvider(notificationRepository),
			SuspendedAppsBadgeProvider()
		)
	)

	init {
		scope.launch {
			DrawerSettingsStore.recentlyInstalledAppsDuration.flow(ctx).collect { duration ->
				badgeProviders.value = badgeProviders.value.filter { it !is RecentAppBadgeProvider }
				badgeProviders.value += RecentAppBadgeProvider(duration)
			}
		}
	}

	override fun getBadge(application: Application): Flow<Badge?> =
		combine(badgeProviders.value.map { it.getBadge(application) }) { it.filterNotNull() }
			.map { it.combine() }
			.flowOn(Dispatchers.Default)
}
