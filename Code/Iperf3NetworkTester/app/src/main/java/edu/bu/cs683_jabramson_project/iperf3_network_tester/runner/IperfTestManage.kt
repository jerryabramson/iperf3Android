package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner


import android.content.Context
import android.util.Log
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Iperf3OutputMonitor

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Manages the lifecycle and logic of running iPerf3-based network tests.
 * Supports advanced features like smart ramp-up, hybrid tests, and automatic bandwidth reduction.
 */
class IperfTestManage(
    var updateProgress: (Float) -> Unit,
    var stdout: (Iperf3OutputMonitor.LineResult, Boolean) -> Unit,
    var stderr: (Iperf3OutputMonitor.LineResult, String) -> Unit,
    private val onTestComplete: () -> Unit
) {
    val tag: String = "IperfTestManage"

    private val iperf3OutputMonitor = Iperf3OutputMonitor()

    private var context: Context? = null
    private var iperf3Parameters: Iperf3Parameters = Iperf3Parameters()

    @Volatile
    private var isIperfRunning = false

    fun getCurrentLineResult() = iperf3OutputMonitor.getCurrentLineResult()

    suspend fun cancelTest(): Int {
        var rc = 0
        if (context == null) return -1
        if (!isIperfRunning) return -1

        stderr(iperf3OutputMonitor.getCurrentLineResult(), "Test Cancelled")
        IperfRunner.forceStop(
            createIperfCallback(
                onLine =
                    {
                        val line = it.trim().removeSuffix("\n")
                        val newLineResult = iperf3OutputMonitor.processCancelLine(line)
                        stdout(newLineResult, true)
                        Log.d(tag, "forceStop: onLine: $line")
                    }, onError =
                    {
                        val err = it.trim().removeSuffix("\n").removePrefix("\n")
                        isIperfRunning = false
                        stderr(iperf3OutputMonitor.getCurrentLineResult(), "❌ Error: $err")
                        onTestComplete()
                        //testCompleted.complete(Unit)
                        updateProgress(1.0f)
                        rc = -1
                    },
                onComplete = {
                    isIperfRunning = false
                    onTestComplete()
                    updateProgress(1.0f)
                    //testCompleted.complete(Unit)
                }
            )
        )
        return rc


    }
    /**
     * Starts the test with the provided arguments and configuration.
     */
    suspend fun startTest(contextParam: Context?, params: Iperf3Parameters): Int
    {
        if (contextParam == null) return -1

        Log.d(tag, "iperf3 JNI Bridge Starting")

        /**
         * 1. Initialize the iperf3 parameters
         */
        var rc = 0
        iperf3Parameters = params
        context = contextParam
        val reverse = if (iperf3Parameters.isReverse)  "--reverse" else ""
        val flush = if (iperf3Parameters.forceFlush) "--forceflush" else ""
        val serverPort = iperf3Parameters.serverPort
        val localTimeout = if (iperf3Parameters.timeout != 0L) iperf3Parameters.timeout else 3000L
        val parallelStreams = iperf3Parameters.parallelStreams
        val durationSecs = iperf3Parameters.durationSecs
        if (durationSecs <= 0 || durationSecs > (60 * 60 * 12)) {
            Log.e("Iperf3Runner", "Invalid duration: $durationSecs. Must be between 1 second to 12 hours.")
            stderr(iperf3OutputMonitor.getCurrentLineResult(), "Invalid duration: ${iperf3Parameters.durationSecs}")
            updateProgress(1.0f)
            return -1
        }

        /**
         *  2. Set the default iperf3 temp directory to the app's cache directory'. Some Android
         *     devices may not allow writing to the external storage directory (/data/data/tmp).
         */
        val tempDirectory: String = if (context != null) context!!.cacheDir.toString() else ""
        IperfRunner.setTempDir(tempDirectory)
        Log.i(tag, "tempDirectory: $tempDirectory")

        /**
         * 3. Construct the iperf3 command line arguments.
         *    **Note** that argv[0] must be set to the program name.
         */
        val currentArgs = arrayOf(
            "iperf3",
            "--client", iperf3Parameters.serverHost,
            "--port", "$serverPort",
            reverse,
            flush,
            "--parallel", parallelStreams.toString(),
            "--connect-timeout",
            localTimeout.toString(),
            "--time", iperf3Parameters.durationSecs.toString(),
            "--omit", iperf3Parameters.skip.toString(),
            //"-V"
        )
        Log.d(tag, "currentArgs: ${currentArgs.joinToString(",")}")

        /**
         * 4. Initialize the output monitor.
         */
        iperf3OutputMonitor.reset()
        iperf3OutputMonitor.setParallel(parallelStreams)
        updateProgress(0.toFloat())



        /**
         * 5. Create a coroutine exception handler for uncaught exceptions
         */
        val handler = CoroutineExceptionHandler { _, exception ->
            stderr(iperf3OutputMonitor.getCurrentLineResult(), "🚨 Uncaught Exception: ${exception.localizedMessage}")
            exception.printStackTrace()
            rc = -1
        }

        /**
         * 6. Start the actual iperf3 test using the coroutine scope.
         */
        Log.d("IperfTestManage: ", "startTest")
        isIperfRunning = true

            // region Start Actual iPerf Test
        var lastIntervalCount = -1L
        var lastNumberOfMessages = 0
        val zeroProgress = 0.0.toFloat()
        val finishedProgress = 1.0.toFloat()
        var runningProgress = zeroProgress
        val runJob = CoroutineScope(Dispatchers.IO + handler).launch {
            isIperfRunning = true
            Log.d("IperfTestManage: ", "runJob")
            IperfRunner.runIperfLive(
                currentArgs, createIperfCallback(
                    onLine =
                        {
                            val line = it.trim().removeSuffix("\n")
                            val newLineResult = iperf3OutputMonitor.processLine(line)
                            Log.d(tag, "Callback -> onLine(\"${newLineResult.rawOutputLine}\")")
                            if (newLineResult.intervalNumber > lastIntervalCount
                                || newLineResult.messages.size > lastNumberOfMessages) {
                                stdout(newLineResult, newLineResult.messages.size > lastNumberOfMessages)
                                if (newLineResult.intervalNumber > lastIntervalCount) {
                                    runningProgress = lastIntervalCount.toFloat() / iperf3Parameters.durationSecs
                                }
                                runningProgress = if (runningProgress > finishedProgress) finishedProgress else runningProgress
                                runningProgress = if (runningProgress < zeroProgress) zeroProgress else runningProgress
                                updateProgress(runningProgress)
                                lastIntervalCount = newLineResult.intervalNumber
                                lastNumberOfMessages = newLineResult.messages.size
                            }
                        }, onError =
                        {
                            val err = it.trim().removeSuffix("\n")
                            isIperfRunning = false
                            stderr(iperf3OutputMonitor.getCurrentLineResult(), "❌ Error: $err")
                            onTestComplete()
                            //testCompleted.complete(Unit)
                            updateProgress(1.0f)
                            rc = -1
                        },
                    onComplete = {
                        isIperfRunning = false
                        onTestComplete()
                        updateProgress(1.0f)
                        //testCompleted.complete(Unit)
                    }
                )
            )

        }
        isIperfRunning = false
        runJob.join()
        runJob.cancel()
        return rc //@withContext -1
    }
}



private fun createIperfCallback(
    onLine: (String) -> Unit = {},
    onError: (String) -> Unit = {},
    onComplete: () -> Unit = {}
): IperfCallback {
    return object : IperfCallback {
        override fun onOutput(line: String) = onLine(line)
        override fun onError(error: String) = onError(error)
        override fun onComplete() = onComplete()
    }
}
