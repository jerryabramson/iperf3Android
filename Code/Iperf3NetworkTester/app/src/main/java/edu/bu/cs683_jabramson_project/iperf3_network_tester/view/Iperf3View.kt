// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import android.R.attr.thickness
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import edu.bu.cs683_jabramson_project.iperf3_network_tester.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getAverage
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getMaximum
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getMinimum
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.toWholeNumber
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.DefaultUIValues
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(viewModel: Iperf3RunViewModel = hiltViewModel<Iperf3RunViewModel>()) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val monoStyle = mesloMonoTextStyle()
    val fieldColors = textFieldColors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end=50.dp).width(280.dp).height(40.dp).background(color = MaterialTheme.colorScheme.tertiaryContainer)
                    )
                    {
                        Text(stringResource(id = R.string.app_name), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 6.dp))
                    }
                },
                actions = { RunButton(viewModel, uiState.isRunning, uiState.isFinished) },

            )
        },
        bottomBar = { BottomBar() }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            /* Input rows */
            HostInputRow(uiState, viewModel, fieldColors, monoStyle)
            Spacer(modifier = Modifier.height(10.dp))
            StreamsAndDebugRow(uiState, viewModel, fieldColors, monoStyle)

            /* Output rows */
            ResultsRow(uiState, monoStyle)
            RunningColumnSection(uiState, monoStyle)
            IperfMessagesSection(uiState, monoStyle)
            ErrorSection(uiState, monoStyle)
            DebugOutputSection(uiState, monoStyle)



        }
    }
}


/**
 * Run button for the UI, part of the top bar.
 * @param viewModel the view model for the UI
 * @param isRunning whether the UI is currently running
  */
@Composable
private fun RunButton(viewModel: Iperf3RunViewModel,
                      isRunning: Boolean = false,
                      isFinished: Boolean = false) {
    androidx.compose.material3.Button(
        modifier = Modifier.padding(end = 10.dp),
        shape = MaterialTheme.shapes.large,
        onClick = viewModel::launch,
        enabled = !isRunning || isFinished
    ) { Text("Run") }
}


/**
 * Bottom bar for the UI.
 */
@Composable
private fun BottomBar() {
    Text(
        text = "Mobile Development Directed Study Application - Jerold Abramson",
        fontSize = 10.sp
    )
}

@Composable
private fun mesloMonoTextStyle(): TextStyle = TextStyle(
    fontFamily = mesloFontFamily(),
    fontSize = 18.sp,
    letterSpacing = 0.2.sp,
    color = MaterialTheme.colorScheme.onSurface
)

@Composable
private fun textFieldColors() = androidx.compose.material3.TextFieldDefaults.colors(
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

/**
 * Host input row for the UI.
 * Consists of:
 *   1. Text field for the host name,
 *   2. Numeric field for the duration
 *   3. Settings section - upload/download

 * @param uiState the current state of the UI
 * @param viewModel the view model for the UI
 * @param colors the colors for the text fields
 * @param monoStyle the style for the monospaced text
  */
@Composable
private fun HostInputRow(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    viewModel: Iperf3RunViewModel,
    colors: androidx.compose.material3.TextFieldColors,
    monoStyle: TextStyle

) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(start = 10.dp).fillMaxWidth()
    ) {
        // placeholder = { Text(placeholder) },
         TextField(
            value = uiState.hostName,
            textStyle = MaterialTheme.typography.bodySmall,
            onValueChange = viewModel::updateHostName,
            enabled = !uiState.isRunning,
            placeholder = { Text(DefaultUIValues.HOST_NAME, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary) },
            modifier = Modifier.width(width = 210.dp).height(height = 50.dp).padding(end = 6.dp),
            label = { Text("iPerf3 Server[:port]", style = monoStyle.copy(fontSize = 12.sp),) },
            colors = colors,
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { viewModel.launch() })
        )
        GenericNumericField(
            value = uiState.durationSecs,
            onValueChange = viewModel::setDuration,
            enabled = !uiState.isRunning,
            placeholder = "10",
            label = "Time",
            modifier = Modifier.width(width = 70.dp).height(height = 50.dp).padding(end = 2.dp),
            colors = colors
        )

        UploadDownloadRadioButtons(uiState, viewModel, monoStyle = monoStyle.copy(fontSize = 13.sp))
    }
}

/**
 * Numeric inputs for the UI.
 * Consists of:
 *    1. Numeric field for the parallel streams
 *    2. Numeric field for the skip/omit
 *    3. Debug on/off radio button
 *    4. Upload/download radio button
*  @param uiState the current state of the UI
 * @param viewModel the view model for the UI
 * @param colors the colors for the text fields
 * @param monoStyle the style for the monospaced text
 */
@Composable
private fun StreamsAndDebugRow (
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    viewModel: Iperf3RunViewModel,
    colors: androidx.compose.material3.TextFieldColors,
    monoStyle: TextStyle
) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp)) {
        GenericNumericField(
            value = uiState.parallelStreams,
            onValueChange = viewModel::setParallelStreams,
            enabled = !uiState.isRunning,
            placeholder = "8",
            label = "Streams",
            modifier = Modifier.width(width = 130.dp).height(height = 50.dp).padding(end = 10.dp),
            colors = colors
        )
        GenericNumericField(
            value = uiState.skip,
            onValueChange = viewModel::setSkip,
            enabled = !uiState.isRunning,
            placeholder = "2",
            label = "Omit",
            modifier = Modifier.width(width = 90.dp).height(height = 50.dp).padding(end = 10.dp),
            colors = colors
        )
        Spacer(modifier = Modifier.width(90.dp))
        DebugOnOffRadioButton(viewModel, uiState.isDebugging, uiState.isVerbose, monoStyle)
     }
}

