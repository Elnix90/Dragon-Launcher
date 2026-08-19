plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.applications"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    api(libs.kotlinx.collections.immutable)
    implementation(libs.jakarta.inject)
    implementation(libs.hilt.core)
    api(libs.settings.core)
    implementation(libs.dragon.logging)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)

    api(project(":core:i18n"))
    api(project(":core:base"))
    api(project(":core:profiles"))
    api(project(":core:settings"))
    api(project(":data:appoverrides"))
    api(project(":data:workspaces"))
    api(project(":core:services:compat"))
}
