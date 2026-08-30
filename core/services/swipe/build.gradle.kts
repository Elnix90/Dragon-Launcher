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
    implementation(libs.kotlinx.collections.immutable)
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

    api(project(":core:services:widgets"))

    api(project(":core:settings"))
    api(project(":core:base"))
    api(project(":core:i18n"))
}
