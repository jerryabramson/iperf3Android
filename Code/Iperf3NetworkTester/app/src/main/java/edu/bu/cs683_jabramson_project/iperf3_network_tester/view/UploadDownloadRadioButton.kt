package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel

@Composable
fun UploadDownload(
    viewModel: Iperf3RunViewModel,
    isReverse: Boolean,
    style: androidx.compose.ui.text.TextStyle
) {
    val selected = if (isReverse) "Down" else "Up"
    viewModel.setDownload(isReverse)
    Column(horizontalAlignment = Alignment.Start,
        modifier = Modifier.selectableGroup().padding(end=2.dp))
    {
        //Text(text = "Direction", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
        listOf("Down", "Up").forEach { text ->
            SelectableOption(
                selected = text == selected,
                onClick = { viewModel.setUploadDownload(text) }
            ) {
                Text(text = text, style = style, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
