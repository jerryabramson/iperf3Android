# AGENTS.md - Iperf3 Network Tester

## Project Overview
- Android single-module app (`:app`), Kotlin + Java, Hilt DI, KSP (not KAPT)
- **Project root**: `Code/Iperf3NetworkTester/` (relative to repo root)
- compileSdk 37, minSdk 26, targetSdk 36, NDK 28.1.13356709, CMake 3.22.1
- Package: `edu.bu.cs683_jabramson_project.iperf3_network_tester`
- AGP via `libs.plugins.android.application`, Kotlin via `libs.plugins.kotlin.compose`
- Gradle wrapper 9.3.1, Kotlin code style: `official`

## iperf3 Execution -- JNI via CMake (NOT subprocess)
iperf3 3.19 source is compiled into `libcellularlab.so`:
- **CMake**: `app/src/main/cpp/CMakeLists.txt` → `add_library(cellularlab SHARED ...)` with iperf3 sources + `iperf_jni.c`, linked against `Threads`, `log`, `android`
- **JNI bridge**: `runner/IperfJNIRunner.kt` — `IperfRunner` object, `System.loadLibrary("cellularlab")`, declares:
  - `external fun runIperfLive(arguments: Array<String>, callback: IperfCallback)`
  - `external fun forceStop(callback: IperfCallback)`
  - `external fun setTempDir(tempDir: String)`
- **Orchestration**: `runner/IperfTestManage.kt` builds args, calls `IperfRunner.runIperfLive(args, callback)`
- **Callback interface**: `runner/IperfCallback.kt` — `onOutput/onError/onComplete`
- **Output parsing**: `utils/Iperf3OutputMonitor.kt` — instance-based (replaces old static Java singleton)

No asset-based iperf3 binaries. No subprocess runner in active code path.

## Execution Flow

```
MainActivity (Hilt @AndroidEntryPoint)
  → RunIperf3Screen (Compose, 836 lines)
  → Iperf3RunViewModel.launchOrCancel()
    → launch() → runIperf3() [suspend]
      → IperfTestManage.startTest(context, params)
        → IperfRunner.runIperfLive(args, callback) [native]
          → Iperf3OutputMonitor.processLine()
            → ViewModel._uiStateFlow.update { copy(...) } → Compose UI
```

## Architecture
- **Single activity**: `MainActivity` always renders `RunIperf3Screen` — no navigation routing
- **ViewModel**: `Iperf3RunViewModel` (`@HiltViewModel`, injected via `SavedStateHandle`) with `MutableStateFlow<UiData>`; composables use `hiltViewModel()` factory
- **UI state**: `UiData` data class (24 fields) in ViewModel file; numeric UI values stored as strings to avoid NumberFormatException
- **Defaults**: `DefaultUIValues` object — host=jabramson.com, port=5201, duration=10s, streams=8, skip=2
- **Font**: MesloLGS NF monospace via `ui/theme/mesloFontFamily.kt`
- **Progress**: `NetworkProgressIndicator` in `view/Iperf3Test.kt` — directional (left→right for upload, right→left for download)

## Build & Commands
All commands from `Code/Iperf3NetworkTester/`:

```bash
./gradlew :app:assembleDebug      # Build debug APK (depends on CMake native build)
./gradlew :app:assembleRelease    # Build release APK
./gradlew :app:testDebugUnitTest  # Unit tests (template stubs only)
./gradlew :app:connectedAndroidTest  # Instrumented tests (needs device/emulator)
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Testing & Lint
- Only template stubs (`junit`, `espresso-core`) — no real test coverage
- No lint configuration
- **No `startEmulator` Gradle task.** Start emulator manually.

## Build Quirks
- **abiFilters**: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`
- **packaging.jniLibs.pickFirsts**: Prevents `libc++_shared.so` duplicates across ABIs
- **CMake cFlags**: `-std=c11 -D__STDC_NO_ATOMICS__=0`
- **Resolution strategy** in `app/build.gradle.kts`: forces `org.jetbrains:annotations:23.0.0`, excludes `com.intellij:annotations`
- **Room declared but unused**: `room-runtime`, `room-compiler`, `room-ktx` in deps — never wired to DAOs or databases
- **Foojay resolver** plugin for Java toolchain (settings.gradle.kts)

