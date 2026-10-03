plugins {
	alias(libs.plugins.dragon.library)
	alias(libs.plugins.dragon.hilt)
}

android {
	namespace = "org.elnix.dragonlauncher.services.widgets"
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlin.reflect)
	implementation(libs.jakarta.inject)
	implementation(libs.hilt.core)
	implementation(libs.timber)
	implementation(libs.hilt.android)
	implementation(libs.settings.core)
	ksp(libs.hilt.compiler)

	api(libs.dagger)
	api(libs.kotlinx.coroutines.core)

	runtimeOnly(libs.kotlinx.coroutines.android)

	implementation(project(":core:settings"))
	api(project(":core:base"))
	implementation(project(":core:i18n"))
	implementation(libs.androidx.compose.ui.unit)
	implementation(libs.kotlinx.serialization.core)
	implementation(libs.kotlinx.serialization.json)
}
