package org.elnix.dragonlauncher.ui.statusbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.common.serializables.StatusBarSerializable
import org.elnix.dragonlauncher.common.messyfolder.showToast

@Composable
fun StatusBarBattery(
    element: StatusBarSerializable.Battery
) {
    val ctx = LocalContext.current
    
    // Get initial battery level from sticky intent
    val initialLevel = remember {
        val intent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val lvl = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (lvl >= 0 && scale > 0) (lvl * 100) / scale else 100
    }

    var level by remember { mutableIntStateOf(initialLevel) }

    DisposableEffect(ctx) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (lvl >= 0 && scale > 0) {
                    level = (lvl * 100) / scale
                }
            }
        }

        ctx.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        onDispose {
            try {
                ctx.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore if already unregistered
            }
        }
    }

    AnimatedVisibility(element.showPercentage) {
        Text(
            text = "$level%",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    LaunchedEffect(element.showIcon) {
        if (element.showIcon) {
            ctx.showToast("Not implemented yet") // TODO
        }
    }
}
