package com.bzk9x.shakefeedback.signal_processing

import com.bzk9x.shakefeedback.config.ShakeFeedbackConfig
import com.bzk9x.shakefeedback.data.SensorData
import com.bzk9x.shakefeedback.data.ShakeEvent
import com.bzk9x.shakefeedback.data_source.SensorDataListener
import kotlin.math.abs

/**
 * Signal Processing Layer: Mathematical filtering and shake validation.
 * This is the "brain" of the framework. It receives raw (x, y, z) streams
 * from the Hardware Layer and runs them through a mathematical pipeline.
 *
 * The pipeline includes:
 * 1. Magnitude Calculation (Euclidean norm)
 * 2. Gravity Isolation (High-Pass Filter)
 * 3. Noise Reduction (Exponential Moving Average)
 * 4. Threshold & Debounce Logic
 *
 * Responsibilities:
 * - Calculate total kinetic force regardless of device orientation
 * - Isolate human-applied force (remove gravity influence)
 * - Smooth noisy sensor data to prevent false positives
 * - Validate that acceleration crosses threshold multiple times within a time window
 * - Implement debounce to prevent duplicate triggers
 */
class ShakeDetector(private val config: ShakeFeedbackConfig) : SensorDataListener {

    private val listeners = mutableSetOf<ShakeDetectionListener>()

    // State tracking for the signal pipeline
    private var previousMagnitude: Float = 0f
    private var smoothedMagnitude: Float = 0f
    private var lastShakeTime: Long = 0L
    private val thresholdCrossingTimestamps = mutableListOf<Long>()

    /**
     * Register a listener to receive shake detection events.
     */
    fun addListener(listener: ShakeDetectionListener) {
        listeners.add(listener)
    }

    /**
     * Unregister a listener from shake detection events.
     */
    fun removeListener(listener: ShakeDetectionListener) {
        listeners.remove(listener)
    }

    /**
     * Reset the internal state of the detector.
     * Useful for testing or when changing configurations.
     */
    fun reset() {
        previousMagnitude = 0f
        smoothedMagnitude = 0f
        lastShakeTime = 0L
        thresholdCrossingTimestamps.clear()
    }

    /**
     * Implementation of SensorDataListener: Processes incoming raw accelerometer data.
     */
    override fun onSensorDataReceived(sensorData: SensorData) {
        // === STEP A: Magnitude Calculation ===
        // Calculate the Euclidean norm (total force magnitude) regardless of device orientation
        val currentMagnitude = sensorData.magnitude()

        // === STEP B: Gravity Isolation (High-Pass Filter) ===
        // Subtract the previous state to isolate only human-applied force
        // This naturally filters out the constant 9.81 m/s² gravity component
        val gravityCorrectedMagnitude = abs(currentMagnitude - previousMagnitude)

        // === STEP C: Noise Reduction (Low-Pass Filter) ===
        // Apply Exponential Moving Average to smooth noisy sensor data
        // EMA = α × currentValue + (1 - α) × previousSmoothedValue
        smoothedMagnitude = (config.smoothingFactor * gravityCorrectedMagnitude) +
                ((1 - config.smoothingFactor) * smoothedMagnitude)

        // === STEP D: Threshold & Debounce Logic ===
        val currentTime = System.currentTimeMillis()

        // Check if we're still within debounce window
        if (currentTime - lastShakeTime < config.debounceInterval) {
            previousMagnitude = currentMagnitude
            return  // Ignore events during debounce period
        }

        // Check if smoothed signal exceeds threshold
        if (smoothedMagnitude > config.shakeThreshold) {
            recordThresholdCrossing(currentTime)
            validateAndEmitShake(currentTime)
        } else {
            // Clean up old threshold crossings outside the time window
            thresholdCrossingTimestamps.removeAll { currentTime - it > config.shakeDurationThreshold }
        }

        previousMagnitude = currentMagnitude
    }

    override fun onSensorError(error: String) {
        // Log or handle sensor errors if needed
    }

    /**
     * Record a timestamp when the signal crosses the threshold.
     */
    private fun recordThresholdCrossing(timestamp: Long) {
        thresholdCrossingTimestamps.add(timestamp)
        // Keep only crossings within the duration window
        thresholdCrossingTimestamps.removeAll { timestamp - it > config.shakeDurationThreshold }
    }

    /**
     * Validate that we have enough threshold crossings within the time window,
     * then emit a shake event if validation passes.
     */
    private fun validateAndEmitShake(timestamp: Long) {
        // Ensure we have the minimum number of threshold crossings
        if (thresholdCrossingTimestamps.size >= config.minimumThresholdCrossings) {
            lastShakeTime = timestamp
            thresholdCrossingTimestamps.clear()

            // Emit the validated shake event
            val shakeEvent = ShakeEvent(timestamp = timestamp)
            listeners.forEach { it.onShakeDetected(shakeEvent) }
        }
    }
}
