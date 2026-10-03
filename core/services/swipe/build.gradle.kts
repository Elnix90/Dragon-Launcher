plugins {
	alias(libs.plugins.dragon.library)
	alias(libs.plugins.dragon.hilt)
}

android {
	namespace = "org.elnix.dragonlauncher.services.swipe"
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlin.reflect)
	implementation(libs.dragon.logging)
	implementation(libs.core)
	implementation(libs.jakarta.inject)
	implementation(libs.hilt.core)
	implementation(libs.timber)
	implementation(libs.hilt.android)
	implementation(libs.settings.core)
	ksp(libs.hilt.compiler)

	api(libs.dagger)
	api(libs.kotlinx.coroutines.core)

	runtimeOnly(libs.kotlinx.coroutines.android)

	api(project(":core:services:points"))
	implementation(project(":core:services:system"))
	api(project(":core:services:widgets"))
	api(project(":core:services:applaunch"))
	api(project(":core:services:lifecycle"))

	api(project(":core:base"))
	implementation(project(":core:i18n"))
	implementation(project(":core:settings"))
	api(project(":core:shizuku"))
	implementation(libs.androidx.compose.animation.core)
	implementation(libs.androidx.compose.foundation)
	api(libs.androidx.compose.runtime)
	api(libs.androidx.compose.ui.geometry)
	implementation(libs.androidx.ui.graphics)
	api(libs.androidx.compose.ui.unit)
	api(libs.androidx.ui)
	implementation(libs.kotlinx.serialization.core)
	implementation(libs.kotlinx.serialization.json)
	implementation(project(":core:ktx"))
	api(project(":data:applications"))
}
