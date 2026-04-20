package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3ResultsData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.runner.iperf3Runner
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.MonitorIPerf3Output
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale.getDefault
import javax.inject.Inject


object DefaultUIValues {
    const val HOST_NAME = "jabramson.com"
    const val PARALLEL_STREAMS = "8"
    const val DURATION = "10"
    const val SKIP = "2"
}

/**
 * Data class to hold the UI state.
 * Notice that all variables are unchangeable `val` that are also mutable.
 * I also decided to store all numeric values as strings to avoid NumberFormatException
 * issues at odd times in the UI.
 */
data class UiData(
    val iperf3Parameters: Iperf3Parameters = Iperf3Parameters(),
    val iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData(),
    val outputLines: MutableList<String> = emptyList<String>().toMutableList(),
    val errorLines: MutableList<String> = emptyList<String>().toMutableList(),
    val iperf3Messages: MutableList<String> = emptyList<String>().toMutableList(),
    val results: MutableList<String> = emptyList<String>().toMutableList(),
    val hostName: String = "",
    val latestLine: String = "",
    val bandWidth: String = "",
    val minimum: String = "",
    val maximum: String = "",
    val average: String = "",
    val progress: Float = 0f,
    val durationSecs: String = "",
    val parallelStreams: String = "",
    val skip: String = "",
    val isRunning: Boolean = false,
    val isDebugging: Boolean = false,
    val isVerbose: Boolean = false,
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

    var iperf3Binary: File = File("")
    val tag = "Iperf3RunViewModel"

    private val iperf3Parameters: Iperf3Parameters = Iperf3Parameters()
    private val iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData()
    private val _uiStateFlow = MutableStateFlow(UiData(iperf3Parameters))
    val uiStateFlow: StateFlow<UiData> = _uiStateFlow.asStateFlow()

    /**
     * Set the iperf3 executable. Should be the first thing called
     * by user interface
     */
    fun setupIperf3Executable(iperf3Binary: File?) {
        if (iperf3Binary != null) {
            this.iperf3Binary = iperf3Binary
            uiStateFlow.value.iperf3Parameters.iperf3Binary = iperf3Binary
        } else {
            Log.e(tag, "iperf3Binary is null")
        }
    }

    /**
     * Initialize the UI state.
     */
    init {
        Log.d(tag, "initialize UI state")
        _uiStateFlow.update {
            it.copy(
                iperf3Parameters = iperf3Parameters,
                iperf3ResultsData = iperf3ResultsData,
                outputLines = emptyList<String>().toMutableList(),
                errorLines = emptyList<String>().toMutableList(),
                iperf3Messages = emptyList<String>().toMutableList(),
                results = emptyList<String>().toMutableList(),
                hostName = "",
                latestLine = "",
                minimum = "",
                maximum = "",
                average = "",
                progress = 0f,
                isRunning = false,
                isFinished = false,
                isDebugging = false,
                isVerbose = false,
                forceFlush = true,
                returnCode = 0,
                lastLine = "",
                bandWidth = "",
                durationSecs = "",
                skip =  "",
                parallelStreams = ""
            )
        }
    }


    /**
     * Callback to save an output line from the iperf3 binary.
     * @param aLine The output line from the process execution.
     */
    fun saveOutputLine(aLine: String) {
        val formattedResult: String = MonitorIPerf3Output.processLine(aLine)
        if (formattedResult.isNotEmpty() || MonitorIPerf3Output.getIperf3Messages().isNotEmpty()) {
            Log.d(tag, "stdout: $formattedResult")
            val lastMessages = MonitorIPerf3Output.getLastIperf3Messages().toMutableList()
            if (lastMessages.isNotEmpty()) Log.d(tag, "lastMessages: $lastMessages")
            _uiStateFlow.update {
                it.copy(
                    lastLine = it.latestLine,
                    bandWidth = MonitorIPerf3Output.getCurrentBandwidth(),
                    latestLine = formattedResult,
                    average = MonitorIPerf3Output.getAverageBitsBytesPerSec(),
                    minimum = MonitorIPerf3Output.getMinimumBitsBytesPerSec(),
                    maximum = MonitorIPerf3Output.getMaximumBitsBytesPerSec(),
                    outputLines = it.outputLines.also { if (formattedResult.isNotEmpty()) it.add(formattedResult) },
                    iperf3Messages = it.iperf3Messages.also { it.addAll(lastMessages) }
                )
            }
        }
    }

    /**
     * Callback to save an error line from the iperf3 binary.
     * @param aLine The error line from the process execution.
     */
    fun saveErrorLine(aLine: String) {
        Log.d(tag, "stderr: $aLine")
        _uiStateFlow.update {
            it.copy(
                errorLines = it.errorLines.also { it.add(aLine) },
            )
        }
    }

    /**
     * Launch the iperf3 binary (notice that this is an asynchronous operation).
     */
    fun launch() {
        // Update the UI active running state with empty values for the start of the test.
        _uiStateFlow.update {
            it.copy(isRunning = true,
                isFinished = false,
                outputLines = it.outputLines.also { it.clear() },
                errorLines = it.errorLines.also { it.clear() },
                iperf3Messages = it.iperf3Messages.also { it.clear() },
                results =  it.results.also { it.clear() },
                bandWidth = "",
                latestLine = "",
                minimum = "",
                maximum = "",
                average = "",
                progress = 0f,
                lastLine = "",
                // Ensure the user can see selections during the run
                hostName =  it.hostName.ifEmpty() { DefaultUIValues.HOST_NAME },
                skip =  it.skip.ifEmpty { DefaultUIValues.SKIP },
                durationSecs = it.durationSecs.ifEmpty { DefaultUIValues.DURATION },
                parallelStreams = it.parallelStreams.ifEmpty { DefaultUIValues.PARALLEL_STREAMS }
            )
        }

        /*
         * Launch the iperf3 binary asynchronously.
         * I decided to wrap the async launch in a separate function
         * to make the code more explicit.
         */
        Log.d(tag, "Async Launch Started")
        viewModelScope.launch {runIperf3() }
        Log.d(tag, "Async Launch Completed")
    }


    /**
     * I had some issues with `NumberFormatException` at odd times during
     * testing, so I wrapped this in a try/catch.
     * @param s The string to convert to an integer.
     * @return The integer value of the string, or 0 if invalid.
     */
    private fun myInt(s: String): Int = try { s.toInt() } catch (ex: Exception) { 0 }


    /**
     * Run the iperf3 binary.
     * This must be a suspend function called from a coroutine.
     * @return The return code from the iperf3 binary.
     */
    suspend fun runIperf3(): Int {
        var rc: Int
        try {
            // Prepare the UI state for the test.

            // Make sure we always have this available!
            _uiStateFlow.value.iperf3Parameters.iperf3Binary = this.iperf3Binary

            // Refresh model state with provided UI choices - booleans
            _uiStateFlow.value.iperf3Parameters.isReverse = _uiStateFlow.value.isReverse
            _uiStateFlow.value.iperf3Parameters.forceFlush = _uiStateFlow.value.forceFlush
            _uiStateFlow.value.iperf3Parameters.serverHost = _uiStateFlow.value.hostName.ifEmpty { DefaultUIValues.HOST_NAME }

            // Numerics
            _uiStateFlow.value.iperf3Parameters.parallelStreams = myInt(_uiStateFlow.value.parallelStreams.ifEmpty { DefaultUIValues.PARALLEL_STREAMS })
            _uiStateFlow.value.iperf3Parameters.durationSecs = myInt(uiStateFlow.value.durationSecs.ifEmpty { DefaultUIValues.DURATION })
            _uiStateFlow.value.iperf3Parameters.skip = myInt(_uiStateFlow.value.skip.ifEmpty { DefaultUIValues.SKIP })

            // Temp: Setup output parsing (old Java code)
            MonitorIPerf3Output.resetGathered()
            MonitorIPerf3Output.setParallel(_uiStateFlow.value.iperf3Parameters.parallelStreams)
            MonitorIPerf3Output.setSingleThread(_uiStateFlow.value.iperf3Parameters.parallelStreams == 1)


            // Run the iperf3 binary with the provided parameters.
            rc = iperf3Runner(
                updateProgress = ::updateProgress,                     // floating point track of progress
                stdout = ::saveOutputLine,                             // output from iperf3
                stderr = ::saveErrorLine,                              // errors from iperf3
                iperf3Parameters = _uiStateFlow.value.iperf3Parameters  // parameters for iperf3
            )
        } catch (e: Exception) {
            /* Shouldn't ever get here, since guards are already in place */
            Log.e(tag, "Failed to run iperf3: ${e.message}", e)
            saveErrorLine("Failed to run iperf3: ${e.message}")
            rc = -1
        }

//        // Temporary hack until I understand the issue with numeric values being lost
//        _uiStateFlow.update {
//            it.copy(
//                parallelStreams = it.iperf3Parameters.parallelStreams.toString(),
//                durationSecs = it.iperf3Parameters.durationSecs.toString(),
//                skip = it.iperf3Parameters.skip.toString()
//            )
//        }

        // Update the UI state to show that the test is finished.
        // Provide the return code to the UI.
        if (rc == 0) {
            // only update statistics on a successful run
            _uiStateFlow.update {it.copy(results = it.results.also { it.add("Average: ${MonitorIPerf3Output.getAverageBitsBytesPerSec()}")})}
            _uiStateFlow.update {it.copy(results = it.results.also { it.add("Maximum: ${MonitorIPerf3Output.getMaximumBitsBytesPerSec()}")})}
            _uiStateFlow.update {it.copy(results = it.results.also { it.add("Minimum: ${MonitorIPerf3Output.getMinimumBitsBytesPerSec()}")})}
        }else {
            // Only need this on failure conditions
            _uiStateFlow.update {it.copy(results = it.results.also { it.add("Return Code: $rc") })}
        }

        // Provide the return code to the UI.
        // Clear the hostName field for the UI.
        _uiStateFlow.update {
            it.copy(
                returnCode = rc,
                isRunning = false,
                isFinished = true,

                // Only remember last choices for non-default user selections
                hostName =  if (it.hostName != DefaultUIValues.HOST_NAME) it.hostName else "",
                skip =  if (it.skip != DefaultUIValues.SKIP) it.skip else "",
                durationSecs = if (it.durationSecs != DefaultUIValues.DURATION) it.durationSecs else "",
                parallelStreams = if (it.parallelStreams != DefaultUIValues.PARALLEL_STREAMS) it.parallelStreams else ""
            )
        }

        Log.d(tag, "runIperf3 Completed, return code: $rc")
        return rc
    }

    /**
     * Callback to update the progress bar.
     * @param l The new progress value.
     */
    fun updateProgress(l: Float) {
        var p = l
        if (_uiStateFlow.value.isReverse) { p = 1.0f - l }
        _uiStateFlow.update {
            it.copy(progress = p)
        }
    }

    /**
     * User entered a new host name.
     * @param host The new host name.
     */
    fun updateHostName(host: String) {
        _uiStateFlow.update {
            it.copy(
                hostName = host,
                iperf3Parameters = iperf3Parameters.copy(serverHost = host)
            )
        }
    }

    /**
     * User entered a new duration.
     * Notice that we do not allow changes to the user interface
     * if the resulting number is invalid.
     * @param duration The new value for duration.
     */
    fun setDuration(duration: String) {
        var d = 0
        var newDuration = _uiStateFlow.value.durationSecs // default to existing value
        if (!duration.isEmpty()) {
            try {
                d = duration.toInt()
                newDuration = "$d" // valid change
            } catch (e: Exception) { /**/ }
        } else {
            newDuration = ""
        }
        _uiStateFlow.update {
            it.copy(durationSecs = newDuration,
                iperf3Parameters = it.iperf3Parameters.copy(durationSecs = d))
        }
    }

    /**
     * User entered a new omitted.
     * Notice that we do not allow changes to the user interface
     * if the resulting number is invalid.
     * @param skip The new value for omitted.
     */
    fun setSkip(skip: String) {
        var d = 0
        var newSkip = _uiStateFlow.value.skip // default to existing value
        if (!skip.isEmpty()) {
            try {
                d = skip.toInt()
                newSkip = "$d"
            } catch (e: Exception) { /**/ }
        } else {
            newSkip = ""
        }
        _uiStateFlow.update {
            it.copy(skip = newSkip,
                iperf3Parameters = it.iperf3Parameters.copy(skip = d))
        }
    }

    /**
     * User entered a new value for parallel streams.
     * Notice that we do not allow changes to the user interface
     * if the resulting number is invalid.
     * @param str The new value for parallel streams.
     */
    fun setParallelStreams(str: String) {
        var d = 0
        // default to existing value
        var newStreams = _uiStateFlow.value.parallelStreams
        if (!str.isEmpty()) {
            try {
                d = str.toInt()
                newStreams = "$d" // valid change
            } catch (e: Exception) { /**/    }
        } else {
            newStreams = ""
        }
        _uiStateFlow.update {
            it.copy(parallelStreams = newStreams,
                iperf3Parameters = it.iperf3Parameters.copy(parallelStreams = d))
        }
    }

    /**
     * User entered a new upload/download.
     * @param str The new value for upload/download.
     */
    fun setUploadDownload(str: String) {
        _uiStateFlow.update {
            it.copy(isReverse = str.lowercase(getDefault()) == "download")
        }
    }


    /**
     * User entered a new force flush.
     * @param forceFlush The new value for forceflush.
     */
    fun setForceFlush(forceFlush: Boolean) {
        _uiStateFlow.update {
            it.copy(iperf3Parameters = it.iperf3Parameters.copy(forceFlush = forceFlush))
        }
    }


    /**
     * User entered a new trace level.
     * @param traceLevel The new value for trace level.
     */
    fun setDebug(traceLevel: String) {
        _uiStateFlow.update {
            it.copy(
                isDebugging = traceLevel.lowercase() == "trace",
                isVerbose = traceLevel.lowercase() == "verbose"
            )
        }
    }


}






