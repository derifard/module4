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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box() {
                Task9Screen()
            }
        }
    }
}

@Composable
fun Task9Screen() {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    var isWorking by remember { mutableStateOf(false) }
    var reportResult by remember { mutableStateOf("") }

    val cities = listOf(
        City("Москва"),
        City("Лондон"),
        City("Нью-Йорк")
    ).let { remember { mutableStateListOf(*it.toTypedArray()) } }

    val workInfos = workManager.getWorkInfosForUniqueWorkLiveData("weather_processing")
        .observeAsState(initial = emptyList())

    LaunchedEffect(workInfos.value) {
        workInfos.value?.forEach { info ->
            when (info.state) {
                WorkInfo.State.RUNNING -> {
                    isWorking = true
                    val cityIndex = info.progress.getInt("CITY_INDEX", -1)
                    val status = info.progress.getString("STATUS")
                    if (cityIndex in cities.indices) {
                        when (status) {
                            "loading" -> cities[cityIndex].status = "loading"
                            "done" -> {
                                val temp = info.progress.getInt("TEMPERATURE", 0)
                                cities[cityIndex].temperature = temp
                                cities[cityIndex].status = "done"
                            }
                        }
                    }
                }
                WorkInfo.State.SUCCEEDED -> {
                    if (info.tags.contains("report")) {
                        val report = info.outputData.getString("REPORT") ?: "Готово"
                        reportResult = report
                        isWorking = false
                    } else {
                        val allData = info.outputData.keyValueMap
                        for (i in cities.indices) {
                            if (allData.containsKey("temp_$i")) {
                                val temp = allData["temp_$i"] as? Int ?: continue
                                cities[i].temperature = temp
                                cities[i].status = "done"
                            }
                        }
                    }
                }
                WorkInfo.State.FAILED -> {
                    reportResult = "Ошибка при загрузке данных"
                    isWorking = false
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
            text = "Прогноз погоды",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        cities.forEach { city ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(city.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            city.temperature != null -> "${city.temperature}°C"
                            city.status == "loading" -> "загрузка..."
                            else -> "ожидание"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isWorking) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (reportResult.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = reportResult,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Button(
            onClick = {
                workManager.cancelUniqueWork("weather_processing")

                val cityWorkers = cities.mapIndexed { index, city ->
                    OneTimeWorkRequestBuilder<WeatherWorker>()
                        .setInputData(workDataOf(
                            "CITY_NAME" to city.name,
                            "CITY_INDEX" to index
                        ))
                        .addTag("city_$index")
                        .build()
                }

                val reportWorker = OneTimeWorkRequestBuilder<WeatherReportWorker>()
                    .addTag("report")
                    .build()

                workManager.beginUniqueWork(
                    "weather_processing",
                    ExistingWorkPolicy.REPLACE,
                    listOf(cityWorkers[0], cityWorkers[1], cityWorkers[2])
                ).then(reportWorker).enqueue()

                isWorking = true
                reportResult = ""
                cities.forEach {
                    it.temperature = null
                    it.status = "pending"
                }
            },
            enabled = !isWorking,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Собрать прогноз")
        }
    }
}