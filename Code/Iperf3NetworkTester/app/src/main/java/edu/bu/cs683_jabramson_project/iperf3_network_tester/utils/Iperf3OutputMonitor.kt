package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.annotation.SuppressLint
import androidx.collection.emptyLongSet


/**
 * Parses iperf3 stdout lines and accumulates bandwidth statistics.
 *
 * Replaces the old static-singleton Java class: MonitorIPerf3Output with an instance-based
 * class, so the state is explicit, testable, and not shared across tests.
 */
class Iperf3OutputMonitor {

    data class LineResult(

        // iperf3 messages
        var messages: MutableList<String> = emptyList<String>().toMutableList(),

        // interval number (1 to duration)
        var resultEntry: Long = -1,

        // statistics - converted to human-readable units
        var currentBandWidth : UnitConvertedData = UnitConvertedData(),
        var currentMax: UnitConvertedData = UnitConvertedData(),
        var currentMin: UnitConvertedData = UnitConvertedData(),
        var currentAvg: UnitConvertedData = UnitConvertedData(),

        // raw output from iperf3
        var rawBandWidth: String = "",
        var rawOutputLine: String = "",
        var connectedString: String = "",
        var timeout: String = "",
        var rawAverage: String = "",


        // statistics - raw numeric values
        var maxRawBitsPerSec: Double = Double.MIN_VALUE,
        var minRawBitsPerSec: Double = Double.MAX_VALUE,
        var avgRawBitsPerSec: Double = 0.0,
        var currentRawBitsPerSec: Double = 0.0,

        // connection details
        var localHost: String = "",
        var remoteHost: String = "",
        var localPort: Long = -1L,
        var remotePort: Long = -1L,


        var lastResult: String = "",

        // -- getters for post-test summary (used after run completes) --



    )

    // -- accumulated state (private, no static fields) --
    private var currentLineResult = LineResult()
    private var gathered = false
    private var lastResult = ""
    private var summaryResults = false
    private var finished = false
    private var lastOmitted = false
    private var isSingleThread = true
    private var parallel = 1
    private var historicalResults: MutableList<Double> = emptyList<Double>().toMutableList()

    /** Reset all accumulated state for a new test run. */
   fun reset() {
       currentLineResult = LineResult()
        gathered = false
        lastResult = ""
        summaryResults = false
        finished = false
        lastOmitted = false
        isSingleThread = true
        parallel = 1
    }

    /** Configure parallel-stream mode (call before starting a test). */
    fun setParallel(parallel: Int) {
        this.parallel = parallel
        this.isSingleThread = parallel == 1
    }

