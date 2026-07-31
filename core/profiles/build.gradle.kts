plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
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