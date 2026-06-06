# KMP Migration Plan — iperf3 Network Tester

**Author**: Research study at Boston University (METCS683)
**Date**: 2026-06-05
**Status**: Planning / Analysis

---

## 1. Current State

Both projects share identical domain logic (parameters, output parsing, unit conversion, test orchestration) but are completely separate codebases with no code sharing.

### Android (`iperf3Android/`)
| Aspect | Detail |
|---|---|
| UI | Jetpack Compose (836-line single screen) |
| Architecture | Hilt DI, `@HiltViewModel`, `StateFlow<UiData>` |
| Native | iperf3 3.19 compiled via CMake -> `libcellularlab.so` (JNI) |
| Bridge | `IperfRunner` object with `external fun` declarations |
| Output parsing | `Iperf3OutputMonitor` - instance-based parser |
| Unit conversion | `UnitConverter.kt` - bandwidth unit math |
| Known issues | `UiData` has 24 fields; StateFlow mutation bugs; redundant coroutines |

### iOS (`iperf3iOS/`)
| Aspect | Detail |
|---|---|
| UI | SwiftUI (331-line `ContentView`) |
| Architecture | `ObservableObject` + `@Published` + Combine |
| Native | iperf3 3.21+ compiled via Xcode -> `libiperf3lib.a` (static) |
| Bridge | C file + bridging header -> Swift wrapper on `Thread` |
| Output parsing | `Iperf3OutputMonitor.swift` - mirrors Android parser |
| Unit conversion | `UnitConverter.swift` - mirrors Android converter |
| Known issues | Hardcoded absolute paths; no real cancellation; async bugs |

---

## 2. What KMP Can Share

### Shareable (~60-70% of domain logic)

| Module | Android Code | iOS Code | KMP Approach |
|---|---|---|---|
| **IPERF PARAMETERS** | `Iperf3Parameters.kt` (8 fields) | `Iperf3Config` struct (same 8 fields) | Share `data class Iperf3Parameters` in `commonMain` |
| **OUTPUT PARSING** | `Iperf3OutputMonitor.kt` (300 lines) | `Iperf3OutputMonitor.swift` (~same logic) | Share parser logic in `commonMain`; pure string processing, no platform deps |
| **UNIT CONVERSION** | `UnitConverter.kt` (92 lines) | `UnitConverter.swift` (~same logic) | Share `UnitConvertedData`, `toHumanUnit()`, `fromHumanUnit()`, `toMbs()` in `commonMain` |
| **TEST ORCHESTRATION** | `IperfTestManage.kt` (218 lines) | `Iperf3TestRunner.swift` (similar flow) | Share arg building, validation, progress tracking in `commonMain`; abstract the native call |
| **MODEL TYPES** | `Iperf3ResultsData.kt` (unused) | N/A | Remove unused; consolidate into shared types |

### NOT Shareable (Platform-Specific)

| Component | Android | iOS | Notes |
|---|---|---|---|
| **UI Layer** | Jetpack Compose (836 lines) | SwiftUI (331 lines) | Compose Multiplatform for iOS exists but SwiftUI is the native path |
| **Native Bridge** | JNI via CMake (`libcellularlab.so`) | Bridging header + static lib (`libiperf3lib.a`) | KMP handles both via expect/actual or native compilation |
| **Dependency Injection** | Hilt (`@HiltViewModel`) | None (plain Swift) | KMP does not provide DI; use Koin for multiplatform |
| **State Management** | `StateFlow<UiData>` (24 fields) | `ObservableObject` + `@Published` | Share data models; keep state management platform-specific |
| **Permissions/Manifest** | `INTERNET` in `AndroidManifest.xml` | No entitlements needed | Platform-specific setup |

### Key Insight: The C Code Is the Shared Foundation

The iperf3 C source is identical across both platforms. This is the single biggest enabler for KMP:

1. **Android**: CMake compiles iperf3 3.19 + JNI bridge -> `libcellularlab.so`
2. **iOS**: Xcode compiles iperf3 3.21+ -> `libiperf3lib.a`
3. **KMP**: Kotlin/Native compiles the SAME C source -> platform-native library for both Android and iOS

