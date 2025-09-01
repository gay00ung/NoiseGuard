import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("NoiseGuardDatabase") {
            packageName.set("net.lateinit.noiseguard.data.database")
        }
    }
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // SqlDelight
            implementation(libs.android.driver)

            // DI
            implementation(libs.koin.android)

            // Audio Processing
            implementation(libs.android.wave.recorder)

            // Visualization
            implementation(libs.compose.audiowaveform)
            implementation(libs.mpandroidchart)

            // ML/AI
             implementation(libs.tensorflow.lite)
             implementation(libs.tensorflow.lite.support)

            // UI
            implementation(libs.androidx.ui)
            implementation(libs.androidx.material3)
            implementation(libs.accompanist.permissions)

            // Camera (for AR features)
            implementation(libs.androidx.camera.camera2)

            // Work Manager (Background tasks)
            implementation(libs.androidx.work.runtime.ktx)

            // Ads
            implementation(libs.play.services.ads)

            // Analytics
            implementation(libs.firebase.analytics.ktx)
            implementation(libs.firebase.crashlytics.ktx)

            // TFLite Task Audio Library
            implementation(libs.tensorflow.lite.task.audio)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Icons
            implementation(compose.materialIconsExtended)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Database
            implementation(libs.runtime)
            implementation(libs.coroutines.extensions)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Logging
            implementation(libs.napier)

            // Settings/Preferences
            implementation(libs.multiplatform.settings)

            // Network
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Navigation
            implementation(libs.navigation.compose)
        }
        iosMain.dependencies {
            // SqlDelight
            implementation(libs.native.driver)

            // Ktor iOS client
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

    }
}

android {
    namespace = "net.lateinit.noiseguard"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "net.lateinit.noiseguard"
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configurations.all {
        exclude(group = "com.google.ai.edge.litert")

        // 버전 충돌 해결
        resolutionStrategy {
            force("org.tensorflow:tensorflow-lite:2.17.0")
            force("org.tensorflow:tensorflow-lite-support:0.4.4")
            force("org.tensorflow:tensorflow-lite-task-audio:0.4.4")
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}