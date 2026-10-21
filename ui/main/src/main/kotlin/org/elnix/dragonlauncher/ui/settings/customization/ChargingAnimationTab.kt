package org.elnix.dragonlauncher.ui.settings.customization

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.ChargingAnimationSettingsStore
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.ChargingAnimation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.statusbar.showChargingAnimation

@Composable
fun ChargingAnimationTab() {
	val ctx = LocalContext.current
	val scope = rememberCoroutineScope()

	var showChargingAnimation by showChargingAnimation()
	var isCharging by remember { mutableStateOf(false) }

	DisposableEffect(null) {
		fun update(intent: Intent?) {
			if (intent == null) return
			val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
			isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
		}

		val receiver =
			object : BroadcastReceiver() {
				override fun onReceive(ctx: Context?, intent: Intent?) {
					update(intent)
				}
			}

		val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

		val intent = ctx.registerReceiver(receiver, intentFilter)
		update(intent)

		onDispose {
			ctx.unregisterReceiver(receiver)
		}
	}

	Box {
		SettingsScaffold(
			title = stringResource(R.string.charging_animation),
			helpText = stringResource(R.string.charging_animation_help),
			resetText = stringResource(R.string.charging_animation_reset),
			onReset = {
				scope.launch {
					ChargingAnimationSettingsStore.resetAll(ctx)
				}
			}
		) {
			DragonSettingsGroup {
				SwitchRow(
					title = R.string.charging_animation,
					description = R.string.charging_animation_desc,
					icon = R.drawable.battery_charging,
					state = showChargingAnimation
				) { showChargingAnimation = it }

				Setting(ChargingAnimationSettingsStore.color, enabled = showChargingAnimation)
				Setting(ChargingAnimationSettingsStore.amountMultiplier, enabled = showChargingAnimation)
				Setting(ChargingAnimationSettingsStore.sizeMultiplier, enabled = showChargingAnimation)
			}

			if (!isCharging) {
				Text(
					text = stringResource(R.string.charging_animation_warning_not_charging),
					style = MaterialTheme.typography.bodyMediumEmphasized
				)
			}
		}

		if (showChargingAnimation) {
			ChargingAnimation()
		}
	}
}
