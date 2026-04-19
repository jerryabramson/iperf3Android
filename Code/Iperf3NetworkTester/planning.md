# Planning

## Application Assessment

This is an early-stage Android Compose app focused on running `iperf3` from a
packaged native binary. The basic shape is sound: single-activity Compose UI,
Hilt-enabled app startup, a small ViewModel layer, and ABI-specific binaries
under assets. The project also builds cleanly at the unit-test task level:
`./gradlew :app:testDebugUnitTest` passed.

The main product constraint is not Kotlin or Compose, it is Android process
execution. The app explicitly depends on finding `/bin/iperf3` or extracting an
asset binary to cache, but the project guidance is correct: on modern Android,
SELinux makes this unreliable unless the device is rooted or the binary is
preinstalled. That means the current app is best understood as a proof of
concept around UI and process orchestration, not yet a generally deployable
Android `iperf3` client.

### What Is Working

- The app startup path is straightforward. `MainActivity` checks for an
  executable binary and either shows the main screen or a stub fallback.
- Hilt is wired end-to-end through `Iperf3Application` and
  `@AndroidEntryPoint` activity usage.
- The binary packaging approach is coherent. `getIperf3Binary()` selects an
  ABI, extracts the matching asset to cache, and marks it executable.
- The matching Gradle rule to avoid asset compression is present in
  `app/build.gradle.kts`.

### Key Risks And Gaps

- The state flow between UI and runner is inconsistent. `RunIperf3Screen`
  launches `runIperf3()`, but most rendered progress and output are local
  Compose state while the ViewModel updates plain fields instead of publishing
  reactive state.
- Default parameters are not safe for execution. The model starts with an empty
  binary path, zero duration, and zero timeout.
- `MainActivity` finds the binary but does not push that file into the
  ViewModel before rendering the main screen.
- `iperf3Runner` builds process arguments with empty strings for optional flags,
  which is brittle for `ProcessBuilder`.
- The app requests `INTERNET` permission at runtime even though it is a normal
  install-time permission.
- Hilt and Room are declared, but Room is not meaningfully integrated yet.
- `README.md` is out of sync with the actual source tree.

### Practical Insights

- The project is closest to a single-test-runner prototype, not yet a full
  tester application.
- The next high-value step is to make the ViewModel the single source of truth
  for UI state.
- The second high-value step is to harden command construction and validation
  before launch.
- The biggest strategic decision is whether the course demo will rely on a
  rooted/preinstalled binary path or whether the app will eventually move to a
  JNI/NDK-based execution model.

## Concrete Implementation Plan

1. Stabilize the execution model.
   Use `Iperf3RunViewModel` as the single source of truth for all run state:
   `iperf3Binary`, `serverHost`, `durationSecs`, `timeout`, `isRunning`,
   `progress`, `outputLines`, `exitCode`, and `errorMessage`. Remove duplicate
   local UI state from `RunIperf3Screen` so the screen only renders from
   collected state and sends intents back to the ViewModel.

2. Fix app initialization and parameter wiring.
   When `MainActivity` finds the binary, pass that file into the ViewModel
   before the run screen is shown. Replace the current default-empty values in
   `Iperf3Parameters` with sensible defaults like `durationSecs = 10`,
   `timeout = 10000`, and avoid using `File("")` as a valid runtime state.

3. Make process launching safe and deterministic.
   Refactor `iperf3Runner` to build a `MutableList<String>` of arguments instead
   of passing empty strings for optional flags. Validate inputs before launch
   and return structured results rather than mutating nested state indirectly.
   Treat stdout, stderr, exit code, and launch failures as explicit outputs.

4. Clean up the Compose screen.
   Simplify `RunIperf3Screen` into three sections: inputs, run controls, and
   results. Remove the runtime `INTERNET` permission request since it is not
   needed. Replace manual `for` rendering with `LazyColumn`, and show clear
   states: idle, running, success, failure, binary unavailable.

5. Decide and isolate the native-execution strategy.
   Short term, explicitly support the course-demo path: rooted emulator, rooted
   device, or preinstalled `/bin/iperf3`. Keep that logic in
   `getIperf3Binary()` and make the fallback UI explain why execution is
   unavailable. Longer term, if this is meant to work on standard Android
   devices, plan a second phase to replace external binary execution with
   JNI/NDK integration.

6. Remove dead code and unfinished scaffolding.
   Delete or park `ProcessRunnerViewModel` and `ProcessOutputScreen` if they are
   no longer part of the active path. Also either wire Room into real
   persistence or remove the dependency declarations until needed.

