package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import android.R.attr.enabled
import android.R.attr.label
import android.R.attr.onClick
import android.R.id.toggle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.DefaultUIValues
import androidx.compose.ui.text.input.KeyboardType
import androidx.leanback.transition.TransitionHelper.setDuration
import androidx.lifecycle.viewmodel.compose.viewModel


/**
 * Host input row for the UI.
 * Consists of:
 *   1. Text field for the host name,
 *   2. Numeric field for the duration
 *   3. Settings section - upload/download

 * @param uiState the current state of the UI
 * @param viewModel the view model for the UI
 * @param colors the colors for the text fields
 * @param style the style for the monospaced text
  */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "HostInputRow", showBackground = true, device = "id:pixel_6")
@Composable
fun HostInputRow(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    isRunning: Boolean = false,
    updateHostName: (String) -> Unit = {},
    uploadDownload: (String) -> Unit = {},
    launch: () -> Unit = {},
    setDuration: (String) -> Unit = {},
    setParallelStreams: (String) -> Unit = {},
    setSkip: (String) -> Unit = {},
    colors: androidx.compose.material3.TextFieldColors = textFieldColors(),
    style: TextStyle = mesloMonoTextStyle())
{
    Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            TextField(
                value = uiState.hostName,
                onValueChange = updateHostName,
                enabled = !isRunning,
                placeholder = {
                    Text(
                        DefaultUIValues.HOST_NAME,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                modifier = Modifier.width(width = 200.dp)
                    .padding(end = 4.dp),
                label = {
                    Text(
                        "iPerf3 Server[:port]",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = colors,
                singleLine = true,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { launch() }),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )
            GenericNumericField(
                value = uiState.durationSecs,
                onValueChange = setDuration,
                enabled = !uiState.isRunning,
                placeholder = "10",
                label = "Secs",
                modifier = Modifier.width(width = 65.dp),
                colors = colors
            )
            UploadDownload(uiState = uiState, setUploadDownload = uploadDownload)

        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
        ) {

            GenericNumericField(
                value = uiState.parallelStreams,
                onValueChange = setParallelStreams,
                enabled = !isRunning,
                placeholder = "8",
                label = "Streams",
                modifier = Modifier.padding(end = 4.dp).width(width = 90.dp),
                colors = colors
            )
            GenericNumericField(
                value = uiState.skip,
                onValueChange = setSkip,
                enabled = !isRunning,
                placeholder = "2",
                label = "Omit",
                modifier = Modifier.padding(end = 4.dp).width(width = 80.dp),
                colors = colors,

                )
        }
    }
}

