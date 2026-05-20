# AGENTS.md - Iperf3 Network Tester

## Project Overview
- Android Jetpack Compose app, Kotlin + Java, AGP 9.1.1, Kotlin 2.3.20, KSP (not KAPT)
- compileSdk 37, minSdk 26, targetSdk 36, NDK 28.1.13356709
- Package: `edu.bu.cs683_jabramson_project.iperf3_network_tester`
- **Project root**: `Code/Iperf3NetworkTester/` (relative to repo root)

## iperf3 Execution -- JNI via CMake
iperf3 3.19 source is compiled into `libcellularlab.so`, not run as a subprocess:
- **CMake**: `app/src/main/cpp/CMakeLists.txt` compiles iperf3 sources + `iperf_jni.c` → `add_library(cellularlab SHARED ...)`
- **JNI bridge**: `runner/IperfJNIRunner.kt` — `IperfRunner` object loads `cellularlab`, declares `external fun runIperfLive()`
- **Orchestration**: `runner/IperfTestManage.kt` builds args, calls `IperfRunner.runIperfLive(args, callback)`
- **Callback interface**: `runner/IperfCallback.kt` — `onOutput/onError/onComplete`
- **Output parsing**: `utils/Iperf3OutputMonitor.kt` (instance-based, replaces old static Java singleton)

No asset-based iperf3 binaries are bundled. No subprocess runner exists in the active code path.

## Execution Flow

```
MainActivity → RunIperf3Screen → Iperf3RunViewModel.launch()
  → runIperf3() [suspend] → IperfTestManage.startTest()
    → IperfRunner.runIperfLive(native) → Iperf3OutputMonitor.processLine()
      → ViewModel._uiStateFlow.update { ... } → Compose UI
```

## Architecture
- **Single activity**: `MainActivity` (Hilt `@AndroidEntryPoint`) always renders `RunIperf3Screen` — no routing, no fallback screen
- **Compose navigation** used (not fragment-based)
- **ViewModel**: `Iperf3RunViewModel` (`@HiltViewModel`, injected via `SavedStateHandle`) with `MutableStateFlow<UiData>`; composables use `hiltViewModel()` factory for automatic injection
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

## Testing & Lint
- Only template stubs (`junit`, `espresso-core`) — no real test coverage
- No lint configuration

**No `startEmulator` Gradle task.** Start emulator manually via Android Studio or `avdmanager`.

## Build Quirks
- **Gradle wrapper**: 9.3.1
- **NDK**: 28.1.13356709 (set in `app/build.gradle.kts`)
- **abiFilters**: armeabi-v7a, arm64-v8a, x86, x86_64
- **packaging.jniLibs.pickFirsts**: Prevents `libc++_shared.so` duplicates across ABIs
- **Resolution strategy** in `app/build.gradle.kts`: forces `org.jetbrains:annotations:23.0.0`, excludes `com.intellij:annotations`
- **Duplicate Room deps** in `app/build.gradle.kts` (harmless but messy — both KSP and commented-out KAPT declarations)

## Deployment Gotchas
- **INTERNET permission** declared in AndroidManifest.xml (install-time, not runtime)
- **No SELinux workaround**: the pre-deploy script and `deployPrepScript` Gradle task referenced in old docs do NOT exist. Debug builds do not auto-disable SELinux.

## Key Files
| File | Purpose |
|---|---|
| `app/src/main/java/.../MainActivity.kt` | Entry point, always shows RunIperf3Screen |
| `app/src/main/java/.../view/Iperf3View.kt` | Main Compose UI (~400 lines) |
| `app/src/main/java/.../viewmodel/Iperf3RunViewModel.kt` | ViewModel + UiData + DefaultUIValues |
| `app/src/main/java/.../runner/IperfTestManage.kt` | Test lifecycle, arg building, JNI call |
| `app/src/main/java/.../runner/IperfJNIRunner.kt` | Native lib loader + timer helpers |
| `app/src/main/java/.../utils/Iperf3OutputMonitor.kt` | iperf3 stdout parser (instance-based) |
| `app/src/main/cpp/CMakeLists.txt` | Native build config — compiles iperf3 3.19 into libcellularlab.so |
| `app/build.gradle.kts` | Gradle config, deps, resolution strategy |

### File Tree - Kotlin
Code/Iperf3NetworkTester/app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/
├── Constants.kt
├── Iperf3Application.kt
├── MainActivity.kt
├── model
│   ├── Iperf3Parameters.kt
│   └── Iperf3ResultsData.kt
├── runner
│   ├── IperfCallback.kt
│   ├── IperfJNIRunner.kt
│   └── IperfTestManage.kt
├── ui
│   └── theme
│       ├── Color.kt
│       ├── mesloFontFamily.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils
│   ├── Iperf3OutputMonitor.kt
│   └── UnitConverter.kt
├── view
│   ├── DebugOnOffRadioButton.kt
│   ├── ForceFlushRadioButton.kt
│   ├── Iperf3Test.kt
│   ├── Iperf3View.kt
│   └── UploadDownloadRadioButton.kt
└── viewmodel
    └── Iperf3RunViewModel.kt
    
### File Tree - Native Code

Code/Iperf3NetworkTester/app/src/main/cpp/
├── CMakeLists.txt
└── iperf
    ├── iperf_config_android.h
    ├── iperf_jni.c
    └── iperf-3.19
        ├── cjson.c
        ├── cjson.h
        ├── dscp.c
        ├── flowlabel.h
        ├── iperf_api.c
        ├── iperf_api.h
        ├── iperf_auth.c
        ├── iperf_auth.h
        ├── iperf_client_api.c
        ├── iperf_config.h
        ├── iperf_config.h.in
        ├── iperf_error.c
        ├── iperf_locale.c
        ├── iperf_locale.h
        ├── iperf_pthread.c
        ├── iperf_pthread.h
        ├── iperf_sctp.c
        ├── iperf_sctp.h
        ├── iperf_server_api.c
        ├── iperf_tcp.c
        ├── iperf_tcp.h
        ├── iperf_time.c
        ├── iperf_time.h
        ├── iperf_udp.c
        ├── iperf_udp.h
        ├── iperf_util.c
        ├── iperf_util.h
        ├── iperf.h
        ├── iperf3.1
        ├── libiperf.3
        ├── main.c
        ├── Makefile.am
        ├── Makefile.in
        ├── net.c
        ├── net.h
        ├── portable_endian.h
        ├── private.pem
        ├── public.pem
        ├── queue.h
        ├── t_api.c
        ├── t_auth.c
        ├── t_timer.c
        ├── t_units.c
        ├── t_uuid.c
        ├── tcp_info.c
        ├── timer.c
        ├── timer.h
        ├── units.c
        ├── units.h
        ├── version.h
        └── version.h.in


## Known Issues Worth Preserving
- `UiData` has ~20 fields; many could be derived or collapsed into nested models
- ViewModel's `launch()` mutates `_uiStateFlow.value.iperf3Parameters` directly (not via copy) before launching coroutine
- No input validation before launch (empty host, zero duration produce silent failures)
- Room dependencies declared but never wired
