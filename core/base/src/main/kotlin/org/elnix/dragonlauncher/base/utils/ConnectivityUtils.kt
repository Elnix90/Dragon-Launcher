package org.elnix.dragonlauncher.base.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.logging.logE
import org.elnix.dragonlauncher.STATUS_BAR_TAG
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.permissionsManager

public object ConnectivityUtils {
	public fun Context.isBluetoothEnabled(): Boolean {
		val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
		return bluetoothManager.adapter?.isEnabled == true
	}

	public fun Context.isHotspotEnabled(): Boolean {
		val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
		return try {
			val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
			method.isAccessible = true
			method.invoke(wifiManager) as Boolean
		} catch (e: Exception) {
			showToast("Error fetching hotspot state: $e")
			logE(STATUS_BAR_TAG, e) { "Security Exception fetching hotspot state!" }

			// Fallback to settings
			Settings.Global.getInt(contentResolver, "wifi_ap_state", 0) == 13
		}
	}

	public fun Context.isWifiEnabled(): Boolean =
		try {
			val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
			wifiManager.isWifiEnabled
		} catch (e: SecurityException) {
			showToast("Error fetching internet state: $e")
			logE(STATUS_BAR_TAG, e) { "Security Exception fetching wifi state!" }
			false
		}

	@SuppressLint("MissingPermission")
	public fun Context.getMobileDataStatus(): Pair<Boolean, String> {
		val resolver = contentResolver
		val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

		// Mobile data status
		// 1. Check if mobile data is enabled (check multiple SIMs)
		val mobileDataEnabled =
			try {
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
			val networkType =
				try {
					telephonyManager.dataNetworkType
				} catch (_: Exception) {
					TelephonyManager.NETWORK_TYPE_UNKNOWN
				}

			val typeStr =
				when (networkType) {
					TelephonyManager.NETWORK_TYPE_LTE -> "LTE"

					20 -> "5G"

					// TelephonyManager.NETWORK_TYPE_NR = 20
					TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"

					else -> "2G"
				}

			val isRoaming =
				try {
					telephonyManager.isNetworkRoaming
				} catch (_: Exception) {
					false
				}

			return true to (if (isRoaming) "$typeStr (Roaming)" else typeStr)
		}

		return true to "Data ON"
	}

	public fun Context.isVpnEnabled(): Boolean {
		val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

		return connectivityManager.allNetworks.any { network ->
			connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
		}
	}

	public fun Context.isAirplaneMode(): Boolean =
		Settings.Global.getInt(
			contentResolver,
			Settings.Global.AIRPLANE_MODE_ON,
			0
		) == 1
}

/**
 * Whether this app is the user default home app, kept in sync by the manager.
 *
 * Reads the published [kotlinx.coroutines.flow.StateFlow] instead of running a
 * second, local lifecycle observer, so the value cannot drift from the one the
 * rest of the app sees.
 */
@Composable
public fun rememberIsDefaultLauncher(): State<Boolean> {
	val ctx = LocalContext.current

	return ctx.permissionsManager
		.hasPermission(PermissionGroup.DefaultLauncher)
		.collectAsState()
}