7. Add targeted tests.
   Add unit tests for argument building, binary selection, and ViewModel state
   transitions. Then add one Compose UI test for the main run screen and one
   instrumented test for the stubbed binary-unavailable path. The current tests
   are only templates and do not cover app behavior.

8. Update documentation to match reality.
   Refresh `README.md` so it reflects the current file tree, active
   architecture, known SELinux limitation, and exactly how to demo the app on a
   rooted emulator or device.

## Backlog

### Milestone 1: Make The Run Path Reliable

- `P0` Refactor `Iperf3RunViewModel.kt` into a single source of truth for screen
  state.
  Acceptance: one immutable UI state model contains host, binary path,
  duration, timeout, running flag, progress, output, exit code, and error
  state.

- `P0` Remove duplicated local state from `RunIperf3Screen.kt`.
  Acceptance: screen renders entirely from ViewModel state and only calls
  ViewModel events.

- `P0` Wire discovered binary into startup flow from `MainActivity.kt`.
  Acceptance: if `getIperf3Binary()` returns a file, the ViewModel receives it
  before a run can start.

- `P0` Replace unsafe defaults in `Iperf3Parameters.kt`.
  Acceptance: no execution path depends on `File("")`, `durationSecs = 0`, or
  `timeout = 0`.

### Milestone 2: Harden Process Execution

- `P0` Refactor `iperf3Runner.kt` to build arguments with a filtered list.
  Acceptance: no empty-string arguments are passed to `ProcessBuilder`.

- `P0` Add preflight validation before launch.
  Acceptance: missing host, missing binary, invalid duration, and invalid
  timeout produce user-visible errors without attempting process start.

- `P1` Return structured execution results from the runner.
  Acceptance: stdout lines, stderr lines, exit code, and launch failures are
  represented explicitly rather than through scattered mutable fields.

- `P1` Make progress calculation deterministic.
  Acceptance: progress is derived from duration or interval parsing and always
  ends at `1.0`.

### Milestone 3: Clean Up The UI

- `P0` Simplify `RunIperf3Screen.kt` into input, controls, and results
  sections.
  Acceptance: the screen clearly shows idle, running, success, failure, and
  unavailable-binary states.

- `P0` Remove the runtime `INTERNET` permission request.
  Acceptance: app relies only on manifest declaration in `AndroidManifest.xml`.

- `P1` Replace manual output rendering with `LazyColumn`.
  Acceptance: output remains scrollable and stable across recomposition.

- `P1` Improve the fallback experience in `StubbedIperf3Screen.kt`.
  Acceptance: screen explains the SELinux limitation and available demo options.

### Milestone 4: Remove Drift And Unused Scaffolding

- `P1` Review `ProcessRunnerViewModel.kt` and `ProcessOutputScreen.kt`.
  Acceptance: either remove them or integrate them intentionally; no dead
  alternate execution path remains.

- `P1` Audit dependency declarations in `app/build.gradle.kts`.
  Acceptance: unused Room or other scaffolding dependencies are removed, or
  real usage is added.

- `P2` Clean up unused imports, commented code, and stale helper variables.
  Acceptance: main app files compile cleanly with minimal noise.

### Milestone 5: Add Meaningful Test Coverage

- `P0` Add unit tests for binary selection logic in `findIperf3Binary.kt`.
  Acceptance: covers preinstalled binary path, extraction path, and failure
  path.

- `P0` Add unit tests for runner argument construction.
  Acceptance: covers reverse mode, force flush, duration, timeout, and invalid
  input rejection.

- `P1` Add ViewModel tests for run-state transitions.
  Acceptance: covers idle to running to success or failure and output and
  progress updates.

- `P1` Add at least one Compose UI test for the main run screen.
  Acceptance: host input, run button state, and visible result or error state
  are verified.

- `P2` Add an instrumented test for the binary-unavailable path.
  Acceptance: fallback screen renders correctly on device or emulator.

### Milestone 6: Document Reality

- `P1` Update `README.md` to match the actual tree and architecture.
  Acceptance: no references to removed files or outdated structure remain.

- `P1` Document the supported execution scenarios.
  Acceptance: README clearly states rooted emulator, rooted device, or
  preinstalled `/bin/iperf3` as the current viable paths.

- `P2` Add a short developer flow for build, test, and demo steps.
  Acceptance: a new contributor can build and understand the app's current
  limitations quickly.

### Future Milestone: Product Features After Core Stability

- `P2` Add test history with Room only after the run flow is stable.
- `P2` Add richer iperf parameter controls beyond host and duration.
- `P3` Explore JNI/NDK integration to replace external binary execution on
  standard devices.

## Suggested Sprint Order

1. Milestone 1
2. Milestone 2
3. Milestone 3
4. Milestone 4
5. Milestone 5
6. Milestone 6
