package edu.bu.cs683_jabramson_project.iperf3_network_tester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.bu.cs683_jabramson_project.iperf3_network_tester.model.Iperf3Parameters
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getIperf3Binary
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
                var iperfBinary: File? = getIperf3Binary(context)
                if (iperfBinary == null) {
                    // We have a simulation in this scenario now
                    iperfBinary = File("/bin/iperf3")
                }
                NavGraph(iperfBinary)
            }
        }
    }
}

@Composable
fun NavGraph(iperf3Binary: File) {

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "PROJECTS_ROUTE"
    ) {

        // 1. Projects list
        composable(route = "PROJECTS_ROUTE") {
            val viewModel: Iperf3RunViewModel = hiltViewModel()

            RunIperf3Screen(
                iperf3Parameters = Iperf3Parameters(iperf3Binary = iperf3Binary),
                viewModel = viewModel
            )
        }
    }
}