**One copy of iperf3 source, one compilation, shared across both platforms.**

---

## 3. KMP Architecture Options

### Option A: Native-First KMP (Recommended)

Kotlin/Native compiles the iperf3 C source directly into a native library. The `commonMain` module exposes Kotlin APIs that both Android and iOS call.

**Why this works best:**
- The C code is already C11, which Kotlin/Native handles well
- No JNI layer needed (Kotlin/Native has direct FFI to C)
- One compilation unit for iperf3 instead of two
- The pipe-based output capture pattern (used on both platforms) can be shared in Kotlin/Native
- Kotlin coroutines on Kotlin/Native map naturally to the pipe-reader thread pattern

**Structure:**

```
iperf3Android/                         iperf3iOS/
+-- app/                               +-- DocumentApp/iperf3NetworkTester/
|   +-- ... (existing Android code)        |   +-- ... (existing iOS code)
+-- shared/                              +-- shared/
    |   build.gradle.kts                     |   build.gradle.kts
    |   +-- src/
    |       +-- commonMain/
    |           +-- model/Iperf3Parameters.kt    (shared data class)
    |           +-- utils/UnitConverter.kt       (shared bandwidth math)
    |           +-- utils/Iperf3OutputMonitor.kt (shared parser)
    |           +-- runner/TestOrchestrator.kt   (shared arg building, validation)
    |           +-- native/
    |           |   +-- Iperf3Native.kt          (expect declarations)
    |           |   +-- c/                       (iperf3 C source, shared)
    |           +-- androidMain/
    |           |   +-- Iperf3NativeAndroid.kt   (actual: calls shared native lib)
    |           +-- iosMain/
    |               +-- Iperf3NativeIos.kt       (actual: calls shared native lib)
```

**How it works:**
1. `shared/build.gradle.kts` declares cinterop or cmake target for Kotlin/Native
2. Kotlin/Native compiles iperf3 C source + Kotlin FFI bridge into a native library
3. Android app module depends on `shared` (Kotlin/Native produces `.so` for Android)
4. iOS app depends on `shared` (Kotlin/Native produces `.framework` for iOS)
5. Both platforms call the same Kotlin APIs, which internally call the same C code

### Option B: Shared Logic Only (Simpler, Less Sharing)

Keep the two separate native builds (CMake for Android, Xcode for iOS) and only share pure Kotlin logic in a common module. This avoids Kotlin/Native complexities.

- Share: Iperf3Parameters, UnitConverter, Iperf3OutputMonitor, DefaultUIValues
- Keep: separate native builds (CMake + JNI for Android, Xcode + bridging header for iOS)
- Each platform builds its own native library independently

**Pros:** Simpler to set up, no Kotlin/Native FFI complexity
**Cons:** Still maintain two C build configs; only ~30-40% code sharing vs ~60-70% in Option A

### Option C: Compose Multiplatform for iOS (Long-term)

Compose Multiplatform now supports iOS. This would let you share the UI layer too.

- Share: UI composables (forms, output display, progress indicators)
- Platform-specific: iOS-native navigation, platform controls, theming
- Requires rewriting iOS SwiftUI into Compose (significant effort)

**Pros:** Up to ~80% code sharing including UI
**Cons:** Compose for iOS is still maturing; the existing SwiftUI UI works well; rewriting 331 lines of SwiftUI for marginal long-term gain

**Recommendation: Start with Option A.** Once the shared logic is stable, evaluate Option C as a Phase 2 effort.

---

## 4. Migration Phases

### Phase 1: Foundation (2-3 weeks)

**Goal:** Set up the KMP shared module, compile iperf3 C source via Kotlin/Native, verify native calls work on both platforms.

