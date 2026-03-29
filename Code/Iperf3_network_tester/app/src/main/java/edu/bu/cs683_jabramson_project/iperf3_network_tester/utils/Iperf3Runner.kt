package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.content.Context
import java.io.File
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Runs the native iperf3 binary that lives under
 * `src/main/jniLibs/<abi>/iperf3`.
 *
 * This is a **suspending** function so it can be called from Compose
 * without blocking the UI thread.
 *
 * @param context   Any Android Context (usually an Activity or Application)
  * @param serverHost Hostname or IP of the iperf3 server
 * @param durationSec Test length in seconds (default = 10)
 * @return Raw stdout of the iperf3 process
 * @throws Exception if the binary cannot be found, executed, or the process fails
 */


@RequiresApi(Build.VERSION_CODES.O)
suspend fun iperf3Runner(
    updateProgress: (Float) -> Unit,
    iperfBinary: File,
    serverHost: String,
    durationSec: Int = 10,
    outputLines: MutableList<String>
): Int = withContext(Dispatchers.IO) {
    Log.d("Iperf3Runner", "iperf3 binary $iperfBinary")
    val command = iperfBinary.absolutePath

    val processBuilder = ProcessBuilder(
        command,
        "-c", serverHost,
        "-R",
        "--forceflush",
        "-t", durationSec.toString())
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
    processBuilder.redirectErrorStream(true)   // merge stderr into stdout

    // -----------------------------------------------------------
    // 4️⃣ Launch, capture output, wait for termination
    // -----------------------------------------------------------
    val process = processBuilder.start()




    fun readStream(
        input: java.io.InputStream,
        consumer: (String) -> Unit
    ) = BufferedReader(InputStreamReader(input)).use { reader ->
        var line: String? = null
        while (reader.readLine().also { line = it } != null) {
            consumer(line!!)
        }
    }

    val extraLines: Float = (durationSec.toFloat() * 1) + 5


    var intervalCount = 0
    var started = false
    // Launch two coroutines: one for stdout, one for stderr
    val stdoutJob = launch {
        readStream(process.inputStream) { line ->
            addLine(line, outputLines)
            if (line.contains("Interval") && !started) {
                started = true
                intervalCount = 0
            } else if (line.contains("[") && line.contains("]")) {
                if (started) intervalCount++
            }
            updateProgress(intervalCount.toFloat() / 10)
        }
    }

    val stderrJob = launch {
        readStream(process.errorStream) { err ->
            addError(err,  outputLines)
        }
    }

    val exitValue = process.waitFor()
    stdoutJob.join()
    stderrJob.join()

    updateProgress((1.0.toFloat()))
    return@withContext exitValue
    //val output = process.inputStream.bufferedReader().readText()


}
fun addLine(line: String, newLine: MutableList<String>) {
    newLine.add(line)
    Log.d("Iperf3Runner: ", "stdout: $line")
}

fun addError(line: String, newLine: MutableList<String>) {
    newLine.add(line)
    Log.d("Iperf3Runner: ", "stderr: $line")
}



