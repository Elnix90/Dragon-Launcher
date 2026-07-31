plugins {
    alias(libs.plugins.dragon.library)
    alias(libs.plugins.dragon.serialization)
    alias(libs.plugins.dragon.hilt)
}

android {
    namespace = "org.elnix.dragonlauncher.models"

    packaging {
        jniLibs.pickFirsts += "META-INF/gradle/incremental.annotation.processors"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.timber)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core)
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.compose.material3)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)

    implementation(libs.hilt.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.javax.inject)

    implementation(project(":core:settings"))
    api(project(":core:common"))
    api(project(":core:i18n"))
    api(project(":core:enumsui"))
    api(project(":core:shizuku"))

    api(project(":core:services:icons"))
    api(project(":core:services:fonts"))
    api(project(":core:services:timer"))
    api(project(":core:services:colors"))
    api(project(":core:services:points"))
    api(project(":core:services:compat"))
    api(project(":core:services:badges"))
    api(project(":core:services:recents"))
    api(project(":core:services:security"))
    api(project(":core:services:migration"))

    api(project(":data:database"))
    api(project(":data:notifications"))
    api(project(":data:applications"))
    api(project(":data:workspaces"))
    api(project(":data:appoverrides"))
    api(project(":core:profiles"))
    api(project(":core:permissions"))
    api(project(":core:ktx"))
}
