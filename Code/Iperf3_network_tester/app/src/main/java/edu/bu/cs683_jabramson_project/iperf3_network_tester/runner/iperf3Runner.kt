package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner

import android.util.Log
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3ResultsData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.concurrent.timer

suspend fun iperf3Runner(
    updateProgress: (Float) -> Unit,
    callback: (String) -> Unit,
    iperf3Parameters: Iperf3Parameters
): Int = withContext(Dispatchers.IO) {

    Log.d("Iperf3Runner", "iperf3 binary ${iperf3Parameters.iperf3Binary}")
    val command = iperf3Parameters.iperf3Binary.absolutePath
    var flush  = ""
    var reverse = ""
    if (iperf3Parameters.forceFlush) flush = "--forceflush"
    if (iperf3Parameters.isReverse) reverse = "-R"
    var localTimeout = 3000L
    if (iperf3Parameters.timeout != 0L) localTimeout = iperf3Parameters.timeout
    val processBuilder = ProcessBuilder(
        command,
        "-c", iperf3Parameters.serverHost,
        reverse,
        flush,
        "--connect-timeout",
        localTimeout.toString(),
        "-t", iperf3Parameters.durationSecs.toString()
    )
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
   // processBuilder.redirectErrorStream(true)   // merge stderr into stdout

    // -----------------------------------------------------------
    // 4️⃣ Launch, capture output, wait for termination
    // -----------------------------------------------------------
    val process = processBuilder.start()


    fun readStream(
        input: InputStream,
        consumer: (String) -> Unit
    ) = BufferedReader(InputStreamReader(input)).use { reader ->
        var line: String? = null
        while (reader.readLine().also { line = it } != null) {
            consumer(line!!)
        }
    }

    val extraLines: Float = (iperf3Parameters.durationSecs.toFloat() * 1) + 5


    var intervalCount = 0
    var started = false
    // Launch two coroutines: one for stdout, one for stderr
    val stdoutJob = launch {
        readStream(process.inputStream) { line ->
            //  addLine(line, outputLines)
            Log.d("Iperf3Runner: ", "stdout: $line")
            if (line.contains("Interval") && !started) {
                started = true
                intervalCount = 0
            } else if (line.contains("[") && line.contains("]")) {
                if (started) intervalCount++
            }
            callback(line)
            updateProgress(intervalCount.toFloat() / 10)
        }
    }

    val stderrJob = launch {
        readStream(process.errorStream) { err ->
            addError(err, iperf3Parameters.results.errors)
        }
    }

    val exitValue = process.waitFor()
    stdoutJob.join()
    stderrJob.join()

    updateProgress((1.0.toFloat()))
    return@withContext exitValue
    //val output = process.inputStream.bufferedReader().readText()
}

fun addError(line: String, newLine: MutableList<String>) {
    newLine.add(line)
    Log.d("Iperf3Runner: ", "stderr: $line")
}