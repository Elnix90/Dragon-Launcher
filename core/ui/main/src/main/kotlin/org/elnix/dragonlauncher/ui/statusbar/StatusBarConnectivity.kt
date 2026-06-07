package org.elnix.dragonlauncher.ui.statusbar

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.StatusBar
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.getMobileDataStatus
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isAirplaneMode
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isBluetoothEnabled
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isHotspotEnabled
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isVpnEnabled
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isWifiEnabled

@Composable
fun StatusBarConnectivity(
    element: StatusBar.Connectivity,
    modifier: Modifier = Modifier,
    previewMode: Boolean = false
) {
    val ctx = LocalContext.current
    var connectivityState by remember {
        mutableStateOf(
            if (previewMode) ConnectivityState(
                isWifiEnabled = true,
                isBluetoothEnabled = true,
                isMobileDataEnabled = true,
                isUsbConnected = true
            ) else ConnectivityState()
        )
    }

    // USB Detection via BroadcastReceiver
    if (!previewMode) {
        DisposableEffect(Unit) {
            val receiver = object : android.content.BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == "android.hardware.usb.action.USB_STATE") {
                        val connected = intent.extras?.getBoolean("connected") ?: false
                        connectivityState = connectivityState.copy(isUsbConnected = connected)
                    }
                }
            }
            ctx.registerReceiver(receiver, IntentFilter("android.hardware.usb.action.USB_STATE"))
            onDispose {
                ctx.unregisterReceiver(receiver)
            }
        }
    }

    // Periodic updates
    if (!previewMode) {
        LaunchedEffect(element.updateFrequency) {
            while (true) {
                connectivityState = readConnectivityState(ctx, connectivityState)
                delay(element.updateFrequency * 1000L)
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (connectivityState.isAirplaneMode && element.showAirplaneMode) {
            Icon(
                painter = painterResource(R.drawable.flight),
                contentDescription = "Airplane",
                modifier = Modifier.size(14.dp)
            )
        }
        if (connectivityState.isWifiEnabled && element.showWifi) {
            Icon(
                painter = painterResource(R.drawable.wifi),
                contentDescription = "WiFi on",
                modifier = Modifier.size(14.dp)
            )
        }

        if (connectivityState.isBluetoothEnabled && element.showBluetooth) {
            Icon(
                painter = painterResource(R.drawable.bluetooth),
                contentDescription = "Bluetooth",
                modifier = Modifier.size(14.dp)
            )
        }

        if (connectivityState.isUsbConnected && element.showUsb) {
            Icon(
                painter = painterResource(R.drawable.usb),
                contentDescription = "USB Connected",
                modifier = Modifier.size(14.dp)
            )
        }

        if (connectivityState.isVpnEnabled && element.showVpn) {
            Icon(
                painter = painterResource(R.drawable.vpn_key),
                contentDescription = "VPN",
                modifier = Modifier.size(14.dp)
            )
        }

        if (!connectivityState.isAirplaneMode && connectivityState.isMobileDataEnabled && element.showMobileData) {
            Icon(
                painter = painterResource(R.drawable.cellular_icon),
                contentDescription = connectivityState.mobileDataStatus,
                modifier = Modifier.size(14.dp)
            )
        }

        if (connectivityState.isHotspotEnabled && element.showHotspot) {
            Icon(
                painter = painterResource(R.drawable.wifi_tethering),
                contentDescription = "Hotspot",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

data class ConnectivityState(
    val isAirplaneMode: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val isVpnEnabled: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val isHotspotEnabled: Boolean = false,
    val isMobileDataEnabled: Boolean = false,
    val isUsbConnected: Boolean = false,
    val mobileDataStatus: String = ""
)

private fun readConnectivityState(ctx: Context, currentState: ConnectivityState = ConnectivityState()): ConnectivityState {
    val (mobileDataEnabled, mobileDataStatus) = ctx.getMobileDataStatus()

    return ConnectivityState(
        isAirplaneMode = ctx.isAirplaneMode(),
        isWifiEnabled = ctx.isWifiEnabled(),
        isVpnEnabled = ctx.isVpnEnabled(),
        isBluetoothEnabled = ctx.isBluetoothEnabled(),
        isHotspotEnabled = ctx.isHotspotEnabled(),
        isMobileDataEnabled = mobileDataEnabled,
        isUsbConnected = currentState.isUsbConnected, // Preserved from BroadcastReceiver
        mobileDataStatus = mobileDataStatus
    )
}