// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/RunIperf3Screen.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester

import android.Manifest
import android.R.attr.progress

import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign


import androidx.compose.ui.text.TextStyle   // ← this is the import you need
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily


import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.iperf3Runner

import kotlinx.coroutines.launch
import java.io.File


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(iperf3Binary: File) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 2️⃣ Build a TextStyle that uses the font
    val mesloMonoStyle = TextStyle(
        fontFamily = mesloFontFamily(),
        fontSize = 10.sp,
        letterSpacing = 0.2.sp)   // optional tweak for monospace readability



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
    var isFinished by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var returnCode by remember { mutableIntStateOf(0) }
    var outputLines by remember { mutableStateOf(emptyList<String>().toMutableList()) }

    // -------------------------------------------------------------------------
    // Helper to pick the ABI (you could expose a dropdown instead)
    // -------------------------------------------------------------------------
    //val abi = remember { chooseAbi(context) }   // from the snippet in section 3

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("iperf‑3 Tester, binary: '$iperf3Binary'") })
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
                    isFinished = false
                    isRunning = true
                    coroutineScope.launch {
                        returnCode = iperf3Runner(
                            { progress ->
                                currentProgress = progress
                            },
                            iperf3Binary,
                            "192.168.1.28",
                            10,
                            outputLines
                        )
                        isRunning = false
                        isFinished = true

                    }
                }, enabled = !isRunning
            ) {
                Text("Run iperf‑3 Test")
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (isRunning) {
                Text(
                    "Testing… (this may take a few seconds)",
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )

                LinearProgressIndicator(
                    progress =  currentProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(currentProgress.toString(), modifier = Modifier.padding(8.dp))

            } else if (isFinished) {
                Column(Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("return Code: $returnCode")
                        Text(text = "iperf3 Output")
                    }
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        thickness = 12.dp,
                        color = Color.DarkGray
                    )
                }

                for (index in 0 until outputLines.size) {
                    var line: String = outputLines.get(index)
                    val annotatedOutputLine = buildAnnotatedString {
                        // Part 2: Blue (bold)
                        withStyle(
                            style = SpanStyle(
                                color = Color.Blue,
                                fontFamily = mesloFontFamily(),
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Normal
                            )
                        ) {
                            append(line)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = annotatedOutputLine,
                            textAlign = TextAlign.Left,
                            style = mesloMonoStyle,
                            modifier = Modifier
                                .fillMaxWidth()

                        )
                    }
                }
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

















