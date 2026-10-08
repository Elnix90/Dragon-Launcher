package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.models.PointPreviewTitleOptions

val LocalPointPreviewTitleOptions: ProvidableCompositionLocal<PointPreviewTitleOptions> =
	compositionLocalOf {
		error("No LocalPointPreviewTitleOptions provided")
	}
