package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(name = "Debug Output Section Preview")
@Composable
fun DebugOutputSectionPreview(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    monoStyle: TextStyle = mesloMonoTextStyle()
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
            style = style.copy(fontSize = 13.sp),
        )
    }
}
