package edu.bu.cs683_jabramson_project.iperf3_network_tester.view


import android.R.attr.enabled
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.width

import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel


/**
 * Upload/Download radio buttons for the UI
 * @param viewModel the view model for the UI
 */
@Composable
fun UploadDownload(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData,
    viewModel: Iperf3RunViewModel
) {
    val color = if (uiState.isRunning) androidx.compose.material3.RadioButtonDefaults.colors().disabledSelectedColor else RadioButtonDefaults.colors().selectedColor
    val isReverse = uiState.isReverse
    val selected = if (isReverse) "Download" else "Upload"
    if (uiState.isRunning) viewModel.setDownload(isReverse)
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.selectableGroup().width(width = 130.dp)
    )
    {
        listOf("Download", "Upload").forEach { text ->
            SelectableOption(
                enabled = !uiState.isRunning,
                selected = text == selected,
                onClick = { if (!uiState.isRunning) viewModel.setUploadDownload(text) },
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }
        }
    }
}

@Preview("Upload/Download Radio Button")
@Composable
fun UploadDownloadPreview(isReverse: Boolean = true,  style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall) {
    val selected = if (isReverse) "Download" else "Upload"
    Column(horizontalAlignment = Alignment.Start,
        modifier = Modifier.selectableGroup().width(width = 110.dp))
    {
        listOf("Download", "Upload").forEach { text ->
            SelectableOption(
                selected = text == selected,
                onClick = { {} }
            ) {
                Text(text = text, style = style, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

}