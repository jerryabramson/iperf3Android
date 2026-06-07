package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme


@Preview(name = "Iperf Messages Section")
@Composable
fun IperfMessagesSectionPreview(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    monoStyle: TextStyle = mesloMonoTextStyle()
) {
    Column() {

        if (uiState.isVerbose || uiState.isDebugging || uiState.errorLines.isNotEmpty() ||
            ((uiState.latestLine.isEmpty() && !uiState.isFinished))
        ) {
            Spacer(modifier = Modifier.height(1.dp))
            val defaultColor =
                if (uiState.returnCode != 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            val defaultThickness = if (uiState.returnCode != 0) 4.dp else 2.dp
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
                thickness = defaultThickness,
                color = defaultColor
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(uiState.iperf3Messages.size) { index ->
                    IperfMessageItemPreview(uiState.iperf3Messages[index], monoStyle)
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
private fun IperfMessageItemPreview(text: String = "", style: TextStyle = mesloMonoTextStyle()) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
        Text(
            text = text,
            textAlign = TextAlign.Left,
            style = style.copy(fontSize = 13.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}