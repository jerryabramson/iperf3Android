package edu.bu.cs683_jabramson_project.iperf3_network_tester.model

import java.io.File

data class Iperf3Parameters(
    val iperf3Binary: File,
    val serverHost: String,
    val durationSecs: Int,
    val results: Iperf3ResultsData,
    val isReverse: Boolean = false,
    val forceFlush: Boolean = true,
    val timeout: Long
)
