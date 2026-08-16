plugins {
    alias(libs.plugins.dragon.library)
}

android {
    namespace = "org.elnix.dragonlauncher.shizuku"
}

dependencies {
    implementation(libs.shizuku.api)

    implementation(libs.dragon.logging)
    implementation(libs.timber)

    api(libs.kotlinx.coroutines.core)
}
