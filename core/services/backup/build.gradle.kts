plugins {
	alias(libs.plugins.dragon.library)
	alias(libs.plugins.dragon.hilt)
}

android {
	namespace = "org.elnix.dragonlauncher.services.backup"
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
	ksp(libs.hilt.compiler)

	api(libs.dagger)
	api(libs.kotlinx.coroutines.core)

	runtimeOnly(libs.kotlinx.coroutines.android)

	api(project(":core:base"))
	implementation(project(":core:i18n"))

	runtimeOnly(libs.androidx.datastore.core)
	implementation(libs.settings.core)
	implementation(project(":core:ktx"))
	implementation(project(":core:settings"))
}
