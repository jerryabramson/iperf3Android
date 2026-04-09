package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel


import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import edu.bu.cs683_jabramson_project.iperf3_network_tester.Constants
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3ResultsData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.runner.iperf3Runner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val iperf3Parameters: Iperf3Parameters = Iperf3Parameters()
    private val projId: String? = savedStateHandle[Constants.PROJECT_STATE]
    private var errorLines: MutableList<String> = emptyList<String>().toMutableList()
    private val outputLines: MutableList<String> = emptyList<String>().toMutableList()
    var aLine: String = ""

    var linePointer = 0



    //var iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData(aLine, errorLines)
    private val _uiStateFlow = MutableStateFlow(iperf3Parameters)
    private val coroutineScope = viewModelScope

    val uiStateFlow: StateFlow<Iperf3Parameters> = _uiStateFlow.asStateFlow()

    init {
        Log.d("Iperf3Runner: ", "init")
//        _uiStateFlow.update {
//            it.copy(
//                iperf3Binary = File("/bin/iperf3"),
//                serverHost = "192.168.1.2",
//                durationSecs = 10,
//                results = iperf3ResultsData,
//                isReverse = true,
//                forceFlush = true,
//                timeout = 10,
//                runner = { runIperf3() })
//        }
    }
    var coroutineContext = coroutineScope.coroutineContext

    fun launch() {
        coroutineScope.launch(coroutineContext) {
            runIperf3()
        }
    }

     fun setupIperf3Parameters(iperf3Parameters: Iperf3Parameters) {
        _uiStateFlow.update { iperf3Parameters }
    }

    fun updateProgress(l: Float) {
        _uiStateFlow.update { it.copy(results = it.results.copy(progress = l)) }
    }

    fun setServerHost(host: String) {
        _uiStateFlow.update { it.copy(serverHost = host) }
    }

    fun setDuration(duration: Int) {
        _uiStateFlow.update { it.copy(durationSecs = duration) }
    }

    fun setReverse(reverse: Boolean) {
        _uiStateFlow.update { it.copy(isReverse = reverse) }
    }

    fun setForceFlush(forceFlush: Boolean) {
        _uiStateFlow.update { it.copy(forceFlush = forceFlush) }
    }

    fun setTimeout(to: Long) {
        _uiStateFlow.update { it.copy(timeout = to) }
    }

    fun getResultLine(line: String) {
        aLine = line
        Log.d("Iperf3Runner: ", "stdout: $line")
        outputLines.add(aLine)
    }

    suspend fun runIperf3() {
        coroutineScope.launch {
            iperf3Runner(
                updateProgress = ::updateProgress,
                callback = ::getResultLine,
                iperf3Parameters = _uiStateFlow.value
            )
        }
    }

    fun getLatestLine(): String {
        return outputLines[linePointer++]
    }

}





