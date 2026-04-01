package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Extracts the iperf3 binary from app assets to the app's cache directory
 * which allows execution. The binary is extracted based on the device's ABI.
 *
 * @param context Any Android Context (usually an Activity or Application)
 * @return File object pointing to the extracted iperf3 binary, or null if extraction fails
 */
fun extractIperf3Binary(context: Context): File? {
    val abi = chooseAbi(context)
    val assetPath = "iperf3_binaries/$abi/iperf3"
    
    // Use cache directory which allows execution on modern Android versions
    val cacheDir = context.cacheDir
    val abiSubdir = File(cacheDir, abi)
    abiSubdir.mkdirs()
    
    val outputFile = File(abiSubdir, "iperf3")
    
    return try {
        context.assets.open(assetPath).use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        
        // Make the binary executable (chmod 755)
        outputFile.setExecutable(true, false)
        
        Log.d("extractIperf3Binary", "Extracted iperf3 binary to: ${outputFile.absolutePath}")
        Log.d("extractIperf3Binary", "Binary exists: ${outputFile.exists()}, executable: ${outputFile.canExecute()}")
        outputFile
    } catch (e: Exception) {
        Log.e("extractIperf3Binary", "Failed to extract iperf3 binary: ${e.message}", e)
        null
    }
}

/**
 * Utility – pick the first ABI that matches a folder we have under assets.
 * Adjust the order if you want a different priority.
 */
private fun chooseAbi(context: Context): String {
    val abiList = Build.SUPPORTED_ABIS
    return listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        .firstOrNull { abiList.contains(it) } ?: "arm64-v8a"
}
