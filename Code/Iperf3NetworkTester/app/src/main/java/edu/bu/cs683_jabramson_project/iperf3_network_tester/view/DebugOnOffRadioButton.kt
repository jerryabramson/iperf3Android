package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel

@Composable
fun DebugOnOffRadioButton(
    viewModel: Iperf3RunViewModel,
    isDebugging: Boolean,
    isVerbose: Boolean,
    style: androidx.compose.ui.text.TextStyle
) {
    val selected = when {
        isDebugging -> "Trace"
        isVerbose -> "Verbose"
        else -> "Normal"
    }

    Column(horizontalAlignment = Alignment.Start,
        modifier = Modifier.selectableGroup().padding(end=2.dp))
    {
//      Spacer(modifier = Modifier.weight(1f))
      //Text(text = "Debug", style = style, fontSize = 12.sp)
        listOf("Normal", "Verbose", "Trace").forEach { text ->
            SelectableOption(
                selected = text == selected,
                onClick = { viewModel.setDebug(text) }
            ) {
                Text(text = text, style = style, fontSize = 8.sp, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}
