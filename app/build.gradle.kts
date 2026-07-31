import com.android.build.api.dsl.ApplicationExtension
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}


val dotenv = Properties().apply {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

fun env(name: String): String? =
    System.getenv(name) ?: dotenv.getProperty(name)


kotlin {
    jvmToolchain(21)
}

extensions.configure<ApplicationExtension> {
    namespace = "org.elnix.dragonlauncher"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        applicationId = "org.elnix.dragonlauncher"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionName = "4.0.0"
        versionCode = 57
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }


    val hasSigningConfig = env("KEYSTORE_FILE") != null &&
            env("KEYSTORE_PASSWORD") != null &&
            env("KEY_ALIAS") != null &&
            env("KEY_PASSWORD") != null

    signingConfigs {
        create("release") {
            if (hasSigningConfig) {
                storeFile = file(env("KEYSTORE_FILE")!!)
                storePassword = env("KEYSTORE_PASSWORD")!!
                keyAlias = env("KEY_ALIAS")!!
                keyPassword = env("KEY_PASSWORD")!!
            } else {
                println("No signing config found, APK will be unsigned")
            }
        }
    }

    flavorDimensions += listOf("version")
    productFlavors {
        create("beta") {
            dimension = "version"
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"
            resValue("string", "app_name", "Dragon Launcher Beta")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            versionNameSuffix = " (${property("version.code") as String})"

            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
            isMinifyEnabled = false

            // I use signing config because when I try to install the debug app, and that the release is installed, it won't work.
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }

            applicationIdSuffix = ".debug"
            versionNameSuffix =  " (${property("version.code") as String})-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    packaging {
        jniLibs.keepDebugSymbols.add("**/*.so")
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.dagger)
    implementation(libs.hilt.core)
    implementation(libs.dragon.logging)
    implementation(libs.settings.core)
    implementation(libs.settings.runtime)

    ksp(libs.hilt.compiler)

    implementation(project(":ui:base"))
    implementation(project(":ui:main"))
    implementation(project(":ui:theme"))
    implementation(project(":ui:composition"))

    implementation(project(":core:common"))
    implementation(project(":core:models"))
    implementation(project(":core:settings"))

    implementation(project(":core:permissions"))
    implementation(project(":core:services:migration"))
}



// Copy files in the fastlane/metadata dir to the assets folder, where they are compiled and added to the app
tasks.register<Copy>("copyChangelogsToAssets") {
    description = "Copy the fastlane changelogs folder directly to the assets folder to have them in-app"
    from("../fastlane/metadata/android/en-US/changelogs")
    into(file("src/main/assets/changelogs"))
    include("*.txt")
}

// Download the extensions registry from GitHub
tasks.register("downloadExtensionsRegistry") {
    description = "Downloads the extensions registry JSON from GitHub"
    outputs.upToDateWhen { false } // Ignore cache for this task
    val registryUrl = "https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/main/extensions-registry.json"
    val outputFile = file("src/main/assets/extensions-registry.json")

    inputs.property("url", registryUrl)
    outputs.file(outputFile)

    doLast {
        println("Downloading extensions registry from $registryUrl...")
        outputFile.parentFile.mkdirs()
        try {
            URI(registryUrl).toURL().openStream().use { input: InputStream ->
                outputFile.outputStream().use { output: OutputStream ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            println("WARNING: Failed to download extensions registry: ${e.message}")
            // Create an empty registry if download fails to avoid runtime errors
            if (!outputFile.exists()) {
                outputFile.writeText("{\"extensions\": []}")
            }
        }
    }
}

// Use preBuild tasks instead of merge* (they exist in AGP)
if (!gradle.startParameter.taskRequests.any { it.args.contains("buildHealth") }) {
    tasks.named("preBuild") {
        dependsOn("copyChangelogsToAssets")

//        // Only download extensions registry on release builds
//        val isReleaseVariant = gradle.startParameter.taskRequests.any {
//            it.args.any { arg -> arg.contains("Release", ignoreCase = true) }
//        }
//        if (isReleaseVariant) {
            dependsOn("downloadExtensionsRegistry")
//        }
    }
} else {
    println("Gradle in using build health, not running preBuild")
}
