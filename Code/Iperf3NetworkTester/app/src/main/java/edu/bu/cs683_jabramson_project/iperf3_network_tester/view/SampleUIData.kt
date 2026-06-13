package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Iperf3OutputMonitor
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.UnitConvertedData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getHeading
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getHeadingUL
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData

class SampleUIData {
    val sampleOutputData = listOf(
        "0.00-1.00   35.6 Mbits/sec Skipped",
        "1.00-2.00   35.6 Mbits/sec Skipped",
        "2.00-3.00   35.6 Mbits/sec 8 Streams ",
        "...",
        "...",
        "...",
        "...",
        "0.00-10.19   24.0 Mbits/sec sender",
        "0.00-10.19   24.0 Mbits/sec receiver",

        ).toMutableList()

    val sampleIperf3Messages = listOf(
        "🚀 Initiating iPerf3 client request...",
        "Connecting to host jabramson.com, port 5201",
        "Reverse mode, remote host 192.168.127.12 is sending",
        "Local Host/IP: 192.168.1.32",
        "Local Port: 48618",
        "Remote Host/IP: 192.168.127.12",
        "Remote Port: 5201",
        "iperf Done."
    ).toMutableList()

    val sampleStatistics = listOf(
        "Samples:       10    2 Omitted",
        "Average:      996.00 Mbits/sec",
        "Maximum:        1.09 Gbits/sec",
        "Minimum:      838.00 Mbits/sec",
        " Median:      981.00 Mbits/sec",
        "Std Dev:      364.72 Mbits/sec",
    ).toMutableList()

    val sampleLineResult = Iperf3OutputMonitor.LineResult(
        currentMax = UnitConvertedData(1.3, "Gbits/sec"),
        currentAvg = UnitConvertedData(1.18, "Gbits/sec"),
        currentMin = UnitConvertedData(525.00, "Mbits/sec"),
        currentMedian = UnitConvertedData(1.30, "Gbits/sec"),
        intervalNumber = 1,
        basicBandWidthString = "1.03 Gbits/sec"
    )

    val sampleErrorLines = listOf(
        "Error: Failed to bind to 192.168.1.1:5201: Address already in use",
    ).toMutableList()


    val sampleUiStateExample= UiData(
        hostName = "jabramson.com",
        durationSecs = "10",
        parallelStreams = "8",
        skip = "2",
        isDebugging = true,
        isVerbose = true,
        isReverse = true,
        isRunning = true,
        isFinished = false,
        iperf3Messages = sampleIperf3Messages,
        bandWidth = "1.00-2.00    35.6 Mbits/sec",
        progress = 0.33.toFloat(),
        results = sampleStatistics,
        outputLines = sampleOutputData,
        lineResult = sampleLineResult,
        errorLines = sampleErrorLines,
        latestLine = "some output"
    )
}
val sampleUiState = SampleUIData().sampleUiStateExample