package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner

import android.util.Log
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

suspend fun iperf3Runner(
    updateProgress: (Float) -> Unit,
    stdout: (String) -> Unit,
    stderr: (String) -> Unit,
    iperf3Parameters: Iperf3Parameters
): Int = withContext(Dispatchers.IO) {
    updateProgress(0.0f)
    if (iperf3Parameters.serverHost == "testing") {
        Log.w("iperf3Runner", "Forced simulation mode, iperf3 binary not found")
        var rc = launchSimulation(updateProgress, stdout, stderr, iperf3Parameters)
        updateProgress((1.0.toFloat()))
        return@withContext rc
    }

    Log.d("Iperf3Runner", "iperf3 binary ${iperf3Parameters.iperf3Binary}")
    val command = iperf3Parameters.iperf3Binary.absolutePath
    var flush  = ""
    var reverse = ""
    val parallelStreams = iperf3Parameters.parallelStreams
    if (iperf3Parameters.forceFlush) flush = "--forceflush"
    if (iperf3Parameters.isReverse) reverse = "-R"
    var localTimeout = 3000L
    if (iperf3Parameters.timeout != 0L) localTimeout = iperf3Parameters.timeout

    val processBuilder = ProcessBuilder(
        command,
        "-c", iperf3Parameters.serverHost,
        reverse,
        flush,
        "-P", parallelStreams.toString(),
        "--connect-timeout",
        localTimeout.toString(),
        "-t", iperf3Parameters.durationSecs.toString(),
        "-O", iperf3Parameters.skip.toString())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
    Log.d("Iperf3Runner", "processBuilder: ${processBuilder.command()}")

    // -----------------------------------------------------------
    // 4️⃣ Launch, capture output, wait for termination
    // -----------------------------------------------------------
    val process: Process
    try {
        process = processBuilder.start()
    } catch (e: Exception) {
        Log.e("iperf3Runner", "Failed to start iperf3 process: ${e.message}", e)
        stdout("Failed to start iperf3 process: ${e.message}, using simulated output")
        return@withContext launchSimulation(updateProgress, stdout, stderr, iperf3Parameters)
    }


    fun readStream(
        input: InputStream,
        consumer: (String) -> Unit
    ) = BufferedReader(InputStreamReader(input)).use { reader ->
        var line: String? = null
        while (reader.readLine().also { line = it } != null) {
            consumer(line!!)
        }
    }

    var intervalCount = 0
    var started = false
    // Launch two coroutines: one for stdout, one for stderr
    val stdoutJob = launch {
        readStream(process.inputStream) { line ->
            if (line.contains("Interval") && !started) {
                started = true
                intervalCount = 0
            } else {
                if (iperf3Parameters.parallelStreams == 1) {
                    if (line.contains("[") && line.contains("]")) {
                        if (started) intervalCount++
                    }
                } else {
                    if (line.contains("[") && line.contains("]") && line.contains("SUM")) {
                        if (started) intervalCount++
                    }
                }
            }
            stdout(line)
            updateProgress(intervalCount.toFloat() / 10)
        }
    }

    val stderrJob = launch {
        readStream(process.errorStream) { err ->
            stderr(err)
        }
    }

    val exitValue = process.waitFor()
    stdoutJob.join()
    stderrJob.join()

    updateProgress((1.0.toFloat()))
    return@withContext exitValue
    //val output = process.inputStream.bufferedReader().readText()
}

suspend fun launchSimulation(
    updateProgress: (Float) -> Unit,
    stdout: (String) -> Unit,
    stderr: (String) -> Unit,
    iperf3Parameters: Iperf3Parameters
): Int = withContext(Dispatchers.IO) {
    var started = false
    var intervalCount = 0
    val sampleText = arrayOf(
        "Connecting SIMULATION",
        "Reverse mode, remote host jabramson.com is sending",
        "[  5] local 10.0.2.16 port 34704 connected to 192.168.1.32 port 5201",
        "[ ID] Interval           Transfer     Bitrate",
        "[  5]   0.00-1.00   sec  45.2 MBytes   379 Mbits/sec",
        "[  5]   1.00-2.00   sec  44.7 MBytes   375 Mbits/sec",
        "[  5]   2.00-3.00   sec  41.7 MBytes   349 Mbits/sec",
        "[  5]   3.00-4.00   sec  41.7 MBytes   350 Mbits/sec",
        "[  5]   4.00-5.00   sec  28.3 MBytes   238 Mbits/sec",
        "[  5]   5.00-6.00   sec  34.4 MBytes   288 Mbits/sec",
        "[  5]   6.00-7.00   sec  40.6 MBytes   341 Mbits/sec",
        "[  5]   7.00-8.00   sec  43.5 MBytes   365 Mbits/sec",
        "[  5]   8.00-9.00   sec  43.4 MBytes   364 Mbits/sec",
        "[  5]   9.00-10.00  sec  44.6 MBytes   374 Mbits/sec",
        "- - - - - - - - - - - - - - - - - - - - - - - - -",
        "[ ID] Interval           Transfer     Bitrate         Retr",
        "[  5]   0.00-10.00  sec   409 MBytes   343 Mbits/sec    2             sender",
        "[  5]   0.00-10.00  sec   408 MBytes   342 Mbits/sec                  receiver",
        "",
        "iperf Done.")
    Thread.sleep(1500)
    sampleText.forEach {line ->
        if (line.contains("Interval") && !started) {
            started = true
            intervalCount = 0
        } else {
            if (iperf3Parameters.parallelStreams == 1) {
                if (line.contains("[") && line.contains("]")) {
                    Thread.sleep(1000)
                    if (started && intervalCount < 10) intervalCount++
                }
            } else {
                if (line.contains("[") && line.contains("]") && line.contains("SUM")) {
                    Thread.sleep(1000)
                    if (started && intervalCount < 10) intervalCount++
                }
            }
        }
        stdout(line)
        updateProgress(intervalCount.toFloat() / 10)
    }
    updateProgress((1.0.toFloat()))
    return@withContext 0
}