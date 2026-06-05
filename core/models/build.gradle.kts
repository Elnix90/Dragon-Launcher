import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.models"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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


    implementation(libs.hilt.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.dagger)
    api(libs.javax.inject)

    implementation(project(":core:settings"))
    api(project(":core:logging"))
    api(project(":core:common"))
    api(project(":core:i18n"))
    api(project(":core:enumsui"))
    api(project(":core:shizuku"))

    api(project(":core:services:icons"))
    api(project(":core:services:points"))
    api(project(":core:services:recents"))
    api(project(":core:services:colors"))
    api(project(":core:services:fonts"))
    api(project(":core:services:security"))
    api(project(":core:services:compat"))
    api(project(":core:services:timer"))


    api(project(":data:applications"))
    api(project(":data:workspaces"))
    api(project(":data:appoverrides"))
    api(project(":core:profiles"))
    api(project(":core:permissions"))
    api(project(":core:ktx"))
}
