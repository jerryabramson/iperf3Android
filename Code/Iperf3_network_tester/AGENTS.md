# AGENTS.md - Guidelines for Agentic Coding in Iperf3 Network Tester

## Introduction
This document provides guidelines for agentic coding agents working on the Iperf3 Network Tester Android project. It covers build/test/lint commands, code style, and best practices.

## Project Overview
- Android application using Jetpack Compose for UI
- Kotlin programming language
- Gradle build system
- Unit tests with JUnit
- Instrumented tests with AndroidJUnitRunner and Espresso

## Build Commands
### General Gradle Tasks
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew build` - Build all variants and run tests
- `./gradlew clean` - Clean build outputs
- `./gradlew :app:dependencies` - Display project dependencies

### Quick Build
- `./gradlew :app:assembleDebug` - Build only the app module debug variant

## Test Commands
### Running Tests
- `./gradlew test` - Run all local unit tests
- `./gradlew androidTest` - Run all instrumented tests (requires emulator/device)
- `./gradlew :app:testDebugUnitTest` - Run unit tests for debug variant
- `./gradlew :app:connectedAndroidTest` - Run instrumented tests on connected devices

### Running a Single Test
To run a single test method, use:
```bash
./gradlew :app:testDebugUnitTest --tests "edu.bu.cs683_jabramson_project.iperf3_network_tester.ExampleUnitTest.addition_isCorrect"
```

For instrumented tests:
```bash
./gradlew :app:connectedAndroidTest --tests "edu.bu.cs683_jabramson_project.iperf3_network_tester.ExampleInstrumentedTest"
```

### Test Reporting
- Test results appear in `app/build/reports/tests/testDebugUnitTest/index.html`
- Android test results in `app/build/reports/androidTests/connected/`

## Lint Commands
- `./gradlew lint` - Run Android lint checks on all variants
- `./gradlew :app:lintDebug` - Run lint on debug variant
- `./gradlew lintRelease` - Run lint on release variant
- View lint reports at `app/build/reports/lint-results.html`

## Code Style Guidelines

### Imports
- Group imports: android, androidx, other third-party, kotlin, project-specific
- Order imports alphabetically within groups
- Use implicit imports for kotlin.* and java.lang.*
- Avoid wildcard imports; import specific classes
- Remove unused imports (Android Studio will highlight them)

### Formatting
- Use 4 spaces for indentation (not tabs)
- Maximum line length: 100 characters (Android Studio default)
- Place opening braces at end of line (Kotlin convention)
- Place closing braces on their own line
- One statement per line
- Blank lines to separate logical sections
- Trailing commas not used in Kotlin (except when necessary for readability)

### Types
- Prefer immutable data structures (`val` over `var`)
- Use nullable types (`T?`) when absence of value is possible
- Avoid `!!` operator; use safe calls (`?.`) or `let`/`run`/`apply`
- Prefer primitive types when appropriate (Int, Boolean, etc.)
- Use sealed classes for restricted class hierarchies
- Prefer interfaces over abstract classes when possible

### Naming Conventions
- Classes: PascalCase (e.g., `MainActivity`)
- Functions and properties: camelCase (e.g., `onCreate`, `currentProgress`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_PROGRESS`)
- Packages: all lowercase with dots (e.g., `edu.bu.cs683_jabramson_project.iperf3_network_tester`)
- Composable functions: PascalCase (e.g., `Greeting`)
- Test methods: descriptive names using backticks if needed (e.g., `addition_isCorrect`)
- Boolean properties/variables: prefix with `is`, `has`, `should`, or `can` (e.g., `isLoading`)

### Error Handling
- Use Kotlin exceptions for unexpected conditions (`IllegalArgumentException`, `IllegalStateException`)
- Prefer returning `Result<T>` or using `Either` for recoverable errors in business logic
- In UI layer, show errors to user via Snackbar, Toast, or dialog
- Log errors using Android Log class (`Log.e`, `Log.w`) for debugging
- Avoid swallowing exceptions; at least log them
- Use `requireNotNull` for argument validation in public functions

### Comments
- Use KDoc for public APIs (`/** ... */`)
- Use `//` for single-line comments
- Explain why, not what (unless code is complex)
- TODO comments: `// TODO: (jerryabramson) description of work needed`
- Keep comments up-to-date when modifying code
- Remove commented-out code; use version control instead

