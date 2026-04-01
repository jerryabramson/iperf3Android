package edu.bu.cs683_jabramson_project.iperf3_network_tester.utils

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Gets the iperf3 binary file. Tries multiple strategies:
 * 1. Check if /bin/iperf3 exists (pre-installed on some devices)
 * 2. Extract from assets to cache directory (for devices where SELinux allows it)
 * 3. Return null if no binary is available
 *
 * @param context Any Android Context (usually an Activity or Application)
 * @return File object pointing to the iperf3 binary, or null if not found
 */
fun getIperf3Binary(context: Context): File? {
    // Strategy 1: Check if /bin/iperf3 exists (pre-installed on some Android devices)
    val preInstalledBinary = File("/bin/iperf3")
    if (preInstalledBinary.exists() && preInstalledBinary.canExecute()) {
        Log.d("getIperf3Binary", "Found pre-installed iperf3 at /bin/iperf3")
        return preInstalledBinary
    }
    
    // Strategy 2: Extract from assets to cache directory
    val abi = chooseAbi(context)
    val cacheDir = context.cacheDir
    val abiSubdir = File(cacheDir, abi)
    abiSubdir.mkdirs()
    
    val extractedBinary = File(abiSubdir, "iperf3")
    
    if (extractedBinary.exists() && extractedBinary.canExecute()) {
        Log.d("getIperf3Binary", "Found extracted iperf3 binary: $extractedBinary")
        return extractedBinary
    }
    
    // Binary doesn't exist, try to extract it
    Log.d("getIperf3Binary", "Binary not found, attempting extraction from assets...")
    return extractIperf3Binary(context)
}

/**
 * Extracts the iperf3 binary from app assets to the app's cache directory
 * and makes it executable.
 *
 * @param context Any Android Context (usually an Activity or Application)
 * @return File object pointing to the extracted iperf3 binary, or null if extraction fails
 */
fun extractIperf3Binary(context: Context): File? {
    val abi = chooseAbi(context)
    val assetPath = "iperf3_binaries/$abi/iperf3"
    
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
