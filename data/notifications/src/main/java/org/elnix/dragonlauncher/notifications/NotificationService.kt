package org.elnix.dragonlauncher.notifications

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.logging.NOTIFICATIONS_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.permissions.PermissionsManager
import java.lang.ref.WeakReference

class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val permissionsManager: PermissionsManager
) : NotificationListenerService() {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        logD(NOTIFICATIONS_TAG) { "Notification listener connected" }
        permissionsManager.reportNotificationListenerState(true)
        instance = WeakReference(this)

        scope.launch {
            val statusBarNotifications = getNotifications().sortedBy { it.postTime }
            val ranking = Ranking()
            val rankingMap = currentRanking

            val notifications = statusBarNotifications.map {
                rankingMap.getRanking(it.key, ranking)
                Notification(it, ranking)
            }

            notificationRepository.setNotifications(notifications)
        }
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap?) {
        super.onNotificationRankingUpdate(rankingMap)
        scope.launch {
            val notifications = notificationRepository.getNotifications()

            val ranking = Ranking()
            val updatedNotifications = notifications.map {
                rankingMap?.getRanking(it.key, ranking)
                Notification(it, ranking)
            }

            notificationRepository.setNotifications(updatedNotifications)
        }
    }

    private fun getNotifications(): Array<StatusBarNotification> {
        return try {
            activeNotifications
        } catch (_: SecurityException) {
            emptyArray()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        notificationRepository.onNotificationRemoved(sbn.key)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        super.onNotificationPosted(sbn, rankingMap)

        val ranking = Ranking()
        rankingMap.getRanking(sbn.key, ranking)
        val notification = Notification(sbn, ranking)

        notificationRepository.onNotificationPosted(notification)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        permissionsManager.reportNotificationListenerState(false)
        notificationRepository.setNotifications(emptyList())

        logD(NOTIFICATIONS_TAG) { "Notification listener disconnected" }
    }

    companion object {
        private var instance: WeakReference<NotificationService>? = null
        internal fun getInstance(): NotificationService? {
            return instance?.get()
        }
    }
}