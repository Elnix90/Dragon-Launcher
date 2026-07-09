plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.elnix.dragonlauncher.profiles"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:base"))
    implementation(project(":core:ktx"))
    implementation(project(":core:permissions"))
}