### Jetpack Compose Specific
- Composable functions should be side-effect free
- Prefer immutable parameters to composables
- Use `remember` for state that survives recomposition
- Use `rememberCoroutineScope` for launching coroutines from composables
- Keep composables small and focused
- Use modifier parameter as first optional parameter (after required params)
- Preview composables with `@Preview` annotation
- Use `androidx.compose.ui.tooling.preview.PreviewParameter` for dynamic previews
- Follow Material Design 3 guidelines for theming and components

### Testing Conventions
- Unit tests: place in `src/test/java`
- Instrumented tests: place in `src/androidTest/java`
- Test class name: `[ClassUnderTest]Test`
- Test method names: descriptive, using backticks if necessary
- Arrange-Act-Assert (AAA) pattern
- Use JUnit 5 (JUnit Jupiter) annotations: `@Test`, `@BeforeEach`, `@AfterEach`
- For Android tests, use `androidx.test.core.app.ApplicationProvider` to get context
- Mock dependencies using Mockito or MockK
- Espresso for UI testing: use `onView`, `perform`, `check`
- Compose UI testing: use `createComposeRule`, `setContent`, `assertIsDisplayed`

## Version Control (Git) Practices
- Commit early and often
- Write clear, descriptive commit messages in present tense
- Format: `<type>: <description>` (e.g., `feat: add progress indicator`)
- Types: feat, fix, docs, style, refactor, perf, test, chore
- Reference issues in commit messages when applicable (e.g., `fix: resolve crash on rotation #123`)
- Keep feature branches short-lived
- Pull requests should include description and screenshots for UI changes
- Ensure code builds and passes tests before creating PR
- Resolve merge conflicts promptly

## Additional Notes
- The project uses Gradle Version Catalogs (libs.versions.toml) for dependency management
- Refer to `gradle/libs.versions.toml` for dependency versions
- Android Studio is the recommended IDE
- Enable "Power Save Mode" only when necessary as it disables inspections
- Run `./gradlew ktlintCheck` if Kotlin linting is configured (not currently in project)
- For Compose previews, use `@Preview` with various parameters (width, height, fontScale, etc.)
- Accessibility: ensure touch targets are at least 48dp, provide content descriptions for icons
- Performance: avoid heavy computations in composables; use `derivedStateOf` for state transformations
- Security: do not hardcode secrets; use Android Keystore or encrypted preferences for sensitive data

## Troubleshooting
- If Gradle sync fails, try `./gradlew --stop` then refresh
- For emulator issues, try wiping data or creating new AVD
- If tests fail due to instrumentation, ensure emulator is running and API level matches
- For Compose preview issues, try invalidating caches and restarting Android Studio

---

```
*This guide is intended to help maintain consistency and quality in the codebase. Adjust as needed based on project evolution.*plugins {
    id("com.android.application")
    kotlin("android")
    // … any other plugins you use …
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.iperf3demo"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // --------------------------------------------------------------
        // 1️⃣  Declare the ABIs you ship.
        // --------------------------------------------------------------
        ndk {
            // `abiFilters` is a **mutable list**.  We start with an empty list
            // and then *append* the ABIs we want.
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        // --------------------------------------------------------------
        // 2️⃣  Optional: ABI splits (so Play Store can serve only the
        //     needed slice).  This block is optional – you can delete it
        //     if you want a single “fat” APK.
        // --------------------------------------------------------------
        splits {
            abi {
                reset()
                enable(true)
                // The list must contain **String** literals, not quoted strings.
                include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
                // If you want a universal APK, set `universalApk = true`
                // universalApk = false   // default is false
            }
        }
    }

    // --------------------------------------------------------------
    // 3️⃣  Packaging options (optional – keep whatever you need)
    // --------------------------------------------------------------
    packagingOptions {
        // Example exclusions – keep or remove as you wish
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE*")
        exclude("META-INF/NOTICE*")
    }

    // --------------------------------------------------------------
    // 4️⃣  Build‑features – this is where `androidResources` lives.
    // --------------------------------------------------------------
    buildFeatures {
        // Turn on the feature you need (e.g. Android resources merging).
        // If you don’t need it, simply delete the whole block.
        androidResources = true
    }

    // --------------------------------------------------------------
    // 5️⃣  Compile options (Java/Kotlin version, etc.)
    // --------------------------------------------------------------
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// --------------------------------------------------------------
// 6️⃣  Application module dependencies
// --------------------------------------------------------------
dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    // … other dependencies you already have …
}
Reasoning

We need to adapt to Jetpack Compose context. User using Jetpack Compose, probably wants to call Iperf3Runner from Compose UI. Need to explain how to run on background thread, use LaunchedEffect, rememberCoroutineScope, etc. Also adjust the runner to not rely on applicationContext but use Context from activity or application. Provide code snippets for composable that triggers test, shows output, handles permissions, maybe use ActivityResultLauncher for permission. Also mention using WorkManager for background. Provide updated Iperf3Runner maybe as a class with suspend function. Provide example composable with button to start test, show text. Also mention adding INTERNET permission. Provide gradle config unchanged. Provide guidance on handling ABI detection. Provide final answer with updated code.

Response

## Running **iperf‑3** from a Jetpack‑Compose UI  
Below is a **complete, copy‑and‑paste‑ready** example that shows how to:

1. **Keep the native binary in `jniLibs/<abi>/`** (exactly as we discussed before).  
2. **Call the runner from a Compose screen** without blocking the UI.  
3. **Request the `INTERNET` permission** (required for any network traffic).  
4. **Display the raw iperf‑3 output** (or a parsed throughput value) inside the UI.  

> **TL;DR** – Add the `Iperf3Runner` object (unchanged except for a small API tweak), request `INTERNET` permission, and launch the test with `LaunchedEffect` / `rememberCoroutineScope()`. The UI code is at the bottom.

---

## 1️⃣ Updated `Iperf3Runner.kt`

We only need a **tiny change**: expose a **suspend** function that receives a `Context`.  
That lets us call it from a coroutine (which Compose already uses).

```kotlin
// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/utils/Iperf3Runner.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.content.Context
import java.io.File

