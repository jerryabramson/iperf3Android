package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(name = "Error Section Preview")
@Composable
fun ErrorSection(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    monoStyle: TextStyle = mesloMonoTextStyle()
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

