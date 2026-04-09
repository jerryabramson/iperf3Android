// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view


import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
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
    val uiState by viewModel.uiStateFlow.collectAsState()
    viewModel.setupIperf3Parameters(iperf3Parameters)
    viewModel.setTimeout(3000)
    viewModel.setDuration(10)

    // 2️⃣ Build a TextStyle that uses the font
    val mesloMonoStyle = TextStyle(
        fontFamily = mesloFontFamily(),
        fontSize = 14.sp,
        letterSpacing = 0.2.sp,
        color = MaterialTheme.colorScheme.onSurface
    )   // optional tweak for monospace readability




    // -------------------------------------------------------------------------
    // UI State
    // -------------------------------------------------------------------------
    //var isRunning by remember { mutableStateOf(false) }
    var numSeconds by remember { mutableFloatStateOf(0f) }
    var hostName by remember { mutableStateOf(uiState.iperf3Parameters.serverHost) }
    val ip = uiState.iperf3Parameters.iperf3Binary.absolutePath
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
            if (uiState.isFinished) {
                Text(
                    "return Code: ${uiState.returnCode}",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Left,
                    fontSize = 18.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),

                ) {

                TextField(
                    value = uiState.hostName, // uiState.iperf3Parameters.serverHost,
                    onValueChange = viewModel::updateHostName,   //viewModel.setServerHost(hostName) },
                    enabled = !uiState.isRunning,
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
                        viewModel.launch()
                    }, enabled = !uiState.isRunning
                ) {
                    Text("Run")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isRunning) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Testing for 10 seconds against remote host ${uiState.iperf3Parameters.serverHost}",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )

                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                    Column(modifier = Modifier.fillMaxWidth()) {
                        numSeconds = uiState.progress * 10
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(
                                "${numSeconds.toInt()} seconds elapsed",
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp
                            )
                        }

                        Text(
                            uiState.latestLine,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Left,
                            modifier = Modifier
                                .fillMaxWidth(),
                            fontSize = 18.sp
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            // Show the accumulated lines in a lazy list
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(uiState.outputLines.size) { index ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = uiState.outputLines.get(index),
                            textAlign = TextAlign.Left,
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = mesloMonoStyle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            //}

        }
    }

}














