import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)
}

extensions.configure<LibraryExtension> {
    namespace = "org.elnix.dragonlauncher.enumsui"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    api(libs.androidx.compose.runtime)
    api(libs.androidx.ui)

    implementation(project(":core:common"))
}
