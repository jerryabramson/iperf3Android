package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme


@Preview(name = "Running Column Section")
@Composable
fun RunningColumnSectionPreview(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    monoStyle: TextStyle = mesloMonoTextStyle()
) {
    //if (!uiState.isRunning) return

    Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),) {
        //LaunchingMessage(uiState.latestLine.isEmpty())
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
