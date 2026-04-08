package edu.bu.cs683_jabramson_project.iperf3_network_tester.model

data class Iperf3ResultsData(var line: String = "",
                             var errors: MutableList<String> = emptyList<String>().toMutableList())

