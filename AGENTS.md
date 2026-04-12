# AGENTS.md - Essential Guidelines for Iperf3 Network Tester Repository

## Repository Structure
- `Code/Iperf3_network_tester/` - Main Android project (source, assets, tests)
- `Doc/` - Course documentation
- `Assets/` - Pre-compiled iperf3 binaries and helper scripts (push/pull)
- `README.md` - General project overview
- `CLAUDE.md` - Detailed architecture, usage, and known issues

## Working with the Android Project
All development commands must be run from the project directory:
```bash
cd Code/Iperf3_network_tester
```

### Critical Non-Obvious Configurations
#### Native Binary Handling (Most Important)
Due to Android 10+ SELinux restrictions, executing binaries from app data is blocked:
- Binaries stored in `src/main/assets/iperf3_binaries/<abi>/iperf3`
- **Must** have `aaptOptions.noCompress += "iperf3"` in `app/build.gradle.kts`
- Runtime extraction: `getIperf3Binary()` tries:
  1. `/bin/iperf3` (pre-installed on some devices/emulators)
  2. Extract from assets to `context.cacheDir/<abi>/iperf3` (chmod +x)
- Returns null if unavailable (shows stubbed UI)
- SELinux note: Extraction to cache dir may still be blocked on non-rooted devices; proper solution requires NDK compilation

#### Build-Specific Quirks
- `abiFilters`: Only armeabi-v7a, arm64-v8a, x86, x86_64 (in build.gradle.kts)
- `packaging.jniLibs.pickFirsts`: Prevents libc++_shared.so duplicates
- Resolution strategy: Forces `org.jetbrains:annotations:23.0.0`, excludes IntelliJ annotations
- Hilt configured with KSP plugin (`ksp(libs.hilt.compiler)` in build.gradle.kts)
- Uses Gradle Version Catalogs (`gradle/libs.versions.toml` for dependency versions)

### Essential Commands
#### Build
- `./gradlew :app:assembleDebug` - Build debug APK (fastest)
- `./gradlew :app:assembleRelease` - Build release APK
- `./gradlew :app:dependencies` - Show dependency tree

#### Testing
- `./gradlew :app:testDebugUnitTest` - All unit tests
- `./gradlew :app:connectedAndroidTest` - Instrumented tests (needs device/emulator)
- **Single unit test**: `./gradlew :app:testDebugUnitTest --tests "edu.bu.cs683_jabramson_project.iperf3_network_tester.ExampleUnitTest.addition_isCorrect"`
- **Single instrumented test**: `./gradlew :app:connectedAndroidTest --tests "edu.bu.cs683_jabramson_project.iperf3_network_tester.ExampleInstrumentedTest"`

#### Verification Order
When making changes: `lint -> test` (or specific variant tests)

### Device Setup & Binary Deployment
1. Ensure device has USB debugging enabled and is visible via `adb devices`
2. For app to find iperf3 binary:
   - Option A (Rooted): Binary must exist at `/bin/iperf3` and be executable
   - Option B (Non-rooted): Asset extraction may work on older devices; SELinux blocks on Android 10+
   - Helper script: `Assets/pushIperf3.sh` pushes binary to `/data/local/tmp/iperf3` (for manual testing via adb shell)
3. Install APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Code Style Notes
- Follows standard Kotlin/Android conventions
- Composables: side-effect free, immutable params, `remember` for state
- Tests: JUnit 5 for unit, AndroidJUnit4/Espresso for instrumented
- Hilt ViewModels: `@HiltViewModel` with ` SavedStateHandle` injection
- Native binary paths: Use `iperf3Parameters.iperf3Binary.absolutePath` for execution

### Troubleshooting
- **Missing iperf3 binary**: Verify assets exist and `aaptOptions.noCompress` is set in app/build.gradle.kts
- **Permission denied (errno=13)**: Expected on Android 10+ without root; binary extracted to cache dir still blocked by SELinux
- **Gradle sync fails**: Try `./gradlew --stop` then refresh
- **Test instrumentation fails**: Ensure emulator/API level matches test requirements (check manifest minSdk/targetSdk)