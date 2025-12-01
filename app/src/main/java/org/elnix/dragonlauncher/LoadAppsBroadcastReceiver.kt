package org.elnix.dragonlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val vm = (context.applicationContext as MyApplication).appViewModel
        vm.viewModelScope.launch {
            vm.reloadApps()
        }
    }
}
