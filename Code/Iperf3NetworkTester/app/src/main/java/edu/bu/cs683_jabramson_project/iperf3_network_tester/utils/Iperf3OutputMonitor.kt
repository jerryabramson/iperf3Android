package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.annotation.SuppressLint
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Units.ZERO_VALUE_STRING
import java.util.Locale
import kotlin.math.sqrt
import kotlin.text.format


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

        // interval number (1 to duration, can go backwards with Omit)
        var intervalNumber: Long = -1,
        var totalSamples: Long = -1,
        var totalOmitted: Long = 0,

        // statistics - converted to human-readable units
        var currentBandWidth : UnitConvertedData = UnitConvertedData(),
        var currentMax: UnitConvertedData = UnitConvertedData(),
        var currentMin: UnitConvertedData = UnitConvertedData(),
        var currentAvg: UnitConvertedData = UnitConvertedData(),
        var currentMedian: UnitConvertedData = UnitConvertedData(),
        var currentStandardDeviation: UnitConvertedData = UnitConvertedData(),

        // processed output from iperf3
        var basicBandWidthString: String = "",
        var debugFormattedOutputLine: String = "",
        var formattedOutputLine: String = "",
        var connectedString: String = "",
        var timeout: String = "",
        var rawAverage: String = "",
        var rawOutputLine: String = "",


        // statistics - raw numeric values
        var maxRawBitsPerSec: Double = Double.MIN_VALUE,
        var minRawBitsPerSec: Double = Double.MAX_VALUE,
        var avgRawBitsPerSec: Double = (-1).toDouble(),
        var medianRawBitsPerSec: Double = (-1).toDouble(),
        var currentRawBitsPerSec: Double = (-1).toDouble(),
        var standardDeviationRawBitsPerSec: Double = (-1).toDouble(),

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

    fun processCancelLine(line: String): LineResult {
        currentLineResult.messages.add(line)
        return currentLineResult
    }
    /** Parse one iperf3 output line and return all extracted data at once. */
    @SuppressLint("DefaultLocale")
    fun processLine(line: String): LineResult {
        val cleanLine = line.replace("\n", "")
        currentLineResult.rawOutputLine = cleanLine
        val firstLeftBracket = cleanLine.indexOf('[')
        val firstRightBracket = cleanLine.indexOf(']')
        if (firstLeftBracket in 0..<firstRightBracket) {
            val id = cleanLine.substring(firstLeftBracket + 1, firstRightBracket)
            val restOfLine = cleanLine.substring(firstRightBracket + 1).split(Regex("[ \\t]+"))
            // Connection-info line (e.g. "[ ID ] Interval...")
            if (id == "ID") {
                if (!gathered) {
                    gathered = true
                    currentLineResult.intervalNumber = 0
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
                        val localHostPort = "%18s:%04d".format(currentLineResult.localHost, currentLineResult.localPort)
                        val remoteHostPort = "%18s:%04d".format(currentLineResult.remoteHost, currentLineResult.remotePort)
                        currentLineResult.messages.add(" Local Host:port $localHostPort")
                        currentLineResult.messages.add("Remote Host:port $remoteHostPort")
                        gathered = true
                    }
                } else {
                    // Post-gathering: interval / summary data
                    if (restOfLine.size >= 7) {
                        val intervalString = restOfLine[1].trim()
                        val bitRateString = restOfLine[5] //.trim()
                        val bitRateUnitString = restOfLine[6] //.trim()
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
                            val intervalParts = intervalString.split("-")
                            var intervalLong = currentLineResult.intervalNumber
                            if (intervalParts.size == 2) {
                                val intervalDouble = intervalParts[0].toDoubleOrNull() ?: 0.0
                                intervalLong = intervalDouble.toLong()
                            }
                            currentLineResult.basicBandWidthString = "$bitRateString $bitRateUnitString"
                            val timeLabel: String
                            when (sendOrReceive.lowercase()) {
                                "(omitted)" -> {
                                    intervalLong = currentLineResult.intervalNumber + 1
                                    lastOmitted = true
                                    timeLabel = "skipped"
                                    currentLineResult.totalOmitted++
                                }

                                "sender", "receiver" -> {
                                    intervalLong = currentLineResult.intervalNumber + 1
                                    lastOmitted = false
                                    if (!finished) {
                                        finished = true
                                        summaryResults = true
                                        currentLineResult.totalSamples++
                                        currentLineResult.currentBandWidth = UnitConvertedData(bitRateValue, bitRateString)
                                        currentLineResult.rawAverage = "$bitRateString $bitRateUnitString"
                                        currentLineResult.currentAvg = UnitConvertedData(bitRateValue, bitRateUnitString)
                                    }
                                    timeLabel = String.format("%s", sendOrReceive)
                                }
                                else -> {
                                    lastOmitted = false
                                    historicalResults.add(fromHumanUnit(currentLineResult.currentBandWidth.value, currentLineResult.currentBandWidth.unit))
                                    updateRunningAverage(historicalResults)
                                    updateRunningMedian(historicalResults)
                                    updateMax(bitRateValue, bitRateUnitString)
                                    updateMin(bitRateValue, bitRateUnitString)
                                    updateRunningDeviation(historicalResults)
                                    currentLineResult.totalSamples++
                                    timeLabel =
                                        if (!isSingleThread) String.format("%2d %s", parallel, "streams")
                                        else String.format("%s", "Running")

                                }
                            }
                            currentLineResult.intervalNumber = intervalLong
                            currentLineResult.formattedOutputLine =
                                String.format("%-12.12s %4.4s %-9.9s %10.10s",
                                    intervalString,
                                    bitRateString.trim(),
                                    bitRateUnitString,
                                    timeLabel)
                            currentLineResult.debugFormattedOutputLine =
                                String.format("%-12.12s %4.4s %-9.9s %7.7s %7.7s %7.7s %10.10s",
                                    intervalString,
                                    bitRateString.trim(),
                                    bitRateUnitString,
                                    toMbs(currentLineResult.avgRawBitsPerSec),
                                    toMbs(currentLineResult.minRawBitsPerSec),
                                    toMbs(currentLineResult.maxRawBitsPerSec),
                                    timeLabel)



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
        if (historicalResults.isEmpty()) return
        var sum = 0.0
        for (i in historicalResults.indices) {
            sum += historicalResults[i]
        }
        currentLineResult.currentAvg = toHumanUnit(sum / historicalResults.size)
        currentLineResult.avgRawBitsPerSec = sum / historicalResults.size
    }

    private fun updateRunningMedian(historicalResults: List<Double>) {
        var median = 0.0
        if (historicalResults.isEmpty()) return
        val mid = historicalResults.size / 2
        if (historicalResults.size % 2 == 0) {
            median = (historicalResults[mid - 1] + historicalResults[mid]) / 2.0
        } else {
            median = historicalResults[mid]
        }
        currentLineResult.currentMedian = toHumanUnit(median)
        currentLineResult.medianRawBitsPerSec = median
    }

    fun updateRunningDeviation(historicalResults: List<Double>) {
        val mean = historicalResults.average()
        val variance = historicalResults.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        currentLineResult.currentStandardDeviation = toHumanUnit(stdDev)
        currentLineResult.standardDeviationRawBitsPerSec = stdDev
    }

    fun getCurrentLineResult() = currentLineResult
}

fun getDebugHeading(): String {
    val heading = "%-12.12s %4.4s %-9.9s %7.7s %7.7s %7.7s %10.10s".format(
        "Interval",
        "rate",
        "Unit",
        "Avg",
        "Min",
        "Max",
        "comment",

    )
    return heading
}

fun getDebugHeadingUL(): String {
    val ul = "%-12.12s %4.4s %-9.9s %7.7s %7.7s %7.7s %10.10s".format(
        "------------",
        "----",
        "---------",
        "-------",
        "-------",
        "-------",
        "----------",
        )
    return ul
}

fun getHeading(): String {
    val heading = "%-12.12s %4.4s %-9.9s %10.10s".format(
        "Interval",
        "rate",
        "Unit",
        "comment",

        )
    return heading
}

fun getHeadingUL(): String {
    val ul = "%-12.12s %4.4s %-9.9s %10.10s".format(
        "------------",
        "----",
        "---------",
        "----------",
    )
    return ul
}

fun getSampleSize(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.totalSamples > 0)
    "%7d    %d omitted".format(Locale.US, lineResult.totalSamples, lineResult.totalOmitted) else ""

fun getMaximum(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.maxRawBitsPerSec > Double.MIN_VALUE) toWholeNumber(lineResult.currentMax) else ""
fun getMinimum(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.minRawBitsPerSec < Double.MAX_VALUE) toWholeNumber(lineResult.currentMin) else ""
fun getAverage(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.avgRawBitsPerSec >= 0) toWholeNumber(lineResult.currentAvg) else ""
fun getMedian(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.medianRawBitsPerSec >= 0) toWholeNumber(lineResult.currentMedian) else ""
fun getStandardDeviation(lineResult: Iperf3OutputMonitor.LineResult): String = if (lineResult.standardDeviationRawBitsPerSec >= 0) toWholeNumber(lineResult.currentStandardDeviation) else ""
fun printLineResult(lineResult: Iperf3OutputMonitor.LineResult): String {
    val out = StringBuilder()
    out.append("LineResult\n ")
    lineResult.messages.indices.forEach {
        val m = lineResult.messages[it]
        out.append("             messages[$it] = $m\n ")
    }
    out.append("        Local Host: ${lineResult.localHost}\n")
    out.append("       Remote Host: ${lineResult.remoteHost}\n")
    out.append("        Local Port: ${lineResult.localPort}\n")
    out.append("       Remote Port: ${lineResult.remotePort}\n")
    out.append("       Last Result: ${lineResult.lastResult}\n")
    out.append("      Result Entry: ${lineResult.intervalNumber}\n")
    out.append(" Current Bandwidth: ${lineResult.currentBandWidth}\n")
    out.append("       Current Max: ${lineResult.currentMax}\n")
    out.append("       Current Min: ${lineResult.currentMin}\n")
    out.append("       Current Avg: ${lineResult.currentAvg}\n")
    out.append("   Basic Bandwidth: ${lineResult.basicBandWidthString}\n")
    out.append("    Formatted Line: ${lineResult.formattedOutputLine}\n")
    out.append("  Connected String: ${lineResult.connectedString}\n")
    out.append("           Timeout: ${lineResult.timeout}\n")
    out.append("       Raw Average: ${lineResult.rawAverage}\n")
    out.append("  Max Raw Bits/sec: ${lineResult.maxRawBitsPerSec}\n")
    out.append("  Min Raw Bits/sec: ${lineResult.minRawBitsPerSec}\n")
    out.append("  Avg Raw Bits/sec: ${lineResult.avgRawBitsPerSec}\n")
    out.append("  Cur Raw Bits/sec: ${lineResult.currentRawBitsPerSec}\n")
    return out.toString()
}
