package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.helpers.SwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader


@Composable
fun BehaviorTab(onBack: () -> Unit) {

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

//    val requirePressingBackTwiceToExit by BehaviorSettingsStore.getRequirePressingBackTwiceToExit(ctx)
//        .collectAsState(initial = true)
//
//    val doubleBAckFeedback by BehaviorSettingsStore.getDoubleBackFeedback(ctx)
//        .collectAsState(initial = false)

    val keepScreenOn by BehaviorSettingsStore.getKeepScreenOn(ctx)
        .collectAsState(initial = false)

    SettingsLazyHeader(
        title = stringResource(R.string.behavior),
        onBack = onBack,
        helpText = stringResource(R.string.behavior_help),
        onReset = {
            scope.launch {
                UiSettingsStore.resetAll(ctx)
            }
        }
    ) {
        item {
            SwitchRow(
                keepScreenOn,
                stringResource(R.string.keep_screen_on),
            ) {
                scope.launch { BehaviorSettingsStore.setKeepScreenOn(ctx, it) }
            }
        }
//        item {
//            SwitchRow(
//                requirePressingBackTwiceToExit,
//                stringResource(R.string.require_pressing_back_twice_to_exit),
//            ) {
//                scope.launch {
//                    BehaviorSettingsStore.setRequirePressingBackTwiceToExit(ctx, it)
//                    if (!it) BehaviorSettingsStore.setDoubleBackFeedback(ctx, false)
//                }
//            }
//        }
//
//        if (requirePressingBackTwiceToExit || doubleBAckFeedback) {
//            item {
//                SwitchRow(
//                    requirePressingBackTwiceToExit,
//                    stringResource(R.string.double_back_press_feedback),
//                ) {
//                    scope.launch {
//                        BehaviorSettingsStore.setDoubleBackFeedback(
//                            ctx,
//                            it
//                        )
//                    }
//                }
//            }
//        }
    }
}
