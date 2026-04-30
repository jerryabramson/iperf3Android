package edu.bu.cs683_jabramson_project.iperf3_network_tester.model

data class Iperf3Parameters(
    var serverHost: String = "",
    var serverPort: Int = 0,
    var durationSecs: Int = 0,
    var results: Iperf3ResultsData = Iperf3ResultsData(),
    var isReverse: Boolean = false,
    var forceFlush: Boolean = true,
    var timeout: Long = 0,
    var parallelStreams: Int = 1,
    var skip: Int = 0,
    var runner: suspend () -> Unit = { }
)

