plugins {
	alias(libs.plugins.dragon.library)
	alias(libs.plugins.dragon.hilt)
}

android {
	namespace = "org.elnix.dragonlauncher.shizuku"
}

dependencies {
	implementation(libs.shizuku.api)
	implementation(libs.hilt.core)
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)

	implementation(libs.dragon.logging)
	implementation(libs.timber)

	api(libs.kotlinx.coroutines.core)

	implementation(project(":core:i18n"))
	api(project(":core:base"))
	implementation(project(":core:ktx"))
	api(libs.dagger)
	implementation(libs.javax.inject)
}