1. Create `shared/` Gradle module with Kotlin/Native configuration
2. Move iperf3 C source from `app/src/main/cpp/iperf/` (Android) to `shared/src/commonMain/c/`
3. Create `iperf_kotlin.c` KMP bridge file (replaces both `iperf_jni.c` and `Iperf3Runner.c`):
   - Define a single C API: `extern void iperf3_run_with_callback(Iperf3Args*, Iperf3Callback)`
   - Implement pipe-based stdout capture (shared pattern from both platforms)
   - Use Kotlin/Native FFI bindings (no JNI, no bridging header)
4. Define `expect fun runIperf3(args: Iperf3Parameters, callback: Iperf3Callback)` in commonMain
5. Implement `actual` for Android (loads native lib, calls C function)
6. Implement `actual` for iOS (loads native framework, calls C function)
7. Build and verify: run a test on both Android emulator and iOS simulator

**Deliverable:** Both platforms can run iperf3 through the shared module. UI is still platform-native.

### Phase 2: Shared Logic (~3 weeks)

**Goal:** Extract and share all pure Kotlin logic (parameters, parsing, unit conversion, orchestration).

1. Extract `Iperf3Parameters` from Android to `shared/model/`
2. Extract and adapt `Iperf3OutputMonitor` from Android to `shared/utils/` (replace Java regex with Kotlin stdlib; merge with iOS version)
3. Extract `UnitConverter.kt` to `shared/utils/` (pure math, trivial)
4. Build `TestOrchestrator.kt` in `shared/runner/` (arg building, validation, progress tracking)
5. Migrate Android `IperfTestManage` to use shared `TestOrchestrator`
6. Migrate iOS `Iperf3TestRunner` to use shared `TestOrchestrator`

**Deliverable:** Parameters, parsing, unit conversion, and orchestration are all in `shared/`. Platform code only handles UI display and platform-specific setup.

### Phase 3: Platform Integration (~2 weeks)

**Goal:** Wire shared module into both platform apps, clean up platform-specific code.

**Android tasks:**
1. Update `app/build.gradle.kts` to depend on `:shared`
2. Replace `IperfTestManage` with shared `TestOrchestrator`
3. Replace `Iperf3OutputMonitor` and `UnitConverter` with shared versions
4. Remove `app/src/main/cpp/` (CMake is now in shared module)
5. Fix known issues: `UiData` mutation bugs, redundant coroutines

**iOS tasks:**
1. Add Kotlin/Native framework dependency to Xcode project
2. Replace `Iperf3TestRunner` with shared orchestrator (via Kotlin/ObjC/Swift bridge)
3. Replace `Iperf3OutputMonitor.swift` and `UnitConverter.swift` with shared versions
4. Remove `iperf3lib/` Xcode project (now in shared module)
5. Remove hardcoded absolute paths

**Deliverable:** Both apps use the shared module end-to-end. Two independent native builds are eliminated.

---

## 5. File-by-File Migration Mapping

### Files Moving to shared/commonMain/

| Source (Android) | Source (iOS) | Target | Notes |
|---|---|---|---|
| model/Iperf3Parameters.kt | Iperf3Runner.h (Iperf3Config) | model/Iperf3Parameters.kt | Identical 8 fields; merge into one Kotlin data class |
| utils/Iperf3OutputMonitor.kt | Iperf3OutputMonitor.swift | utils/Iperf3OutputMonitor.kt | ~300 lines of parsing; iOS version is a mirror; merge into one |
| utils/UnitConverter.kt | UnitConverter.swift | utils/UnitConverter.kt | ~92 lines of pure math; trivial to share |
| runner/IperfTestManage.kt (partial) | Iperf3TestRunner.swift (partial) | runner/TestOrchestrator.kt | Arg building, validation, progress logic; merge both |
| viewmodel/DefaultUIValues.kt | (hardcoded in SwiftUI) | model/DefaultUIValues.kt | Constants move to shared |

### Files Staying Platform-Specific

