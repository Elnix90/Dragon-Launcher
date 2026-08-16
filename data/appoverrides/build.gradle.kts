plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.appoverrides"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.hilt.core)
    implementation(libs.timber)

    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.javax.inject)
    api(libs.hilt.android)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)

    api(project(":core:base"))
    implementation(project(":core:settings"))
}
