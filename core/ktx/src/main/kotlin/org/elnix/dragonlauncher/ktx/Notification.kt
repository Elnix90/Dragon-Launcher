package org.elnix.dragonlauncher.ktx

import android.app.Notification
import android.content.Context
import android.graphics.drawable.Drawable

fun Notification.getBadgeIcon(context: Context, packageName: String): Drawable? {
    return smallIcon.loadDrawable(context)
}