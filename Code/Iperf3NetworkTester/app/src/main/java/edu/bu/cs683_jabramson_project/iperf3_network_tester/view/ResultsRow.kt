package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Column
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


@Preview(name = "Results Row Preview")
/**
 * Results row for the UI.
 * @param uiState the current state of the UI
 * @param monoStyle the style for the monospaced text
 */
@Composable
fun ResultsRow(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    monoStyle: TextStyle = mesloMonoTextStyle()
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(start = 10.dp, end =  10.dp).fillMaxWidth()
    ) {
        if (uiState.isRunning || !uiState.isFinished) return
        if (uiState.results.isEmpty()) return
        var thick  = if (uiState.returnCode != 0) 5.dp else 2.dp
        val resultColor = if (uiState.returnCode != 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        if (!uiState.results.isEmpty()) {
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
}