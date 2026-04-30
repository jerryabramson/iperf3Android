// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.DefaultUIValues
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel


@Composable
fun MyScreen() {
    val colors = MaterialTheme.colorScheme // Access all color roles here

    // Example usage:
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(viewModel: Iperf3RunViewModel = hiltViewModel<Iperf3RunViewModel>())
{
    Iperf3NetworkTesterTheme(content = {
        val uiState by viewModel.uiStateFlow.collectAsState()

        val mesloMonoStyle = TextStyle(
            fontFamily = mesloFontFamily(),
            fontSize = 18.sp,
            letterSpacing = 0.2.sp,
            color = MaterialTheme.colorScheme.onSurface
        )   // optional tweak for monospace readability


        val baseTextFieldColors = TextFieldDefaults.colors(
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
        )


        // 2️⃣ Build a TextStyle that uses the font


        // -------------------------------------------------------------------------
        // UI State
        // -------------------------------------------------------------------------
        //var isRunning by remember { mutableStateOf(false) }
        var numSeconds by remember { mutableFloatStateOf(0f) }
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("iperf3 Performance Tester") })
            },
            bottomBar = {
                Text("CS683-MobileDevelopment Project Jerold Abramson", fontSize =  16.sp)
            }
        )
        { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

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
                        placeholder = { Text(DefaultUIValues.HOST_NAME) },
                        modifier = Modifier
                            .padding(end = 30.dp),
                        label = { Text("Host Name:port") },
                        colors = baseTextFieldColors,
                        singleLine = true,
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.launch()
                        })
                    )


                    Spacer(modifier = Modifier.weight(1f))
                    // -------------------------------------------------------------
                    // Button to start the test
                    // -------------------------------------------------------------
                    Button(
                        modifier = Modifier.padding(end = 10.dp),
                        shape = MaterialTheme.shapes.large,
                        onClick = {
                            viewModel.launch()
                        }, enabled = !uiState.isRunning
                    ) {
                        Text("Run")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = uiState.durationSecs,
                        onValueChange = viewModel::setDuration,
                        enabled = !uiState.isRunning,
                        placeholder = { Text("10") },
                        modifier = Modifier
                            .width(150.dp)
                            .padding(start = 10.dp),
                        label = { Text("Duration") },
                        colors = baseTextFieldColors,
                        singleLine = true
                    )
                    TextField(
                        value = uiState.parallelStreams,
                        onValueChange = viewModel::setParallelStreams,
                        enabled = !uiState.isRunning,
                        placeholder = { Text("8") },
                        modifier = Modifier
                            .width(160.dp)
                            .padding(start = 4.dp),
                        label = {Text("Streams") },
                        colors = baseTextFieldColors,
                        singleLine = true
                    )
                    TextField(
                        value = uiState.skip,
                        onValueChange = viewModel::setSkip,
                        enabled = !uiState.isRunning,
                        placeholder = {
                            Text(
                                "2",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .width(150.dp)
                            .padding(start = 4.dp, end =  10.dp),
                        label = {
                            Text(
                                "Omit"
                            )
                        },
                        colors = baseTextFieldColors,
                        singleLine = true
                    )

                }


                Column(modifier = Modifier.padding(start = 10.dp, end =  10.dp)) {
                    if (!uiState.isRunning) {
                        var currentUploadButton = 0
                        if (uiState.isReverse) currentUploadButton = 0
                        else currentUploadButton = 1
                        UploadDownload(viewModel = viewModel, currentUploadButton, mesloMonoStyle)
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    var currentDebugButton = 0
                    if (uiState.isDebugging) currentDebugButton = 2
                    else if (uiState.isVerbose) currentDebugButton = 1
                    DebugOnOffRadioButton(viewModel = viewModel, currentDebugButton, mesloMonoStyle)                }

                if (!uiState.isRunning && uiState.isFinished) {
                    Spacer(modifier = Modifier.height(4.dp))
                    var color = MaterialTheme.colorScheme.primary
                    if (uiState.returnCode != 0) color = MaterialTheme.colorScheme.onError
                    if (uiState.results.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(uiState.results.size) { index ->
                                Text(
                                    style = mesloMonoStyle,
                                    text = uiState.results[index],
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Left,
                                    fontSize = 16.sp,
                                    color = color
                                )
                            }
                        }
                    }
                }

                if (uiState.isRunning) {
                    Column(
                        Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
                    ) {
                        if (uiState.latestLine.isEmpty()) {
                            Text(
                                "Launching iperf3 ...",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        val primaryColor =
                            if (!uiState.isReverse) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val secondaryColor =
                            if (!uiState.isReverse) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary

                        if (uiState.bandWidth.isNotEmpty()) {
                            var num = uiState.progress
                            if (uiState.isReverse) num = 1 - num
                            val percentComplete = (num * 100).toInt()
                            Text(
                                "${percentComplete}% complete",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp
                            )
                        }

                        LinearProgressIndicator(
                            progress = { uiState.progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = primaryColor,
                            trackColor = secondaryColor,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )
                    }
                    if (uiState.bandWidth.isNotEmpty()) {
                        Text(
                            uiState.bandWidth,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(modifier = Modifier.padding(start = 10.dp, end = 10.dp)) {
                            Text(
                                modifier = Modifier.padding(start =  10.dp),
                                text = uiState.minimum,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                modifier = Modifier.padding(start =  40.dp),
                                text = uiState.maximum,
                                color = MaterialTheme.colorScheme.tertiary,
                                textAlign = TextAlign.Right,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }


                if (uiState.errorLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                    // Show the accumulated lines in a lazy list
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.errorLines.size) { index ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = uiState.errorLines.get(index),
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Left,
                                    style = mesloMonoStyle,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                if (((uiState.isVerbose || uiState.isDebugging || uiState.iperf3Messages.size <= 1)  && (uiState.isRunning || uiState.isFinished) && uiState.iperf3Messages.isNotEmpty())) {
                    Spacer(modifier = Modifier.height(1.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.iperf3Messages.size) { index ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = uiState.iperf3Messages[index],
                                    textAlign = TextAlign.Left,
                                    style = mesloMonoStyle,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth().padding(start = 10.dp, end = 10.dp)
                                )
                            }
                        }
                    }
                    if (!uiState.isFinished && uiState.latestLine.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        if (!uiState.isDebugging) {
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 10.dp, end = 10.dp),
                                thickness = 4.dp,
                                color = MaterialTheme.colorScheme.tertiary
                            )

                        }
                    }
                }


                // Show the accumulated lines in a lazy list
                if (uiState.isDebugging) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.outputLines.size) { index ->
                            Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
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








