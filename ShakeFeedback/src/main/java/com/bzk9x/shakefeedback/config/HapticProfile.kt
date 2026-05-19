package com.bzk9x.shakefeedback.config

/**
 * Haptic feedback profile for vibration patterns when shake is detected.
 * Provides predefined profiles and support for custom patterns.
 */
sealed class HapticProfile(
    val pattern: LongArray,
    val amplitudes: IntArray
) {
    /**
     * A tiny, crisp bump. Perfect for highly sensitive shake settings.
     * Single pulse of 10ms.
     */
    object LIGHT_CLICK : HapticProfile(
        pattern = longArrayOf(0, 10),
        amplitudes = intArrayOf(0, 200)
    )

    /**
     * Two rapid, sharp pulses. Industry standard for "action triggered" gestures.
     * Great at cutting through the residual vibration from the user's hand.
     */
    object DOUBLE_TAP : HapticProfile(
        pattern = longArrayOf(0, 20, 30, 20),
        amplitudes = intArrayOf(0, 255, 0, 255)
    )

    /**
     * A deeper, longer vibration. Use when the app runs in noisy environments
     * or for warning states where subtle haptics might be missed.
     */
    object HEAVY_THUD : HapticProfile(
        pattern = longArrayOf(0, 40, 10, 40),
        amplitudes = intArrayOf(0, 255, 100, 255)
    )

    /**
     * Custom haptic pattern with developer-defined timing and amplitudes.
     * @param timings LongArray alternating between wait time and vibration duration (milliseconds)
     * @param amplitudes IntArray of vibration amplitudes (0-255) corresponding to each timing
     */
    data class Custom(
        val customPattern: LongArray,
        val customAmplitudes: IntArray
    ) : HapticProfile(customPattern, customAmplitudes) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Custom) return false
            if (!customPattern.contentEquals(other.customPattern)) return false
            if (!customAmplitudes.contentEquals(other.customAmplitudes)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = customPattern.contentHashCode()
            result = 31 * result + customAmplitudes.contentHashCode()
            return result
        }
    }

    companion object {
        /**
         * Creates a custom haptic pattern from raw timing and amplitude data.
         * @param timings LongArray alternating between wait time and vibration duration
         * @param amplitudes IntArray of vibration amplitudes (0-255)
         * @return Custom HapticProfile instance
         */
        fun custom(timings: LongArray, amplitudes: IntArray): HapticProfile {
            return Custom(timings, amplitudes)
        }
    }
}
