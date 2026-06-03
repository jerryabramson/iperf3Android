package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import android.R.attr.label
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.DefaultUIValues
import androidx.compose.material3.Surface
import org.w3c.dom.Text


@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Host Input Row")
@Composable
fun HostInputRowPreview(colors: androidx.compose.material3.TextFieldColors = textFieldColors(), monoStyle: TextStyle = mesloMonoTextStyle()) {

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(start = 2.dp).fillMaxWidth()
    ) {
        // placeholder = { Text(placeholder) },
        TextField(
            value = sampleUiState.hostName,
            textStyle = MaterialTheme.typography.bodySmall,
            onValueChange = {},
            enabled = !sampleUiState.isRunning,
            placeholder = {
                Text(
                    DefaultUIValues.HOST_NAME,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            },
            modifier = Modifier.width(width = 160.dp).height(height = 50.dp)
                .padding(end = 4.dp),
            label = {
                Text(
                    "iPerf3 Server[:port]",
                    style = monoStyle.copy(fontSize = 10.sp),
                )
            },
            colors = colors,
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { {} })
        )
        GenericNumericField(
            value = sampleUiState.durationSecs,
            onValueChange = {},
            enabled = !sampleUiState.isRunning,
            placeholder = "10",
            label = "Time",
            modifier = Modifier.width(width = 70.dp).height(height = 50.dp)
                .padding(end = 4.dp),
            colors = colors
        )
        GenericNumericField(
            value = sampleUiState.parallelStreams,
            onValueChange = {},
            enabled = !sampleUiState.isRunning,
            placeholder = "8",
            label = "Streams",
            modifier = Modifier.width(width = 90.dp).height(height = 50.dp)
                .padding(end = 4.dp),
            colors = colors
        )
        GenericNumericField(
            value = sampleUiState.skip,
            onValueChange = {},
            enabled = !sampleUiState.isRunning,
            placeholder = "2",
            label = "Omit",
            modifier = Modifier.width(width = 70.dp).height(height = 50.dp)
                .padding(end = 4.dp),
            colors = colors,

        )

    }
}