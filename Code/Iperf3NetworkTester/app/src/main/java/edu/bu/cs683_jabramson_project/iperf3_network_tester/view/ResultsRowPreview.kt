package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import androidx.compose.material3.Surface


@Preview(name = "Results Row Preview")
@Composable
fun ResultsRowPreview(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    monoStyle: TextStyle = mesloMonoTextStyle()
) {

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(start = 10.dp, end = 10.dp).fillMaxWidth()
    ) {
        //if (sampleUiState.isRunning || !uiState.isFinished) return
        //if (sampleUiState.results.isEmpty()) return
        var thick = if (sampleUiState.returnCode != 0) 5.dp else 2.dp
        val resultColor =
            if (sampleUiState.returnCode != 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = thick,
            color = MaterialTheme.colorScheme.tertiary
        )
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(sampleUiState.results.size) { index ->
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


