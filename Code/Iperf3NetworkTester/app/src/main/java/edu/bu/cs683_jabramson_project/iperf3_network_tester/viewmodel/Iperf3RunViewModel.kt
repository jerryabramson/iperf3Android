package edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel

import android.content.Context
import android.provider.SyncStateContract.Helpers.update
import android.util.Log
import android.util.Log.e
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3ResultsData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.runner.IperfTestManage
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Iperf3OutputMonitor
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getAverage

import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getMaximum
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getMedian
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getMinimum
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getSampleSize
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getStandardDeviation
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.printLineResult


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale.getDefault
import javax.inject.Inject



object DefaultUIValues {
    const val HOST_NAME = "jabramson.com"
    const val PORT_NUMBER = 5201
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
    val portNumber: Int = 0,
    val latestLine: String = "",
    val bandWidth: String = "",
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
    //val iperf3OutputMonitor: Iperf3OutputMonitor = Iperf3OutputMonitor(),
    val resultNumber: Long = -1,
    val numberOfMessages: Int = 0,
    val lineResult: Iperf3OutputMonitor.LineResult = Iperf3OutputMonitor.LineResult(),
    val context: Context? = null
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

    val tag = "Iperf3RunViewModel"



    private val iperf3Parameters: Iperf3Parameters = Iperf3Parameters()
    private val iperf3ResultsData: Iperf3ResultsData = Iperf3ResultsData()
    private val _uiStateFlow = MutableStateFlow(UiData(iperf3Parameters))
    val uiStateFlow: StateFlow<UiData> = _uiStateFlow.asStateFlow()

