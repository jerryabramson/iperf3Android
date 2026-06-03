package edu.bu.cs683_jabramson_project.iperf3_network_tester.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.leanback.widget.Row
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme


@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Streams and Debug Row")
@Composable
fun StreamsAndDebugRowPreview (colors: androidx.compose.material3.TextFieldColors = textFieldColors(), monoStyle: TextStyle = mesloMonoTextStyle()) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp)) {
//        GenericNumericField(
//            value = sampleUiState.parallelStreams,
//            onValueChange = {},
//            enabled = !sampleUiState.isRunning,
//            placeholder = "8",
//            label = "Streams",
//            modifier = Modifier.width(width = 90.dp).height(height = 50.dp)
//                .padding(end = 10.dp),
//            colors = colors
//        )
//        GenericNumericField(
//            value = sampleUiState.skip,
//            onValueChange = {},
//            enabled = !sampleUiState.isRunning,
//            placeholder = "2",
//            label = "Omit",
//            modifier = Modifier.width(width = 80.dp).height(height = 50.dp)
//                .padding(end = 10.dp),
//            colors = colors
//        )
//        //Spacer(modifier = Modifier.width(90.dp))
        //DebugOnOffRadioButtonPreview(
          //  sampleUiState.isDebugging,
            //sampleUiState.isVerbose,
            //monoStyle
        //)
    }
}
