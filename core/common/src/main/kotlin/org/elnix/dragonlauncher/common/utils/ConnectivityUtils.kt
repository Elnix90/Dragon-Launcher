package org.elnix.dragonlauncher.common.utils

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.elnix.dragonlauncher.common.utils.ConnectivityUtils.isDefaultLauncher
import org.elnix.dragonlauncher.ktx.showToast
import io.github.elnix90.logging.STATUS_BAR_TAG
import io.github.elnix90.logging.logE

object ConnectivityUtils {

    fun Context.isBluetoothEnabled(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    fun Context.isHotspotEnabled(): Boolean {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            showToast("Error fetching hotspot state: $e")
            logE(STATUS_BAR_TAG, e) { "Security Exception fetching hotspot state!"}

            // Fallback to settings
            Settings.Global.getInt(contentResolver, "wifi_ap_state", 0) == 13
        }
    }

    fun Context.isWifiEnabled(): Boolean {
        return try {
            val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled
        } catch (e: SecurityException) {
            showToast("Error fetching internet state: $e")
            logE(STATUS_BAR_TAG, e) { "Security Exception fetching wifi state!"}
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun Context.getMobileDataStatus(): Pair<Boolean, String> {

        val resolver = contentResolver
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


        /*  ─────────────  Mobile data status  ─────────────  */
        // 1. Check if mobile data is enabled (check multiple SIMs)
        val mobileDataEnabled = try {
            Settings.Global.getInt(resolver, "mobile_data", 0) == 1 ||
                    Settings.Global.getInt(resolver, "mobile_data1", 0) == 1 ||
                    Settings.Global.getInt(resolver, "mobile_data2", 0) == 1
        } catch (_: Exception) {
            true // Default to enabled if unable to access
        }

        if (!mobileDataEnabled) return false to "Data OFF"

        // 2. Get active cellular network + signal
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
            // For now, just return network type without signal strength access might require additional permissions
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val networkType = try {
                telephonyManager.dataNetworkType
            } catch (_: Exception) {
                TelephonyManager.NETWORK_TYPE_UNKNOWN
            }

            val typeStr = when (networkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                20 -> "5G" // TelephonyManager.NETWORK_TYPE_NR = 20
                TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"
                else -> "2G"
            }

            val isRoaming = try {
                telephonyManager.isNetworkRoaming
            } catch (_: Exception) {
                false
            }

            return true to (if (isRoaming) "$typeStr (Roaming)" else typeStr)
        }

        return true to "Data ON"
    }

    fun Context.isVpnEnabled(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        }
    }

    fun Context.isAirplaneMode(): Boolean {
        return Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    }
    val Context.isDefaultLauncher: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            } else {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }

                val resolveInfo = packageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

                resolveInfo?.activityInfo?.packageName == packageName
            }
        }
}

@Composable
fun rememberIsDefaultLauncher(): State<Boolean> {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isDefaultLauncher = remember { mutableStateOf(ctx.isDefaultLauncher) }

    LaunchedEffect(lifecycleOwner) {
        snapshotFlow { lifecycleOwner.lifecycle.currentState }
            .collect { state ->
                if (state == Lifecycle.State.RESUMED) {
                    isDefaultLauncher.value = ctx.isDefaultLauncher
                }
            }
    }

    return isDefaultLauncher
}