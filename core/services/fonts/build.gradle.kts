plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.fonts"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.settings.core)
    implementation(libs.jakarta.inject)
    implementation(libs.hilt.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.androidx.compose.ui.text)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)
    runtimeOnly(project(":data:notifications"))

    implementation(project(":core:settings"))
}
