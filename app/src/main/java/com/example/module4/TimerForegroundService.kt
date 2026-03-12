package com.example.module4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class TimerForegroundService : Service() {

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null
    private var seconds = 0
    private var listeners = mutableListOf<TimerUpdateListener>()

    interface TimerUpdateListener {
        fun onTimerUpdate(seconds: Int)
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startTimer()
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "timer_channel")
            .setContentTitle("Счётчик времени")
            .setContentText("Прошло: 0 секунд")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "timer_channel",
                "Таймер",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Канал для отображения времени таймера"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startTimer() {
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                seconds++
                updateNotification()
                listeners.forEach { it.onTimerUpdate(seconds) }
            }
        }
    }

    private fun updateNotification() {
        val notification = NotificationCompat.Builder(this, "timer_channel")
            .setContentTitle("Счётчик времени")
            .setContentText("Прошло: $seconds секунд")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    fun addListener(listener: TimerUpdateListener) {
        listeners.add(listener)
        listener.onTimerUpdate(seconds)
    }

    fun removeListener(listener: TimerUpdateListener) {
        listeners.remove(listener)
    }

    fun stopTimer() {
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        listeners.clear()
    }
}