| Platform | Files | Notes |
|---|---|---|
| Android | MainActivity.kt | Entry point, stays Android |
| Android | view/Iperf3View.kt (836 lines) | Jetpack Compose UI |
| Android | view/Iperf3Test.kt | Compose progress indicators |
| Android | viewmodel/Iperf3RunViewModel.kt | Android state management (adapt to use shared logic) |
| Android | ui/theme/* | Compose theme |
| iOS | ContentView.swift | SwiftUI UI |
| iOS | Iperf3App.swift | @main entry point |
| iOS | TestViewModel.swift | SwiftUI state management (adapt to use shared logic) |
| iOS | Assets.xcassets | iOS resources |

### Files Removed

| File | Why |
|---|---|
| app/src/main/cpp/CMakeLists.txt | Replaced by shared module CMake |
| app/src/main/cpp/iperf/iperf_jni.c | Replaced by shared/iperf_kotlin.c |
| app/src/main/cpp/iperf/iperf-3.19/* | Moved to shared/src/commonMain/c/ |
| iperf3lib/ (Xcode project) | Replaced by shared module |
| DocumentApp/.../Iperf3Runner.c | Replaced by shared bridge |
| DocumentApp/.../Iperf3Runner.h | Replaced by shared bridge |
| DocumentApp/.../Iperf3iOS-Bridging-Header.h | No longer needed (Kotlin/Native FFI) |
| DocumentApp/.../Iperf3TestRunner.swift | Replaced by shared TestOrchestrator |
| DocumentApp/.../Iperf3OutputMonitor.swift | Replaced by shared parser |
| DocumentApp/.../UnitConverter.swift | Replaced by shared converter |
| app/src/main/java/.../model/Iperf3ResultsData.kt | Unused; consolidate into shared TestResult |

---

## 6. Technical Considerations

### Kotlin/Native vs JNI

| Aspect | Current Android (JNI) | KMP (Kotlin/Native) |
|---|---|---|
| Bridge file | `iperf_jni.c` (Java JNI API) | `iperf_kotlin.c` (K/N FFI, simpler) |
| Header generation | `javac -h` for JNI headers | Kotlin/Native generates bindings |
| Threading | Native thread + Java callback | Kotlin coroutines (structured concurrency) |
| Pipe handling | `dup2()` + pthread in Java | `dup2()` + Kotlin coroutine |
| Callback to UI | Java interface -> ViewModel | Kotlin suspend function -> StateFlow |
| Maintenance | Separate JNI layer | One FFI layer, shared with iOS |

### iOS Swift Interop

Kotlin/Native produces an Objective-C header that Swift can import:

1. `shared` module compiles to `iperf3shared.framework`
2. Xcode links the framework (like it currently links `libiperf3lib.a`)
3. Swift imports the generated ObjC header
4. Kotlin `expect/actual` callbacks map to Swift closures
5. Kotlin coroutines on iOS map to `DispatchQueue.main.async` for UI updates

### C Code Compatibility

The iperf3 C source uses:
- C11 standard (already declared in both Android CMake and iOS build)
- POSIX threads (`pthread.h`) - available on both Android and iOS
- POSIX pipes (`unistd.h`, `fcntl.h`) - available on both
- `dup2()`, `read()`, `write()` - POSIX, works on both
- `cJSON` (bundled) - pure C, no platform deps
- `arpa/inet.h`, `sys/socket.h` - POSIX sockets, available on both

The only C code that needs platform adaptation is the bridge file (`iperf_jni.c` -> `iperf_kotlin.c`). Everything else is pure portable C.

---

## 7. Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Kotlin/Native FFI complexity | High | Start with a minimal POC: compile one C file + one Kotlin function on both platforms |
| iperf3 C source version mismatch (3.19 Android vs 3.21+ iOS) | Medium | Unify on 3.21+ (newer); update Android CMake |
| Pipe-based stdout capture differs between platforms | Medium | Implement once in shared iperf_kotlin.c; both platforms use the same approach |
| iOS Swift interop with Kotlin/Native | Medium | Kotlin/Native produces ObjC headers; Swift imports ObjC seamlessly |
| Gradle + Xcode coordination | Low | Gradle manages the shared module; Xcode links the resulting framework |
| Research timeline | High | This is a directed research study; scope may need adjustment |

---

## 8. Tradeoffs Summary

| Approach | Code Sharing | Complexity | Time to MVP | Best For |
|---|---|---|---|---|
| Option A: Native-First KMP | ~60-70% | High | 7-8 weeks | Research with long-term multiplatform goals |
| Option B: Shared Logic Only | ~30-40% | Medium | 3-4 weeks | Quick win, less risk |
| Option C: Compose Multiplatform | ~80% | Very High | 12+ weeks | Full UI sharing, future-proof |

---

## 9. Recommended Next Steps

### Immediate (Week 1)

1. **POC: Single C file on Kotlin/Native**
   - Create a minimal `shared/` module
   - Compile one C file (e.g., `units.c`) + one Kotlin function
   - Verify it runs on Android emulator and iOS simulator
   - This validates the KMP + iperf3 C source compatibility

2. **Unify iperf3 version**
   - Adopt iperf3 3.21+ (iOS version) across both platforms
   - Update Android CMakeLists.txt to reference the newer source

3. **Define the shared C API contract**
   - `struct Iperf3Args { const char* server_host; int server_port; int duration; ... }`
   - `typedef void (*Iperf3Callback)(const char* line, int lineType)`
   - `void iperf3_run_with_callback(Iperf3Args* args, Iperf3Callback cb, void* context)`
   - `void iperf3_cancel(void* context)`
   - This single C API replaces both `iperf_jni.c` and `Iperf3Runner.c`

### Short-term (Weeks 2-4)

4. **Build the shared C bridge** (`iperf_kotlin.c`)
   - Pipe creation, `dup2()`, reader thread
   - Callback invocation with line classification (output/error/complete)
   - Integration with Kotlin/Native FFI bindings

5. **Extract shared Kotlin models**
   - Iperf3Parameters, UnitConverter, Iperf3OutputMonitor
   - Migrate Android code to use shared versions
   - Fix UiData mutation bugs while refactoring

### Medium-term (Weeks 5-8)

6. **Wire iOS to shared module**
   - Add Kotlin/Native framework to Xcode
   - Migrate iOS TestViewModel to use shared orchestrator
   - Verify end-to-end: SwiftUI UI -> shared logic -> KMP native -> iperf3

7. **Clean up platform-specific code**
   - Remove duplicate iperf3lib/ from iOS
   - Remove CMake from Android
   - Remove hardcoded paths
   - Fix known issues on both platforms

---

## 10. Key Questions for the Professor

1. **Research scope:** Is the goal to demonstrate KMP feasibility, or to produce a production-ready dual-platform app? This affects how much polish vs. proof-of-concept is needed.

2. **Timeline:** What is the target completion date? Phase 1 (POC) can be validated in 1-2 weeks; full migration is 7-8 weeks.

3. **UI sharing:** Is Compose Multiplatform for iOS in scope, or is the goal to share only the logic layer (keep SwiftUI + Compose)?

4. **iperf3 version:** Should we unify on 3.21+ (iOS) or 3.19 (Android)? 3.21+ is preferred for bug fixes.

5. **Evaluation criteria:** How will success be measured? Code line reduction? Build time? Feature parity?

---

## 11. Estimated Code Impact

| Metric | Before KMP | After KMP (Option A) | Change |
|---|---|---|---|
| C source files | ~50 (duplicated in 2 locations) | ~50 (single location) | -50% duplication |
| Kotlin source lines | ~1,500 | ~1,000 shared + ~500 platform | -33% total, +67% shared |
| Swift source lines | ~400 | ~250 (UI only) | -38% |
| Native bridge files | 2 (iperf_jni.c + Iperf3Runner.c) | 1 (iperf_kotlin.c) | -50% |
| C build configs | 2 (CMakeLists.txt + Xcode project) | 1 (shared CMakeLists.txt) | -50% |
| Total lines of code | ~2,500 | ~2,200 | -12% (dedup) |

The real value is not line count reduction but **single source of truth**: one parser, one unit converter, one orchestrator, one iperf3 build.
