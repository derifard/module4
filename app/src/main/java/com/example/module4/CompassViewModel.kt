package com.example.module4

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CompassViewModel : ViewModel(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _sensorError = MutableStateFlow<String?>(null)
    val sensorError: StateFlow<String?> = _sensorError.asStateFlow()

    fun startSensing(context: Context) {
        if (sensorManager != null) return // уже запущен

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            _sensorError.value = "Устройство не поддерживает датчик ориентации"
            return
        }

        sensorManager?.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        _sensorError.value = null
    }

    fun stopSensing() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val normalized = (azimuthDeg + 360) % 360
            _azimuth.value = normalized
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // не используется
    }

    override fun onCleared() {
        stopSensing()
        super.onCleared()
    }
}