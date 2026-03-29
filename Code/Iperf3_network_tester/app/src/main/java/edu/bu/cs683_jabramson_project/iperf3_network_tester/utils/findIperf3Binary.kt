package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlin.collections.contains

fun findIperf3Binary(context: Context): File? {

    val abi = chooseAbi(context)
    val libDir = File(context.filesDir, "jniLibs/$abi")
    var iperfBinary = File(libDir, "iperf3")

    if (!iperfBinary.exists()) {
        // Fall back to the one I manually installed
        iperfBinary = File("/bin/iperf3")
    }


    Log.d("findIperf3Binary", "iperf3 binary: $iperfBinary")


    // -----------------------------------------------------------
    // 2️⃣ Verify existence and make it executable (chmod 755)
    // -----------------------------------------------------------
    if (!iperfBinary.exists()) {
        Log.e("findIperf3Binary","iperf3 binary not found at $iperfBinary")
        return null
    }

    iperfBinary.setExecutable(true, false)   // <-- equivalent to chmod 755
    return iperfBinary


}

/**
 * Utility – pick the first ABI that matches a folder we have under jniLibs/.
 * Adjust the order if you want a different priority.
 */
private fun chooseAbi(context: Context): String {
    val abiList = Build.SUPPORTED_ABIS
    return listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        .firstOrNull { abiList.contains(it) } ?: "arm64-v8a"
}