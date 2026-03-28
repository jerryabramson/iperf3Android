package edu.bu.cs683_jabramson_project.iperf3_network_tester.view
// ProcessOutputScreen.kt


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.bu.cs683_jabramson_project.iperf3_network_tester.viewmodel.ProcessRunnerViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessOutputScreen(
    viewModel: ProcessRunnerViewModel = viewModel()   // obtained via naive factory; replace with hiltViewModel() if you use Hilt
) {
    // Collect the flow with lifecycle awareness (starts/stops automatically)
    val lines by viewModel.outputLines.collectAsStateWithLifecycle()
    var isRunning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var returnCode by remember { mutableStateOf(0) }

    val hasInternetPermission = remember {
        context.checkSelfPermission(Manifest.permission.INTERNET) ==
                PackageManager.PERMISSION_GRANTED
    }

    // Launcher that shows the permission dialog
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.w("RunIperf3Screen", "Internet permission denied – test cannot run.")
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Process Output") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Button (re)starts the command – you could also start it automatically in init{}
            Button(
                onClick = {
                    isRunning = true
                    // Example command: list the contents of /sdcard
                    viewModel.runCommand(context,
                        listOf("/bin/iperf3", "--forceflush", "-c", "192.168.1.28"))
                },
                // Disable while a command is running – you could expose a Boolean StateFlow for this.
                enabled = !isRunning   // replace it with a real flag if you need it
            ) {
                Text("Run iperf3 -c 192.168.1.28")
            }


            Spacer(modifier = Modifier.height(8.dp))

            // Show the accumulated lines in a lazy list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)   // take the remaining vertical space
            ) {
                items(lines.size) { index ->
                    Text(
                        text = lines[index],
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
