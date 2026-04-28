# AGENTS.md - Iperf3 Network Tester

## Project Overview
- Android Jetpack Compose app, Kotlin + Java, AGP 9.1.1, Kotlin 2.3.20
- Hilt DI via KSP (not KAPT), Room declared but not yet integrated
- Package: `edu.bu.cs683_jabramson_project.iperf3_network_tester`
- Project root: `Code/Iperf3NetworkTester/` (camelCase, no underscore)

## Dual iperf3 Execution -- JNI is Active

iperf3 runs via **JNI**, not subprocess. CMake compiles iperf3 3.19 source into `libcellularlib.so`:
- CMake config: `app/src/main/cpp/CMakeLists.txt`
- JNI bridge: `runner/IperfJNIRunner.kt` loads `System.loadLibrary("cellularlab")`
- Test orchestration: `runner/IperfTestManage.kt` calls `IperfRunner.runIperfLive()`
- Output parsing: `utils/MonitorIPerf3Output.java` (Java, not Kotlin)

The older subprocess-based `runner/iperf3Runner.kt` is present but not called by the active code path.

### Binary Assets Still Present
- `src/main/assets/iperf3_binaries/<abi>/iperf3` -- pre-compiled binaries (4 ABIs)
- `aaptOptions.noCompress += "iperf3"` in `app/build.gradle.kts`
- `findIperf3Binary()` in `utils/findIperf3Binary.kt` still extracts to `cacheDir` but is only used to populate `iperf3Binary` path in UI state (not for execution)
- **Bug**: `findIperf3Binary.kt:20` checks `/bin/iperfff3` (three f's -- typo)

## Build & Commands

All commands from `Code/Iperf3NetworkTester/`:

```bash
./gradlew :app:assembleDebug      # Build debug APK
./gradlew :app:assembleRelease    # Build release APK
./gradlew :app:testDebugUnitTest  # Unit tests (stub only)
./gradlew :app:connectedAndroidTest  # Instrumented tests (needs device/emulator)
```

### Pre-deploy SELinux Workaround
`assembleDebug` depends on `deployPrepScript`, which runs `app/scripts/pre-deploy.sh`. This script disables SELinux enforcement (`setenforce 0`) on the connected device/emulator via adb. It runs automatically before every debug build.

There is **no** `startEmulator` Gradle task. Start emulator manually via Android Studio or `avdmanager`.

## Build Quirks
- `abiFilters`: armeabi-v7a, arm64-v8a, x86, x86_64
- `packaging.jniLibs.pickFirsts`: Prevents `libc++_shared.so` duplicates across ABIs
- Resolution strategy: Forces `org.jetbrains:annotations:23.0.0`, excludes `com.intellij:annotations`
- Duplicate Room dependency declarations in `app/build.gradle.kts` (harmless but messy)
- `local.properties` with `sdk.dir` is required for pre-deploy script

## Architecture
- **Single activity**: `MainActivity` (Hilt `@AndroidEntryPoint`) always renders `RunIperf3Screen`
- **ViewModel**: `Iperf3RunViewModel` (`@HiltViewModel`) with `MutableStateFlow<UiData>`
- **UI**: `view/Iperf3View.kt` contains `RunIperf3Screen`; supporting composables in `view/`
- **Theme**: `ui/theme/` with custom MesloLGS NF monospace font

## Testing
- Only template stubs exist (`ExampleUnitTest`, `ExampleInstrumentedTest`)
- No lint configuration -- skip lint step

## Troubleshooting
- **Permission denied (errno=13)**: SELinux blocks native execution. Pre-deploy script handles this for debug builds. Production deployment requires rooted device or full NDK integration.
- **Gradle sync fails**: `./gradlew --stop` then retry
- **CMake build errors**: Ensure NDK 28.1.13356709 is installed (set in `app/build.gradle.kts`)
