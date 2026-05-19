package com.bzk9x.shakefeedback

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bzk9x.shakefeedback.config.ShakeFeedbackConfig
import com.bzk9x.shakefeedback.config.HapticProfile
import com.bzk9x.shakefeedback.data.ShakeEvent
import com.bzk9x.shakefeedback.orchestration.ShakeFeedbackManager
import com.bzk9x.shakefeedback.presentation.FeedbackData
import com.bzk9x.shakefeedback.presentation.ShakeFeedbackCallback
import com.bzk9x.shakefeedback.ui.ShakeFeedbackBottomSheet

/**
 * Example MainActivity demonstrating how to integrate the ShakeFeedback library.
 * 
 * This example shows:
 * 1. Initializing the shake feedback system with custom configuration
 * 2. Handling shake detection callbacks
 * 3. Displaying the built-in feedback UI
 * 4. Customizing the feedback experience
 */
class MainActivity : AppCompatActivity() {

    private lateinit var shakeFeedbackManager: ShakeFeedbackManager
    private lateinit var feedbackStatusView: TextView
    private lateinit var eventLogView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createMainLayout())

        initializeShakeFeedback()
    }

    /**
     * Creates the UI layout programmatically.
     * In a real app, you'd typically use an XML layout file.
     */
    private fun createMainLayout(): FrameLayout {
        val root = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16.dpToPx(), 24.dpToPx(), 16.dpToPx(), 24.dpToPx())
        }

        // Title
        val titleView = TextView(this).apply {
            text = "Shake to Report Bug"
            textSize = 24f
            setTextColor(android.graphics.Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16.dpToPx()
            }
        }
        containerLayout.addView(titleView)

        // Instructions
        val instructionsView = TextView(this).apply {
            text = "Shake your device to trigger the feedback form. This example demonstrates:\n\n" +
                    "• Automatic shake detection using accelerometer\n" +
                    "• Haptic feedback confirmation (double tap pattern)\n" +
                    "• Screenshot capture on shake\n" +
                    "• Built-in feedback UI with auto-filled context\n" +
                    "• Custom configuration options\n\n" +
                    "Try shaking your device now!"
            textSize = 14f
            setTextColor(android.graphics.Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24.dpToPx()
            }
        }
        containerLayout.addView(instructionsView)

        // Status display
        feedbackStatusView = TextView(this).apply {
            text = "Status: Initializing..."
            textSize = 12f
            setTextColor(android.graphics.Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16.dpToPx()
            }
        }
        containerLayout.addView(feedbackStatusView)

        // Event log
        eventLogView = TextView(this).apply {
            text = "Event Log:\n"
            textSize = 11f
            setTextColor(android.graphics.Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16.dpToPx()
            }
            setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"))
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
        }
        containerLayout.addView(eventLogView)

        // Test button
        val testButton = Button(this).apply {
            text = "Test Shake (Manual Trigger)"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8.dpToPx()
            }
            setOnClickListener {
                logEvent("Manual shake triggered!")
                ShakeFeedback.triggerShakeForTesting()
            }
        }
        containerLayout.addView(testButton)

        // Config info button
        val configButton = Button(this).apply {
            text = "Show Configuration"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                showConfigurationInfo()
            }
        }
        containerLayout.addView(configButton)

        scrollView.addView(containerLayout)
        root.addView(scrollView)

        return root
    }

    /**
     * Initialize the ShakeFeedback system with custom configuration.
     */
    private fun initializeShakeFeedback() {
        // Create custom configuration
        val config = ShakeFeedbackConfig.Builder()
            .shakeThreshold(13.0f)  // Default threshold
            .shakeDurationThreshold(400L)  // Time window for shake detection
            .debounceInterval(1000L)  // Cool-down period
            .hapticFeedbackEnabled(true)  // Enable vibration
            .hapticPattern(HapticProfile.DOUBLE_TAP)  // Double tap pattern
            .autoCaptureScreenshot(true)  // Auto-capture screenshot
            .smoothingFactor(0.9f)  // Noise filtering strength
            .minimumThresholdCrossings(2)  // Minimum peaks in acceleration
            .build()

        // Create callback to handle shake events
        val callback = object : ShakeFeedbackCallback {
            override fun onShakeDetected(event: ShakeEvent) {
                logEvent("🔔 Shake detected! Showing feedback form...")
                showFeedbackForm(event)
            }

            override fun onFeedbackSubmitted(data: FeedbackData) {
                logEvent("✅ Feedback submitted: \"${data.description.take(30)}...\"")
                feedbackStatusView.text = "Status: Feedback received! (${data.timestamp})"
            }

            override fun onFeedbackDismissed() {
                logEvent("❌ Feedback dismissed")
                feedbackStatusView.text = "Status: Ready for next shake"
            }

            override fun onError(errorMessage: String) {
                logEvent("⚠️ Error: $errorMessage")
                feedbackStatusView.text = "Status: Error - $errorMessage"
            }
        }

        // Initialize the ShakeFeedback system
        shakeFeedbackManager = ShakeFeedback.initialize(
            activity = this,
            config = config,
            callback = callback
        )

        feedbackStatusView.text = "Status: Shake feedback active!"
        logEvent("✨ ShakeFeedback initialized with default settings")
    }

    /**
     * Display the built-in feedback form when a shake is detected.
     */
    private fun showFeedbackForm(event: ShakeEvent) {
        val feedbackSheet = ShakeFeedbackBottomSheet.newInstance(
            screenshot = event.screenshot,
            diagnosticLogs = event.diagnosticLogs
        )

        feedbackSheet.setOnFeedbackSubmittedListener { feedback ->
            logEvent("Feedback submitted by user")
        }

        feedbackSheet.setOnDismissedListener {
            logEvent("User dismissed feedback form")
        }

        feedbackSheet.show(supportFragmentManager, "shake_feedback")
    }

    /**
     * Display the current configuration in a dialog.
     */
    private fun showConfigurationInfo() {
        val configInfo = """
            Configuration Details:
            
            Shake Threshold: 13.0 m/s²
            Shake Duration: 400 ms
            Debounce Interval: 1000 ms
            Haptic Feedback: ENABLED (DOUBLE_TAP)
            Auto-Screenshot: ENABLED
            Smoothing Factor: 0.9
            Min Threshold Crossings: 2
            
            These defaults are optimized for:
            • Filtering out walking/running
            • Preventing false positives
            • Immediate tactile confirmation
            • Capturing visual context
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("Configuration Info")
            .setMessage(configInfo)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Log events to the UI for debugging purposes.
     */
    private fun logEvent(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val newLog = eventLogView.text.toString() + "[$timestamp] $message\n"
        eventLogView.text = newLog

        // Keep only the last 20 lines
        val lines = newLog.split("\n").takeLast(20)
        eventLogView.text = lines.joinToString("\n")
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        ShakeFeedback.destroy()
    }
}
