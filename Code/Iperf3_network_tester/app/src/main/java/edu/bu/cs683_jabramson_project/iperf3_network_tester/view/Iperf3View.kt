// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view


import androidx.annotation.Px
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.*



import androidx.hilt.navigation.compose.hiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel


@Composable
fun MyScreen() {
    val colors = MaterialTheme.colorScheme // Access all color roles here

    // Example usage:
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(iperf3Parameters: Iperf3Parameters,
                    viewModel: Iperf3RunViewModel = hiltViewModel<Iperf3RunViewModel>())
{
    Iperf3NetworkTesterTheme(content = {
        val uiState by viewModel.uiStateFlow.collectAsState()
        viewModel.setupIperf3Parameters(iperf3Parameters)
        viewModel.setTimeout(3000)
        viewModel.setDuration(10)
//        viewModel.setReverse(true)


        // 2️⃣ Build a TextStyle that uses the font
        val mesloMonoStyle = TextStyle(
            fontFamily = mesloFontFamily(),
            fontSize = 18.sp,
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
                TopAppBar(title = { Text("iperf3 Performance Tester") })
            },
            bottomBar = {
                Text(
                    "Executable:  ${uiState.iperf3Parameters.iperf3Binary}",
                    style = mesloMonoStyle,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        )
        { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                //viewModel.setDebug(true)
                val prompt = "Host Name or IP Address"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),

                    ) {

                    TextField(
                        value = uiState.hostName,
                        onValueChange = viewModel::updateHostName,
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

                if (!uiState.isRunning && uiState.isFinished && uiState.errorLines.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tested for ${uiState.iperf3Parameters.durationSecs} second(s)",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )

                    var color = MaterialTheme.colorScheme.primary
                    if (uiState.returnCode != 0) color = MaterialTheme.colorScheme.onError
                    Text(
                        "Return Code: ${uiState.returnCode}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left,
                        fontSize = 20.sp,
                        style = mesloMonoStyle,
                        color = color
                    )
                    if (uiState.returnCode == 0) {
                        Text(
                            "    Average: ${uiState.averageLine}",
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 20.sp,
                            style = mesloMonoStyle
                        )
                        Text(
                            "    Maximum: ${uiState.maximumLine}",
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 20.sp,
                            style = mesloMonoStyle
                        )
                        Text(
                            "    Minimum: ${uiState.minimumLine}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 20.sp,
                            style = mesloMonoStyle
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                }

                if (uiState.isRunning) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Testing for ${uiState.iperf3Parameters.durationSecs} second(s)",
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                        Text(
                            "Remote host ${uiState.hostName}",
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                        ) {
                            val primaryColor =
                                if (!uiState.iperf3Parameters.isReverse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val secondaryColor =
                                if (!uiState.iperf3Parameters.isReverse) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary


                            LinearProgressIndicator(
                                progress = { uiState.progress },
                                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                                color = primaryColor,
                                trackColor = secondaryColor,
                                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                            )

                        }
                        Column(modifier = Modifier.fillMaxWidth()) {
                            numSeconds = uiState.progress * 10
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
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
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                fontSize = 18.sp
                            )
                        }
                    }
                }


                if (uiState.errorLines.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (uiState.isRunning || uiState.isFinished) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )

                }
                if (uiState.isRunning && uiState.iperf3Messages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.iperf3Messages.size) { index ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = uiState.iperf3Messages[index],
                                    textAlign = TextAlign.Left,
                                    style = mesloMonoStyle,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }


                // Show the accumulated lines in a lazy list
                if (uiState.isDebugging) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.outputLines.size) { index ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = uiState.outputLines[index],
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    style = mesloMonoStyle
                                )
                            }
                        }
                    }
                }


                if (uiState.errorLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    // Show the accumulated lines in a lazy list
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.errorLines.size) { index ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = uiState.errorLines.get(index),
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }

                }
            }
        }
    })
}















