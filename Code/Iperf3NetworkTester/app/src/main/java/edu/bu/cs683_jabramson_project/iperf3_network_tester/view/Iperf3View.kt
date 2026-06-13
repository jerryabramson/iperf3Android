// app/src/main/java/edu/bu/cs683_jabramson_project/iperf3_network_tester/ui/Iperf3View.kt
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

//import androidx.hilt.navigation.compose.hiltViewModel
import android.R.attr.thickness
import android.annotation.SuppressLint
import android.renderscript.Sampler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import edu.bu.cs683_jabramson_project.iperf3_network_tester.view.sampleUiState


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
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.onPrimaryContainer).padding(start = 10.dp, end =10.dp)

                    )
                    {
                        Text(style = MaterialTheme.typography.titleLarge, text = stringResource(id = R.string.app_name), color = MaterialTheme.colorScheme.surface)
                        //Text(, style = MaterialTheme.typography.titleLarge, Text(text = "Run", color = MaterialTheme.colorScheme.surface))
                    }
                },
                actions = {
                    RunButton(uiState,
                        isRunning = uiState.isRunning,
                        uiState.isFinished,
                        buttonAction = viewModel::launchOrCancel)
                }



            )
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    ProjectBottomBar(uiState, viewModel::toggleDebug)
                },
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(start = 10.dp, end = 10.dp)) {

            /* Input rows */
            HostInputRow(
                uiState = uiState,
                isRunning =  uiState.isRunning,
                uploadDownload = viewModel::setUploadDownload,
                updateHostName =  viewModel::updateHostName,
                launch =  viewModel::launchOrCancel,
                setDuration = viewModel::setDuration,
                setParallelStreams = viewModel::setParallelStreams,
                setSkip = viewModel::setSkip,
                colors =  fieldColors,
                style = monoStyle
            )
            Spacer(modifier = Modifier.height(10.dp))

            /* Output rows */
            ResultsRow(uiState = uiState, monoStyle = monoStyle)
            RunningColumnSection(uiState, monoStyle)
            IperfMessagesSection(uiState = uiState, monoStyle = monoStyle)
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
                      isRunning: Boolean,
                      isFinished: Boolean = false,
                      buttonAction: () -> Unit = {})
{
    var buttonColor = MaterialTheme.colorScheme.primary
    if (isRunning) buttonColor = MaterialTheme.colorScheme.onErrorContainer
    androidx.compose.material3.Button(
        modifier = Modifier.padding(end = 10.dp),
        shape = MaterialTheme.shapes.large,
        onClick =  buttonAction, //viewModel::launchOrCancel,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
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
private fun ProjectBottomBar(uiState: UiData = sampleUiState,
                             toggleDebug: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(start = 10.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(end = 10.dp)) {
            Text(
                text = "Mobile Development Directed Study",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "Jerold Abramson",
                style = MaterialTheme.typography.labelSmall
            )
        }
        DebugOnOffRadioButton(uiState, toggleDebug)
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
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
        },
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors = colors,
        singleLine = true
    )
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
fun BandwidthDisplay(uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState) {
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
        val med = uiState.lineResult.currentMedian
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium,
            )
//            Text(
//                text = "Mean",
//                color = MaterialTheme.colorScheme.secondary,
//                style = MaterialTheme.typography.labelMedium
//            )
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
                style = MaterialTheme.typography.bodyMedium
            )
//            Text(
//                text = toWholeNumber(med),
//                color = MaterialTheme.colorScheme.primary,
//                style = MaterialTheme.typography.bodyMedium
//            )
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
            DebugOutputItem(uiState.outputLines[uiState.outputLines.size - index - 1], style)
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






@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Iperf3Screen", showBackground = true, device = "id:pixel_9")
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
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.onPrimaryContainer).padding(start = 10.dp, end =10.dp)

                            )
                            {
                                Text(style = MaterialTheme.typography.titleLarge, text = stringResource(id = R.string.app_name), color = MaterialTheme.colorScheme.surface)
                                //Text(, style = MaterialTheme.typography.titleLarge, Text(text = "Run", color = MaterialTheme.colorScheme.surface))
                            }
                        },
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RunButton(sampleUiState,
                                    false,
                                    false,
                                    buttonAction = {})
                            }
                        },
                    )
                },
                bottomBar = {
                    BottomAppBar() {
                        ProjectBottomBar()
                    }
                },
            ) { padding ->
                Column(modifier = Modifier.padding(padding).padding(start = 2.dp, end = 2.dp)) {
                    //HostInputRowPreview(fieldColors)
                    HostInputRow(uiState =  sampleUiState,
                        isRunning = false,
                        updateHostName = {},
                        uploadDownload = {},
                        launch = {},
                        setDuration = {},
                        setParallelStreams = {},
                        setSkip = {},
                        colors = fieldColors,
                        style = monoStyle)
                    Spacer(modifier = Modifier.height(10.dp))

                    /* Output rows */
                    RunningColumnSection(sampleUiState, monoStyle)
                    ResultsRow(sampleUiState, monoStyle)
                    IperfMessagesSection(sampleUiState, monoStyle)
                    ErrorSection(sampleUiState, monoStyle)
                    DebugOutputSection(sampleUiState, monoStyle)
                }
            }
        }
    }
}

/**
 * Preview for the run button.
 */





