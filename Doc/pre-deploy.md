# AI QUESTION
is it possible to include a small shell script for when android studio deploys an application to an emulator?


# AN ANSWER
##Absolutely! Here's the exact implementation for Kotlin DSL (build.gradle.kts)—the modern, type-safe way to configure Gradle in Android Studio. This hooks your shell script into the deployment process when you click the ▶️ Run button in Android Studio.

##✅ Step-by-Step Solution (Kotlin DSL)
1. Create your shell script (e.g., scripts/pre-deploy.sh)

```shell
#!/bin/bash
echo "🔧 Running pre-deploy script for emulator/device..."
echo "🔧 Turn off selinux for testing purposes"
set -x
adb shell getenforce
adb shell su root  setenforce 0
adb shell getenforce
set +x

# Clear app data (ensures clean state)
#adb shell pm clear com.yourcompany.yourapp

echo "✅ Pre-deploy script completed."
```

## Gradle Kotlin
```gradle
plugins {
    id("com.android.application")
    kotlin("android")
    // ... your other plugins ...
}

android {
    // ... your existing android config ...
}

// ======================
// CUSTOM DEPLOY HOOK (KOTLIN DSL)
// ======================

// 1. Define the Exec task (runs your shell script)
val deployPrepScript by tasks.register(Exec::class) {
    // Path to your script (relative to project root)
    // NOTE: Uses projectDir for safe path resolution in Kotlin DSL
    commandLine = listOf(
        "$projectDir/scripts/pre-deploy.sh".toString()
    )
    
    // Optional: Pass emulator serial number (if needed)
    // commandLine = listOf(
    //     "$projectDir/scripts/pre-deploy.sh".toString(),
    //     System.getenv("ANDROID_SERIAL")
    // )
}

// 2. Make 'installDebug' (used by AS Run button) depend on your script
//    This runs your script BEFORE installing the APK to emulator/device
tasks.named("installDebug") {
    dependsOn(deployPrepScript)
}

// Alternative: Run AFTER install (less common for deployment setup)
// tasks.named("installDebug") {
//     finalizedBy(deployPrepScript)
// }
```
