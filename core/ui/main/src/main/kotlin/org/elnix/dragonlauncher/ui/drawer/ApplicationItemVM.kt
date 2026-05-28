package org.elnix.dragonlauncher.ui.drawer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.unit.IntRect
import androidx.core.app.ActivityOptionsCompat
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.badges.BadgeService
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.notifications.Notification
import org.elnix.dragonlauncher.notifications.NotificationRepository

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ApplicationItemVM @Inject constructor(
    private val badgeService: BadgeService,
    private val notificationRepository: NotificationRepository,
    val iconService: IconService,
) : ListItemViewModel() {


    val application = MutableStateFlow<Application?>(null)
    private val iconSize = MutableStateFlow(0)

    fun init(application: Application, iconSize: Int) {
        this.application.value = application
        this.iconSize.value = iconSize
    }

    val badge = application.flatMapLatest {
        if (it == null) emptyFlow() else badgeService.getBadge(it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val icon = application.combine(iconSize) { sh, sz -> sh to sz }.flatMapLatest { (s, size) ->
        if (s == null || size == 0) emptyFlow() else iconService.getAppIcon(s)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    val notifications = application.flatMapLatest { application ->
        if (application !is Application) emptyFlow()
        else notificationRepository.notifications.map { notifications -> notifications.filter { it.packageName == application.componentName.packageName && !it.isGroupSummary } }
    }

    fun launch(ctx: Context, bounds: IntRect? = null): Boolean {
        val application = application.value ?: return false

        val view = (ctx as? AppCompatActivity)?.window?.decorView
        val options = if (bounds != null && view != null) {
            ActivityOptionsCompat.makeScaleUpAnimation(
                view,
                bounds.left,
                bounds.top,
                bounds.width,
                bounds.height,
            )
        } else {
            ActivityOptionsCompat.makeBasic()
        }
        val bundle = options.toBundle()
        return application.launch(ctx, bundle)
    }

    fun clearNotification(notification: Notification) {
        notificationRepository.cancelNotification(notification)
    }
}