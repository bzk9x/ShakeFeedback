package com.bzk9x.shakefeedback.data_source

import com.bzk9x.shakefeedback.data.SensorData

/**
 * Interface for receiving raw accelerometer data from the Hardware Acquisition Layer.
 * Implementations should handle incoming sensor readings with minimal processing.
 */
interface SensorDataListener {
    /**
     * Called when new accelerometer data is available.
     * This represents raw (x, y, z) acceleration vectors in m/s².
     */
    fun onSensorDataReceived(sensorData: SensorData)

    /**
     * Called when the sensor listener encounters an error or becomes unavailable.
     */
    fun onSensorError(error: String)
}
