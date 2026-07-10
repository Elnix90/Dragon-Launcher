plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "org.elnix.dragonlauncher.services.notifications"
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.dragon.logging)
    implementation(libs.hilt.android)

    ksp(libs.hilt.compiler)

    implementation(project(":core:permissions"))

}