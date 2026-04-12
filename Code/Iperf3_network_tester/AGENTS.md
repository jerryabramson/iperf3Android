# AGENTS.md - Essential Guidelines for Iperf3 Network Tester

## Project Overview
- Android Jetpack Compose app using Kotlin, Gradle Version Catalogs
- Hilt for DI (with KSP, not KAPT), Room for local storage
- Native iperf3 binary execution with SELinux workarounds
- Package: `edu.bu.cs683_jabramson_project.iperf3_network_tester`

## Critical Non-Obvious Configurations

### Native Binary Handling (Most Important)
Due to Android 10+ SELinux restrictions, executing binaries from app data is blocked:
- Binaries stored in `src/main/assets/iperf3_binaries/<abi>/iperf3`
- **Must** have `aaptOptions.noCompress += "iperf3"` in `app/build.gradle.kts`
- Runtime extraction: `getIperf3Binary()` tries:
  1. `/bin/iperf3` (pre-installed on some devices)
  2. Extract from assets to `context.cacheDir/<abi>/iperf3` (chmod +x)
- Returns null if unavailable (shows stubbed UI)
- SELinux note: Proper solution requires NDK compilation (see CellularLab repo)

### Build-Specific Quirks
- `abiFilters`: Only armeabi-v7a, arm64-v8a, x86, x86_64
- `packaging.jniLibs.pickFirsts`: Prevents libc++_shared.so duplicates
- Resolution strategy: Forces `org.jetbrains:annotations:23.0.0`, excludes IntelliJ annotations
- Hilt configured with KSP plugin (`ksp(libs.hilt.compiler)`)

## Essential Commands

### Build
- `./gradlew :app:assembleDebug` - Build debug APK (fastest)
- `./gradlew :app:assembleRelease` - Build release APK
- `./gradlew :app:dependencies` - Show dependency tree

### Testing
- `./gradlew :app:testDebugUnitTest` - All unit tests
- `./gradlew :app:connectedAndroidTest` - Instrumented tests (needs device/emulator)
- **Single unit test**: `./gradlew :app:testDebugUnitTest --tests "edu.bu.cs683_jabramson_project.iperf3_network_tester.ExampleUnitTest.addition_isCorrect"`
- **Single instrumented test**: `./gradlew :app:connectedAndroidTest --tests "edu.bu.cs683_jabramson_project.iperf3_network_tester.ExampleInstrumentedTest"`

### Verification Order
When making changes: `lint -> test` (or specific variant tests)

## Code Style Notes
- Follows standard Kotlin/Android conventions
- Composables: side-effect free, immutable params, `remember` for state
- Tests: JUnit 5 for unit, AndroidJUnit4/Espresso for instrumented
- Hilt ViewModels: `@HiltViewModel` with ` SavedStateHandle` injection
- Native binary paths: Use `iperf3Parameters.iperf3Binary.absolutePath` for execution

## Troubleshooting
- **Missing iperf3 binary**: Verify assets exist and `aaptOptions.noCompress` is set
- **Permission denied (errno=13)**: Expected on Android 10+ without root; binary extracted to cache dir still blocked by SELinux
- **Gradle sync fails**: Try `./gradlew --stop` then refresh
- **Test instrumentation fails**: Ensure emulator/API level matches test requirements