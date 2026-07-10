plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "org.elnix.dragonlauncher.shizuku"
}

dependencies {
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.dragon.logging)

    api(libs.kotlinx.coroutines.core)

    implementation(project(":core:common"))
}
