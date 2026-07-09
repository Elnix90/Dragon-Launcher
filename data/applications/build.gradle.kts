plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.elnix.dragonlauncher.applications"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":core:base"))
    implementation(project(":core:ktx"))
    implementation(project(":core:common"))
    implementation(project(":core:profiles"))
    implementation(project(":core:services:compat"))
    implementation(project(":data:appoverrides"))
}