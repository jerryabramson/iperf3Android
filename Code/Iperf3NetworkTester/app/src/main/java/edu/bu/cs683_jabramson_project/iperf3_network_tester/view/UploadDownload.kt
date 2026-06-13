package edu.bu.cs683_jabramson_project.iperf3_network_tester.view


import android.R
import android.R.attr.onClick
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton


import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel


/**
 * Upload/Download radio buttons for the UI
 * @param viewModel the view model for the UI
 */
@Preview("Upload/Download Radio Button", showBackground = true, device = "id:pixel_6")
@Composable
fun UploadDownload(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    setUploadDownload: (String) -> Unit = {})
{
    val color = if (uiState.isRunning) androidx.compose.material3.RadioButtonDefaults.colors().disabledSelectedColor else RadioButtonDefaults.colors().selectedColor
    val isReverse = uiState.isReverse
    val selected = if (isReverse) "Download" else "Upload"
    if (uiState.isRunning) setUploadDownload(selected)
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.selectableGroup()
    )
    {
        listOf("Download", "Upload").forEach { text ->
            SelectableOption(
                enabled = !uiState.isRunning,
                selected = text == selected,
                onClick = { if (!uiState.isRunning) setUploadDownload(text) }
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

@Composable
fun SelectableOption(enabled: Boolean = true, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val selectedColor = if (!enabled) androidx.compose.material3.RadioButtonDefaults.colors().disabledSelectedColor else RadioButtonDefaults.colors().selectedColor
    val unSelectedColor = if (!enabled) androidx.compose.material3.RadioButtonDefaults.colors().disabledUnselectedColor else RadioButtonDefaults.colors().unselectedColor
    Row(
        Modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.Checkbox
        ),
        verticalAlignment = Alignment.CenterVertically,

        ) {
        RadioButton(selected = selected,
            onClick = onClick,
            modifier = Modifier.height(25.dp),
            colors = if (selected) RadioButtonDefaults.colors(selectedColor = selectedColor) else RadioButtonDefaults.colors(unselectedColor = unSelectedColor)
        )
        content()
    }
}
