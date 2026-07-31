plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
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
