# AGENTS.md - Guidelines for Agentic Coding in Iperf3 Network Tester

## Introduction
This document provides guidelines for agentic coding agents working on the Iperf3 Network Tester Android project. It covers build/test/lint commands, code style, and best practices.

## Project Overview
- Android application using Jetpack Compose for UI
- Kotlin programming language
- Gradle build system with Version Catalogs (libs.versions.toml)
- Unit tests with JUnit 5 (JUnit Jupiter)
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
- Unit test results: `app/build/reports/tests/testDebugUnitTest/index.html`
- Instrumented test results: `app/build/reports/androidTests/connected/`

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
- Test methods: descriptive names using underscores (e.g., `addition_isCorrect`)
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
- Test method names: descriptive, using underscores (e.g., `addition_isCorrect`)
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
- Reference issues in commit messages when applicable
- Keep feature branches short-lived
- Pull requests should include description and screenshots for UI changes
- Ensure code builds and passes tests before creating PR
- Resolve merge conflicts promptly

## Additional Notes
- Refer to `gradle/libs.versions.toml` for dependency versions
- Android Studio is the recommended IDE
- Enable "Power Save Mode" only when necessary as it disables inspections
- For Compose previews, use `@Preview` with various parameters (width, height, fontScale, etc.)
- Accessibility: ensure touch targets are at least 48dp, provide content descriptions for icons
- Performance: avoid heavy computations in composables; use `derivedStateOf` for state transformations
- Security: do not hardcode secrets; use Android Keystore or encrypted preferences for sensitive data

### Native Binary Handling (iperf3)
This project uses a native iperf3 binary for network testing. Due to Android's SELinux restrictions, executing standalone native binaries from app data directories is not possible on Android 10+.

**Course Constraints (One of these must be satisfied for project viability):**
1. **Android Emulator with Root**: Upload iperf3 and make it executable using root
2. **Pre-installed Binary**: Some Android devices include `/bin/iperf3` - use that
3. **Instructor Device**: Ensure the instructor has a rooted Android device

**Current Implementation:**
- Tries multiple strategies for finding iperf3 binary
- Strategy 1: Check `/bin/iperf3` (pre-installed on some devices)
- Strategy 2: Extract from assets to cache directory
- Returns null if no binary is available

**Packaging:**
1. Place binaries in `src/main/assets/iperf3_binaries/<abi>/iperf3` for each supported ABI
2. Update `build.gradle.kts` with `aaptOptions.noCompress += "iperf3"` to prevent compression
3. Supported ABIs: arm64-v8a, armeabi-v7a, x86_64, x86

**Runtime Execution:**
4. Use `getIperf3Binary()` which tries multiple strategies
5. Binaries are extracted to `context.cacheDir/<abi>/iperf3` if not found elsewhere
6. Make extracted binaries executable with `setExecutable(true, false)`

**SELinux Note:** Android uses SELinux which restricts process execution. On Android 10+, files in app data directories cannot be executed directly. For a complete solution, see CellularLab repo (https://github.com/Abhi5h3k/CellularLab) which uses CMake + JNI to compile iperf3's C code as a shared library, bypassing SELinux restrictions.

**Future Enhancement (Course Project):**
- Work with CellularLab repo to enhance user interface
- Add features like test history, AI analysis, and improved test strategies
- Integrate CellularLab's UI patterns and test management

## Troubleshooting
- If Gradle sync fails, try `./gradlew --stop` then refresh
- For emulator issues, try wiping data or creating new AVD
- If tests fail due to instrumentation, ensure emulator is running and API level matches
- For Compose preview issues, try invalidating caches and restarting Android Studio

### SELinux Execution Issues
Android uses SELinux which restricts process execution. If you see "permission denied (errno=13)" when running the iperf3 binary:
- This is expected behavior on Android 10+ without NDK compilation
- The proper solution requires compiling iperf3 C code as a shared library with NDK
- See CellularLab repo for reference implementation using CMake + JNI
  - https://github.com/davidBar-On/android-iperf3.git

**For Course Project (Three Viable Solutions):**
1. **Rooted Emulator**: Use `adb root` and `adb shell` to upload iperf3 to `/bin/` or `/data/local/tmp/`
2. **Pre-installed Binary**: Check if device has `/bin/iperf3` - the app tries this first
3. **Rooted Device**: Ensure instructor device is rooted and provide instructions

### Native Binary Issues
- If iperf3 binary is not found at runtime, ensure it's in `src/main/assets/iperf3_binaries/<abi>/iperf3`
- Verify `getIperf3Binary()` is called instead of the old `findIperf3Binary()` reference
- Check that `aaptOptions.noCompress += "iperf3"` is in `build.gradle.kts`
- Run `./gradlew :app:assembleDebug` and verify binaries appear in `unzip -l app/build/outputs/apk/debug/app-debug.apk | grep iperf3`

---

*This guide is intended to help maintain consistency and quality in the codebase. Adjust as needed based on project evolution.*
