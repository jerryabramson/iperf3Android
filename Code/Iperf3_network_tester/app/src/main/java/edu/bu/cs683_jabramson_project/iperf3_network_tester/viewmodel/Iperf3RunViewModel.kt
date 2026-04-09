package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.Constants
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3ResultsData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.runner.iperf3Runner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.MonitorIPerf3Output



data class UiData(
    val iperf3Parameters: Iperf3Parameters = Iperf3Parameters(),
    val outputLines: MutableList<String> = emptyList<String>().toMutableList(),
    val errorLines: MutableList<String> = emptyList<String>().toMutableList(),
    var latestLine: String = "",
    val errors: MutableList<String> = emptyList<String>().toMutableList(),
    val progress: Float = 0f,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val returnCode: Int = 0,
    val lastLine: String = "",
    val iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData(),
    var hostName: String = ""

)

/**
 * View model to Runs the native iperf3 binary extracted from
 * app assets to the app's private files' directory.
 *
 * @param savedStateHandle The SavedStateHandle instance for this ViewModel.
 */

@HiltViewModel
class Iperf3RunViewModel @Inject constructor (
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    //private val projId: String? = savedStateHandle[Constants.PROJECT_STATE]
    val iperf3Parameters: Iperf3Parameters = Iperf3Parameters()
    private val _uiStateFlow = MutableStateFlow(UiData(iperf3Parameters))
    val uiStateFlow: StateFlow<UiData> = _uiStateFlow.asStateFlow()

    //private val iperf3ResultsData = Iperf3ResultsData()

    init {
        Log.d("Iperf3Runner: ", "init")
    }



    fun saveOutputLine(aLine: String) {
        val formattedResult: String = MonitorIPerf3Output.processLine(aLine)
        if (!formattedResult.isEmpty()) {
            Log.d("formattedOutput: ", "stdout: $formattedResult")
            _uiStateFlow.update {
                it.copy(
                    lastLine = it.latestLine,
                    latestLine = formattedResult,
                    outputLines = it.outputLines.also { it.add(formattedResult) },
                )
            }
        }
    }

    fun launch() {
        if (_uiStateFlow.value.hostName.isEmpty()) _uiStateFlow.value.hostName = "jabramson.com"
        val coroutineScope = viewModelScope
        _uiStateFlow.value.iperf3Parameters.serverHost = _uiStateFlow.value.hostName
        val coroutineContext = coroutineScope.coroutineContext
        coroutineScope.launch(coroutineContext) {
            runIperf3()
        }
    }

    suspend fun runIperf3() {
        _uiStateFlow.update {
            it.copy(isRunning = true,
                isFinished = false,
                outputLines = it.outputLines.also { it.clear() })
        }

        var rc = iperf3Runner(
            updateProgress = ::updateProgress,
            callback = ::saveOutputLine,
            iperf3Parameters = _uiStateFlow.value.iperf3Parameters
        )
        _uiStateFlow.update {
            it.copy(returnCode = rc,
                isRunning = false,
                isFinished = true,
                hostName = ""
                //outputLines = it.outputLines.also { it.clear() }
            )
        }

    }

    fun setupIperf3Parameters(ip: Iperf3Parameters) {
        _uiStateFlow.update {
            it.copy(
                iperf3Parameters = it.iperf3Parameters.copy(
                    iperf3Binary = ip.iperf3Binary,
                    serverHost = ip.serverHost,
                    durationSecs = ip.durationSecs,
                    isReverse = ip.isReverse,
                    forceFlush = ip.forceFlush,
                    timeout = ip.timeout
                )
            )
        }
    }

    fun updateProgress(l: Float) {
        _uiStateFlow.update { it.copy(progress = l) }
    }

    fun updateHostName(host: String) {
        _uiStateFlow.update {
            it.copy(hostName = host )// iperf3Parameters = it.iperf3Parameters.copy(serverHost = host))
        }
    }

    fun setDuration(duration: Int) {
        _uiStateFlow.update {
            it.copy(iperf3Parameters = it.iperf3Parameters.copy(durationSecs = duration))
        }
    }

    fun setReverse(reverse: Boolean) {
        _uiStateFlow.update {
            it.copy(iperf3Parameters = it.iperf3Parameters.copy(isReverse = reverse))
        }
    }

    fun setForceFlush(forceFlush: Boolean) {
        _uiStateFlow.update {
            it.copy(iperf3Parameters = it.iperf3Parameters.copy(forceFlush = forceFlush))
        }
    }

    fun setTimeout(to: Long) {
        _uiStateFlow.update {
            it.copy(iperf3Parameters = it.iperf3Parameters.copy(timeout = to))
        }
    }
}