/**
 * Runs the native iperf3 binary that lives under
 * `src/main/jniLibs/<abi>/iperf3`.
 *
 * This is a **suspending** function so it can be called from Compose
 * without blocking the UI thread.
 *
 * @param context   Any Android Context (usually an Activity or Application)
 * @param abi       Folder name under `jniLibs/` that contains the binary
 * @param serverHost Hostname or IP of the iperf3 server
 * @param durationSec Test length in seconds (default = 10)
 * @return Raw stdout of the iperf3 process
 * @throws Exception if the binary cannot be found, executed, or the process fails
 */
suspend fun runIperf3Test(
    context: Context,
    abi: String,
    serverHost: String,
    durationSec: Int = 10
): String {
    // -----------------------------------------------------------
    // 1️⃣ Resolve the binary location: <app‑files-dir>/jniLibs/<abi>/iperf3
    // -----------------------------------------------------------
    val libDir = File(context.filesDir, "jniLibs/$abi")
    val iperfBinary = File(libDir, "iperf3")

    // -----------------------------------------------------------
    // 2️⃣ Verify existence and make it executable (chmod 755)
    // -----------------------------------------------------------
    require(iperfBinary.exists()) {
        "iperf3 binary not found at $iperfBinary"
    }
    iperfBinary.setExecutable(true, false)   // <-- equivalent to chmod 755

    // -----------------------------------------------------------
    // 3️⃣ Build the command line (reverse test, -R)
    // -----------------------------------------------------------
    val command = iperfBinary.absolutePath
    val processBuilder = ProcessBuilder(
        command,
        "-c", serverHost,
        "-t", durationSec.toString(),
        "-R"
    )
    processBuilder.redirectErrorStream(true)   // merge stderr into stdout

    // -----------------------------------------------------------
    // 4️⃣ Launch, capture output, wait for termination
    // -----------------------------------------------------------
    val process = processBuilder.start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()                         // block until finished

    return output
}
```

### Why a **suspend** function?
* Compose runs on the UI thread.  
* If you call a blocking `Process.waitFor()` directly from a composable, the UI freezes.  
* By marking the function `suspend`, you can launch it in a coroutine (`launch`, `produceState`, etc.) and keep the UI responsive.

---

## 2️⃣ Manifest – add the Internet permission

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest ... >
    <uses-permission android:name="android.permission.INTERNET"/>
    <application
        ... >
        ...
    </application>
</manifest>
```

No other changes are required.

---

## 3️⃣ Detect the ABI that matches the device

You can either **hard‑code** a single ABI (e.g., the one you built the binary for) or **detect** it at runtime.  
The detection code below works in a Composable:

