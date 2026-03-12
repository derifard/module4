package com.example.module4

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class WeatherReportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                setForeground(createForegroundInfo("Формируем отчёт..."))

                delay(1500)
                val temps = mutableListOf<Int>()
                val cityNames = mutableListOf<String>()

                for (i in 0..2) {
                    val temp = inputData.getInt("temp_$i", Int.MIN_VALUE)
                    if (temp != Int.MIN_VALUE) {
                        temps.add(temp)
                        cityNames.add(inputData.getString("city_$i") ?: "Город $i")
                    }
                }

                val averageTemp = if (temps.isNotEmpty()) temps.average().toInt() else 0
                val reportText = buildString {
                    append("Отчёт готов!\n")
                    cityNames.zip(temps).forEach { (city, temp) ->
                        append("$city: ${temp}°C\n")
                    }
                    append("Средняя температура: $averageTemp°C")
                }

                setForeground(createForegroundInfo(reportText))

                Result.success(workDataOf(
                    "AVERAGE_TEMP" to averageTemp,
                    "REPORT" to reportText,
                    "CITY_COUNT" to temps.size
                ))
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "weather_channel")
            .setContentTitle("Сбор прогноза погоды")
            .setContentText(progress)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ForegroundInfo(43, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(43, notification)
        }
    }
}