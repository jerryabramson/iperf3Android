package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner

import android.util.Log
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Run the iperf3 binary.
 * @param updateProgress Callback to update the progress bar.
 * @param stdout Callback to output to the UI.
 * @param stderr Callback to output to the UI.
 * @param iperf3Parameters The parameters for the iperf3 binary.
 * @return The return code from the iperf3 binary.
 */
suspend fun iperf3Runner(
    updateProgress: (Float) -> Unit,
    stdout: (String) -> Unit,
    stderr: (String) -> Unit,
    iperf3Parameters: Iperf3Parameters
): Int = withContext(Dispatchers.IO) {
    updateProgress(0.0f)
    if (iperf3Parameters.serverHost == "testing") {
        Log.w("iperf3Runner", "Forced simulation mode, iperf3 binary not found")
        stderr("Forced simulation mode due to hostname = 'testing'")
        var rc = launchSimulation(updateProgress, stdout, stderr)
        updateProgress((1.0.toFloat()))
        return@withContext rc
    }

    Log.d("Iperf3Runner", "iperf3 binary ${iperf3Parameters.iperf3Binary}")
    if (iperf3Parameters.durationSecs <= 0) {
        Log.e("Iperf3Runner", "Invalid duration: ${iperf3Parameters.durationSecs}")
        stderr("Invalid duration: ${iperf3Parameters.durationSecs}")
        return@withContext -1
    }
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
        stderr("Failed to start iperf3 process")
        stderr("  ${e.message}")
        return@withContext launchSimulation(updateProgress, stdout, stderr)
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
            updateProgress(intervalCount.toFloat() / iperf3Parameters.durationSecs)
        }
    }

    val stderrJob = launch {
        readStream(process.errorStream) { err ->
            stderr(err)
        }
    }

    // Wait for the process to finish
    val exitValue = process.waitFor()

    // Wait for the coroutines to finish
    stdoutJob.join()
    stderrJob.join()

    /**
     * Feedback from instructor:
     * The purpose of process.destroy():
     *   - Ensures the iperf3 subprocess is terminated
     *   - Releases system resources associated with the process
     *   - Is a safety measure:
     *       - even though waitFor() should have already terminated the process,
     *         calling destroy() guarantees cleanup
     *   ** It is safe to call multiple times (no effect if a process already terminated) **
     */
    process.destroy()

    // Ensure that the progress bar is fully updated
    updateProgress((1.0.toFloat()))
    return@withContext exitValue

}

