plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.i18n"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.commons.lang3)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)
    implementation(libs.javax.inject)
    implementation(libs.dragon.logging)

    ksp(libs.hilt.compiler)

    api(libs.dagger)
    runtimeOnly(libs.dagger.lint.aar)
}
