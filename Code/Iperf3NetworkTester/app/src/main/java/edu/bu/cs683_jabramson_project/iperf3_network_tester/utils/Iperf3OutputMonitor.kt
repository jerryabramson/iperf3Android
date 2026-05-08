package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.annotation.SuppressLint


/**
 * Parses iperf3 stdout lines and accumulates bandwidth statistics.
 *
 * Replaces the old static-singleton Java class: MonitorIPerf3Output with an instance-based
 * class, so the state is explicit, testable, and not shared across tests.
 */
class Iperf3OutputMonitor {

    data class LineResult(
        val displayText: String = "",
        val currentBandwidth: String = "",
        val messages: List<String> = emptyList(),
        var currentMax: String = "",
        var currentMin: String = "",
        var currentAvg: String = "",
    )

    // -- accumulated state (private, no static fields) --

    private var gathered = false
    private var resultEntry = 0
    private var localHost = ""
    private var remoteHost = ""
    private var localPort = -1L
    private var remotePort = -1L
    private var connectedString = ""
    private var timeout = ""
    private var lastResult = ""
    private var currentBandwidth = ""
    private var maxBitsBytesPerSec = Double.MIN_VALUE
    private var maxUnit = ""
    private var minBitsBytesPerSec = Double.MAX_VALUE
    private var minUnit = ""
    private var avgBitsBytesPerSec = 0.0
    private var avgUnit = ""
    private val iperf3Messages = mutableListOf<String>()
    private var summaryResults = false
    private var finished = false
    private var lastOmitted = false
    private var isSingleThread = true
    private var parallel = 1

    /** Reset all accumulated state for a new test run. */
   fun reset() {
        gathered = false
        resultEntry = 0
        localHost = ""
        remoteHost = ""
        localPort = -1L
        remotePort = -1L
        connectedString = ""
        timeout = ""
        lastResult = ""
        currentBandwidth = ""
        maxBitsBytesPerSec = Double.MIN_VALUE
        maxUnit = ""
        minBitsBytesPerSec = Double.MAX_VALUE
        minUnit = ""
        avgBitsBytesPerSec = 0.0
        avgUnit = ""
        iperf3Messages.clear()
        summaryResults = false
        finished = false
        lastOmitted = false
    }

    /** Configure parallel-stream mode (call before starting a test). */
    fun setParallel(parallel: Int) {
        this.parallel = parallel
        this.isSingleThread = parallel == 1
    }

    /** Parse one iperf3 output line and return all extracted data at once. */
    @SuppressLint("DefaultLocale")
    fun processLine(line: String): LineResult {
        var max = ""
        var min = ""
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
                    resultEntry = 0
                    return LineResult()
                }
            }

            // Pre-gathering: extract connection details or collect messages
            if (!gathered) {
                if (restOfLine.size == 10) {
                    localHost = restOfLine[2]
                    localPort = restOfLine[4].toLongOrNull() ?: -1L
                    remoteHost = restOfLine[7]
                    remotePort = restOfLine[9].toLongOrNull() ?: -1L
                    connectedString = restOfLine[5]
                    timeout = restOfLine[6]
                    iperf3Messages.add("    Local Host/IP: $localHost")
                    iperf3Messages.add("   Remote Host/IP: $remoteHost")
                    iperf3Messages.add("      Remote Port: $remotePort")
                    gathered = true
                } else if (cleanLine.trim().isNotEmpty()) {
                    iperf3Messages.add(cleanLine)
                }
                return LineResult()
            }

            // Post-gathering: interval / summary data
            if (restOfLine.size >= 7) {
                val interval = restOfLine[1].trim()
                val bitRate = restOfLine[5].trim()
                val bitRateUnit = restOfLine[6].trim()

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
                    val bitRateValue = bitRate.toDoubleOrNull() ?: -1.0

                    if (sendOrReceive.isEmpty()) {
                        max = updateMax(bitRateValue, bitRateUnit)
                        min = updateMin(bitRateValue, bitRateUnit)
                    }

                    currentBandwidth = "$bitRate $bitRateUnit"
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
                                avgBitsBytesPerSec = bitRateValue
                                avgUnit = " $bitRateUnit"
                            }
                            timeLabel = String.format("%9.9s", sendOrReceive)
                        }
                        else -> {
                            resultEntry++
                            lastOmitted = false
                            timeLabel =
                                if (!isSingleThread) {
                                    String.format("%d%8s", parallel, "streams")
                                } else {
                                    String.format("%10.10s", "Running")
                                }
                        }
                    }
                    val formattedInterval = String.format("%-12.12s", interval)
                    val displayText = "$timeLabel $formattedInterval$bitRate $bitRateUnit"

                    // Collect any messages accumulated since the last call
                    val pendingMessages = iperf3Messages.toList()
                    iperf3Messages.clear()

                    return LineResult(
                        displayText = displayText,
                        currentBandwidth = currentBandwidth,
                        messages = pendingMessages,
                        currentMax = max,
                        currentMin = min,
                        currentAvg = "$avgBitsBytesPerSec $avgUnit"
                    )
                }
            }
        } else if (!cleanLine.startsWith("- -") && cleanLine.trim().isNotEmpty()) {
            iperf3Messages.add(cleanLine)
        }
        return LineResult()
    }

    // -- getters for post-test summary (used after run completes) --

    fun getMaximum(): String =
        if (maxBitsBytesPerSec > Double.MIN_VALUE) "$maxBitsBytesPerSec$maxUnit" else ""

    fun getMinimum(): String =
        if (minBitsBytesPerSec < Double.MAX_VALUE) "$minBitsBytesPerSec$minUnit" else ""

    fun getAverage(): String = "$avgBitsBytesPerSec$avgUnit"

    /** Returns accumulated messages without clearing them. */
    fun getMessages(): List<String> = iperf3Messages.toList()

    // -- private helpers --

    private fun updateMax(value: Double, unit: String): String {
        if (value > maxBitsBytesPerSec) {
            maxBitsBytesPerSec = value
            maxUnit = " $unit"
        }
        return "$maxBitsBytesPerSec$maxUnit"
    }

    private fun updateMin(value: Double, unit: String): String {
        if (value < minBitsBytesPerSec) {
            minBitsBytesPerSec = value
            minUnit = " $unit"
        }
        return "$minBitsBytesPerSec$minUnit"
    }

}
