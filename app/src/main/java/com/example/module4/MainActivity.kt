package com.example.module4

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                Task6Screen()
            }
        }
    }
}

@Composable
fun Task6Screen() {
    val context = LocalContext.current
    var secondsInput by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Одноразовый таймер",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = secondsInput,
            onValueChange = { secondsInput = it.filter { char -> char.isDigit() } },
            label = { Text("Количество секунд") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isRunning
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val seconds = secondsInput.toIntOrNull() ?: 0
                if (seconds > 0) {
                    isRunning = true
                    val intent = Intent(context, TimerBackgroundService::class.java).apply {
                        putExtra("SECONDS", seconds)
                    }
                    context.startService(intent)
                }
            },
            enabled = !isRunning && secondsInput.isNotBlank()
        ) {
            Text("Запустить таймер")
        }

        if (isRunning) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Таймер запущен",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}