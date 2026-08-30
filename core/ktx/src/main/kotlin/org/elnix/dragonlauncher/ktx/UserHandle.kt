package org.elnix.dragonlauncher.ktx

import android.content.Context
import android.os.UserHandle
import android.os.UserManager

public fun UserHandle.getSerialNumber(ctx: Context): Long {
    val userManager = ctx.getSystemService(Context.USER_SERVICE) as UserManager
    return userManager.getSerialNumberForUser(this)
}
