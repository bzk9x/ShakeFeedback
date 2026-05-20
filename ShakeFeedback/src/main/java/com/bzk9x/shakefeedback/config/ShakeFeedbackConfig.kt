package com.bzk9x.shakefeedback.config

/**
 * Configuration object for the ShakeFeedback library.
 * Provides sensible defaults but allows complete customization via builder pattern.
 */
data class ShakeFeedbackConfig(
    /**
     * Threshold acceleration in m/s² required to trigger a shake.
     * Default 30.0f is the optimal sweet spot for reliable detection.
     * Range: 3-20 m/s² (3=very sensitive, 20=very strict)
     */
    val shakeThreshold: Float = 30.0f,

    /**
     * Time window (in milliseconds) during which acceleration spikes must occur
     * to count as a single shake. Prevents false positives from single jolts.
     * Default 250ms allows for faster detection.
     */
    val shakeDurationThreshold: Long = 250L,

    /**
     * Cool-down period (in milliseconds) after a shake is detected.
     * During this time, the sensor completely ignores new movements.
     * Default 500ms allows multiple shakes in quick succession.
     */
    val debounceInterval: Long = 500L,

    // ========== Behavior & Feedback Controls ==========
    /**
     * Enable/disable physical vibration feedback when shake is detected.
     * Default true provides immediate tactile confirmation to the user.
     */
    val isHapticFeedbackEnabled: Boolean = true,

    /**
     * Style of vibration to use when shake is detected.
     * Default DOUBLE_TAP stands out brilliantly against residual hand vibration.
     */
    val hapticPattern: HapticProfile = HapticProfile.DOUBLE_TAP,

    /**
     * Automatically capture a screenshot of the app when shake is detected.
     * Default true captures the bug context instantly, saving developer friction.
     */
    val autoCaptureScreenshot: Boolean = true,

    /**
     * Smoothing constant for Exponential Moving Average filter (0.0 to 1.0).
     * Higher values = more smoothing but delayed response.
     * Default 0.7 is more responsive to actual shake events.
     */
    val smoothingFactor: Float = 0.7f,

    /**
     * Minimum number of acceleration threshold crossings within the duration
     * window to validate a shake. Default 1 makes detection very responsive.
     */
    val minimumThresholdCrossings: Int = 1
) {
    class Builder {
        private var shakeThreshold: Float = 30.0f
        private var shakeDurationThreshold: Long = 250L
        private var debounceInterval: Long = 500L
        private var isHapticFeedbackEnabled: Boolean = true
        private var hapticPattern: HapticProfile = HapticProfile.DOUBLE_TAP
        private var autoCaptureScreenshot: Boolean = true
        private var smoothingFactor: Float = 0.7f
        private var minimumThresholdCrossings: Int = 1

        fun shakeThreshold(threshold: Float) = apply { this.shakeThreshold = threshold }
        fun shakeDurationThreshold(duration: Long) = apply { this.shakeDurationThreshold = duration }
        fun debounceInterval(interval: Long) = apply { this.debounceInterval = interval }
        fun hapticFeedbackEnabled(enabled: Boolean) = apply { this.isHapticFeedbackEnabled = enabled }
        fun hapticPattern(pattern: HapticProfile) = apply { this.hapticPattern = pattern }
        fun autoCaptureScreenshot(enabled: Boolean) = apply { this.autoCaptureScreenshot = enabled }
        fun smoothingFactor(factor: Float) = apply { this.smoothingFactor = factor }
        fun minimumThresholdCrossings(crossings: Int) = apply { this.minimumThresholdCrossings = crossings }

        fun build(): ShakeFeedbackConfig = ShakeFeedbackConfig(
            shakeThreshold = shakeThreshold,
            shakeDurationThreshold = shakeDurationThreshold,
            debounceInterval = debounceInterval,
            isHapticFeedbackEnabled = isHapticFeedbackEnabled,
            hapticPattern = hapticPattern,
            autoCaptureScreenshot = autoCaptureScreenshot,
            smoothingFactor = smoothingFactor,
            minimumThresholdCrossings = minimumThresholdCrossings
        )
    }
}
