import java.nio.file.Paths
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")

}


android {
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.bu.cs683_jabramson_project.iperf3_network_tester"
        namespace = "edu.bu.cs683_jabramson_project.iperf3_network_tester"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Move abiFilters inside ndk block
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    // Native library packaging
    packaging {
        jniLibs {
            pickFirsts += listOf("armeabi-v7a/libc++_shared.so", "arm64-v8a/libc++_shared.so", "x86/libc++_shared.so", "x86_64/libc++_shared.so")
        }
    }
    
    // Prevent compression of iperf3 binaries in assets
    aaptOptions {
        noCompress += "iperf3"
    }

    // ... rest of your android configuration (buildFeatures, compileOptions, etc.) ...


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}



// 1. Define the Exec task (runs your shell script)
val deployPrepScript by tasks.register("deployPrepScript", Exec::class.java) {
    val rootDir = project.rootDir
    val localProperties = File(rootDir, "local.properties")
    if (localProperties.exists()) {
        val properties: Properties = Properties()
        localProperties.inputStream().use {
            properties.load(it)
            var sdkDir = properties.getProperty("sdk.dir")
            val exeName = if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
                "adb.exe"

            } else {
                "adb"
            }
            val scriptName = if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
                "pre-deploy.bat"
            } else {
                "pre-deploy.sh"
            }
            var adb = Paths.get(sdkDir, "platform-tools", exeName)
            var scriptExecutable = Paths.get("$projectDir", "scripts", scriptName)
            commandLine = listOf(scriptExecutable.toString())
            environment("ANDROID_HOME", sdkDir)
            environment("ADB_EXECUTABLE", adb)
        }
    }
}

// 2. Make 'installDebug' (used by AS Run button) depend on your script
//    This runs your script BEFORE installing the APK to emulator/device
afterEvaluate {
    tasks.named("assembleDebug") {
        dependsOn(deployPrepScript)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.leanback)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.compose.android)

    implementation(libs.androidx.hilt.navigation.compose)


    // Hilt Runtime
    implementation(libs.hilt.android.core)

    // Hilt Compiler (USING KSP - REQUIRES KSP PLUGIN APPLIED)
    ksp(libs.hilt.compiler)  // ← NOW USING KSP + CORRECT ARTIFACT

    // Optional: Hilt for testing
    kspTest(libs.hilt.android.testing)


    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.compiler)

    // kapt + room
    implementation(libs.androidx.room.runtime)

    // Room Compiler (via KAPT)
    //kapt(libs.androidx.room.compiler)

    // Optional: Room Kotlin Extensions (recommended for Kotlin)
    implementation(libs.androidx.room.ktx)
}


// ⚠️ CRITICAL: Add this resolution strategy OUTSIDE android/dependencies blocks
configurations.all {
    resolutionStrategy {
        // Force the NEWER, canonical JetBrains annotations to win
        force("org.jetbrains:annotations:23.0.0")

        // EXCLUDE the legacy IntelliJ annotations entirely (safe to do)
        exclude(group = "com.intellij", module = "annotations")
    }
}