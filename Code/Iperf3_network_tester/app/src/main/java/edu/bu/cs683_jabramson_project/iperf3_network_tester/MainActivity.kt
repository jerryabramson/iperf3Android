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
                var iperfBinary: File? = findIperf3Binary(context)
                NavGraph(iperfBinary)
            }
        }
    }
}

@Composable
fun NavGraph(iperf3Binary: File?) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "RUNTIME_ROUTE"
    ) {

        // 1. Only route/screen so far
        composable(route = "RUNTIME_ROUTE") {
            val viewModel: Iperf3RunViewModel = hiltViewModel()


            RunIperf3Screen(iperf3Binary = iperf3Binary, viewModel = viewModel)
        }
    }
}




