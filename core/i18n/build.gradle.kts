plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
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
