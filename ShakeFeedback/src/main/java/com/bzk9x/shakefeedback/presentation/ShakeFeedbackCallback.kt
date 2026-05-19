package com.bzk9x.shakefeedback.presentation

import com.bzk9x.shakefeedback.data.ShakeEvent

/**
 * Callback interface that the Presentation Layer uses to emit state changes
 * back to the host application. This allows developers to handle shake events
 * with their own custom UI or logic.
 *
 * Instead of forcing a specific rigid design, this framework emits a state
 * change and leaves it to the developer to decide how to respond.
 */
interface ShakeFeedbackCallback {
    /**
     * Called when a shake has been detected and the feedback system is activated.
     * The developer can use this to show their custom UI, log events, or trigger custom behavior.
     */
    fun onShakeDetected(event: ShakeEvent)

    /**
     * Called when the user submits feedback (if using the built-in UI).
     */
    fun onFeedbackSubmitted(data: FeedbackData)

    /**
     * Called when the user dismisses the feedback UI without submitting.
     */
    fun onFeedbackDismissed()

    /**
     * Called if an error occurs in the shake detection or feedback system.
     */
    fun onError(errorMessage: String)
}

/**
 * Data structure for feedback submitted by the user.
 * This bundles together all the context gathered when the shake was triggered.
 */
data class FeedbackData(
    val description: String,
    val screenshot: android.graphics.Bitmap?,
    val diagnosticLogs: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String? = null
)
