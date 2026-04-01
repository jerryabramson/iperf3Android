package edu.bu.cs683_jabramson_project.iperf3_network_tester

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import edu.bu.cs683_jabramson_project.iperf3_network_tester.ui.theme.Iperf3NetworkTesterTheme
import edu.bu.cs683_jabramson_project.iperf3_network_tester.utils.getIperf3Binary
import edu.bu.cs683_jabramson_project.iperf3_network_tester.view.ProcessOutputScreen
import java.io.File


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val context = LocalContext.current
            var iperfBinary: File? = getIperf3Binary(context)
            if (iperfBinary == null) {
                StubbedIperf3Screen()
            } else {
                RunIperf3Screen(iperfBinary)
            }
            //ProcessOutputScreen()
        }
    }
}


