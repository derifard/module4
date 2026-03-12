package com.example.module4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val dataStore = ReminderDataStore(context)
                val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("reminder_enabled", false)
                if (enabled) {
                    val alarmScheduler = AlarmScheduler(context)
                    alarmScheduler.scheduleReminder()
                }
            }
        }
    }
}