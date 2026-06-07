package edu.bu.cs683_jabramson_project.iperf3_network_tester.model

data class Iperf3ResultsData(var line: String = "",
                             var outputLines: MutableList<String> = emptyList<String>().toMutableList(),
                             var errors: MutableList<String> = emptyList<String>().toMutableList(),
                             var progress: Float = 0f)

