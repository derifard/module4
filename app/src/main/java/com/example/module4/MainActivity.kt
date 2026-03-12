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
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box() {
                Task11Screen()
            }
        }
    }
}
@Composable
fun Task11Screen() {
    val context = LocalContext.current
    val dataStore = remember { ReminderDataStore(context) }
    val alarmScheduler = remember { AlarmScheduler(context) }
    val scope = rememberCoroutineScope()

    var isEnabled by remember { mutableStateOf(false) }
    var nextReminderText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        dataStore.isEnabled.collectLatest { enabled ->
            isEnabled = enabled
            if (enabled) {
                nextReminderText = alarmScheduler.getNextReminderTime()
            } else {
                nextReminderText = ""
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Напоминание о таблетке",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) Color.Green else Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEnabled) "Включено" else "Выключено",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (isEnabled && nextReminderText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Следующее напоминание: $nextReminderText",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    val newState = !isEnabled
                    dataStore.setEnabled(newState)
                    if (newState) {
                        alarmScheduler.scheduleReminder()
                    } else {
                        alarmScheduler.cancelReminder()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (isEnabled) "Выключить напоминание" else "Включить напоминание",
                fontSize = 18.sp
            )
        }

        if (isEnabled) {
            Text(
                text = "Уведомление будет приходить каждый день в 20:00",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}