import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.ui.theme"

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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    runtimeOnly(libs.androidx.lifecycle.process)


    api(libs.androidx.compose.ui.text)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.ui.graphics)

    implementation(project(":core:ui:base"))
    implementation(project(":core:ui:composition"))

    api(project(":core:base"))
    api(project(":core:enumsui"))
    implementation(project(":core:settings"))
}
