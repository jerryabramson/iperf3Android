package edu.bu.cs683_jabramson_project.iperf3_network_tester.runner

interface IperfCallback {
    fun onOutput(line: String)
    fun onError(error: String)
    fun onComplete()
}