    private var iperfManager: IperfTestManage = IperfTestManage(
        updateProgress = ::updateProgress,                       // floating point track of progress
        stdout = ::saveOutputLine,                               // output from iperf3
        stderr = ::saveErrorLine,                                // errors from iperf3
        onTestComplete = { completeTest() }
    )


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
                portNumber = 0,
                latestLine = "",
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
                parallelStreams = "",
                resultNumber = -1,
                numberOfMessages = 0,
                lineResult = iperfManager.getCurrentLineResult(),
                context = savedStateHandle.get<Context>("context")
            )
        }
    }



    /**
     * Callback to save an output line from the iperf3 binary.
     * @param lineResult The output line from the process execution.
     */
    fun saveOutputLine(lineResult: Iperf3OutputMonitor.LineResult, newMessage: Boolean = false) {
        val lineResultStr = printLineResult(lineResult)
        Log.d(tag, "viewModel: saveOutputLine() -> $lineResultStr")
        val lastMessages = lineResult.messages.toMutableList()

        if (newMessage) {
            lastMessages.forEach { Log.d(tag, "lastMessages: $it") }
            _uiStateFlow.update {
                it.copy(
                    iperf3Messages = lineResult.messages.toMutableList(),
                    numberOfMessages = lastMessages.size,
                    lineResult =  lineResult
                )
            }
        } else {
            _uiStateFlow.update {
                it.copy(
                    lastLine = it.latestLine,
                    bandWidth = lineResult.basicBandWidthString,
                    latestLine = lineResult.formattedOutputLine,
                    outputLines = it.outputLines.also {
                        if (lineResult.formattedOutputLine.isNotEmpty()) {
                            it.add(lineResult.formattedOutputLine)
                        }
                    },
                    resultNumber = lineResult.intervalNumber,
                    iperf3Messages = it.iperf3Messages.toMutableList(),
                    lineResult =  lineResult
                )
            }

        }
    }

    /**
     * Callback to save an error line from the iperf3 binary.
     * @param aLine The error line from the process execution.
     */
    fun saveErrorLine(lineResult: Iperf3OutputMonitor.LineResult, aLine: String = "") {
        Log.d(tag, "stderr: $aLine")
        _uiStateFlow.update { data ->
            data.copy(
                errorLines = data.errorLines.also { it.add(aLine) },
                lineResult = lineResult
            )
        }
    }

    fun launchOrCancel() {
        if (!_uiStateFlow.value.isRunning) launch() else cancel()
    }

    /**
     * Launch the iperf3 binary (notice that this is an asynchronous operation).
     */
    fun launch() {
        var tempHostName = _uiStateFlow.value.hostName
        if (tempHostName.isEmpty()) tempHostName = DefaultUIValues.HOST_NAME
        var tempPortNumber = _uiStateFlow.value.portNumber
        if (tempHostName.contains(":")) {
            val parts = tempHostName.split(":")
            if (parts.size == 2) {
                tempPortNumber = myInt(parts[1])
                tempHostName = parts[0]
            }
        }
        if (tempPortNumber == 0) tempPortNumber = DefaultUIValues.PORT_NUMBER
        _uiStateFlow.value.iperf3Parameters.serverHost = tempHostName
        _uiStateFlow.value.iperf3Parameters.serverPort = tempPortNumber

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
                progress = 0f,
                lastLine = "",
                // Ensure the user can see selections during the run
                hostName =  "${tempHostName}:${tempPortNumber}",
                portNumber = tempPortNumber,
                skip =  it.skip.ifEmpty { DefaultUIValues.SKIP },
                durationSecs = it.durationSecs.ifEmpty { DefaultUIValues.DURATION },
                parallelStreams = it.parallelStreams.ifEmpty { DefaultUIValues.PARALLEL_STREAMS },
                resultNumber = -1,
                returnCode =  0
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


    fun cancel() {
        Log.d(tag, "Async cancel Started")
        // Update the UI active running state with empty values for the start of the test.
        _uiStateFlow.update {
            it.copy(
                returnCode = -1,
                isRunning = false,
                isFinished = true,
                // Only remember last choices for non-default user selections
                hostName =  if (it.hostName != DefaultUIValues.HOST_NAME) it.hostName else "",
                skip =  if (it.skip != DefaultUIValues.SKIP) it.skip else "",
                durationSecs = if (it.durationSecs != DefaultUIValues.DURATION) it.durationSecs else "",
                parallelStreams = if (it.parallelStreams != DefaultUIValues.PARALLEL_STREAMS) it.parallelStreams else ""
            )
        }
        Log.d(tag, "Async Cancel Started")
        viewModelScope.launch {cancelTest() }
        Log.d(tag, "Async Cancel Completed")
    }

    suspend fun cancelTest(): Int {
        var rc: Int
        try {
            rc = iperfManager.cancelTest()
        } catch (e: Exception) {
            /* Shouldn't ever get here, since guards are already in place */
            e(tag, "Failed to cancel iperf3: ${e.message}", e)
            saveErrorLine(_uiStateFlow.value.lineResult, "Failed to cancel iperf3: ${e.message}")
            rc = -1
        }
        return rc
    }

    /**
     * Run the iperf3 binary.
     * This must be a suspend function called from a coroutine.
     * @return The return code from the iperf3 binary.
     */
    suspend fun runIperf3(): Int {
        var rc: Int
        try {
            // Prepare the UI state for the test.

            // Refresh model state with provided UI choices - booleans
            _uiStateFlow.value.iperf3Parameters.isReverse = _uiStateFlow.value.isReverse
            _uiStateFlow.value.iperf3Parameters.forceFlush = _uiStateFlow.value.forceFlush

            // Numerics
            _uiStateFlow.value.iperf3Parameters.parallelStreams =
                myInt(_uiStateFlow.value.parallelStreams.ifEmpty { DefaultUIValues.PARALLEL_STREAMS })
            _uiStateFlow.value.iperf3Parameters.durationSecs =
                myInt(uiStateFlow.value.durationSecs.ifEmpty { DefaultUIValues.DURATION })
            _uiStateFlow.value.iperf3Parameters.skip =
                myInt(_uiStateFlow.value.skip.ifEmpty { DefaultUIValues.SKIP })

            /**
             * Prepare to launch the iperf3 library as a suspended function.
             */
            updateProgress(0f)
            Log.d(tag, "sync iperfManager.startTest() starts")
            rc = iperfManager.startTest(
                _uiStateFlow.value.context,
                _uiStateFlow.value.iperf3Parameters
            )
            Log.d(tag, "sync iperfManager.startTest() ends")

            _uiStateFlow.update {
                it.copy(
                    lineResult = iperfManager.getCurrentLineResult(),
                    resultNumber = -1
                )
            }
        } catch (e: Exception) {
            /* Shouldn't ever get here, since guards are already in place */
            e(tag, "Failed to run iperf3: ${e.message}", e)
            saveErrorLine(_uiStateFlow.value.lineResult, "Failed to run iperf3: ${e.message}")
            rc = -1
        }


        //Update the UI state to show that the test is finished and
        // Provide the return code to the UI.
        if (rc != 0) {
            // Only need this on failure conditions
            _uiStateFlow.update { data -> data.copy(results = data.results.also { it.add("Error: Return Code = $rc") }) }
        }
        val outputCount = _uiStateFlow.value.lineResult.intervalNumber
        if (outputCount > 0) {
            val exe = getSampleSize(_uiStateFlow.value.lineResult)
            val max = getMaximum(_uiStateFlow.value.lineResult)
            val min = getMinimum(_uiStateFlow.value.lineResult)
            val avg = getAverage(_uiStateFlow.value.lineResult)
            val med = getMedian(_uiStateFlow.value.lineResult)
            val std = getStandardDeviation(_uiStateFlow.value.lineResult)
            if (!exe.isEmpty()) _uiStateFlow.update { it.copy(results = it.results.also { it.add("Samples: $exe") }) }
            if (!avg.isEmpty()) _uiStateFlow.update { it.copy(results = it.results.also { it.add("Average: $avg") }) }
            if (!max.isEmpty()) _uiStateFlow.update { it.copy(results = it.results.also { it.add("Maximum: $max") }) }
            if (!min.isEmpty()) _uiStateFlow.update { it.copy(results = it.results.also { it.add("Minimum: $min") }) }
            if (!med.isEmpty()) _uiStateFlow.update { it.copy(results = it.results.also { it.add(" Median: $med") }) }
            if (!std.isEmpty()) _uiStateFlow.update { it.copy(results = it.results.also { it.add("Std Dev: $std") }) }
        } else {
            _uiStateFlow.update { it.copy(results = it.results.also { it.add("No Results") }) }
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

    fun completeTest() {
        var tempHostName = _uiStateFlow.value.hostName
        var tempPortNumber = _uiStateFlow.value.portNumber
        if (tempHostName.isEmpty()) tempHostName = DefaultUIValues.HOST_NAME
         if (tempHostName.contains(":")) {
            val parts = tempHostName.split(":")
            if (parts.size == 2) {
                tempHostName = parts[0]
                tempPortNumber = myInt(parts[1])
            }
        }
        if (tempPortNumber != DefaultUIValues.PORT_NUMBER) tempHostName = _uiStateFlow.value.hostName
        _uiStateFlow.update {
            it.copy(hostName = tempHostName)
        }
    }

    /**
     * Callback to update the progress bar.
     * @param newProgress The new progress value.
     * We implement the progress bar as a floating point value between 0.0 and 1.0.
     * If uploading, the progress bar goes from 0.0 to 1.0 [left to right]
     * If downloading, the progress bar goes from 1.0 to 0.0 [right to left]
     */
    fun updateProgress(newProgress: Float) {
        val normalizedProgress = if (!_uiStateFlow.value.isReverse) newProgress else 1.0f - newProgress
        _uiStateFlow.update {
            it.copy(progress = normalizedProgress)
        }
    }

    /**
     * User entered a new host name.
     * @param host The new host name.
     */
    fun updateHostName(host: String) {
        var tempPortNumber = _uiStateFlow.value.portNumber
        if (host.contains(":")) {
            val parts = host.split(":")
            if (parts.size == 2) {
                tempPortNumber = myInt(parts[1])
            }
        }
        if (tempPortNumber == 0) {
            tempPortNumber = DefaultUIValues.PORT_NUMBER
        }

        _uiStateFlow.update {
            it.copy(
                hostName = host,
                portNumber = tempPortNumber,
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
     * User entered a new upload/download.
     * @param str The new value for upload/download.
     */
    fun setDownload(reverse: Boolean) {
        _uiStateFlow.update {
            it.copy(isReverse = reverse)
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

    fun setIperf3Parameters(iperf3Parameters: Iperf3Parameters) {
        _uiStateFlow.update {
            it.copy(iperf3Parameters = iperf3Parameters)
        }
    }

    fun setContext(context: Context) {
        _uiStateFlow.update {
            it.copy(context = context)
        }
    }

    fun toggleDebug()  {
        var newState = !_uiStateFlow.value.isDebugging
        _uiStateFlow.update {
            it.copy(
                isDebugging = newState,
                isVerbose = newState
            )
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






