
package edu.bu.cs683_jabramson_project.iperf3_network_tester.view



import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow

import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


// ======================
// HELPER: DIRECTIONAL PROGRESS INDICATOR
// ======================
@Composable
fun NetworkProgressIndicator(
    progress: Float, // 0.0 to 1.0
    isDownload: Boolean = true, // true = download (data IN), false = upload (data OUT)
    modifier: Modifier = Modifier,
    arrowSize: Dp = 20.dp,
    progressBarHeight: Dp = 4.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    arrowColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var widthDp by remember { mutableStateOf(0f) } // Store width in dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(progressBarHeight)
    ) {
        // Base progress indicator (direction depends on data flow)
        if (isDownload) {
            // DOWNLOAD: Data flows NETWORK → DEVICE (right → left)
            // Visual: Filled portion grows FROM RIGHT TO LEFT
            ReverseLinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                indicatorColor = indicatorColor,
                trackColor = trackColor
            )
        } else {
            // UPLOAD: Data flows DEVICE → NETWORK (left → right)
            // Visual: Filled portion grows FROM LEFT TO RIGHT
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                trackColor = trackColor
            )
        }

        // Directional arrow positioned at progress point
        val arrowCenterX =
            if (isDownload)
                widthDp * (1 - progress) // Download: arrow moves RIGHT→LEFT
            else
                widthDp * progress       // Upload: arrow moves LEFT→RIGHT

        // Clamp arrow position to prevent clipping (all calculations in dp)
        val clampedOffset = arrowCenterX.coerceIn(
            arrowSize.value / 2,
            widthDp.toInt() - (arrowSize.value / 2)
        )

        // Vertical offset to center arrow on progress bar
        val verticalOffset = ((progressBarHeight - arrowSize) / 2)

        Icon(
            imageVector = if (isDownload) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
            contentDescription = if (isDownload)
                "Download progress: ${(progress * 100).toInt()}%"
            else
                "Upload progress: ${(progress * 100).toInt()}%",
            modifier = Modifier
                .size(arrowSize)
                .offset(
                    x = (clampedOffset - (arrowSize.value / 2)).dp, // Convert center offset to top-left
                    y = verticalOffset.value.dp
                )
                .wrapContentSize(align = Alignment.TopStart)
            ,
            tint = arrowColor
        )
    }
}

// ======================
// HELPER: REVERSE PROGRESS INDICATOR (for download)
// ======================
@Composable
private fun ReverseLinearProgressIndicator(
    progress: Float, // 0.0 to 1.0 (0% to 100%)
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    strokeWidth: Dp = 4.dp
) {
    val reversedProgress = 1f - progress // Critical: Invert progress for RTL effect
    LinearProgressIndicator(
        progress = reversedProgress,
        modifier = modifier
            .wrapContentWidth()
    )
}
