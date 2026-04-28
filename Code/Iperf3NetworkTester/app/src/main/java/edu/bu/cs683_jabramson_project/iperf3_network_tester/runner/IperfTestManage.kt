package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.animation.core.updateTransition
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages the lifecycle and logic of running iPerf3-based network tests.
 * Supports advanced features like smart ramp-up, hybrid tests, and automatic bandwidth reduction.
 */
class IperfTestManage(
    val tag: String = "IperfTestManage",
    //private val context: Context,
    var updateProgress: (Float) -> Unit,
    var stdout: (String) -> Unit,
    var stderr: (String) -> Unit,
    var iperf3Parameters: Iperf3Parameters,
    private val onTestComplete: () -> Unit
    ) {

    private val mainScope = CoroutineScope(Dispatchers.Main)
    @Volatile
    private var isIperfRunning = false
    // endregion

    // region Public Lifecycle

    /**
     * Starts the test with the provided arguments and configuration.
     */
    suspend fun startTest(): Int {
        Log.d(tag, "iperf3 JNI Bridge")

        updateProgress(0.0f)
        if (iperf3Parameters.durationSecs <= 0) {
            Log.e("Iperf3Runner", "Invalid duration: ${iperf3Parameters.durationSecs}")
            stderr("Invalid duration: ${iperf3Parameters.durationSecs}")
            updateProgress(1.0f)
            return -1 // @withContext -1
        }
        //val command = iperf3Parameters.iperf3Binary.absolutePath
        var flush = ""
        var reverse = ""
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
            reverse,
            flush,
            "--parallel", parallelStreams.toString(),
            "--connect-timeout",
            localTimeout.toString(),
            "--time", iperf3Parameters.durationSecs.toString(),
            "--omit", iperf3Parameters.skip.toString()
        )


        Log.d(tag, "currentArgs: ${currentArgs.joinToString(",")}")

        val handler = CoroutineExceptionHandler { _, exception ->
            stderr("🚨 Uncaught Exception: ${exception.localizedMessage}")
            exception.printStackTrace()
        }


        //val iperfJob = CoroutineScope(Dispatchers.IO + handler).launch {
            //val testCompleted = CompletableDeferred<Unit>()
            Log.d("IperfTestManage: ", "startTest")

            // region Start Actual iPerf Test
            var intervalCount = 0
            var started = false
        val runJob = CoroutineScope(Dispatchers.IO + handler).launch {
            //val runJob = mainScope.launch(handler) {
            isIperfRunning = true
            Log.d("IperfTestManage: ", "runJob")
            IperfRunner.runIperfLive(
                currentArgs, createIperfCallback(
                    onLine =
                        {
                            val line = it.trim().removeSuffix("\n")
                            Log.d(tag, "stdout: $line")
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
                            val progress = intervalCount.toFloat() / iperf3Parameters.durationSecs
                            if (progress < 1.0) {
                                updateProgress(progress)
                            } else {
                                updateProgress(1.0f)
                            }
                        }, onError =
                        {
                            val err = it.trim().removeSuffix("\n")
                            isIperfRunning = false
                            stderr("\n❌ Error: $err")
                            onTestComplete()
                            //testCompleted.complete(Unit)
                            updateProgress(1.0f)
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
        return -1//@withContext -1
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
