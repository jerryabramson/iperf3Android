package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.Constants
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3ResultsData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.runner.iperf3Runner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File


/**
 * Runs the native iperf3 binary that is extracted from app assets to the app's
 * private files directory.
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


@HiltViewModel
class Iperf3RunViewModel @Inject constructor (
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val projId: String? = savedStateHandle[Constants.PROJECT_STATE]
    private var errorLines: MutableList<String> = emptyList<String>().toMutableList()
    private val outputLines: MutableList<String> = emptyList<String>().toMutableList()
    var aLine: String = ""
    var progress: Float = 0.toFloat()
    var linePointer = 0
    var iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData(aLine, errorLines)
    private val _uiStateFlow = MutableStateFlow(
        Iperf3Parameters(
            iperf3Binary = File("/bin/iperf3"),
            serverHost = "192.168.1.2",
            durationSecs = 10,
            results = iperf3ResultsData,
            isReverse = true,
            forceFlush = true,
            timeout = 10
        )
    )
    val uiStateFlow: StateFlow<Iperf3Parameters> = _uiStateFlow.asStateFlow()


    fun updateProgress(l: Float) {
        progress = l
    }

    fun getResultLine(line: String) {
        aLine = line
        Log.d("Iperf3Runner: ", "stdout: $line")
        outputLines.add(aLine)
    }

    suspend fun runIperf3() {
        iperf3Runner(updateProgress = ::updateProgress,
            callback = ::getResultLine,
            iperf3Parameters = uiStateFlow.value)
    }

    fun getLatestLine(): String {
        return outputLines[linePointer++]
    }

}





