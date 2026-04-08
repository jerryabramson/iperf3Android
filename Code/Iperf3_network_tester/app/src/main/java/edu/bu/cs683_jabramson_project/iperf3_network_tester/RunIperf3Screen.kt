// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/RunIperf3Screen.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester

import android.Manifest

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.LinearProgressIndicator
import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign


import androidx.compose.ui.text.TextStyle   // ← this is the import you need
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily


import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel

import kotlinx.coroutines.launch
import java.io.File

import dagger.hilt.android.lifecycle.HiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(iperf3Binary: File) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val viewModel: Iperf3RunViewModel = hiltViewModel<Iperf3RunViewModel>()


    // 2️⃣ Build a TextStyle that uses the font
    val mesloMonoStyle = TextStyle(
        fontFamily = mesloFontFamily(),
        fontSize = 10.sp,
        letterSpacing = 0.2.sp
    )   // optional tweak for monospace readability


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
    var hostName by remember { mutableStateOf("") }
    var hostNameEntry by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var numSeconds by remember { mutableFloatStateOf(0f) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var foo: Float
    var returnCode by remember { mutableIntStateOf(0) }
    var outputLines by remember { mutableStateOf(emptyList<String>().toMutableList()) }
    var latestLine by remember { mutableStateOf("") }
    fun outputIt(line: String) {
        outputLines.add(line)
        latestLine = line
        Log.d("Iperf3Runner: ", "stdout: $line")
    }


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

            val prompt = "Host Name or IP Address"

            Row(
                //modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = R.dimen.common_padding)),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),

                ) {
                TextField(
                    value = hostNameEntry,
                    onValueChange = { hostNameEntry = it },
                    enabled = !isRunning,
                    placeholder = { Text("jabramson.com") },
                    modifier = Modifier
                        .width(260.dp)
                        .padding(end = 16.dp),
                    label = { Text(prompt) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        errorIndicatorColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        errorTextColor = MaterialTheme.colorScheme.error
                    ),
                    singleLine = true
                )


                // -------------------------------------------------------------
                // Button to start the test
                // -------------------------------------------------------------
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (!hasInternetPermission) {
                            // Request permission and abort launching the test
                            requestPermissionLauncher.launch(Manifest.permission.INTERNET)
                            return@Button
                        }
                        if (hostNameEntry.isEmpty()) hostNameEntry = "jabramson.com"
                        hostName = hostNameEntry
                        hostNameEntry = ""
                        isFinished = false
                        isRunning = true
                        coroutineScope.launch {
                            returnCode = iperf3Runner(
                                { progress ->
                                    currentProgress = progress
                                },
                                ::outputIt,
                                iperf3Binary,
                                hostName,
                                10,
                                outputLines
                            )
                            isRunning = false
                            isFinished = true

                        }
                    }, enabled = !isRunning
                ) {
                    Text("Run")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (isRunning) {

                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Testing for 10 seconds against remote host $hostName",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )

                    LinearProgressIndicator(
                        progress = currentProgress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                    numSeconds = currentProgress * 10
                    Text(
                        "${numSeconds.toInt()} seconds elapsed",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    // Show the accumulated lines in a lazy list

                    Text("Results")
                    Column(Modifier.fillMaxWidth()
                        .padding(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = latestLine,
                                textAlign = TextAlign.Left,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxWidth()) {
                    Text("return Code: $returnCode",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Left,
                        fontSize = 18.sp)
                }
                currentProgress = 0f

                for (index in 0 until outputLines.size) {
                    var line: String = outputLines.get(index)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = line,
                            textAlign = TextAlign.Left,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                outputLines.clear()
            }
        }
    }

}














