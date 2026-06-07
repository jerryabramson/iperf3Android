// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

//import androidx.hilt.navigation.compose.hiltViewModel
import android.R.attr.thickness
import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.R
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.mesloFontFamily
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.Iperf3OutputMonitor
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.UnitConvertedData
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getHeading
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getHeadingUL
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.toWholeNumber
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.DefaultUIValues
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunIperf3Screen(viewModel: Iperf3RunViewModel = hiltViewModel(
    checkNotNull<ViewModelStoreOwner>(
        LocalViewModelStoreOwner.current
    ) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }, null
)
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val monoStyle = mesloMonoTextStyle()
    val fieldColors = textFieldColors()
    val context = LocalContext.current
    viewModel.setContext(context)


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        //verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(end=20.dp).background(color = MaterialTheme.colorScheme.outlineVariant)
                    )
                    {
                        Text(stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start =10.dp,top = 5.dp,bottom = 5.dp))
                    }
                },
                actions = { RunButton(uiState, viewModel, isRunning = uiState.isRunning, uiState.isFinished) },


            )
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    ProjectBottomBar(uiState, viewModel)
                },
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            /* Input rows */
            HostInputRow(uiState, viewModel, fieldColors, monoStyle)
            Spacer(modifier = Modifier.height(10.dp))

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
private fun RunButton(uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
                      viewModel: Iperf3RunViewModel,
                      isRunning: Boolean,
                      isFinished: Boolean = false) {
    var buttonColor = MaterialTheme.colorScheme.primary
    if (isRunning) buttonColor = MaterialTheme.colorScheme.onErrorContainer
    androidx.compose.material3.Button(
        modifier = Modifier.padding(end = 10.dp),
        shape = MaterialTheme.shapes.large,
        onClick =  viewModel::launchOrCancel,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
        //enabled = !isRunning || isFinished
    ) {
        if (!isRunning) {
            Text(text = "Run", color = MaterialTheme.colorScheme.surface)
        } else {
            Text(text = "Stop", color = MaterialTheme.colorScheme.surface)
        }
    }
}


/**
 * Bottom bar for the UI.
 */
@Composable
private fun ProjectBottomBar(uiState: UiData, viewModel: Iperf3RunViewModel) {
    Row(verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(start = 10.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(end = 10.dp)) {
            Text(
                text = "Mobile Development Directed Study Application",
                fontSize = 10.sp,
            )
            Text(
                text = "Jerold Abramson",
                fontSize = 12.sp,
            )
        }
        DebugOnOffRadioButton(uiState, viewModel)
    }
}


/**
 * Bottom bar for the UI.
 */
