plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
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