/**
 * Used for numeric fields in the UI. Expected to be part of a row or column.
 * @param value the current value of the field
 * @param onValueChange the callback to be called when the value changes
 * @param enabled whether the field is enabled
 * @param placeholder the placeholder text to be displayed when the field is empty
 * @param label the label text to be displayed next to the field
 * @param modifier the modifier to be applied to the field
 * @param colors the colors for the text field
 *
 */
@Composable
private fun GenericNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    label: String,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.TextFieldColors
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary) },
        modifier = modifier,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        colors = colors,
        singleLine = true
    )
}

/**
 * Debug the on / off radio button for the UI.
 * @param viewModel the view model for the UI
  * @param monoStyle the style for the monospaced text
 */
@Composable
private fun UploadDownloadRadioButtons(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    viewModel: Iperf3RunViewModel,
    monoStyle: TextStyle
) {
    if (!uiState.isRunning) {
        UploadDownload(viewModel, uiState.isReverse, MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Results row for the UI.
 * @param uiState the current state of the UI
 * @param monoStyle the style for the monospaced text
 */
@Composable
private fun ResultsRow(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    monoStyle: TextStyle
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(start = 10.dp, end =  10.dp).fillMaxWidth()
    ) {
        if (uiState.isRunning || !uiState.isFinished) return
        if (uiState.results.isEmpty()) return
        var thick  = if (uiState.returnCode != 0) 5.dp else 2.dp
        val resultColor = if (uiState.returnCode != 0) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.primary

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = thick,
            color = MaterialTheme.colorScheme.tertiary
        )
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(uiState.results.size) { index ->
                Text(
                    text = uiState.results[index],
                    style = monoStyle.copy(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Left,
                    color = resultColor
                )
            }
        }
    }
}

@Composable
private fun RunningColumnSection(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    monoStyle: TextStyle
) {
    if (!uiState.isRunning) return

    Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
        LaunchingMessage(uiState.latestLine.isEmpty())
        val (barColor, trackColor) = progressColors(uiState.isReverse)

        if (uiState.bandWidth.isNotEmpty()) {
            ProgressPercent(uiState)
        }
        if (uiState.latestLine.isNotEmpty()) {
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = barColor,
                trackColor = trackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )
        }

        if (uiState.bandWidth.isNotEmpty()) {
            BandwidthDisplay(uiState)
        }
    }
}

@Composable
private fun LaunchingMessage(show: Boolean) {
    if (show) {
        Text("Launching iperf3 ...", style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun progressColors(isReverse: Boolean): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return if (!isReverse) {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun ProgressPercent(uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData) {
    val num = if (uiState.isReverse) 1f - uiState.progress else uiState.progress
    val percent = (num * 100).toInt()
    Text(
        text = "${percent}% complete",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 18.sp
    )
}

@Composable
private fun BandwidthDisplay(uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
        text = uiState.lineResult.rawBandWidth,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        var max = uiState.lineResult.currentMax
        var min = uiState.lineResult.currentMin
        var avg = uiState.lineResult.currentAvg
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = toWholeNumber(min),
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = toWholeNumber(avg),
                //color = MaterialTheme.colorScheme.surfaceVariant,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = toWholeNumber(max),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ErrorSection(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    monoStyle: TextStyle
) {
    if (uiState.errorLines.isEmpty()) return
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(start = 10.dp, end =  10.dp).fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
            thickness = 4.dp,
            color = MaterialTheme.colorScheme.error
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
            items(uiState.errorLines.size) { index ->
                ErrorLineItem(uiState.errorLines[index], monoStyle)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ErrorLineItem(text: String, style: TextStyle) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Left,
            style = style.copy(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun IperfMessagesSection(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    monoStyle: TextStyle
) {
    Column() {

        if (uiState.isVerbose || uiState.isDebugging || uiState.returnCode != 0 ||
            (uiState.latestLine.isEmpty() && !uiState.isFinished)) {
            Spacer(modifier = Modifier.height(1.dp))
            if (uiState.returnCode == 0) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                    thickness = 4.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(uiState.iperf3Messages.size) { index ->
                    IperfMessageItem(uiState.iperf3Messages[index], monoStyle)
                }
            }

            if (uiState.isRunning && !uiState.isFinished && uiState.latestLine.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun IperfMessageItem(text: String, style: TextStyle) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
        Text(
            text = text,
            textAlign = TextAlign.Left,
            style = style.copy(fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun DebugOutputSection(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    monoStyle: TextStyle
) {
    if (!uiState.isDebugging || uiState.outputLines.isEmpty()) return

    Spacer(modifier = Modifier.height(4.dp))
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
        thickness = 4.dp,
        color = MaterialTheme.colorScheme.tertiary
    )
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(uiState.outputLines.size) { index ->
            DebugOutputItem(uiState.outputLines[index], monoStyle)
        }
    }
}

@Composable
private fun DebugOutputItem(text: String, style: TextStyle) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
        Text(
            text = text,
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth(),
            style = style
        )
    }
}

@Composable
fun SelectableOption(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Row(
        Modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.Checkbox
        ),
        verticalAlignment = Alignment.CenterVertically,

    ) {
        RadioButton(selected = selected,
            onClick = onClick,
            modifier = Modifier.height(25.dp)
        )
        content()
    }
}
