# CLAUDE.md — Project Overview: Iperf3 Network Tester (Android)
```
claude --resume 1beae14d-87c4-4a89-acd3-870736994802
```
## What This Project Is

An Android application for running **iperf3 network performance tests** from a mobile device. Built as a Boston University CS 683 course project by Jerry Abramson.

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with Hilt DI
- **Build System:** Gradle 8.10.2 with KSP
- **Target Platform:** Android API 26+ (Android 8.0+), compiled against API 36

The app bundles pre-compiled iperf3 binaries for 4 ABIs (arm64-v8a, armeabi-v7a, x86, x86_64), extracts them at runtime, and executes them as a subprocess — streaming output line-by-line to the UI.

---

## Directory Structure

```
project-jerryabramson/
├── Code/Iperf3_network_tester/             # Android project root
│   ├── app/src/main/
│   │   ├── assets/iperf3_binaries/         # Pre-compiled iperf3 binaries (4 ABIs)
│   │   └── java/edu/bu/cs683_jabramson_project/iperf3_network_tester/
│   │       ├── Constants.kt
│   │       ├── Iperf3Application.kt        # Hilt @HiltAndroidApp entry point
│   │       ├── MainActivity.kt             # Single activity, binary check + routing
│   │       ├── RunIperf3Screen.kt          # Primary Compose UI
│   │       ├── StubbedIperf3Screen.kt      # Fallback when binary unavailable
│   │       ├── model/                      # Data classes: Iperf3Parameters, Iperf3ResultsData
│   │       ├── runner/iperf3Runner.kt      # Suspend function: process execution + streaming
│   │       ├── utils/findIperf3Binary.kt   # Binary detection + extraction logic
│   │       ├── viewmodel/
│   │       │   ├── Iperf3RunViewModel.kt   # Active ViewModel (Hilt-injected)
│   │       │   └── ProcessRunnerViewModel.kt  # Legacy/unused
│   │       └── view/ProcessOutputScreen.kt # Legacy/unused screen
│   ├── gradle/libs.versions.toml           # Centralized dependency versions
│   ├── planning.md                         # Known issues + roadmap
│   └── commandLineBuildApp.md              # Build + deploy instructions
└── Doc/CS683_JerryAbramson_Iperf3_network_tester.md  # Course documentation
```

---

## Key Components

### Binary Management (`utils/findIperf3Binary.kt`)
- Strategy 1: Check `/bin/iperf3` (rooted devices, emulators)
- Strategy 2: Extract matching ABI binary from `assets/` to `context.cacheDir`
- Returns `File` or `null`; `null` routes to `StubbedIperf3Screen`

### Process Execution (`runner/iperf3Runner.kt`)
- Suspending function on `Dispatchers.IO`
- Builds `ProcessBuilder` from `Iperf3Parameters`
- Streams stdout line-by-line via `callback: (String) -> Unit`
- Accumulates stderr into `Iperf3ResultsData.errors`

### State Management (`viewmodel/Iperf3RunViewModel.kt`)
- Single source of truth: `MutableStateFlow<Iperf3Parameters>`
- Exposes read-only `uiStateFlow` for Compose `collectAsState()`
- Update methods: `setServerHost()`, `setDuration()`, `setReverse()`, `setTimeout()`, `setForceFlush()`

### Primary UI (`RunIperf3Screen.kt`)
- Inputs: server host, duration (sec), timeout, reverse mode toggle, force flush toggle
- Run button + progress indicator
- Output rendered in Meslo monospace font for terminal-like appearance

---

## Build & Run

```bash
# Build debug APK
cd Code/Iperf3_network_tester
./gradlew assembleDebug

# Install on connected device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# For devices without /bin/iperf3, push binary manually
# (see Assets/pushIperf3.sh or commandLineBuildApp.md)
```

**Important:** Process execution requires a rooted device or emulator. SELinux on standard Android blocks execution of binaries from `cacheDir`. The app is **not suitable for general Play Store deployment** without a JNI/NDK refactor.

---

## Dependencies (Key)

| Dependency | Version | Purpose |
|---|---|---|
| Compose BOM | 2026.03.01 | UI framework (Material 3) |
| Hilt | 2.59.2 | Dependency injection |
| KSP | 2.3.6 | Annotation processing |
| lifecycle-viewmodel-compose | 2.10.0 | ViewModel + Compose integration |
| Room | 2.8.4 | Declared, not yet integrated |

---

## Current State

### Working
- Binary detection and extraction (multi-ABI)
- Hilt DI wiring (app + activity)
- Compose UI with all input controls
- StateFlow-driven reactive state
- Live line-by-line output streaming from iperf3 subprocess
- Fallback UI when binary unavailable

### Incomplete / Known Issues
- `ProcessRunnerViewModel` and `ProcessOutputScreen` are dead code (unused)
- Room database declared but not integrated
- No real test coverage (template stubs only)
- Default parameter values unsafe (empty binary path, zero duration/timeout)
- Binary not passed from `MainActivity` to ViewModel before screen renders
- INTERNET permission requested at runtime (it's install-time on Android)
- README.md out of sync with actual directory structure

### Roadmap (from `planning.md`)
1. Stabilize ViewModel as single source of truth
2. Harden process argument building (guard against empty strings)
3. Decide on long-term native execution strategy (NDK/JNI vs. subprocess)
4. Remove dead code (`ProcessRunnerViewModel`, `ProcessOutputScreen`)
5. Add targeted unit tests

---

## Architecture Notes

- **Single-activity** pattern: `MainActivity` handles binary check, routes to either `RunIperf3Screen` or `StubbedIperf3Screen`
- **Compose navigation** used (not fragment-based)
- **`@AndroidEntryPoint`** on `MainActivity` for Hilt field injection
- **`hiltViewModel()`** factory used in composables for automatic VM injection
- Binary assets are marked `noCompress` in Gradle so they aren't deflated (required for `setExecutable()` to work after extraction)
