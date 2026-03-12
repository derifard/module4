package com.example.module4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerBackgroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("TIMER_SERVICE", "onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val seconds = intent?.getIntExtra("SECONDS", 0) ?: 0

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            for (i in seconds downTo 1) {
                Log.d("TIMER_SERVICE", "Осталось: $i сек")
                delay(1000)
            }
            Log.d("TIMER_SERVICE", "Таймер завершён, показываем уведомление")
            showNotification(seconds)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "timer_background_channel",
                "Фоновый таймер",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о завершении фонового таймера"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(seconds: Int) {
        val notification = NotificationCompat.Builder(this, "timer_background_channel")
            .setContentTitle("Таймер завершён!")
            .setContentText("Время вышло! Прошло $seconds секунд")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
    }
}