suspend fun launchSimulation(
    updateProgress: (Float) -> Unit,
    stdout: (String) -> Unit,
    stderr: (String) -> Unit
): Int = withContext(Dispatchers.IO) {
    var started = false
    var intervalCount = 0
    stderr("  Simulation; 10 secs, 8 streams, reverse")
    val sampleText = arrayOf(
"    Connecting to host jabramson.com, port 5201",
"Reverse mode, remote host jabramson.com is sending",
"[  5] local 192.168.1.163 port 46048 connected to 192.168.1.32 port 5201",
"[  7] local 192.168.1.163 port 46060 connected to 192.168.1.32 port 5201",
"[  9] local 192.168.1.163 port 46062 connected to 192.168.1.32 port 5201",
"[ 11] local 192.168.1.163 port 46066 connected to 192.168.1.32 port 5201",
"[ 13] local 192.168.1.163 port 46072 connected to 192.168.1.32 port 5201",
"[ 15] local 192.168.1.163 port 46078 connected to 192.168.1.32 port 5201",
"[ 17] local 192.168.1.163 port 46086 connected to 192.168.1.32 port 5201",
"[ 19] local 192.168.1.163 port 46092 connected to 192.168.1.32 port 5201",
"[ ID] Interval           Transfer     Bitrate",
"[  5]   0.00-1.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[  7]   0.00-1.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[  9]   0.00-1.00   sec  1.25 MBytes  10.5 Mbits/sec",
"[ 11]   0.00-1.00   sec  2.00 MBytes  16.8 Mbits/sec",
"[ 13]   0.00-1.00   sec  3.00 MBytes  25.1 Mbits/sec",
"[ 15]   0.00-1.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[ 17]   0.00-1.00   sec  1.25 MBytes  10.5 Mbits/sec",
"[ 19]   0.00-1.00   sec  2.00 MBytes  16.8 Mbits/sec",
"[SUM]   0.00-1.00   sec  14.8 MBytes   124 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   1.00-2.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[  7]   1.00-2.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[  9]   1.00-2.00   sec  1.25 MBytes  10.5 Mbits/sec",
"[ 11]   1.00-2.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[ 13]   1.00-2.00   sec  2.38 MBytes  19.9 Mbits/sec",
"[ 15]   1.00-2.00   sec  1.38 MBytes  11.5 Mbits/sec",
"[ 17]   1.00-2.00   sec  1.25 MBytes  10.5 Mbits/sec",
"[ 19]   1.00-2.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[SUM]   1.00-2.00   sec  12.2 MBytes   103 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   2.00-3.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[  7]   2.00-3.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[  9]   2.00-3.00   sec  1.38 MBytes  11.5 Mbits/sec",
"[ 11]   2.00-3.00   sec  2.12 MBytes  17.8 Mbits/sec",
"[ 13]   2.00-3.00   sec  3.12 MBytes  26.2 Mbits/sec",
"[ 15]   2.00-3.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[ 17]   2.00-3.00   sec  1.38 MBytes  11.5 Mbits/sec",
"[ 19]   2.00-3.00   sec  2.00 MBytes  16.8 Mbits/sec",
"[SUM]   2.00-3.00   sec  15.6 MBytes   131 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   3.00-4.00   sec  2.00 MBytes  16.8 Mbits/sec",
"[  7]   3.00-4.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[  9]   3.00-4.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[ 11]   3.00-4.00   sec  2.00 MBytes  16.8 Mbits/sec",
"[ 13]   3.00-4.00   sec  3.12 MBytes  26.2 Mbits/sec",
"[ 15]   3.00-4.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[ 17]   3.00-4.00   sec  1.38 MBytes  11.5 Mbits/sec",
"[ 19]   3.00-4.00   sec  2.00 MBytes  16.8 Mbits/sec",
"[SUM]   3.00-4.00   sec  15.6 MBytes   131 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   4.00-5.00   sec  1.62 MBytes  13.6 Mbits/sec",
"[  7]   4.00-5.00   sec  1.62 MBytes  13.6 Mbits/sec",
"[  9]   4.00-5.00   sec  1.25 MBytes  10.5 Mbits/sec",
"[ 11]   4.00-5.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[ 13]   4.00-5.00   sec  2.75 MBytes  23.0 Mbits/sec",
"[ 15]   4.00-5.00   sec  1.62 MBytes  13.6 Mbits/sec",
"[ 17]   4.00-5.00   sec  1.25 MBytes  10.5 Mbits/sec",
"[ 19]   4.00-5.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[SUM]   4.00-5.00   sec  13.8 MBytes   115 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   5.00-6.00   sec  1.62 MBytes  13.7 Mbits/sec",
"[  7]   5.00-6.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[  9]   5.00-6.00   sec  1.25 MBytes  10.5 Mbits/sec",
"[ 11]   5.00-6.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[ 13]   5.00-6.00   sec  2.62 MBytes  22.1 Mbits/sec",
"[ 15]   5.00-6.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[ 17]   5.00-6.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[ 19]   5.00-6.00   sec  1.62 MBytes  13.7 Mbits/sec",
"[SUM]   5.00-6.00   sec  13.6 MBytes   114 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   6.00-7.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[  7]   6.00-7.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[  9]   6.00-7.00   sec  1.38 MBytes  11.5 Mbits/sec",
"[ 11]   6.00-7.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[ 13]   6.00-7.00   sec  2.75 MBytes  23.1 Mbits/sec",
"[ 15]   6.00-7.00   sec  1.62 MBytes  13.6 Mbits/sec",
"[ 17]   6.00-7.00   sec  2.12 MBytes  17.8 Mbits/sec",
"[ 19]   6.00-7.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[SUM]   6.00-7.00   sec  15.1 MBytes   127 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   7.00-8.00   sec  1.00 MBytes  8.39 Mbits/sec",
"[  7]   7.00-8.00   sec   896 KBytes  7.34 Mbits/sec",
"[  9]   7.00-8.00   sec   768 KBytes  6.29 Mbits/sec",
"[ 11]   7.00-8.00   sec  1.38 MBytes  11.5 Mbits/sec",
"[ 13]   7.00-8.00   sec  2.12 MBytes  17.8 Mbits/sec",
"[ 15]   7.00-8.00   sec   896 KBytes  7.34 Mbits/sec",
"[ 17]   7.00-8.00   sec  1.12 MBytes  9.43 Mbits/sec",
"[ 19]   7.00-8.00   sec   896 KBytes  7.34 Mbits/sec",
"[SUM]   7.00-8.00   sec  9.00 MBytes  75.5 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   8.00-9.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[  7]   8.00-9.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[  9]   8.00-9.00   sec  1.38 MBytes  11.5 Mbits/sec",
"[ 11]   8.00-9.00   sec  1.50 MBytes  12.6 Mbits/sec",
"[ 13]   8.00-9.00   sec  3.38 MBytes  28.3 Mbits/sec",
"[ 15]   8.00-9.00   sec  1.75 MBytes  14.7 Mbits/sec",
"[ 17]   8.00-9.00   sec  2.50 MBytes  21.0 Mbits/sec",
"[ 19]   8.00-9.00   sec  1.88 MBytes  15.7 Mbits/sec",
"[SUM]   8.00-9.00   sec  16.0 MBytes   134 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[  5]   9.00-10.00  sec  1.62 MBytes  13.6 Mbits/sec",
"[  7]   9.00-10.00  sec  1.62 MBytes  13.6 Mbits/sec",
"[  9]   9.00-10.00  sec  1.25 MBytes  10.5 Mbits/sec",
"[ 11]   9.00-10.00  sec  1.75 MBytes  14.7 Mbits/sec",
"[ 13]   9.00-10.00  sec  3.50 MBytes  29.4 Mbits/sec",
"[ 15]   9.00-10.00  sec  1.50 MBytes  12.6 Mbits/sec",
"[ 17]   9.00-10.00  sec  2.25 MBytes  18.9 Mbits/sec",
"[ 19]   9.00-10.00  sec  1.50 MBytes  12.6 Mbits/sec",
"[SUM]   9.00-10.00  sec  15.0 MBytes   126 Mbits/sec",
"- - - - - - - - - - - - - - - - - - - - - - - - -",
"[ ID] Interval           Transfer     Bitrate         Retr",
"[  5]   0.00-10.03  sec  19.5 MBytes  16.3 Mbits/sec   43            sender",
"[  5]   0.00-10.00  sec  16.5 MBytes  13.8 Mbits/sec                  receiver",
"[  7]   0.00-10.03  sec  18.6 MBytes  15.6 Mbits/sec   22            sender",
"[  7]   0.00-10.00  sec  16.4 MBytes  13.7 Mbits/sec                  receiver",
"[  9]   0.00-10.03  sec  14.2 MBytes  11.9 Mbits/sec    8            sender",
"[  9]   0.00-10.00  sec  12.6 MBytes  10.6 Mbits/sec                  receiver",
"[ 11]   0.00-10.03  sec  20.9 MBytes  17.5 Mbits/sec   20            sender",
"[ 11]   0.00-10.00  sec  17.8 MBytes  14.9 Mbits/sec                  receiver",
"[ 13]   0.00-10.03  sec  32.5 MBytes  27.2 Mbits/sec   23            sender",
"[ 13]   0.00-10.00  sec  28.8 MBytes  24.1 Mbits/sec                  receiver",
"[ 15]   0.00-10.03  sec  17.9 MBytes  14.9 Mbits/sec   45            sender",
"[ 15]   0.00-10.00  sec  15.8 MBytes  13.2 Mbits/sec                  receiver",
"[ 17]   0.00-10.03  sec  18.3 MBytes  15.3 Mbits/sec   20            sender",
"[ 17]   0.00-10.00  sec  16.0 MBytes  13.4 Mbits/sec                  receiver",
"[ 19]   0.00-10.03  sec  19.5 MBytes  16.3 Mbits/sec   67            sender",
"[ 19]   0.00-10.00  sec  17.0 MBytes  14.3 Mbits/sec                  receiver",
"[SUM]   0.00-10.03  sec   161 MBytes   135 Mbits/sec  248             sender",
"[SUM]   0.00-10.00  sec   141 MBytes   118 Mbits/sec                  receiver",
"",
"iperf Done.")
    sampleText.forEach {line ->
        delay(250)
        if (line.contains("Interval") && !started) {
            started = true
            intervalCount = 0
        } else {
            if (false) { // simulation is 8 streams
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
    updateProgress((1.0.toFloat()))
    return@withContext 0
}