## Key Files
| File | Purpose |
|---|---|
| `app/src/main/java/.../MainActivity.kt` | Entry point, Hilt @AndroidEntryPoint, always shows RunIperf3Screen |
| `app/src/main/java/.../view/Iperf3View.kt` | Main Compose UI (836 lines) |
| `app/src/main/java/.../view/Iperf3Test.kt` | Directional progress indicator |
| `app/src/main/java/.../viewmodel/Iperf3RunViewModel.kt` | ViewModel + UiData (24 fields) + DefaultUIValues |
| `app/src/main/java/.../runner/IperfTestManage.kt` | Test lifecycle, arg building, JNI call |
| `app/src/main/java/.../runner/IperfJNIRunner.kt` | Native lib loader + timer helpers |
| `app/src/main/java/.../runner/IperfCallback.kt` | Callback interface (onOutput/onError/onComplete) |
| `app/src/main/java/.../utils/Iperf3OutputMonitor.kt` | iperf3 stdout parser (instance-based) |
| `app/src/main/java/.../utils/UnitConverter.kt` | Bandwidth unit conversion utilities |
| `app/src/main/java/.../model/Iperf3Parameters.kt` | Data class for test parameters |
| `app/src/main/cpp/CMakeLists.txt` | Native build — compiles iperf3 3.19 into libcellularlab.so |
| `app/build.gradle.kts` | Gradle config, deps, resolution strategy |

### Source Tree (Java/Kotlin)
```
app/src/main/java/.../iperf3_network_tester/
├── Constants.kt
├── Iperf3Application.kt          # @HiltAndroidApp
├── MainActivity.kt
├── model/
│   ├── Iperf3Parameters.kt       # serverHost/port, clientHost/port, duration, reverse, flush, streams, skip
│   └── Iperf3ResultsData.kt      # (mostly unused alongside UiData)
├── runner/
│   ├── IperfCallback.kt          # interface
│   ├── IperfJNIRunner.kt         # IperfRunner object
│   └── IperfTestManage.kt        # startTest/cancelTest
├── ui/theme/
│   ├── Color.kt, Theme.kt, Type.kt
│   └── mesloFontFamily.kt
├── utils/
│   ├── Iperf3OutputMonitor.kt    # LineResult model + parser
│   └── UnitConverter.kt          # UnitConvertedData, toHumanUnit, fromHumanUnit
├── view/
│   ├── Iperf3View.kt             # RunIperf3Screen composable (836 lines)
│   ├── Iperf3Test.kt             # NetworkProgressIndicator + ReverseLinearProgressIndicator
│   ├── DebugOnOffRadioButton.kt
│   ├── ForceFlushRadioButton.kt
│   ├── UploadDownloadRadioButton.kt
│   └── *Preview.kt               # Compose @Preview helpers
└── viewmodel/
    └── Iperf3RunViewModel.kt
```

## KMP Migration (Directed Research)

A full KMP migration plan is documented in `KMP_Migration_Plan.md`. It covers:
- Three architecture options (Native-First KMP recommended)
- 3-phase migration timeline (~8 weeks total)
- File-by-file mapping between Android, iOS, and shared modules
- The iperf3 C source is the shared foundation: one compilation replaces two

See `KMP_Migration_Plan.md` for details. Do not start KMP work without reviewing that document first.

## Known Issues Worth Preserving
- **`UiData` has 24 fields** — many could be derived or collapsed into nested models
- **`launch()` and `runIperf3()` mutate `_uiStateFlow.value.iperf3Parameters` directly** (not via copy) before launching — potential StateFlow mutation bug
- **`startTest()` is `suspend` but creates its own `CoroutineScope.launch()` internally** — redundant coroutine management; `runJob.join()` followed by `runJob.cancel()` is immediately after join (redundant)
- **No input validation before launch** — empty host, zero duration produce silent failures
- **Room dependencies declared but never wired** — dead deps in `app/build.gradle.kts`
- **`Iperf3ResultsData.kt` is unused** — `UiData` supersedes it
