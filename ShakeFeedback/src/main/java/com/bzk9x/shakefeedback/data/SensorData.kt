package com.bzk9x.shakefeedback.data

/**
 * Raw sensor reading from the accelerometer.
 * All values in meters per second squared (m/s²).
 */
data class SensorData(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculates the magnitude (Euclidean norm) of the acceleration vector.
     * This represents the total kinetic force regardless of device orientation.
     */
    fun magnitude(): Float {
        return kotlin.math.sqrt(x * x + y * y + z * z)
    }
}
