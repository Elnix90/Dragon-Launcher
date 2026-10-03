plugins {
	alias(libs.plugins.dragon.library)
	alias(libs.plugins.dragon.hilt)
}

android {
	namespace = "org.elnix.dragonlauncher.services.logs"
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlin.reflect)
	api(libs.dragon.logging)
	implementation(libs.jakarta.inject)
	implementation(libs.hilt.core)
	implementation(libs.timber)
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	api(libs.dagger)
	api(libs.kotlinx.coroutines.core)

	runtimeOnly(libs.kotlinx.coroutines.android)

	implementation(libs.settings.core)
	implementation(project(":core:settings:"))
  implementation(project(":core:i18n"))
}
