plugins {
	alias(libs.plugins.dragon.library)
	alias(libs.plugins.dragon.hilt)
}

android {
	namespace = "org.elnix.dragonlauncher.services.applaunch"
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlin.reflect)
	implementation(libs.dragon.logging)
	implementation(libs.jakarta.inject)
	implementation(libs.hilt.core)
	implementation(libs.timber)
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	api(libs.dagger)
	api(libs.kotlinx.coroutines.core)

	runtimeOnly(libs.kotlinx.coroutines.android)

	api(project(":core:permissions"))
	api(project(":core:base"))
	api(project(":core:profiles"))
	implementation(project(":core:i18n"))

	api(project(":core:services:recents"))
	implementation(project(":core:services:timer"))
	api(project(":data:applications"))
	implementation(libs.settings.core)
	implementation(project(":core:ktx"))
	api(project(":core:services:compat"))
	implementation(project(":core:settings"))
}
