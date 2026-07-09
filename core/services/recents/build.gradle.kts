plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.elnix.dragonlauncher.services.points"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.androidx.lifecycle)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:ktx"))
    implementation(project(":data:applications"))
    implementation(project(":data:notifications"))
    implementation(project(":core:settings"))
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)
    implementation(project(":core:profiles"))
    implementation(project(":core:base"))
    implementation(project(":data:applications"))
}