```kotlin
private fun currentAbi(context: Context): String {
    // `Build.SUPPORTED_ABIS` returns a Set<String> like ["arm64-v8a", "armeabi-v7a"]
    // Pick the first one that matches a folder we actually have under jniLibs.
    val supported = context.packageManager.getInstalledPackages(0)
        .firstOrNull()?.packageName // dummy just to keep compiler happy – we actually don’t need packages

    // Simpler: just look at the device’s own list:
    val abiSet = android.os.Build.SUPPORTED_ABIS
    return abiSet.firstOrNull() ?: "arm64-v8a"   // fallback
}
```

If you ship **separate binaries for each ABI**, you can simply pick the folder that matches the ABI the device reports as supported:

```kotlin
private fun chooseAbi(context: Context): String {
    val abiList = android.os.Build.SUPPORTED_ABIS
    // The order below mirrors the order we placed the folders in jniLibs/
    for (abi in listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")) {
        if (abiList.contains(abi)) return abi
    }
    throw IllegalStateException("No matching ABI found")
}
```

---

## 4️⃣ Compose UI – launch the test on a background coroutine

Below is a **single‑file** composable that:

* Shows a **Button** “Run iperf‑3 test”.  
* When pressed, requests the `INTERNET` permission (if not already granted).  
* Starts a coroutine, calls `runIperf3Test`, and displays the raw output (or a parsed throughput line).  

> **NOTE** – Starting with Android 13 (API 33) you need to request `android.permission.INTERNET` at runtime only if you target API 33+ **and** you plan to use clear‑text network. For most cases the permission is automatically granted if you declare it in the manifest, but the snippet below shows the safe way to request it.

```kotlin
// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/RunIperf3Screen.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.runIperf3Test

@Composable
fun RunIperf3Screen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // -------------------------------------------------------------------------
    // Permission handling (INTERNET) – only needed on Android 13+ if you use
    // clear‑text network; otherwise the manifest declaration is enough.
    // -------------------------------------------------------------------------
    val hasInternetPermission = remember {
        context.checkSelfPermission(Manifest.permission.INTERNET) ==
                PackageManager.PERMISSION_GRANTED
    }

    // Launcher that shows the permission dialog
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.w("RunIperf3Screen", "Internet permission denied – test cannot run.")
        }
    }

    // -------------------------------------------------------------------------
    // UI State
    // -------------------------------------------------------------------------
    var output by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // -------------------------------------------------------------------------
    // Helper to pick the ABI (you could expose a dropdown instead)
    // -------------------------------------------------------------------------
    val abi = remember { chooseAbi(context) }   // from the snippet in section 3

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("iperf‑3 Test") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // -------------------------------------------------------------
            // Button to start the test
            // -------------------------------------------------------------
            Button(
                onClick = {
                    if (!hasInternetPermission) {
                        // Request permission and abort launching the test
                        requestPermissionLauncher.launch(Manifest.permission.INTERNET)
                        return@Button
                    }
                    // Reset UI state
                    output = null
                    errorMessage = null
                    isRunning = true
                },
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run iperf‑3 Test")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------------------------------------------
            // Show a progress indicator while the test is running
            // -------------------------------------------------------------
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
                Text("Testing… (this may take a few seconds)", modifier = Modifier.padding(8.dp))
            }

            // -------------------------------------------------------------
            // Show raw output or parsed throughput once finished
            // -------------------------------------------------------------
            output?.let { txt ->
                Text("Raw output:", modifier = Modifier.padding(8.dp))
                Text(txt, modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp))
            }

            errorMessage?.let { msg ->
                Text("Error: $msg", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

/**
 * Utility – pick the first ABI that matches a folder we have under jniLibs/.
 * Adjust the order if you want a different priority.
 */
private fun chooseAbi(context: Context): String {
    val abiList = android.os.Build.SUPPORTED_ABIS
    return listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        .firstOrNull { abiList.contains(it) } ?: "arm64-v8a"
}
```

### What’s happening?

| Step | Code | Reason |
|------|------|--------|
| **Permission check** | `hasInternetPermission` | Guarantees we have the `INTERNET` permission before launching the test. |
| **Permission request** | `requestPermissionLauncher.launch(Manifest.permission.INTERNET)` | Pops up the Android permission dialog (only needed on Android 13+ for runtime grants). |
| **ABI selection** | `chooseAbi(context)` | Picks the first supported ABI that actually has a folder in `jniLibs/`. |
| **Coroutine launch** | `coroutineScope.launch { … }` (inside the button) | Runs the **suspending** `runIperf3Test

