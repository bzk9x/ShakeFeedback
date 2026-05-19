package com.bzk9x.shakefeedback.data_source

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.bzk9x.shakefeedback.data.SensorData

/**
 * Hardware Acquisition Layer: Continuous polling of the device's accelerometer.
 * This layer is completely agnostic to what a "shake" is—it simply acts as
 * a conduit for raw physics data from the accelerometer sensor.
 *
 * Responsibilities:
 * - Bind to the system's SensorManager
 * - Request accelerometer data stream
 * - Extract (x, y, z) floating-point values in m/s²
 * - Push raw data to listeners
 */
class AccelerometerDataSource(context: Context) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val listeners = mutableSetOf<SensorDataListener>()
    private var isListening = false

    /**
     * Start listening to accelerometer data.
     * Data will be streamed to all registered listeners at the sensor's native refresh rate.
     */
    fun start() {
        if (isListening || accelerometer == null) return

        isListening = sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME  // ~16ms updates, suitable for motion detection
        )

        if (!isListening) {
            notifyListenersOfError("Failed to register accelerometer listener")
        }
    }

    /**
     * Stop listening to accelerometer data.
     * This should always be called to prevent battery drain.
     */
    fun stop() {
        if (!isListening) return
        sensorManager.unregisterListener(this, accelerometer)
        isListening = false
    }

    /**
     * Register a listener to receive sensor data streams.
     */
    fun addListener(listener: SensorDataListener) {
        listeners.add(listener)
    }

    /**
     * Unregister a listener from receiving sensor data.
     */
    fun removeListener(listener: SensorDataListener) {
        listeners.remove(listener)
    }

    /**
     * Check if the sensor is currently active and streaming data.
     */
    fun isActive(): Boolean = isListening

    /**
     * SensorEventListener implementation: receives raw acceleration data.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val sensorData = SensorData(
            x = event.values[0],
            y = event.values[1],
            z = event.values[2],
            timestamp = System.currentTimeMillis()
        )

        // Broadcast to all listeners
        listeners.forEach { it.onSensorDataReceived(sensorData) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed for accuracy changes in shake detection
    }

    private fun notifyListenersOfError(error: String) {
        listeners.forEach { it.onSensorError(error) }
    }
}
