plugins {
	alias(libs.plugins.dragon.library)
	alias(libs.plugins.dragon.hilt)
}

android {
	namespace = "org.elnix.dragonlauncher.services.notifications"
}

dependencies {
	implementation(libs.kotlin.stdlib)
	implementation(libs.kotlin.reflect)
	implementation(libs.dragon.logging)
	implementation(libs.core)
	api(libs.hilt.core)
	implementation(libs.javax.inject)
	implementation(libs.timber)
	api(libs.hilt.android)

	ksp(libs.hilt.compiler)

	api(libs.dagger)
	api(libs.kotlinx.coroutines.core)

	runtimeOnly(libs.kotlinx.coroutines.android)

	implementation(project(":core:i18n"))
	implementation(libs.androidx.annotation)
	api(libs.jakarta.inject)
}
