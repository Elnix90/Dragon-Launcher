plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.timer"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.core)
    implementation(libs.androidx.annotation)
    implementation(libs.timber)

    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.hilt.core)
    api(libs.hilt.android)
    api(libs.jakarta.inject)

    runtimeOnly(libs.kotlinx.coroutines.android)
    runtimeOnly(project(":data:notifications"))

    implementation(project(":core:ktx"))
    implementation(project(":core:i18n"))
    implementation(project(":core:settings"))
    api(project(":core:base"))
    api(project(":core:permissions"))
}
