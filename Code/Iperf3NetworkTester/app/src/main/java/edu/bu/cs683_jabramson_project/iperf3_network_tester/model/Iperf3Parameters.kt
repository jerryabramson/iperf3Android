package edu.bu.cs683_jabramson_project.iperf3_network_tester.model

data class Iperf3Parameters(
    var serverHost: String = "",
    var clientHost: String = "",
    var serverPort: Int = 0,
    var clientPort: Int = 0,
    var durationSecs: Int = 0,
    var isReverse: Boolean = true,
    var forceFlush: Boolean = true,
    var timeout: Long = 0,
    var parallelStreams: Int = 1,
    var skip: Int = 0,
)

