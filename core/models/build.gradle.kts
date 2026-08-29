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
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.timber)
    implementation(libs.core)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.settings.core)

    implementation(libs.hilt.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.javax.inject)
    api(libs.androidx.appcompat)
    api(libs.androidx.compose.material3)
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.text)
    api(libs.androidx.fragment)
    api(libs.dragon.logging)
    api(libs.kotlinx.coroutines.core)

    runtimeOnly(libs.kotlinx.coroutines.android)

    implementation(project(":core:settings"))
    implementation(project(":core:i18n"))
    implementation(project(":core:services:timer"))
    implementation(project(":core:ktx"))

    api(project(":core:base"))
    api(project(":core:shizuku"))
    api(project(":core:permissions"))

    api(project(":core:services:icons"))
    api(project(":core:services:fonts"))
    api(project(":core:services:colors"))
    api(project(":core:services:points"))
    api(project(":core:services:compat"))
    api(project(":core:services:badges"))
    api(project(":core:services:widgets"))
    api(project(":core:services:recents"))
    api(project(":core:services:security"))
    api(project(":core:services:migration"))

    api(project(":data:notifications"))
    api(project(":data:applications"))
    api(project(":data:workspaces"))
    api(project(":data:appoverrides"))
    api(project(":core:profiles"))

}
