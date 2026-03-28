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
    context: Context,
    serverHost: String,
    durationSec: Int = 10,
    outputLines: MutableList<String>
): Int = withContext(Dispatchers.IO) {
    // -----------------------------------------------------------
    // 1️⃣ Resolve the binary location: <app‑files-dir>/jniLibs/<abi>/iperf3
    // -----------------------------------------------------------
    val abi = chooseAbi(context)
    val libDir = File(context.filesDir, "jniLibs/$abi")
    var iperfBinary = File(libDir, "iperf3")

    if (!iperfBinary.exists()) {
        // Fall back to the one I manually installed
        iperfBinary = File("/bin/iperf3")
    }
    println("iperf3 binary: $iperfBinary")
    // -----------------------------------------------------------
    // 2️⃣ Verify existence and make it executable (chmod 755)
    // -----------------------------------------------------------
    require(iperfBinary.exists()) {
        "iperf3 binary not found at $iperfBinary"
    }
    iperfBinary.setExecutable(true, false)   // <-- equivalent to chmod 755

    // -----------------------------------------------------------
    // 3️⃣ Build the command line (reverse test, -R)
    // -----------------------------------------------------------
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

    var lineCount: Int = 0
    val extraLines: Float = (durationSec.toFloat() * 1) + 5


    // Launch two coroutines: one for stdout, one for stderr
    val stdoutJob = launch {
        readStream(process.inputStream) { line ->
            addLine(line, outputLines)
            lineCount++
            updateProgress(lineCount.toFloat() / extraLines)
        }
    }

    val stderrJob = launch {
        readStream(process.errorStream) { err ->
            addError(err,  outputLines)
            lineCount++
            updateProgress(lineCount.toFloat() / extraLines)
        }
    }

    val exitValue = process.waitFor()
    stdoutJob.join()
    stderrJob.join()

    updateProgress(1f)
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

/**
 * Utility – pick the first ABI that matches a folder we have under jniLibs/.
 * Adjust the order if you want a different priority.
 */
private fun chooseAbi(context: Context): String {
    val abiList = Build.SUPPORTED_ABIS
    return listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        .firstOrNull { abiList.contains(it) } ?: "arm64-v8a"
}


