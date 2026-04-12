package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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



// Data class to hold the UI state. Notice that
// all variables unchangeable `val` that are also mutable.
data class UiData(
    val iperf3Parameters: Iperf3Parameters = Iperf3Parameters(),
    val iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData(),
    val outputLines: MutableList<String> = emptyList<String>().toMutableList(),
    val errorLines: MutableList<String> = emptyList<String>().toMutableList(),
    val iperf3Messages: MutableList<String> = emptyList<String>().toMutableList(),
    var hostName: String = "",
    val latestLine: String = "",
    val minimumLine: String = "",
    val maximumLine: String = "",
    val averageLine: String = "",
    val progress: Float = 0f,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val returnCode: Int = 0,
    val lastLine: String = ""
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

    val tag = "Iperf3RunViewModel"
    private val iperf3Parameters: Iperf3Parameters = Iperf3Parameters()
    private val iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData()
    private val _uiStateFlow = MutableStateFlow(UiData(iperf3Parameters))
    val uiStateFlow: StateFlow<UiData> = _uiStateFlow.asStateFlow()


    // initialization
    init {
        Log.d(tag, "initialize")
        _uiStateFlow.update {
            it.copy(
                iperf3Parameters = iperf3Parameters,
                iperf3ResultsData = iperf3ResultsData,
                outputLines = emptyList<String>().toMutableList(),
                errorLines = emptyList<String>().toMutableList(),
                iperf3Messages = emptyList<String>().toMutableList(),
                hostName = "",
                latestLine = "",
                minimumLine = "",
                maximumLine = "",
                averageLine = "",
                progress = 0f,
                isRunning = false,
                isFinished = false,
                returnCode = 0,
                lastLine = ""
            )
        }
    }



    fun saveOutputLine(aLine: String) {
        val formattedResult: String = MonitorIPerf3Output.processLine(aLine)
        if (!formattedResult.isEmpty()) {
            Log.d(tag, "stdout: $formattedResult")
            _uiStateFlow.update {
                it.copy(
                    lastLine = it.latestLine,
                    latestLine = formattedResult,
                    outputLines = it.outputLines.also { it.add(formattedResult) },
                    iperf3Messages = MonitorIPerf3Output.getIperf3Messages().toMutableList()
                )
            }
        }
    }
    fun saveErrorLine(aLine: String) {
            Log.d(tag, "stderr: $aLine")
            _uiStateFlow.update {
                it.copy(
                    errorLines = it.errorLines.also { it.add(aLine) },
                )
            }
        }

    fun launch() {

        // Prepare the iperf3 parameters. The default hostname is 'jabramson.com'
        if (_uiStateFlow.value.hostName.isEmpty()) _uiStateFlow.value.hostName = "jabramson.com"
        _uiStateFlow.value.iperf3Parameters.serverHost = _uiStateFlow.value.hostName

        // Update the UI state to show that the test is about to run
        _uiStateFlow.update {
            it.copy(isRunning = true,
                isFinished = false,
                outputLines = it.outputLines.also { it.clear() },
                errorLines = it.errorLines.also { it.clear() },
                iperf3Messages = it.iperf3Messages.also { it.clear() }
            )
        }

        Log.d(tag, "Async Launch Started")
        viewModelScope.launch {
            // Run the iperf3 binary asynchronously.
            runIperf3()
        }
        Log.d(tag, "Async Launch Completed")

    }

    // Run the iperf3 binary.
    // Must be a suspend function called from a coroutine.
    suspend fun runIperf3(): Int {
        var rc = -1

        try {
            // Run the iperf3 binary with the provided parameters.
            MonitorIPerf3Output.resetGathered()
            rc = iperf3Runner(
                updateProgress = ::updateProgress,
                stdout = ::saveOutputLine,
                stderr = ::saveErrorLine,
                iperf3Parameters = _uiStateFlow.value.iperf3Parameters
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to run iperf3: ${e.message}", e)
            saveErrorLine("Failed to run iperf3: ${e.message}")
            rc = -1
        }

        // Update the UI state to show that the test is finished.
        // Provide the return code to the UI.
        // Clear the hostName field for the UI.
        _uiStateFlow.update {
            it.copy(
                returnCode = rc,
                isRunning = false,
                isFinished = true,
                hostName = "",
                maximumLine = MonitorIPerf3Output.getMaximumBitsBytesPerSec(),
                minimumLine = MonitorIPerf3Output.getMinimumBitsBytesPerSec(),
                averageLine = MonitorIPerf3Output.getAverageBitsBytesPerSec(),
                iperf3Messages = MonitorIPerf3Output.getIperf3Messages().toMutableList()
            )
        }

        Log.d(tag, "runIperf3 Completed, return code: $rc")
        return rc
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
            it.copy(
                hostName = host,
                iperf3Parameters = iperf3Parameters.copy(serverHost = host)
            )
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






