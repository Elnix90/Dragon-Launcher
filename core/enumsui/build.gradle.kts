plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.compose)
}

android {
    namespace = "org.elnix.dragonlauncher.enumsui"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    api(libs.androidx.compose.runtime)

    implementation(project(":core:common"))
}
