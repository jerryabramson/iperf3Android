# Remove Latent iperf3 Binary Code

iperf3 now runs via JNI (`libcellularlib.so`). The old subprocess-based code path that depends on the bundled iperf3 binary is dead code. The following removes it.

---

## Assets (delete)

```
app/src/main/assets/iperf3_binaries/
```

4 pre-compiled binaries (~500 KB total).

## Utility file (delete)

```
app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/utils/findIperf3Binary.kt
```

Contains `findIperf3Binary()`, `extractIperf3Binary()`, and `chooseAbi()`.

## Dead subprocess runner (delete)

```
app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/runner/iperf3Runner.kt
```

Old `ProcessBuilder`-based runner. Not called by the active JNI code path (`IperfTestManage` calls `IperfRunner.runIperfLive()` instead). The `launchSimulation()` function inside it is also orphaned.

---

## Build config (edit)

**File**: `app/build.gradle.kts:46-48`

Remove the `aaptOptions` block:
```kotlin
    aaptOptions {
        noCompress += "iperf3"
    }
```

## MainActivity (edit)

**File**: `MainActivity.kt`

| Line | Action |
|------|--------|
| 14 | Remove `import ...findIperf3Binary` |
| 17 | Remove `import java.io.File` |
| 30 | Remove `val iperf3Binary: File? = findIperf3Binary(context)` |
| 32 | Change `RunIperf3Screen(iperf3Binary = iperf3Binary, viewModel = viewModel)` to `RunIperf3Screen(viewModel = viewModel)` |

## Iperf3View (edit)

**File**: `view/Iperf3View.kt`

| Line | Action |
|------|--------|
| 48 | Remove `import java.io.File` |
| 59 | Remove the `iperf3Binary: File?` parameter from `RunIperf3Screen` |
| 64 | Remove `viewModel.setupIperf3Executable(iperf3Binary)` |
| 104-112 | Remove the `Text` composable that displays `${uiState.iperf3Parameters.iperf3Binary}` in the bottom bar |

## Iperf3RunViewModel (edit)

**File**: `viewmodel/Iperf3RunViewModel.kt`

| Line | Action |
|------|--------|
| 23 | Remove `import java.io.File` |
| 80 | Remove `var iperf3Binary: File = File("")` |
| 93-100 | Remove the entire `setupIperf3Executable()` function |
| 233 | Remove `_uiStateFlow.value.iperf3Parameters.iperf3Binary = this.iperf3Binary` |

## Iperf3Parameters model (edit)

**File**: `model/Iperf3Parameters.kt`

| Line | Action |
|------|--------|
| 3 | Remove `import java.io.File` |
| 6 | Remove `var iperf3Binary:File = File(""),` |

---

## Active Path (untouched)

The JNI execution path remains intact:
- `IperfTestManage.startTest()` calls `IperfRunner.runIperfLive()`
- `IperfJNIRunner` loads `libcellularlib.so` via `System.loadLibrary()`
- CMake builds iperf3 3.19 source from `app/src/main/cpp/`