    /** Parse one iperf3 output line and return all extracted data at once. */
    @SuppressLint("DefaultLocale")
    fun processLine(line: String): LineResult {
        val cleanLine = line.replace("\n", "")
        val firstLeftBracket = cleanLine.indexOf('[')
        val firstRightBracket = cleanLine.indexOf(']')
        if (firstLeftBracket in 0..<firstRightBracket) {
            val id = cleanLine.substring(firstLeftBracket + 1, firstRightBracket)
            val restOfLine = cleanLine.substring(firstRightBracket + 1).split(Regex("[ \\t]+"))
            // Connection-info line (e.g. "[ ID ] Interval...")
            if (id == "ID") {
                if (!gathered) {
                    gathered = true
                    currentLineResult.resultEntry = 0
                }
            } else {
                // Pre-gathering: extract connection details or collect messages
                if (!gathered) {
                    if (restOfLine.size == 10) {
                        currentLineResult.localHost = restOfLine[2]
                        currentLineResult.localPort = restOfLine[4].toLongOrNull() ?: -1L
                        currentLineResult.remoteHost = restOfLine[7]
                        currentLineResult.remotePort = restOfLine[9].toLongOrNull() ?: -1L
                        currentLineResult.connectedString = restOfLine[5]
                        currentLineResult.timeout = restOfLine[6]
                        currentLineResult.messages.add("    Local Host/IP: ${currentLineResult.localHost}")
                        currentLineResult.messages.add("       Local Port: ${currentLineResult.localPort}")
                        currentLineResult.messages.add("   Remote Host/IP: ${currentLineResult.remoteHost}")
                        currentLineResult.messages.add("      Remote Port: ${currentLineResult.remotePort}")
                        gathered = true
                    }
                } else {
                    // Post-gathering: interval / summary data
                    if (restOfLine.size >= 7) {
                        val intervalString = restOfLine[1].trim()
                        val bitRateString = restOfLine[5].trim()
                        val bitRateUnitString = restOfLine[6].trim()
                        var sendOrReceive = ""
                        for (i in 7..10) {
                            if (i < restOfLine.size) {
                                sendOrReceive = restOfLine[i].trim()
                            }
                        }
                        if (sendOrReceive.isNotEmpty()) {
                            val lower = sendOrReceive.lowercase()
                            if (!lower.contains("sender") && !lower.contains("receive") && !lower.contains("omit")) {
                                sendOrReceive = ""
                            }
                        }

                        // Only process SUM lines or single-thread intervals
                        if (id.contains("SUM") || isSingleThread) {
                            val bitRateValue = bitRateString.toDoubleOrNull() ?: -1.0
                            currentLineResult.currentBandWidth = UnitConvertedData(bitRateValue, bitRateUnitString)
                            val currentBandWidthString = toString(currentLineResult.currentBandWidth)
                            currentLineResult.rawBandWidth = "$bitRateString $bitRateUnitString"
                            if (sendOrReceive.isEmpty()) {
                                updateMax(bitRateValue, bitRateUnitString)
                                updateMin(bitRateValue, bitRateUnitString)
                                historicalResults.add(fromHumanUnit(currentLineResult.currentBandWidth.value, currentLineResult.currentBandWidth.unit))
                                updateRunningAverage(historicalResults)
                            }
                            val timeLabel: String
                            when (sendOrReceive.lowercase()) {
                                "(omitted)" -> {
                                    lastOmitted = true
                                    timeLabel = "skipped  "
                                }

                                "sender", "receiver" -> {
                                    lastOmitted = false
                                    if (!finished) {
                                        finished = true
                                        summaryResults = true
                                        currentLineResult.currentBandWidth = UnitConvertedData(bitRateValue, bitRateString)
                                        currentLineResult.rawAverage = "$bitRateString $bitRateUnitString"
                                        currentLineResult.currentAvg = UnitConvertedData(bitRateValue, bitRateUnitString)
                                    }
                                    timeLabel = String.format("%9.9s", sendOrReceive)
                                }

                                else -> {
                                    currentLineResult.resultEntry++
                                    lastOmitted = false
                                    timeLabel = if (!isSingleThread) String.format("%d%8s", parallel, "streams") else String.format("%10.10s", "Running")

                                }
                            }
                            currentLineResult.rawOutputLine =
                                String.format("%s %-12.12s%s %s",
                                    timeLabel,
                                    intervalString,
                                    bitRateString, bitRateUnitString)

                        }
                    }
                }
            }
        } else if (!cleanLine.startsWith("- -") && cleanLine.trim().isNotEmpty()) {
            currentLineResult.messages.add(cleanLine)
        }
        return currentLineResult
    }


    // -- private helpers --
    private fun updateMax(value: Double, unit: String) {
        val rawValue = fromHumanUnit(value, unit)
        if (rawValue > currentLineResult.maxRawBitsPerSec) {
            currentLineResult.maxRawBitsPerSec = rawValue
            currentLineResult.currentMax = toHumanUnit(rawValue)
        }
    }

    private fun updateMin(value: Double, unit: String) {
        val rawValue = fromHumanUnit(value, unit)
        if (rawValue < currentLineResult.minRawBitsPerSec) {
            currentLineResult.minRawBitsPerSec = rawValue
            currentLineResult.currentMin = toHumanUnit(rawValue)
        }
    }

    private fun updateRunningAverage(historicalResults: List<Double>) {
        var sum = 0.0
        for (i in historicalResults.indices) {
            sum += historicalResults[i]
        }
        currentLineResult.currentAvg = toHumanUnit(sum / historicalResults.size)
    }

}
fun getMaximum(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.maxRawBitsPerSec > Double.MIN_VALUE) toWholeNumber(lineResult.currentMax) else ""
fun getMinimum(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.minRawBitsPerSec < Double.MAX_VALUE) toWholeNumber(lineResult.currentMin) else ""
fun getAverage(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.avgRawBitsPerSec >= 0) toWholeNumber(lineResult.currentAvg) else ""
