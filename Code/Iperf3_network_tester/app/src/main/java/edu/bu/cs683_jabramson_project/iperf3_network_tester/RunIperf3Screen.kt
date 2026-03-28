// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/RunIperf3Screen.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester

import android.Manifest

import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.iperf3Runner

import kotlinx.coroutines.launch



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // -------------------------------------------------------------------------
    // Permission handling (INTERNET) – only needed on Android 13+ if you use
    // clear‑text network; otherwise the manifest declaration is enough.
    // -------------------------------------------------------------------------
    val hasInternetPermission = remember {
        context.checkSelfPermission(Manifest.permission.INTERNET) ==
                PackageManager.PERMISSION_GRANTED
    }

    // Launcher that shows the permission dialog
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.w("RunIperf3Screen", "Internet permission denied – test cannot run.")
        }
    }

    // -------------------------------------------------------------------------
    // UI State
    // -------------------------------------------------------------------------
    var output by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var returnCode by remember { mutableIntStateOf(0) }

    //val outputLines by remember { mutableStateListOf<String>() }

    // -------------------------------------------------------------------------
    // Helper to pick the ABI (you could expose a dropdown instead)
    // -------------------------------------------------------------------------
    //val abi = remember { chooseAbi(context) }   // from the snippet in section 3

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("iperf‑3 Test") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // -------------------------------------------------------------
            // Button to start the test
            // -------------------------------------------------------------
            Button(
                //    modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!hasInternetPermission) {
                        // Request permission and abort launching the test
                        requestPermissionLauncher.launch(Manifest.permission.INTERNET)
                        return@Button
                    }

                    isRunning = true
                    coroutineScope.launch {
                        returnCode = iperf3Runner(
                            { progress ->
                                currentProgress = progress
                            },
                            context,
                            "192.168.1.28",
                            10,
                            outputLines = mutableListOf()
                        )
                        isRunning = false

                    }
                }, enabled = !isRunning
            ) {
                Text("Run iperf‑3 Test")
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (isRunning) {
                Text(
                    "Testing… (this may take a few seconds)",
                    modifier = Modifier.padding(8.dp).fillMaxWidth()
                )

                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(currentProgress.toString(), modifier = Modifier.padding(8.dp))

            }




            errorMessage?.let { msg ->
                Text(
                    "Error: $msg",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            output = null
            errorMessage = null
        }

    }
}

















