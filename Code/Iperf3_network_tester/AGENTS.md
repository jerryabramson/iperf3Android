# AGENTS.md - Guidelines for Agentic Coding in Iperf3 Network Tester

## Introduction
This document provides guidelines for agentic coding agents working on the Iperf3 Network Tester Android project. It covers build/test/lint commands, code style, and best practices.

## Project Overview
- Android application using Jetpack Compose for UI
- Kotlin programming language
- Gradle build system
- Unit tests with JUnit
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
- Test results appear in `app/build/reports/tests/testDebugUnitTest/index.html`
- Android test results in `app/build/reports/androidTests/connected/`

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
- Test methods: descriptive names using backticks if needed (e.g., `addition_isCorrect`)
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
- Test method names: descriptive, using backticks if necessary
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
- Reference issues in commit messages when applicable (e.g., `fix: resolve crash on rotation #123`)
- Keep feature branches short-lived
- Pull requests should include description and screenshots for UI changes
- Ensure code builds and passes tests before creating PR
- Resolve merge conflicts promptly

## Additional Notes
- The project uses Gradle Version Catalogs (libs.versions.toml) for dependency management
- Refer to `gradle/libs.versions.toml` for dependency versions
- Android Studio is the recommended IDE
- Enable "Power Save Mode" only when necessary as it disables inspections
- Run `./gradlew ktlintCheck` if Kotlin linting is configured (not currently in project)
- For Compose previews, use `@Preview` with various parameters (width, height, fontScale, etc.)
- Accessibility: ensure touch targets are at least 48dp, provide content descriptions for icons
- Performance: avoid heavy computations in composables; use `derivedStateOf` for state transformations
- Security: do not hardcode secrets; use Android Keystore or encrypted preferences for sensitive data

## Troubleshooting
- If Gradle sync fails, try `./gradlew --stop` then refresh
- For emulator issues, try wiping data or creating new AVD
- If tests fail due to instrumentation, ensure emulator is running and API level matches
- For Compose preview issues, try invalidating caches and restarting Android Studio

---
*This guide is intended to help maintain consistency and quality in the codebase. Adjust as needed based on project evolution.*