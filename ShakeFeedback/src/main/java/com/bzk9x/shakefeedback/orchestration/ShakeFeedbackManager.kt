package com.bzk9x.shakefeedback.orchestration

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bzk9x.shakefeedback.config.ShakeFeedbackConfig
import com.bzk9x.shakefeedback.data.ShakeEvent
import com.bzk9x.shakefeedback.data_source.AccelerometerDataSource
import com.bzk9x.shakefeedback.presentation.ShakeFeedbackCallback
import com.bzk9x.shakefeedback.signal_processing.ShakeDetector
import com.bzk9x.shakefeedback.signal_processing.ShakeDetectionListener
import com.bzk9x.shakefeedback.utils.DiagnosticLogger
import com.bzk9x.shakefeedback.utils.HapticFeedbackProvider
import com.bzk9x.shakefeedback.utils.ScreenshotCapture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Orchestration Layer: Lifecycle management and coordination.
 * This layer acts as the traffic cop, managing when sensors are active
 * and coordinating all the lower layers into a cohesive system.
 *
 * Responsibilities:
 * - Observe activity lifecycle (onStart, onStop)
 * - Manage sensor binding/unbinding based on lifecycle
 * - Integrate haptic feedback
 * - Capture screenshots and diagnostics
 * - Emit state to the Presentation Layer via callbacks
 * - Zero background battery drain guarantee
 */
class ShakeFeedbackManager(
    context: Context,
    private val config: ShakeFeedbackConfig = ShakeFeedbackConfig(),
    private val callback: ShakeFeedbackCallback? = null
) : ShakeDetectionListener {

    private val appContext = context.applicationContext
    private val accelerometerDataSource = AccelerometerDataSource(appContext)
    private val shakeDetector = ShakeDetector(config)
    private val hapticFeedbackProvider = HapticFeedbackProvider(appContext)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var currentActivity: Activity? = null
    private var isInitialized = false

    init {
        shakeDetector.addListener(this)
        accelerometerDataSource.addListener(shakeDetector)
    }

    /**
     * Attach the shake feedback system to an activity.
     * This should be called in your Activity (or Fragment) to start monitoring for shakes.
     * Lifecycle will be managed automatically.
     */
    fun attachToActivity(activity: Activity) {
        if (isInitialized && currentActivity == activity) return

        currentActivity = activity
        isInitialized = true

        if (activity !is LifecycleOwner) {
            callback?.onError("Activity must implement LifecycleOwner (use AppCompatActivity or ComponentActivity)")
            return
        }

        // Observe lifecycle to manage sensor binding
        activity.lifecycle.addObserver(
            ShakeFeedbackLifecycleObserver(
                onStartListener = { startListening() },
                onStopListener = { stopListening() }
            )
        )

        // If the activity is already started, activate immediately
        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startListening()
        }
    }

    /**
     * Attach the shake feedback system to a lifecycle owner (e.g., Activity, Fragment).
     * For more granular control than attachToActivity.
     */
    fun attachToLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            ShakeFeedbackLifecycleObserver(
                onStartListener = { startListening() },
                onStopListener = { stopListening() }
            )
        )
    }

    /**
     * Start the shake detection system.
     * The sensor will begin streaming data and the signal processor will analyze it.
     */
    fun startListening() {
        if (!accelerometerDataSource.isActive()) {
            accelerometerDataSource.start()
        }
    }

    /**
     * Stop the shake detection system.
     * The sensor will be unregistered, guaranteeing zero battery drain.
     */
    fun stopListening() {
        accelerometerDataSource.stop()
        shakeDetector.reset()
    }

    /**
     * Manually trigger a shake event (useful for testing or custom gestures).
     */
    fun triggerShakeManually() {
        onShakeDetected(ShakeEvent())
    }

    /**
     * Reset the detector's internal state without stopping the sensor.
     */
    fun reset() {
        shakeDetector.reset()
    }

    /**
     * Update the configuration at runtime.
     * Note: This doesn't take effect on existing data in the pipeline.
     */
    fun updateConfig(newConfig: ShakeFeedbackConfig) {
        shakeDetector.reset()
        // Note: In a production system, you might want to recreate the detector with the new config
    }

    /**
     * Clean up resources. Call this when you're done with shake feedback.
     */
    fun destroy() {
        stopListening()
        accelerometerDataSource.removeListener(shakeDetector)
        shakeDetector.removeListener(this)
    }

    /**
     * === Implementation of ShakeDetectionListener ===
     * Called when the Signal Processing Layer detects a valid shake.
     * This is where we coordinate the Orchestration & Presentation layers.
     */
    override fun onShakeDetected(event: ShakeEvent) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                if (config.isHapticFeedbackEnabled) {
                    hapticFeedbackProvider.vibrate(config.hapticPattern)
                }

                val screenshot = if (config.autoCaptureScreenshot && currentActivity != null) {
                    ScreenshotCapture.captureScreenshot(currentActivity!!)
                } else {
                    null
                }

                val diagnostics = DiagnosticLogger.collectDiagnostics(appContext)

                val enrichedEvent = event.copy(
                    screenshot = screenshot,
                    diagnosticLogs = diagnostics
                )

                callback?.onShakeDetected(enrichedEvent)

            } catch (e: Exception) {
                callback?.onError("Error processing shake: ${e.message}")
            }
        }
    }
}
