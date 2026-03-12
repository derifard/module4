package com.example.module4

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.module4.ui.theme.Module4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box() {
                Task7Screen()
            }
        }
    }
}

@Composable
fun Task7Screen() {
    val context = LocalContext.current
    var currentNumber by remember { mutableStateOf(0) }
    var isBound by remember { mutableStateOf(false) }
    var service by remember { mutableStateOf<RandomNumberBoundService?>(null) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val randomBinder = binder as RandomNumberBoundService.RandomBinder
                service = randomBinder.getService()
                service?.addListener(object : RandomNumberBoundService.NumberUpdateListener {
                    override fun onNumberUpdate(number: Int) {
                        currentNumber = number
                    }
                })
                currentNumber = service?.getCurrentNumber() ?: 0
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                isBound = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
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
            text = "Генератор случайных чисел",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBound) "$currentNumber" else "---",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }

        Text(
            text = if (isBound) "Сервис подключен" else "Сервис отключен",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isBound) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, RandomNumberBoundService::class.java)
                    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                },
                enabled = !isBound
            ) {
                Text("Подключиться")
            }

            Button(
                onClick = {
                    if (isBound) {
                        context.unbindService(connection)
                        service = null
                        isBound = false
                        currentNumber = 0
                    }
                },
                enabled = isBound
            ) {
                Text("Отключиться")
            }
        }
    }
}