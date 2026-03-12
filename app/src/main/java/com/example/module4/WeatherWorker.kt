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
import kotlin.random.Random

class WeatherWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val cityName = inputData.getString("CITY_NAME") ?: return@withContext Result.failure()
                val cityIndex = inputData.getInt("CITY_INDEX", 0)

                setForeground(createForegroundInfo("Загружаем погоду для $cityName..."))

                for (i in 1..5) {
                    delay(600)
                    val progress = i * 20
                    setProgress(workDataOf(
                        "CITY_INDEX" to cityIndex,
                        "CITY_NAME" to cityName,
                        "PROGRESS" to progress,
                        "STATUS" to "loading"
                    ))
                }

                val temperature = Random.nextInt(-5, 25)

                setProgress(workDataOf(
                    "CITY_INDEX" to cityIndex,
                    "CITY_NAME" to cityName,
                    "TEMPERATURE" to temperature,
                    "STATUS" to "done"
                ))

                Result.success(workDataOf(
                    "temp_$cityIndex" to temperature,
                    "city_$cityIndex" to cityName
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
            ForegroundInfo(42, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(42, notification)
        }
    }
}