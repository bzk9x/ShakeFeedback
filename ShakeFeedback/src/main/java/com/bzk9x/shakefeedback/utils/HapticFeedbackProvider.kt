package com.bzk9x.shakefeedback.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.bzk9x.shakefeedback.config.HapticProfile

/**
 * Utility for triggering haptic feedback when a shake is detected.
 * Abstracts away Android version differences in vibration APIs.
 */
class HapticFeedbackProvider(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val hasVibrator: Boolean = vibrator?.hasVibrator() ?: false

    /**
     * Trigger a haptic feedback pattern.
     * Gracefully handles devices without vibration support.
     */
    fun vibrate(profile: HapticProfile) {
        if (!hasVibrator) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(profile.pattern, profile.amplitudes, -1)
                vibrator?.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(profile.pattern, -1)
            }
        } catch (e: Exception) {
            // Silently fail if vibration is not supported or allowed
        }
    }

    /**
     * Cancel any ongoing vibration.
     */
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Silently fail
        }
    }
}
