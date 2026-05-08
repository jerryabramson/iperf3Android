# AGENTS.md - Iperf3 Network Tester

## Project Overview
- Android Jetpack Compose app, Kotlin + Java, AGP 9.1.1, Kotlin 2.3.20, KSP (not KAPT)
- compileSdk 37, minSdk 26, NDK 28.1.13356709
- Package: `edu.bu.cs683_jabramson_project.iperf3_network_tester`
- Project root: `Code/Iperf3NetworkTester/` (relative to repo root)

## iperf3 Execution -- JNI via CMake

iperf3 3.19 source is compiled into `libcellularlab.so`, not run as a subprocess:
- **CMake**: `app/src/main/cpp/CMakeLists.txt` compiles iperf3 sources + `iperf_jni.c`
- **JNI bridge**: `runner/IperfJNIRunner.kt` -- `IperfRunner` object loads `cellularlab`, declares `external fun runIperfLive()`
- **Orchestration**: `runner/IperfTestManage.kt` builds args, calls `IperfRunner.runIperfLive(args, callback)`
- **Callback interface**: `runner/IperfCallback.kt` -- `onOutput/onError/onComplete`
- **Output parsing**: `utils/MonitorIPerf3Output.java` (Java singleton with static state)

No asset-based iperf3 binaries are bundled. No subprocess runner exists in the active code path.

## Execution Flow

```
MainActivity → RunIperf3Screen → Iperf3RunViewModel.launch()
  → runIperf3() [suspend] → IperfTestManage.startTest()
    → IperfRunner.runIperfLive(native) → MonitorIPerf3Output.processLine()
      → ViewModel._uiStateFlow.update { ... } → Compose UI
```

## Architecture
- **Single activity**: `MainActivity` (Hilt `@AndroidEntryPoint`) always renders `RunIperf3Screen` -- no routing, no fallback screen
- **ViewModel**: `Iperf3RunViewModel` (`@HiltViewModel`, injected via `SavedStateHandle`) with `MutableStateFlow<UiData>`
- **UI state**: `UiData` data class (~20 fields) in the ViewModel file; all numeric UI values stored as strings to avoid NumberFormatException
- **Default values**: `DefaultUIValues` object (host=jabramson.com, port=5201, duration=10s, streams=8, skip=2)
- **Font**: MesloLGS NF monospace loaded via `ui/theme/mesloFontFamily.kt`

## Build & Commands

All commands from `Code/Iperf3NetworkTester/`:

```bash
./gradlew :app:assembleDebug      # Build debug APK (depends on CMake native build)
./gradlew :app:assembleRelease    # Build release APK
./gradlew :app:testDebugUnitTest  # Unit tests (template stubs only)
./gradlew :app:connectedAndroidTest  # Instrumented tests (needs device/emulator)
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Build Quirks
- **abiFilters**: armeabi-v7a, arm64-v8a, x86, x86_64 (in `ndk {}` block)
- **jniLibs.pickFirsts**: Prevents `libc++_shared.so` duplicates across ABIs
- **Resolution strategy** (outside android/dependencies blocks): forces `org.jetbrains:annotations:23.0.0`, excludes `com.intellij:annotations`
- **Duplicate Room deps** in `app/build.gradle.kts`: declared twice, neither integrated -- harmless but messy
- **CMake**: version 3.22.1, C standard c11, defines `__ANDROID__` and `HAVE_PTHREAD`

## Testing & Lint
- Only template stubs (`junit`, `espresso-core`) -- no real test coverage
- No lint configuration

## Deployment Gotchas
- **INTERNET permission** declared in AndroidManifest.xml (install-time, not runtime)
- **No SELinux workaround**: the pre-deploy script and `deployPrepScript` Gradle task referenced in old docs do NOT exist. Debug builds do not auto-disable SELinux.
- **iperf3 binary must be pushed manually**: `Assets/pushIperf3.sh` (repo root) pushes to `/bin/iperf3` on device/emulator
- App is a proof-of-concept -- requires rooted device or emulator with preinstalled iperf3

## Key Files
| File | Purpose |
|---|---|
| `app/src/main/java/.../MainActivity.kt` | Entry point, always shows RunIperf3Screen |
| `app/src/main/java/.../view/Iperf3View.kt` | Main Compose UI (~400 lines) |
| `app/src/main/java/.../viewmodel/Iperf3RunViewModel.kt` | ViewModel + UiData + DefaultUIValues |
| `app/src/main/java/.../runner/IperfTestManage.kt` | Test lifecycle, arg building, JNI call |
| `app/src/main/java/.../runner/IperfJNIRunner.kt` | Native lib loader + timer helpers |
| `app/src/main/java/.../utils/MonitorIPerf3Output.java` | iperf3 stdout parser (static singleton) |
| `app/src/main/cpp/CMakeLists.txt` | Native build config |
| `app/build.gradle.kts` | Gradle config, deps, resolution strategy |

## Known Issues Worth Preserving
- `UiData` has ~20 fields; many could be derived or collapsed into nested models
- `MonitorIPerf3Output.java` uses static mutable state -- not thread-safe, hard to test
- ViewModel's `launch()` mutates `_uiStateFlow.value.iperf3Parameters` directly (not via copy) before launching coroutine
- No input validation before launch (empty host, zero duration produce silent failures)
- Room dependencies declared but never wired
