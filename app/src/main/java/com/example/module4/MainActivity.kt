package com.example.module4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.module4.ui.theme.Module4Theme
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box() {
                Task5Screen()
            }
        }
    }
}

@Composable
fun Task5Screen() {
    val context = LocalContext.current
    var seconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var service by remember { mutableStateOf<TimerForegroundService?>(null) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val timerBinder = binder as TimerForegroundService.TimerBinder
                service = timerBinder.getService()
                service?.addListener(object : TimerForegroundService.TimerUpdateListener {
                    override fun onTimerUpdate(sec: Int) {
                        seconds = sec
                    }
                })
                isRunning = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                isRunning = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isRunning) {
                context.unbindService(connection)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Счётчик времени",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "$seconds",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(32.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, TimerForegroundService::class.java)
                    ContextCompat.startForegroundService(context, intent)
                    Intent(context, TimerForegroundService::class.java).also { intent ->
                        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    }
                },
                enabled = !isRunning
            ) {
                Text("Старт")
            }

            Button(
                onClick = {
                    service?.stopTimer()
                    context.unbindService(connection)
                    isRunning = false
                    seconds = 0
                },
                enabled = isRunning
            ) {
                Text("Стоп")
            }
        }
    }
}