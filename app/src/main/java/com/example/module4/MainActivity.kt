package com.example.module4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.module4.ui.theme.Module4Theme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box(){
                Task14Screen()
            }
        }
    }
}


@Composable
fun Task14Screen(viewModel: CompassViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val azimuth by viewModel.azimuth.collectAsState()
    val error by viewModel.sensorError.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startSensing(context)
                Lifecycle.Event.ON_PAUSE -> viewModel.stopSensing()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopSensing()
        }
    }

    var smoothAzimuth by remember { mutableStateOf(azimuth) }
    LaunchedEffect(azimuth) {
        val target = azimuth
        val start = smoothAzimuth
        val duration = 200L
        val steps = 20
        for (i in 0..steps) {
            val fraction = i / steps.toFloat()
            smoothAzimuth = start + (target - start) * fraction
            delay(duration / steps)
        }
        smoothAzimuth = target
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Компас",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color.DarkGray,
                            style = Stroke(width = 4f)
                        )
                        drawContext.canvas.save()
                        rotate(degrees = -smoothAzimuth) {
                            // Север
                            drawLine(
                                color = Color.Red,
                                start = Offset(size.width / 2, size.height / 2 - 100f),
                                end = Offset(size.width / 2, size.height / 2 + 100f),
                                strokeWidth = 8f
                            )
                            // Юг
                            drawLine(
                                color = Color.Gray,
                                start = Offset(size.width / 2, size.height / 2 + 100f),
                                end = Offset(size.width / 2, size.height / 2 - 100f),
                                strokeWidth = 8f
                            )
                        }
                        drawContext.canvas.restore()
                    }
                    Text(
                        "N",
                        fontSize = 24.sp,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .rotate(-smoothAzimuth)
                            .padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Азимут: ${azimuth.roundToInt()}°",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}