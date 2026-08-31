plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.services.security"
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.biometric)
    implementation(libs.dragon.logging)
    implementation(libs.core)
    implementation(libs.hilt.core)
    implementation(libs.javax.inject)
    implementation(libs.timber)
    implementation(libs.hilt.android)
    implementation(libs.settings.core)

    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.androidx.fragment)

    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(project(":core:i18n"))
    implementation(project(":core:settings"))
}