//@Preview(name="bottom bar")
@Composable
private fun ProjectBottomBarPreview() {
    Row(verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(start = 10.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(end = 20.dp)) {
            Text(
                text = "Mobile Development Directed Study Application",
                fontSize = 10.sp,
            )
            Text(
                text = "Jerold Abramson",
                fontSize = 8.sp,
            )
        }
        DebugOnOffRadioButtonPreview(true)
    }
}
@Composable
fun mesloMonoTextStyle(): TextStyle = TextStyle(
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
    Column() {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp)
        ) {
            // placeholder = { Text(placeholder) },
            TextField(
                value = uiState.hostName,
                onValueChange = viewModel::updateHostName,
                enabled = !uiState.isRunning,
                placeholder = {
                    Text(
                        DefaultUIValues.HOST_NAME,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                modifier = Modifier.width(width = 150.dp).height(height = 50.dp)
                    .padding(end = 2.dp),
                label = {
                    Text(
                        "iPerf3 Server[:port]",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = colors,
                singleLine = true,
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
            UploadDownload(uiState, viewModel)

        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp)
        ) {

            GenericNumericField(
                value = uiState.parallelStreams,
                onValueChange = viewModel::setParallelStreams,
                enabled = !sampleUiState.isRunning,
                placeholder = "8",
                label = "Streams",
                modifier = Modifier.padding(end = 4.dp).width(width = 90.dp).height(height = 50.dp),
                colors = colors
            )
            GenericNumericField(
                value = uiState.skip,
                onValueChange = viewModel::setSkip,
                enabled = !sampleUiState.isRunning,
                placeholder = "2",
                label = "Omit",
                modifier = Modifier.padding(end = 4.dp).width(width = 80.dp).height(height = 50.dp),
                colors = colors,

                )
        }
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
fun GenericNumericField(
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
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        },
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        colors = colors,
        singleLine = true
    )
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
        val resultColor = if (uiState.returnCode != 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

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
fun progressColors(isReverse: Boolean): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return if (!isReverse) {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.primary
    }
}


@Composable
fun ProgressPercent(uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData) {
    val num = if (uiState.isReverse) 1f - uiState.progress else uiState.progress
    val percent = (num * 100).toInt()

    @SuppressLint("DefaultLocale")
    val iter = String.format("%3d / %3.3s",
        uiState.lineResult.intervalNumber,
        uiState.durationSecs)

    Text(
        text = "${percent}% complete [$iter]",
        modifier = Modifier.fillMaxWidth(),
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun BandwidthDisplay(uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
        text = uiState.lineResult.basicBandWidthString,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        var max = uiState.lineResult.currentMax
        var min = uiState.lineResult.currentMin
        var avg = uiState.lineResult.currentAvg
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "Avg",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "Max",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium
            )

        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = toWholeNumber(min).trim(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = toWholeNumber(avg),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = toWholeNumber(max),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
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
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(start = 1.dp, end = 1.dp),
            thickness = 4.dp,
            color = MaterialTheme.colorScheme.error
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(start = 1.dp, end = 1.dp)) {
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
            style = style.copy(fontSize = 13.sp),
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

        if (uiState.isVerbose || uiState.isDebugging || uiState.errorLines.isNotEmpty() ||
            ((uiState.latestLine.isEmpty() && !uiState.isFinished)))
        {
            Spacer(modifier = Modifier.height(1.dp))
            val defaultColor = if (uiState.returnCode != 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            val defaultThickness = if (uiState.returnCode != 0) 4.dp else 2.dp
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                thickness = defaultThickness,
                color = defaultColor
            )
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
    if (!uiState.isVerbose && !uiState.isDebugging) return

    val fontSize = 14.sp
    val style = monoStyle.copy(fontSize = fontSize)
    Spacer(modifier = Modifier.height(4.dp))
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
        thickness = 4.dp,
        color = MaterialTheme.colorScheme.tertiary
    )
    Spacer(modifier = Modifier.height(4.dp))

    if (uiState.outputLines.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
            Text(
                text = getHeading(),
                textAlign = TextAlign.Left,
                style = style,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = getHeadingUL(),
                textAlign = TextAlign.Left,
                style = style,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
        items(uiState.outputLines.size) { index ->
            DebugOutputItem(uiState.outputLines[index], style)
        }
    }
}

@Composable
fun DebugOutputItem(text: String, style: TextStyle) {
    Row() {
        Text(
            text = text,
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth(),
            style = style,
            color = MaterialTheme.colorScheme.tertiary

        )
    }
}

@Composable
fun SelectableOption(enabled: Boolean = true, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val selectedColor = if (!enabled) androidx.compose.material3.RadioButtonDefaults.colors().disabledSelectedColor else RadioButtonDefaults.colors().selectedColor
    val unSelectedColor = if (!enabled) androidx.compose.material3.RadioButtonDefaults.colors().disabledUnselectedColor else RadioButtonDefaults.colors().unselectedColor
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
            modifier = Modifier.height(25.dp),
            colors = if (selected) RadioButtonDefaults.colors(selectedColor = selectedColor) else RadioButtonDefaults.colors(unselectedColor = unSelectedColor)
        )
        content()
    }
}

val sampleOutputData = listOf(
    "Skipped   0.00-1.00  35.6 Mbits/sec",
    "8 Streams 1.00-2.00  35.6 Mbits/sec",
    "...",
    "...",
    "...",
    "...",
    "sender    0.00-10.19  24.0 Mbits/sec",
    "receiver  0.00-10.19  24.0 Mbits/sec",

).toMutableList()

val sampleIperf3Messages = listOf(
    "🚀 Initiating iPerf3 client request...",
    "Connecting to host jabramson.com, port 5201",
    "Reverse mode, remote host 192.168.127.12 is sending",
    "Local Host/IP: 192.168.1.32",
    "Local Port: 48618",
    "Remote Host/IP: 192.168.127.12",
    "Remote Port: 5201",
    "iperf Done."
).toMutableList()

val sampleStatistics = listOf(
    "Average: 24.40 Mbits/sec",
    "Maximum: 92.58 Mbits/sec",
    "Minimum: 24.00 Mbits/sec",
).toMutableList()

val sampleLineResult = Iperf3OutputMonitor.LineResult(
    currentMax = UnitConvertedData(10.19, "Mbits/sec"),
    currentAvg = UnitConvertedData(24.40, "Mbits/sec"),
    currentMin = UnitConvertedData(24.00, "Mbits/sec"),
    basicBandWidthString = "35.6 Mbits/sec"
)

val sampleErrorLines = listOf(
    "Error: Failed to bind to 192.168.127.12:5201: Address already in use",
).toMutableList()

val sampleUiState = UiData(
    hostName = "jabramson.com",
    durationSecs = "10",
    parallelStreams = "8",
    skip = "2",
    isDebugging = true,
    isVerbose = true,
    isReverse = false,
    isRunning = false,
    isFinished = true,
    iperf3Messages = sampleIperf3Messages,
    bandWidth = "1.00-2.00    35.6 Mbits/sec",
    progress =  0.33.toFloat(),
    results = sampleStatistics,
    outputLines = sampleOutputData,
    lineResult = sampleLineResult,
    errorLines = sampleErrorLines,
    latestLine = "some output"
)





@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Iperf3Screen")
@Composable
fun PreviewIperf3Screen() {
    val monoStyle = mesloMonoTextStyle()
    val fieldColors = textFieldColors()
    Iperf3NetworkTesterTheme() {
        Surface(
            modifier = Modifier.fillMaxSize().padding(5.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            /* Input rows */
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                //verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.onPrimaryContainer).padding(start = 5.dp)

                            )
                            {
                                Text(style = MaterialTheme.typography.titleLarge, text = stringResource(id = R.string.app_name), color = MaterialTheme.colorScheme.surface)
                                //Text(, style = MaterialTheme.typography.titleLarge, Text(text = "Run", color = MaterialTheme.colorScheme.surface))
                            }
                        },
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                //UploadDownloadPreview()
                                RunButtonPreview()
                                //DebugOnOffRadioButtonPreview(true)
                            }
                        },
                    )
                },
                bottomBar = {
                    BottomAppBar(modifier = Modifier.height(50.dp)) {
                        ProjectBottomBarPreview()
                    }
                },
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(start = 10.dp, end = 10.dp)) {
                    HostInputRowPreview(fieldColors)
                    //Spacer(modifier = Modifier.height(10.dp))
                    //StreamsAndDebugRowPreview(fieldColors, monoStyle)

                    /* Output rows */
                    RunningColumnSectionPreview(sampleUiState, monoStyle)
                    ResultsRowPreview(sampleUiState, monoStyle)
                    IperfMessagesSectionPreview(sampleUiState, monoStyle)
                    ErrorSectionPreview(sampleUiState, monoStyle)
                    DebugOutputSection(sampleUiState, monoStyle)
//                    Row(verticalAlignment =Alignment.Bottom) {
//                        DebugOnOffRadioButtonPreview(true)
//                    }

                }
            }
        }
    }
}

/**
 * Preview for the run button.
 */

//@Preview(name = "Run Button Preview")
@Composable
fun RunButtonPreview() {
    val buttonColor = MaterialTheme.colorScheme.primary
    androidx.compose.material3.Button(
        shape = MaterialTheme.shapes.large,
        onClick =  {},
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        //enabled = !isRunning || isFinished
    ) {
        Text(text = "Run", color = MaterialTheme.colorScheme.surface)
    }
}





