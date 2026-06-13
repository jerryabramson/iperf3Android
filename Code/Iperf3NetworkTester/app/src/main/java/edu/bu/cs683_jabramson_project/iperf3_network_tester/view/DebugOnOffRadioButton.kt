package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import android.R.attr.end
import android.R.attr.onClick
import android.os.Trace
import android.service.controls.templates.ToggleTemplate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Toggle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel

@Preview(name ="DebugOnOffRadioButtonPreview")
@Composable
fun DebugOnOffRadioButton(
    uiState: edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.UiData = sampleUiState,
    toggleDebug: () -> Unit = {}
) {
    val buttonColor = if (uiState.isDebugging) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.End) {
        Text(text = "iperf3 Output",
            modifier = Modifier.padding(end = 10.dp),
            style = MaterialTheme.typography.bodySmall)
        Button(
            onClick =  toggleDebug, // 1. Flip state on click
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(1.dp)//.width(240.dp).height(50.dp)
        ) {
            if (uiState.isDebugging) {
                Text(text = "Turn Off",
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.labelSmall)
            } else {
                Text(text = "Turn On",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
