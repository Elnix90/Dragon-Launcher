package org.elnix.dragonlauncher.ui.wellbeing

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.timer.UsageStatsReader
import org.elnix.dragonlauncher.timer.UsageStatsReader.AppUsageStats
import javax.inject.Inject

@HiltViewModel
@Stable
class DigitalPauseViewModel
	@Inject
	constructor(
		application: Application,
		private val usageStatsReader: UsageStatsReader
	) : AndroidViewModel(application) {
		var countdown = mutableIntStateOf(60)
		var showChoice = mutableStateOf(false)
		var showTimePicker = mutableStateOf(false)
		var countdownFinished = mutableStateOf(false)
		var currentPhraseIndex = mutableIntStateOf(0)

		@RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
		fun getUsageStats(ctx: Context, packageName: String): AppUsageStats? = usageStatsReader.getUsageStats(ctx, packageName)
	}
