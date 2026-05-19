package com.bzk9x.shakefeedback.data

import android.graphics.Bitmap

/**
 * Represents a validated shake event with optional context data.
 * This is emitted when the Signal Processing Layer confirms a deliberate shake.
 */
data class ShakeEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val screenshot: Bitmap? = null,
    val diagnosticLogs: String? = null
)
