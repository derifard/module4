package com.example.module4

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.*

class RandomNumberBoundService : Service() {

    private val binder = RandomBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var numberJob: Job? = null
    private var currentNumber = 0
    private var listeners = mutableListOf<NumberUpdateListener>()

    interface NumberUpdateListener {
        fun onNumberUpdate(number: Int)
    }

    inner class RandomBinder : Binder() {
        fun getService(): RandomNumberBoundService = this@RandomNumberBoundService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        startGeneratingNumbers()
    }

    private fun startGeneratingNumbers() {
        numberJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                currentNumber = (0..100).random()
                listeners.forEach { it.onNumberUpdate(currentNumber) }
            }
        }
    }

    fun addListener(listener: NumberUpdateListener) {
        listeners.add(listener)
        listener.onNumberUpdate(currentNumber)
    }

    fun removeListener(listener: NumberUpdateListener) {
        listeners.remove(listener)
    }

    fun getCurrentNumber(): Int = currentNumber

    override fun onUnbind(intent: Intent?): Boolean {
        listeners.clear()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        numberJob?.cancel()
        serviceScope.cancel()
        listeners.clear()
    }
}