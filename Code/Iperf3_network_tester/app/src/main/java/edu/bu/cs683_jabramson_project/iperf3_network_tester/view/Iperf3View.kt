// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view


import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(iperf3Parameters: Iperf3Parameters,
                    viewModel: Iperf3RunViewModel = hiltViewModel<Iperf3RunViewModel>())
{
    //val context = LocalContext.current
    val uiState by viewModel.uiStateFlow.collectAsState()

    viewModel.setupIperf3Parameters(iperf3Parameters)
    viewModel.setTimeout(3000)
    viewModel.setDuration(10)

    // 2️⃣ Build a TextStyle that uses the font
    val mesloMonoStyle = TextStyle(
        fontFamily = mesloFontFamily(),
        fontSize = 10.sp,
        letterSpacing = 0.2.sp
    )   // optional tweak for monospace readability




    // -------------------------------------------------------------------------
    // UI State
    // -------------------------------------------------------------------------


    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var numSeconds by remember { mutableFloatStateOf(0f) }
    var currentProgress by remember { mutableFloatStateOf(0f) }

    var returnCode by remember { mutableIntStateOf(0) }
    var outputLines by remember { mutableStateOf(emptyList<String>().toMutableList()) }

    val ip = uiState.iperf3Binary.absolutePath


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("iperf‑3 binary: '$ip'") })
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
                    value = uiState.serverHost,
                    onValueChange = { viewModel.setServerHost(it) },
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
                        if (uiState.serverHost.isEmpty()) viewModel.setServerHost("jabramson.com")
                        isRunning = true
                        viewModel.launch()
                        //coroutineScope.launch {
                        //    viewModel.run { runIperf3() }
//
  //                      }
//                            returnCode = iperf3Runner(
//                                { progress ->
//                                    currentProgress = progress
//                                },
//                                ::outputIt,
//                                iperf3Binary,
//                                hostName,
//                                10,
//                                outputLines
//                            )
//                            isRunning = false
//                            isFinished = true
//
//                        }
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
                        "Testing for 10 seconds against remote host ${uiState.serverHost}",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )

                    LinearProgressIndicator(
                        progress = { uiState.results.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                    numSeconds = uiState.results.progress * 10
                    Text(
                        "${numSeconds.toInt()} seconds elapsed",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    // Show the accumulated lines in a lazy list
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = uiState.results.line, //latestLine,
                            textAlign = TextAlign.Left,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(outputLines.size) { index ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = uiState.results.outputLines.get(index),
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Results")
                    Column(Modifier.fillMaxWidth()
                        .padding(8.dp)) {
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














