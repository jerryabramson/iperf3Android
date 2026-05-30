package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner


import android.R.attr.tag
import android.content.Context
import android.util.Log
import androidx.room.util.newStringBuilder
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
    val tag: String = "IperfTestManage",
    var updateProgress: (Float) -> Unit,
    var stdout: (Iperf3OutputMonitor.LineResult, Boolean) -> Unit,
    var stderr: (Iperf3OutputMonitor.LineResult, String) -> Unit,
    var iperf3Parameters: Iperf3Parameters,
    private val onTestComplete: () -> Unit
) {

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val iperf3OutputMonitor = Iperf3OutputMonitor()
    private var context: Context? = null

    fun getCurrentLineResult() = iperf3OutputMonitor.getCurrentLineResult()



    @Volatile
    private var isIperfRunning = false

    // endregion

    // region Public Lifecycle

    fun setContext(contextParam: Context?) { context = contextParam }

    suspend fun cancelTest(): Int {
        var rc = 0
        if (context == null) return -1
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
    suspend fun startTest(): Int {
        if (context == null) return -1
        Log.d(tag, "iperf3 JNI Bridge")

        if (context == null) return -1

        var rc = 0
        updateProgress(0.0f)
        iperf3OutputMonitor.reset()

        if (iperf3Parameters.durationSecs <= 0) {
            Log.e("Iperf3Runner", "Invalid duration: ${iperf3Parameters.durationSecs}")
            stderr(iperf3OutputMonitor.getCurrentLineResult(), "Invalid duration: ${iperf3Parameters.durationSecs}")
            updateProgress(1.0f)
            return -1 // @withContext -1
        }
        //val command = iperf3Parameters.iperf3Binary.absolutePath


        Log.d(tag, "tempDirectory: tempDirectory")
        var flush = ""
        var reverse = ""
        val serverPort = iperf3Parameters.serverPort
        val parallelStreams = iperf3Parameters.parallelStreams
        if (iperf3Parameters.forceFlush) flush = "--forceflush"
        if (iperf3Parameters.isReverse) reverse = "--reverse"
        var localTimeout = 3000L
        if (iperf3Parameters.timeout != 0L) localTimeout = iperf3Parameters.timeout

        Log.d(tag, "iperf3 JNI beginning test")
        updateProgress(0.toFloat())


        //var currentArgs = args.copyOf()
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

        val handler = CoroutineExceptionHandler { _, exception ->
            stderr(iperf3OutputMonitor.getCurrentLineResult(), "🚨 Uncaught Exception: ${exception.localizedMessage}")
            exception.printStackTrace()
            rc = -1
        }

        val tempDirectory: String = if (context != null) context!!.cacheDir.toString() else ""
        IperfRunner.setTempDir(tempDirectory)

        //val iperfJob = CoroutineScope(Dispatchers.IO + handler).launch {
            //val testCompleted = CompletableDeferred<Unit>()
            Log.d("IperfTestManage: ", "startTest")

            // region Start Actual iPerf Test
        var intervalCount = -1L
        var started = false
        var numberOfMessages = 0
        iperf3OutputMonitor.setParallel(parallelStreams)
        val runJob = CoroutineScope(Dispatchers.IO + handler).launch {
            //val runJob = mainScope.launch(handler) {
            isIperfRunning = true
            Log.d("IperfTestManage: ", "runJob")
            IperfRunner.runIperfLive(
                currentArgs, createIperfCallback(
                    onLine =
                        {
                            val line = it.trim().removeSuffix("\n")
                            val newLineResult = iperf3OutputMonitor.processLine(line)
                            Log.d(tag, "onLine: $line")
                            if (newLineResult.resultEntry > intervalCount && !started) {
                                started = true
                                intervalCount = newLineResult.resultEntry
                                stdout(newLineResult, false)
                            }
                            if (newLineResult.resultEntry > intervalCount || newLineResult.messages.size > numberOfMessages) {
                                if (started && newLineResult.resultEntry > intervalCount) {
                                    intervalCount = newLineResult.resultEntry
                                    stdout(newLineResult, false)
                                    val progress =
                                        intervalCount.toFloat() / iperf3Parameters.durationSecs
                                    if (progress < 1.0) {
                                        updateProgress(progress)
                                    } else {
                                        updateProgress(1.0f)
                                    }
                                }
                                if (newLineResult.messages.size > numberOfMessages) {
                                    numberOfMessages = newLineResult.messages.size
                                    stdout(newLineResult, true)
                                }

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
