package com.example.module4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.module4.ui.theme.Module4Theme
import androidx.work.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box() {
                Task8Screen()
            }
        }
    }
}

@Composable
fun Task8Screen() {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    var currentStep by remember { mutableStateOf("Готов к работе") }
    var progress by remember { mutableStateOf(0f) }
    var isWorking by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }

    val workInfo = workManager.getWorkInfosForUniqueWorkLiveData("image_processing")
        .observeAsState(initial = emptyList())

    LaunchedEffect(workInfo.value) {
        workInfo.value?.forEach { info ->
            when (info.state) {
                WorkInfo.State.RUNNING -> {
                    isWorking = true
                    val step = info.progress.getString("STEP") ?: "working"
                    progress = (info.progress.getInt("PROGRESS", 0) / 100f)
                    currentStep = when (step) {
                        "compress" -> "Сжимаем фото..."
                        "watermark" -> "Добавляем водяной знак..."
                        "upload" -> "Загружаем в облако..."
                        else -> "Обработка..."
                    }
                }
                WorkInfo.State.SUCCEEDED -> {
                    if (info.tags.contains("upload")) {
                        val path = info.outputData.getString("UPLOADED_PATH") ?: "unknown"
                        resultMessage = "Готово! Фото загружено: $path"
                        isWorking = false
                        currentStep = "Завершено"
                        progress = 1f
                    }
                }
                WorkInfo.State.FAILED -> {
                    resultMessage = "Ошибка: обработка прервана"
                    isWorking = false
                    currentStep = "Ошибка"
                }
                else -> {}
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
            text = "Обработка фото",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentStep,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp)
                )

                if (isWorking) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = resultMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Button(
            onClick = {
                val compress = OneTimeWorkRequestBuilder<CompressImageWorker>()
                    .setInputData(workDataOf("IMAGE_PATH" to "photo.jpg"))
                    .addTag("compress")
                    .build()

                val watermark = OneTimeWorkRequestBuilder<AddWatermarkWorker>()
                    .addTag("watermark")
                    .build()

                val upload = OneTimeWorkRequestBuilder<UploadImageWorker>()
                    .addTag("upload")
                    .build()

                workManager.beginUniqueWork(
                    "image_processing",
                    ExistingWorkPolicy.REPLACE,
                    compress
                ).then(watermark)
                    .then(upload)
                    .enqueue()

                isWorking = true
                currentStep = "Сжимаем фото..."
                progress = 0f
                resultMessage = ""
            },
            enabled = !isWorking,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Начать обработку и загрузку")
        }
    }
}