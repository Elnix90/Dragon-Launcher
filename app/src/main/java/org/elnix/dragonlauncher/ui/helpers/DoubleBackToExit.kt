package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext


@Composable
fun DoubleBackToExit() {
    var lastBackPress by remember { mutableLongStateOf(0L) }
    val ctx = LocalContext.current

//    val requirePressingBackTwiceToExit by BehaviorSettingsStore.getRequirePressingBackTwiceToExit(ctx)
//        .collectAsState(initial = true)
//
//    val doubleBAckFeedback by BehaviorSettingsStore.getDoubleBackFeedback(ctx)
//        .collectAsState(initial = false)


//    BackHandler(requirePressingBackTwiceToExit) {
//        val now = System.currentTimeMillis()
//        if (now - lastBackPress < 2000L) {
//            (ctx as Activity).finish()
//        } else {
//            lastBackPress = now
//            if (doubleBAckFeedback) {
//                Toast.makeText(ctx, "Press back again to exit", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}
