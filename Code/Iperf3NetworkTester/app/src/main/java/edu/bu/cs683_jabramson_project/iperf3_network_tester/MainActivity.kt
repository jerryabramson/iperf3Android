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
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.findIperf3Binary
import edu.bu.cs683_jabramson_project.iperf3_network_tester.view.RunIperf3Screen
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.Iperf3RunViewModel
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val context = LocalContext.current
                val iperf3Binary: File? = findIperf3Binary(context)
                val viewModel: Iperf3RunViewModel = hiltViewModel()
                RunIperf3Screen(iperf3Binary = iperf3Binary, viewModel = viewModel)
            }
        }
    }
}




