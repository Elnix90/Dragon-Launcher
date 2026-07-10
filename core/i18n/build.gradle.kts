plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "org.elnix.dragonlauncher.i18n"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.commons.text) {
        exclude(group = "javax.script")
    }
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}