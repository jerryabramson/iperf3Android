package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel


import android.R.attr.duration
import android.util.Log
import androidx.compose.material3.ModalNavigationDrawer
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
    var durationSecs: String = "",
    var parallelStreams: String = "",
    val isRunning: Boolean = false,
    val isDebugging: Boolean = false,
    val isFinished: Boolean = false,
    val forceFlush: Boolean = iperf3Parameters.forceFlush,
    val returnCode: Int = 0,
    val lastLine: String = "",
    val isReverse: Boolean = iperf3Parameters.isReverse,
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
                isDebugging = false,
                forceFlush = true,
                returnCode = 0,
                lastLine = "",
                durationSecs = ""
            )
        }
    }



    fun saveOutputLine(aLine: String) {
        val formattedResult: String = MonitorIPerf3Output.processLine(aLine)
        if (formattedResult.isNotEmpty() || MonitorIPerf3Output.getIperf3Messages().isNotEmpty()) {
            Log.d(tag, "stdout: $formattedResult")
            val lastMessages = MonitorIPerf3Output.getLastIperf3Messages().toMutableList()
            if (lastMessages.isNotEmpty()) Log.d(tag, "lastMessages: $lastMessages")
            _uiStateFlow.update {
                it.copy(
                    lastLine = it.latestLine,
                    latestLine = formattedResult,
                    outputLines = it.outputLines.also { it.add(formattedResult) },
                    iperf3Messages = it.iperf3Messages.also { it.addAll(lastMessages) }
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
        // 72.65.115.120
        if (_uiStateFlow.value.hostName.isEmpty()) _uiStateFlow.value.hostName = "jabramson.com"
        if (_uiStateFlow.value.parallelStreams.isEmpty()) _uiStateFlow.value.parallelStreams = "8"
        if (_uiStateFlow.value.durationSecs.isEmpty()) _uiStateFlow.value.durationSecs = "10"
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

    fun saveIperf3Message(aMessage: String) {
        _uiStateFlow.update {
            it.copy(iperf3Messages = it.iperf3Messages.also { it.add(aMessage) })
        }
    }

    // Run the iperf3 binary.
    // Must be a suspend function called from a coroutine.
    suspend fun runIperf3(): Int {
        var rc: Int
        try {
            _uiStateFlow.value.iperf3Parameters.isReverse = _uiStateFlow.value.isReverse
            _uiStateFlow.value.iperf3Parameters.forceFlush = _uiStateFlow.value.forceFlush
            _uiStateFlow.value.iperf3Parameters.parallelStreams = _uiStateFlow.value.parallelStreams.toInt()
            _uiStateFlow.value.iperf3Parameters.durationSecs = _uiStateFlow.value.durationSecs.toInt()
            _uiStateFlow.value.iperf3Parameters.timeout = _uiStateFlow.value.iperf3Parameters.timeout
            // Run the iperf3 binary with the provided parameters.
            MonitorIPerf3Output.resetGathered()
            MonitorIPerf3Output.setParallel(_uiStateFlow.value.iperf3Parameters.parallelStreams)
            MonitorIPerf3Output.setSingleThread(_uiStateFlow.value.iperf3Parameters.parallelStreams == 1)
            rc = iperf3Runner(
                updateProgress = ::updateProgress,
                stdout = ::saveOutputLine,
                stderr = ::saveErrorLine,
                iperf3Parameters = uiStateFlow.value.iperf3Parameters
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
                hostName =  if (it.hostName != "jabramson.com") it.hostName else "",
                maximumLine = MonitorIPerf3Output.getMaximumBitsBytesPerSec(),
                minimumLine = MonitorIPerf3Output.getMinimumBitsBytesPerSec(),
                averageLine = MonitorIPerf3Output.getAverageBitsBytesPerSec()
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
        var p = l
        if (_uiStateFlow.value.isReverse) { p = 1.0f - l }
        _uiStateFlow.update {
            it.copy(progress = p)
        }
    }

    fun updateHostName(host: String) {
        _uiStateFlow.update {
            it.copy(
                hostName = host,
                iperf3Parameters = iperf3Parameters.copy(serverHost = host)
            )
        }
    }

    fun setDuration(duration: String) {
        var d = 0
        if (!duration.isEmpty()) {
            try {
                d = duration.toInt()
            } catch (e: Exception) {
                return
            }
        }
        _uiStateFlow.update {
            it.copy(durationSecs = duration,
                iperf3Parameters = it.iperf3Parameters.copy(durationSecs = d))
        }
    }

    fun setParallelStreams(str: String) {
        var d = 0
        if (!str.isEmpty()) {
            try {
                d = str.toInt()
            } catch (e: Exception) {
                return
            }
        }
        _uiStateFlow.update {
            it.copy(parallelStreams = str,
                iperf3Parameters = it.iperf3Parameters.copy(parallelStreams = d))
        }
    }

    fun toggleReverse() {
        _uiStateFlow.update {
            it.copy(isReverse = !it.isReverse)
        }
    }

    fun toggleForceFlush() {
        _uiStateFlow.update {
            it.copy(forceFlush = !it.forceFlush)
        }
    }

    fun toggleDebug() {
        _uiStateFlow.update {
            it.copy(isDebugging = !it.isDebugging)
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






