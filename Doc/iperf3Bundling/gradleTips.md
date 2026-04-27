# Expected Gradle layout
```
app/
 └─ src/
     └─ main/
         ├─ java/… (your Java/Kotlin code)
         ├─ jniLibs/
         │   ├─ arm64-v8a/
         │   │   └─ iperf3          ← executable (or .so)
         │   ├─ armeabi-v7a/
         │   │   └─ iperf3
         │   ├─ x86/
         │   │   └─ iperf3
         │   └─ x86_64/
         │       └─ iperf3
         └─ AndroidManifest.xml
 ```

# Gradle specifics
```
android {
    compileSdk 35

    defaultConfig {
        applicationId "com.example.iperf3demo"
        minSdk 21
        targetSdk 35
        versionCode 1
        versionName "1.0"

        // Tell Gradle which ABIs we ship
        ndk {
            // If you only ship the pre‑built binaries, you can keep them all:
            abiFilters "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
        }

        // Optional: if you want to ship only a subset, list them here.
    }

    // -----------------------------------------------------------------
    // 1️⃣ Enable *ABI splits* so the Play Store can generate separate
    //    APKs for each architecture (reduces download size for users).
    // -----------------------------------------------------------------
    splits {
        abi {
            reset()
            enable true
            universalApk false               // set true if you want a “fat” universal APK
            include "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
            // Optional: limit the number of APKs per channel (e.g., 5)
            // limit 4
        }

        // -----------------------------------------------------------------
        // 2️⃣ (Optional) Configure the APK’s packagingOptions to strip
        //    unwanted files from the native lib directory.
        // -----------------------------------------------------------------
        packagingOptions {
            // Example: drop *.debugsymbols if they made it into the APK
            exclude "META-INF/DEPENDENCIES"
            exclude "META-INF/LICENSE*"
            exclude "META-INF/NOTICE*"
        }
    }

    // -----------------------------------------------------------------
    // 3️⃣ Tell Gradle not to extract native libs into the APK’s
    //    packaged resources (helps with large executables).
    // -----------------------------------------------------------------
    buildFeatures {
        // If you are using Android Studio’s default build system,
        // this flag is not strictly required but can avoid “Failed to
        // extract native libraries” on some devices.
        androidResources true
    }

    // -----------------------------------------------------------------
    // 4️⃣ Enable Java 17 language support (or whatever you need)
    // -----------------------------------------------------------------
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```
