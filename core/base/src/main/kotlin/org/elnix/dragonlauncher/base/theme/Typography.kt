package org.elnix.dragonlauncher.base.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.i18n.R

val GoogleSansFlex = FontFamily(
	Font(
		resId = R.font.google_sans_flex,
		variationSettings = FontVariation.Settings(
			FontVariation.Setting("ROND", 100f),
			FontVariation.weight(FontWeight.Bold.weight),
			FontVariation.width(110f)
		)
	)
)

val JetBrainsMono = FontFamily(
	Font(R.font.jetbrains_mono)
)

val Typography: Typography =
	Typography(
		bodyLarge =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 16.sp,
				lineHeight = 24.sp,
				letterSpacing = 0.5.sp
			),
		titleLarge =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 22.sp,
				lineHeight = 28.sp,
				letterSpacing = 0.sp
			),
		labelSmall =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Medium,
				fontSize = 11.sp,
				lineHeight = 16.sp,
				letterSpacing = 0.5.sp
			),
		displayLarge =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 57.sp,
				lineHeight = 64.sp,
				letterSpacing = 0.sp
			),
		displayMedium =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 45.sp,
				lineHeight = 52.sp,
				letterSpacing = 0.sp
			),
		displaySmall =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 36.sp,
				lineHeight = 44.sp,
				letterSpacing = 0.sp
			),
		headlineLarge =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 32.sp,
				lineHeight = 40.sp,
				letterSpacing = 0.sp
			),
		headlineMedium =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 28.sp,
				lineHeight = 36.sp,
				letterSpacing = 0.sp
			),
		headlineSmall =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 24.sp,
				lineHeight = 32.sp,
				letterSpacing = 0.sp
			),
		titleMedium =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Medium,
				fontSize = 16.sp,
				lineHeight = 24.sp,
				letterSpacing = 0.1.sp
			),
		titleSmall =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Medium,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				letterSpacing = 0.1.sp
			),
		bodyMedium =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				letterSpacing = 0.2.sp
			),
		bodySmall =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Normal,
				fontSize = 12.sp,
				lineHeight = 16.sp,
				letterSpacing = 0.4.sp
			),
		labelLarge =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Medium,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				letterSpacing = 0.1.sp
			),
		labelMedium =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Medium,
				fontSize = 12.sp,
				lineHeight = 16.sp,
				letterSpacing = 0.5.sp
			),
		displayLargeEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 57.sp,
				lineHeight = 64.sp,
				letterSpacing = 0.sp
			),
		displayMediumEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 45.sp,
				lineHeight = 52.sp,
				letterSpacing = 0.sp
			),
		displaySmallEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 36.sp,
				lineHeight = 44.sp,
				letterSpacing = 0.sp
			),
		headlineLargeEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 32.sp,
				lineHeight = 40.sp,
				letterSpacing = 0.sp
			),
		headlineMediumEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 28.sp,
				lineHeight = 36.sp,
				letterSpacing = 0.sp
			),
		headlineSmallEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 24.sp,
				lineHeight = 32.sp,
				letterSpacing = 0.sp
			),
		titleLargeEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 22.sp,
				lineHeight = 28.sp,
				letterSpacing = 0.sp
			),
		titleMediumEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 16.sp,
				lineHeight = 24.sp,
				letterSpacing = 0.1.sp
			),
		titleSmallEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				letterSpacing = 0.1.sp
			),
		bodyLargeEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 16.sp,
				lineHeight = 24.sp,
				letterSpacing = 0.5.sp
			),
		bodyMediumEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				letterSpacing = 0.2.sp
			),
		bodySmallEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 12.sp,
				lineHeight = 16.sp,
				letterSpacing = 0.4.sp
			),
		labelLargeEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 14.sp,
				lineHeight = 20.sp,
				letterSpacing = 0.1.sp
			),
		labelMediumEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 12.sp,
				lineHeight = 16.sp,
				letterSpacing = 0.5.sp
			),
		labelSmallEmphasized =
			TextStyle(
				fontFamily = GoogleSansFlex,
				fontWeight = FontWeight.Bold,
				fontSize = 11.sp,
				lineHeight = 16.sp,
				letterSpacing = 0.5.sp
			)
	)
