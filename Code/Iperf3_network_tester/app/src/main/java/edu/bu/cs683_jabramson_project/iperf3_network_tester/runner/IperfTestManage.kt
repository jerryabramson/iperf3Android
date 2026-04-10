package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
//import com.abhishek.cellularlab.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages the lifecycle and logic of running iPerf3-based network tests.
 * Supports advanced features like smart ramp-up, hybrid tests, and automatic bandwidth reduction.
 */
class IperfTestManage(
    private val updateProgress: (Float) -> Unit,
    private val myCallback: (String) -> Unit,
    private val iperf3Parameters: Iperf3Parameters

    ) {


    private val mainScope = CoroutineScope(Dispatchers.Main)

    @Volatile
    private var isIperfRunning = false
    // endregion

    // region Public Lifecycle

    /**
     * Starts the test with the provided arguments and configuration.
     */
    suspend fun startTest() {
        var flush = ""
        var reverse = "-R"
        if (iperf3Parameters.forceFlush) flush = "--forceflush"
        if (iperf3Parameters.isReverse) reverse = "-R"
        var localTimeout = 3000L
        if (iperf3Parameters.timeout != 0L) localTimeout = iperf3Parameters.timeout
        var progress: Float = 0.toFloat()
        updateProgress(progress)


        //var currentArgs = args.copyOf()
        var currentArgs = arrayOf<String>(
            "-c", iperf3Parameters.serverHost,
            reverse,
            flush,
            "--connect-timeout",
            localTimeout.toString(),
            "-t", iperf3Parameters.durationSecs.toString()
        )
        Log.d("IperfTestManage: ", "currfentArgs: ${currentArgs.joinToString(",")}")
        val handler = CoroutineExceptionHandler { _, exception ->
            myCallback("🚨 Uncaught Exception: ${exception.localizedMessage}")
            exception.printStackTrace()
        }


        val iperfJob = CoroutineScope(Dispatchers.IO + handler).launch {
            val testCompleted = CompletableDeferred<Unit>()
            Log.d("IperfTestManage: ", "startTest")

            // region Start Actual iPerf Test
            val runJob = CoroutineScope(Dispatchers.IO + handler).launch {
                isIperfRunning = true
                Log.d("IperfTestManage: ", "runJob")
                IperfRunner.runIperfLive(
                    currentArgs, createIperfCallback(
                        onLine =
                            {
                                Log.d("IperfTestManage: ", "stdout: $it")
                                myCallback(it)
                                progress += 0.1f
                                updateProgress(progress)

                            }, onError =
                            {
                                isIperfRunning = false
                                myCallback("\n❌ Error: $it")
                                testCompleted.complete(Unit)
                            },
                        onComplete = {
                            isIperfRunning = false
                            testCompleted.complete(Unit)
                        }
                    )
                )
            }
            runJob.cancel()
            runJob.join()
        }
        iperfJob.join()

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
