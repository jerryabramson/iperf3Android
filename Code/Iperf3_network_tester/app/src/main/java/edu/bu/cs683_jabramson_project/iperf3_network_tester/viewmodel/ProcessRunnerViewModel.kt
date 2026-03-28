package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel

// ProcessRunnerViewModel.kt


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File



/**
 * ViewModel that owns the data flow for an external command.
 *
 * It runs the command in a coroutine, reads stdout line‑by‑line,
 * and pushes each line into a StateFlow<List<String>> so the UI can
 * collect it with collectAsState()/collectAsStateWithLifecycle().
 */
class ProcessRunnerViewModel : ViewModel() {

    /** Holds the accumulated output lines. */
    private val _outputLines = MutableStateFlow<List<String>>(emptyList())
    val outputLines: StateFlow<List<String>> = _outputLines.asStateFlow()

    /**
     * Starts the given command and begins streaming its stdout.
     *
     * @param command   The command and its arguments, e.g. listOf("ls","-l","/sdcard")
     * @param workDir   Optional working directory (null = inherit)
     * @param env       Optional environment variables (null = inherit)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun runCommand(
        context: Context,
        command: List<String>,
        workDir: File? = null,
        env: Map<String, String>? = null
    ) {



        viewModelScope.launch {
            // ---------- Build the process ----------
            val pb = ProcessBuilder(
                "/bin/iperf3",
                "-c", "192.168.1.28",
                "-R",
                "--forceflush",
                "-t", "10")
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
            env?.forEach { (k, v) -> pb.environment().put(k, v) }


            val process = pb.start()

            // ---------- Helper to read a stream line‑by‑line ----------
            suspend fun readStream(
                input: java.io.InputStream,
                onLine: (String) -> Unit
            ) = BufferedReader(InputStreamReader(input)).use { reader ->
                var line: String? = null
                while (reader.readLine().also { line = it } != null) {
                    onLine(line!!)
                    Log.d("ProcessRunnerViewModel", "readStream: $line")
                }
            }

            // ---------- Launch two coroutines: stdout & stderr ----------
            val stdoutJob = launch {
                readStream(process.inputStream) { line ->
                    // Emit a new snapshot each line arrives.
                    // Using update { } avoids allocating a brand‑new List on every emission.
                    _outputLines.update { it + line }
                    Log.d("ProcessRunnerViewModel", "stdout: $line")
                }
            }

            val stderrJob = launch {
                readStream(process.errorStream) { err ->
                    // For demo purposes we just log stderr; you could expose it via another flow.
                    _outputLines.update { it + err }
                    Log.d("ProcessRunnerViewModel", "stderr $err")
                }
            }

            // ---------- Wait for the process to finish (after draining started) ----------
            val exitValue = process.waitFor()
            println("Process finished with exitCode=$exitValue")

            // ---------- Clean up reader coroutines ----------
            stdoutJob.join()
            stderrJob.join()


        }
    }

    /** Cancel any ongoing work when the ViewModel is cleared. */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}
