package com.example.module4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_SHOW_REMINDER") {
            showNotification(context)
            scheduleNextReminder(context)
        }
    }

    private fun showNotification(context: Context) {
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setContentTitle("Напоминание о таблетке")
            .setContentText("Время принять таблетку!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1001, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Ежедневные напоминания",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для напоминания о таблетке"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNextReminder(context: Context) {
        val alarmScheduler = AlarmScheduler(context)
        alarmScheduler.scheduleReminder()
    }
}