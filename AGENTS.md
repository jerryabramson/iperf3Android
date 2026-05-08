# AGENTS.md - Iperf3 Network Tester

## Project Overview
- Android Jetpack Compose app, Kotlin + Java, AGP 9.1.1, Kotlin 2.3.20
- Hilt DI via KSP (not KAPT), Room declared but not integrated
- Package: `edu.bu.cs683_jabramson_project.iperf3_network_tester`
- Project root: `Code/Iperf3NetworkTester/`

## iperf3 Execution -- JNI is Active
CMake compiles iperf3 3.19 source into `libcellularlab.so`:
- CMake: `app/src/main/cpp/CMakeLists.txt` (sources under `app/src/main/cpp/iperf/`)
- JNI bridge: `runner/IperfJNIRunner.kt` loads `System.loadLibrary("cellularlab")`, exposes `runIperfLive()`
- Test orchestration: `runner/IperfTestManage.kt` calls `IperfRunner.runIperfLive()` with callback-based output
- Output parsing: `utils/MonitorIPerf3Output.java` (Java, not Kotlin)

**No subprocess execution path exists.** The old `iperf3Runner.kt` has been removed. No iperf3 binaries are bundled in assets -- only images remain there.

## Build & Commands
All commands from `Code/Iperf3NetworkTester/`:

```bash
./gradlew :app:assembleDebug      # Build debug APK (requires connected device for pre-deploy step)
./gradlew :app:assembleRelease    # Build release APK
./gradlew :app:testDebugUnitTest  # Unit tests (stub only, passes)
./gradlew :app:connectedAndroidTest  # Instrumented tests (needs device/emulator)
```

**No `startEmulator` Gradle task.** Start emulator manually via Android Studio or `avdmanager`.

## Build Quirks
- **Gradle wrapper**: 9.3.1 (not the version in CLAUDE.md)
- **compileSdk = 37**, minSdk = 26, targetSdk = 36
- **NDK**: 28.1.13356709 (set in `app/build.gradle.kts`)
- **abiFilters**: armeabi-v7a, arm64-v8a, x86, x86_64
- **packaging.jniLibs.pickFirsts**: Prevents `libc++_shared.so` duplicates across ABIs
- **Resolution strategy** in `build.gradle.kts`: forces `org.jetbrains:annotations:23.0.0`, excludes `com.intellij:annotations`
- **Duplicate Room deps** in `app/build.gradle.kts` (harmless but messy -- both KSP and commented-out KAPT declarations)

## Architecture
- **Single activity**: `MainActivity` (`@AndroidEntryPoint`) always renders `RunIperf3Screen` -- no binary check, no routing
- **ViewModel**: `Iperf3RunViewModel` (`@HiltViewModel`) with `MutableStateFlow<UiData>`
- **UI**: `view/Iperf3View.kt` contains `RunIperf3Screen`; supporting composables in `view/` (UploadDownloadRadioButton, DebugOnOffRadioButton, ForceFlushRadioButton)
- **Theme**: `ui/theme/` with custom MesloLGS NF monospace font (`mesloFontFamily.kt`)
- **Model**: `model/Iperf3Parameters.kt`, `model/Iperf3ResultsData.kt`

## Testing
- Only template stubs exist (`ExampleUnitTest`, `ExampleInstrumentedTest`)
- No lint configuration -- skip lint step

## Troubleshooting
- **CMake build errors**: Ensure NDK 28.1.13356709 is installed (set in `app/build.gradle.kts`)
- **Gradle sync fails**: `./gradlew --stop` then retry
- **No pre-deploy script exists** -- the old SELinux workaround (`pre-deploy.sh`, `deployPrepScript` Gradle task) has been removed

## Stale References (do NOT look for these)
- ~~`runner/iperf3Runner.kt``~~ -- removed, subprocess path deleted
- ~~`utils/findIperf3Binary.kt`~~ -- removed, no binary extraction
- ~~`app/scripts/pre-deploy.sh`~~ -- removed
- ~~`StubbedIperf3Screen`~~ -- removed
- ~~`ProcessRunnerViewModel`~~ -- removed
- ~~`ProcessOutputScreen`~~ -- removed
- ~~`assets/iperf3_binaries/`~~ -- removed, only images remain
