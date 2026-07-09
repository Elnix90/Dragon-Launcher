plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.elnix.dragonlauncher.services.security"

}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.biometric)
    implementation(libs.dragon.logging)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:ktx"))
    implementation(project(":core:base"))
    implementation(project(":core:i18n"))
}