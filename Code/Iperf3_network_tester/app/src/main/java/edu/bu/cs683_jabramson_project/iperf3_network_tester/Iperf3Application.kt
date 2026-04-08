package edu.bu.cs683_jabramson_project.iperf3_network_tester
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// need the annotation @HiltAndroid to use Hilt
// Also need to add the application name in the manifest file

@HiltAndroidApp
class Iperf3Application: Application()
