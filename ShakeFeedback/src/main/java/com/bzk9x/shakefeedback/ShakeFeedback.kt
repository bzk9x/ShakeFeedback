package com.bzk9x.shakefeedback

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.bzk9x.shakefeedback.config.ShakeFeedbackConfig
import com.bzk9x.shakefeedback.orchestration.ShakeFeedbackManager
import com.bzk9x.shakefeedback.presentation.ShakeFeedbackCallback

/**
 * Public API entry point for the ShakeFeedback library.
 * This is the main interface developers should use to integrate shake-to-feedback
 * into their Android applications.
 *
 * Usage:
 * ```
 * // In your Activity
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     
 *     val shakeFeedback = ShakeFeedback.initialize(
 *         activity = this,
 *         config = ShakeFeedbackConfig.Builder()
 *             .shakeThreshold(30.0f)  // Sweet spot for reliable detection
 *             .build(),
 *         callback = object : ShakeFeedbackCallback {
 *             override fun onShakeDetected(event: ShakeEvent) {
 *                 // Show your custom UI or handle the shake
 *             }
 *             // ... other callback implementations
 *         }
 *     )
 * }
 * ```
 */
object ShakeFeedback {
    private var instance: ShakeFeedbackManager? = null

    /**
     * Initialize the ShakeFeedback system with an activity and optional callback.
     *
     * @param activity The Activity to attach the shake feedback to
     * @param config Optional configuration. If not provided, uses sensible defaults.
     * @param callback Optional callback for handling shake events and user feedback
     * @return The initialized ShakeFeedbackManager instance
     */
    fun initialize(
        activity: Activity,
        config: ShakeFeedbackConfig = ShakeFeedbackConfig(),
        callback: ShakeFeedbackCallback? = null
    ): ShakeFeedbackManager {
        val manager = ShakeFeedbackManager(activity, config, callback)
        manager.attachToActivity(activity)
        instance = manager
        return manager
    }

    /**
     * Initialize the ShakeFeedback system with a lifecycle owner (Fragment, etc.).
     *
     * @param lifecycleOwner The LifecycleOwner to observe
     * @param activity Optional Activity for screenshot capture
     * @param config Optional configuration
     * @param callback Optional callback
     * @return The initialized ShakeFeedbackManager instance
     */
    fun initializeWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        activity: Activity? = null,
        config: ShakeFeedbackConfig = ShakeFeedbackConfig(),
        callback: ShakeFeedbackCallback? = null
    ): ShakeFeedbackManager {
        val context = activity ?: (lifecycleOwner as? Activity)
            ?: throw IllegalArgumentException("Must provide an Activity for context")
        
        val manager = ShakeFeedbackManager(context, config, callback)
        manager.attachToLifecycle(lifecycleOwner)
        instance = manager
        return manager
    }

    /**
     * Get the currently active ShakeFeedback instance, if any.
     */
    fun getInstance(): ShakeFeedbackManager? = instance

    /**
     * Destroy the current ShakeFeedback instance and clean up resources.
     */
    fun destroy() {
        instance?.destroy()
        instance = null
    }

    /**
     * Manually trigger a shake for testing purposes.
     */
    fun triggerShakeForTesting() {
        instance?.triggerShakeManually()
    }
}
