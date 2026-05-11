package edu.bu.cs683_jabramson_project.iperf3_network_tester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester.view.RunIperf3Screen
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Iperf3NetworkTesterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val viewModel: Iperf3RunViewModel = hiltViewModel()
                    RunIperf3Screen(viewModel = viewModel)
                }
            }
        }
    }
}




