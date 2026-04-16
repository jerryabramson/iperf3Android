// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view


import android.R.attr.enabled
import android.R.attr.fontWeight
import android.R.attr.label
import android.R.attr.singleLine
import android.widget.ToggleButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
//        viewModel.setTimeout(3000)
//        viewModel.setDuration("10")
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

        val results by remember { mutableStateOf(mutableListOf<String>()) }



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

                val prompt = "Host Name or IP Address"
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .fillMaxWidth()
                    ) {
                    TextField(
                        value = uiState.hostName,
                        onValueChange = viewModel::updateHostName,
                        enabled = !uiState.isRunning,
                        placeholder = { Text("jabramson.com") },
                        modifier = Modifier
                            .padding(end = 6.dp),
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
                        singleLine = true,
                        keyboardActions = KeyboardActions(onDone = {
                            results.clear()
                            viewModel.launch()
                        })
                    )


                    // -------------------------------------------------------------
                    // Button to start the test
                    // -------------------------------------------------------------
                    Button(
                        modifier = Modifier.padding(end = 4.dp),
                        shape = MaterialTheme.shapes.small,
                        onClick = {
                            results.clear()
                            viewModel.launch()
                        }, enabled = !uiState.isRunning
                    ) {
                        Text("Run")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = uiState.durationSecs,
                        textStyle = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Normal),
                        onValueChange = { viewModel.setDuration(it) },
                        enabled = !uiState.isRunning,
                        placeholder = {
                            Text(
                                "10",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .width(120.dp),
                        label = {
                            Text(
                                "Duration",
                                fontSize = 8.sp,
                            )
                        },
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
                    TextField(
                        value = uiState.parallelStreams,
                        onValueChange = { viewModel.setParallelStreams(it) },
                        enabled = !uiState.isRunning,
                        textStyle = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Normal),
                        placeholder = {
                            Text(
                                "8",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .width(120.dp),
                        label = {
                            Text(
                                "Parallel Streams",
                                fontSize = 8.sp,
                            )
                        },
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
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 10.dp)) {

                    val uploadDownload = OnOff(!uiState.isReverse, "Upload", "Download")
                    val forceFlush = OnOff(uiState.forceFlush, "Force Flush", "No Force Flush")
                    val debug = OnOff(uiState.isDebugging, "Debug", "No Debug")

                    if (!uiState.isRunning) {
                        VerticalDivider(
                            modifier = Modifier.height(48.dp).padding(start = 2.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ToggleButton(
                            isSelected = uiState.isReverse,
                            mode = uploadDownload,
                            onClick = viewModel::toggleReverse,
                            fontSize = 10
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        VerticalDivider(
                            modifier = Modifier.height(48.dp).padding(start = 2.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ToggleButton(
                            isSelected = uiState.forceFlush,
                            mode = forceFlush,
                            onClick = viewModel::toggleForceFlush,
                            fontSize = 10
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        VerticalDivider(
                            modifier = Modifier.height(48.dp).padding(start = 2.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    ToggleButton(
                        isSelected = uiState.isDebugging,
                        mode = debug,
                        onClick = viewModel::toggleDebug,
                        fontSize = 8
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (!uiState.isRunning && uiState.isFinished && uiState.errorLines.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    var color = MaterialTheme.colorScheme.primary
                    if (uiState.returnCode != 0) color = MaterialTheme.colorScheme.onError

                    results.add("Return Code: ${uiState.returnCode}")
                    if (uiState.returnCode == 0) {
                        results.add("    Average: ${uiState.averageLine}")
                        results.add("    Maximum: ${uiState.maximumLine}")
                        results.add("    Minimum: ${uiState.minimumLine}")
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(results.size) { index ->

                            Text(
                                results[index], //"Return Code: ${uiState.returnCode}",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Left,
                                fontSize = 20.sp,
                                style = mesloMonoStyle,
                                color = color
                            )
                            if (uiState.returnCode == 0) {
                                Text(
                                    results[index], //"    Average: ${uiState.averageLine}",
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 20.sp,
                                    style = mesloMonoStyle
                                )
                                Text(
                                    results[index], //"    Maximum: ${uiState.maximumLine}",
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 20.sp,
                                    style = mesloMonoStyle
                                )
                                Text(
                                    results[index], //"    Minimum: ${uiState.minimumLine}",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = 20.sp,
                                    style = mesloMonoStyle
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                        }
                    }
                }

                if (uiState.isRunning) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Testing for ${uiState.durationSecs} second(s); Remote host ${uiState.hostName}",
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Left,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        ) {
                            val primaryColor =
                                if (!uiState.isReverse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val secondaryColor =
                                if (!uiState.isReverse) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary


                            LinearProgressIndicator(
                                progress = { uiState.progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
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
                                var num = numSeconds
                                if (uiState.isReverse) num = uiState.durationSecs.toFloat() - numSeconds
                                Text(
                                    "${num.toInt()} seconds elapsed",
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
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
                if ((uiState.isRunning || uiState.isFinished) && uiState.iperf3Messages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(1.dp))
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
                    Spacer(modifier = Modifier.height(4.dp))
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






@Composable
private fun ToggleButton(
    mode: OnOff,
    isSelected: Boolean,
    onClick: () -> Unit,
    fontSize: Int
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    TextButton(
        onClick = onClick,
        modifier = Modifier
            //.padding(vertical = 12.dp, horizontal = 8.dp)
            .background(containerColor, shape = RoundedCornerShape(8.dp)),
        enabled = true
    ) {
//        Icon(
//            imageVector = if (mode.onOff)
//                Icons.Default.ArrowForward
//            else
//                Icons.Default.ArrowBack,
//            contentDescription = if (mode.onOff)
//                mode.onMessage
//            else
//                mode.offMessage,
//            tint = contentColor,
//            modifier = Modifier.size(24.dp)
//        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (mode.onOff) mode.onMessage else mode.offMessage,
            color = contentColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Define your transfer mode (separate file or inside the composable)
data class OnOff (
    var onOff: Boolean,
    var onMessage: String,
    var offMessage: String
)








