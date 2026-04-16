package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel

@Composable
fun DebugOnOffRadioButton(viewModel: Iperf3RunViewModel = hiltViewModel<Iperf3RunViewModel>(),
                          current: Int,
                          style: TextStyle
) {
    val radioOptions = listOf("Off", "Verbose", "Trace")

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[current]) }

    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Row(Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
        Text(text =  "      Debug", style = style, fontSize = 12.sp)
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = { onOptionSelected(text) },
                )
                Text(
                    text = text,
                    style = style,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
        viewModel.setDebug(selectedOption)
    }
}
