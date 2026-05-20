package com.bzk9x.testapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.LinearLayout
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.bzk9x.shakefeedback.ShakeFeedback
import com.bzk9x.shakefeedback.config.ShakeFeedbackConfig
import com.bzk9x.shakefeedback.config.HapticProfile
import com.bzk9x.shakefeedback.data.ShakeEvent
import com.bzk9x.shakefeedback.orchestration.ShakeFeedbackManager
import com.bzk9x.shakefeedback.presentation.FeedbackData
import com.bzk9x.shakefeedback.presentation.ShakeFeedbackCallback
import com.bzk9x.shakefeedback.ui.ShakeFeedbackBottomSheet
import com.bzk9x.shakefeedback.utils.HapticFeedbackProvider

/**
 * Comprehensive test app for ShakeFeedback library.
 * 
 * Features:
 * - Real-time configuration customization (all 8 parameters)
 * - Manual shake trigger with different haptic patterns
 * - Event logging with detailed information
 * - Screenshot preview
 * - Diagnostic logs display
 * - Test individual haptic profiles
 */
class MainActivity : AppCompatActivity() {

    private lateinit var shakeFeedbackManager: ShakeFeedbackManager
    private lateinit var hapticProvider: HapticFeedbackProvider
    private lateinit var feedbackStatusView: TextView
    private lateinit var eventLogView: TextView
    private lateinit var configDisplayView: TextView

    // Configuration controls
    private lateinit var thresholdSeekBar: SeekBar
    private lateinit var durationSeekBar: SeekBar
    private lateinit var debounceSeekBar: SeekBar
    private lateinit var smoothingSeekBar: SeekBar
    private lateinit var crossingsSeekBar: SeekBar
    private lateinit var hapticEnabledCheckBox: CheckBox
    private lateinit var screenshotEnabledCheckBox: CheckBox
    private lateinit var hapticPatternSpinner: Spinner

    private var currentConfig: ShakeFeedbackConfig? = null
    private val hapticPatterns = listOf("LIGHT_CLICK", "DOUBLE_TAP", "HEAVY_THUD")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createMainLayout())
        hapticProvider = HapticFeedbackProvider(this)
        initializeShakeFeedback()
    }

    @SuppressLint("SetTextI18n")
    private fun createMainLayout(): FrameLayout {
        val root = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // === HEADER ===
        val titleView = TextView(this).apply {
            text = "ShakeFeedback Test App"
            textSize = 22f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
        }
        container.addView(titleView)

        feedbackStatusView = TextView(this).apply {
            text = "Status: Initializing..."
            textSize = 12f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12.dpToPx() }
        }
        container.addView(feedbackStatusView)


        container.addView(createControlRow("Shake Threshold (m/s²): ", 3f, 50f, 30f) {
            thresholdSeekBar = it.second
            updateConfigAndRestart()
        })

        // Shake Duration (100 - 800 ms) - Reduced for faster detection
        container.addView(createControlRow("Duration Threshold (ms): ", 100f, 800f, 250f) {
            durationSeekBar = it.second
            updateConfigAndRestart()
        })

        // Debounce (200 - 2000 ms) - Reduced for more frequent shake detection
        container.addView(createControlRow("Debounce Interval (ms): ", 200f, 2000f, 500f) {
            debounceSeekBar = it.second
            updateConfigAndRestart()
        })

        // Smoothing Factor (0.3 - 0.95) - Reduced for more responsiveness
        container.addView(createControlRow("Smoothing Factor (α): ", 0.3f, 0.95f, 0.7f) {
            smoothingSeekBar = it.second
            updateConfigAndRestart()
        })

        // Minimum Threshold Crossings (1 - 4) - Reduced for easier detection
        container.addView(createControlRow("Min Peaks: ", 1f, 4f, 1f) {
            crossingsSeekBar = it.second
            updateConfigAndRestart()
        })

        // Haptic Pattern Spinner
        hapticPatternSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                hapticPatterns
            )
            setSelection(1) // Default: DOUBLE_TAP
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
        }
        container.addView(hapticPatternSpinner)

        // Haptic & Screenshot Checkboxes
        hapticEnabledCheckBox = CheckBox(this).apply {
            text = "✓ Haptic Feedback Enabled"
            isChecked = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 4.dpToPx() }
            setOnCheckedChangeListener { _, _ -> updateConfigAndRestart() }
        }
        container.addView(hapticEnabledCheckBox)

        screenshotEnabledCheckBox = CheckBox(this).apply {
            text = "✓ Auto-Capture Screenshot"
            isChecked = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12.dpToPx() }
            setOnCheckedChangeListener { _, _ -> updateConfigAndRestart() }
        }
        container.addView(screenshotEnabledCheckBox)


        val shakeButton = Button(this).apply {
            text = "Trigger Shake Detection"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
            setOnClickListener {
                logEvent("Manual shake triggered")
                ShakeFeedback.triggerShakeForTesting()
            }
        }
        container.addView(shakeButton)

        // Test Haptic Patterns
        val testHapticButton = Button(this).apply {
            text = "Test Current Haptic Pattern"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
            setOnClickListener {
                val pattern = when (hapticPatternSpinner.selectedItemPosition) {
                    0 -> HapticProfile.LIGHT_CLICK
                    1 -> HapticProfile.DOUBLE_TAP
                    else -> HapticProfile.HEAVY_THUD
                }
                hapticProvider.vibrate(pattern)
                logEvent("Testing: ${hapticPatterns[hapticPatternSpinner.selectedItemPosition]}")
            }
        }
        container.addView(testHapticButton)

        val configButton = Button(this).apply {
            text = "Show Current Config"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
            setOnClickListener { showCurrentConfig() }
        }
        container.addView(configButton)

        val diagButton = Button(this).apply {
            text = "Show Diagnostics"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12.dpToPx() }
            setOnClickListener { showDiagnostics() }
        }
        container.addView(diagButton)


        val logTitle = TextView(this).apply {
            text = "Event Log"
            textSize = 14f
            setTextColor(Color.parseColor("#1976D2"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8.dpToPx(); bottomMargin = 8.dpToPx() }
        }
        container.addView(logTitle)

        eventLogView = TextView(this).apply {
            text = "Ready...\n"
            textSize = 10f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
        }
        container.addView(eventLogView)

        scrollView.addView(container)
        root.addView(scrollView)
        return root
    }

    private fun createControlRow(
        label: String,
        minVal: Float,
        maxVal: Float,
        defaultVal: Float,
        callback: (Pair<Float, SeekBar>) -> Unit
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8.dpToPx() }
        }

        val labelView = TextView(this).apply {
            text = "$label${defaultVal.toInt()}"
            textSize = 12f
            setTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 4.dpToPx() }
        }
        row.addView(labelView)

        val seekBar = SeekBar(this).apply {
            max = ((maxVal - minVal) * 100).toInt()
            progress = ((defaultVal - minVal) * 100).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val value = minVal + (progress / 100f) * (maxVal - minVal)
                        labelView.text = "$label${if (maxVal <= 1.0) String.format("%.2f", value) else value.toInt()}"
                        updateConfigAndRestart()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        row.addView(seekBar)

        callback(Pair(defaultVal, seekBar))
        return row
    }

    private fun initializeShakeFeedback() {
        updateConfigAndRestart()
    }

    private fun updateConfigAndRestart() {
        val threshold = if (::thresholdSeekBar.isInitialized) {
            3f + (thresholdSeekBar.progress / 100f) * 47f
        } else {
            30.0f
        }

        val duration = if (::durationSeekBar.isInitialized) {
            100L + (durationSeekBar.progress / 100f) * 700f
        } else {
            250L
        }.toLong()

        val debounce = if (::debounceSeekBar.isInitialized) {
            200L + (debounceSeekBar.progress / 100f) * 1800f
        } else {
            500L
        }.toLong()

        val smoothing = if (::smoothingSeekBar.isInitialized) {
            0.3f + (smoothingSeekBar.progress / 100f) * 0.65f
        } else {
            0.7f
        }

        val crossings = if (::crossingsSeekBar.isInitialized) {
            1 + (crossingsSeekBar.progress / 100f) * 3
        } else {
            1
        }.toInt()

        val hapticPattern = when (
            if (::hapticPatternSpinner.isInitialized) hapticPatternSpinner.selectedItemPosition else 1
        ) {
            0 -> HapticProfile.LIGHT_CLICK
            2 -> HapticProfile.HEAVY_THUD
            else -> HapticProfile.DOUBLE_TAP
        }

        val config = ShakeFeedbackConfig.Builder()
            .shakeThreshold(threshold)
            .shakeDurationThreshold(duration)
            .debounceInterval(debounce)
            .hapticFeedbackEnabled(
                if (::hapticEnabledCheckBox.isInitialized) hapticEnabledCheckBox.isChecked else true
            )
            .hapticPattern(hapticPattern)
            .autoCaptureScreenshot(
                if (::screenshotEnabledCheckBox.isInitialized) screenshotEnabledCheckBox.isChecked else true
            )
            .smoothingFactor(smoothing)
            .minimumThresholdCrossings(crossings)
            .build()

        currentConfig = config

        if (::shakeFeedbackManager.isInitialized) {
            shakeFeedbackManager.destroy()
        }

        val callback = object : ShakeFeedbackCallback {
            override fun onShakeDetected(event: ShakeEvent) {
                logEvent("Shake detected! Screenshot: ${event.screenshot != null}")
                showFeedbackForm(event)
            }

            override fun onFeedbackSubmitted(data: FeedbackData) {
                logEvent("Feedback submitted (${data.userEmail ?: "anonymous"})")
                if (::feedbackStatusView.isInitialized) {
                    feedbackStatusView.text = "Status: Feedback received!"
                }
            }

            override fun onFeedbackDismissed() {
                logEvent("Feedback dismissed")
                if (::feedbackStatusView.isInitialized) {
                    feedbackStatusView.text = "Status: Ready for next shake"
                }
            }

            override fun onError(errorMessage: String) {
                logEvent("Error: $errorMessage")
            }
        }

        shakeFeedbackManager = ShakeFeedback.initialize(this, config, callback)
        if (::feedbackStatusView.isInitialized) {
            feedbackStatusView.text = "Status: Active"
        }
        updateConfigDisplay()
        logEvent("Config updated and restarted")
    }

    private fun updateConfigDisplay() {
        // Only update if the view has been initialized
        if (!::configDisplayView.isInitialized) return
        
        currentConfig?.let { cfg ->
            val info = """Threshold: ${String.format("%.1f", cfg.shakeThreshold)} m/s²  |  Duration: ${cfg.shakeDurationThreshold}ms
Debounce: ${cfg.debounceInterval}ms  |  α: ${String.format("%.2f", cfg.smoothingFactor)}
Haptic: ${if (cfg.isHapticFeedbackEnabled) "ON" else "OFF"}  |  Screenshot: ${if (cfg.autoCaptureScreenshot) "ON" else "OFF"}"""
            configDisplayView.text = info
        }
    }

    private fun showFeedbackForm(event: ShakeEvent) {
        val feedbackSheet = ShakeFeedbackBottomSheet.newInstance(
            screenshot = event.screenshot,
            diagnosticLogs = event.diagnosticLogs
        )

        feedbackSheet.setOnFeedbackSubmittedListener {
            logEvent("User submitted feedback")
        }

        feedbackSheet.setOnDismissedListener {
            logEvent("Feedback form closed")
        }

        feedbackSheet.show(supportFragmentManager, "shake_feedback")
    }

    private fun showCurrentConfig() {
        currentConfig?.let { cfg ->
            val info = """
                CURRENT CONFIGURATION
                
                Shake Threshold: ${String.format("%.2f", cfg.shakeThreshold)} m/s²
                Shake Duration: ${cfg.shakeDurationThreshold} ms
                Debounce Interval: ${cfg.debounceInterval} ms
                
                Smoothing Factor (α): ${String.format("%.2f", cfg.smoothingFactor)}
                Min Threshold Crossings: ${cfg.minimumThresholdCrossings}
                
                Haptic Feedback: ${if (cfg.isHapticFeedbackEnabled) "ENABLED" else "DISABLED"}
                Haptic Pattern: ${cfg.hapticPattern::class.simpleName}
                
                Auto-Screenshot: ${if (cfg.autoCaptureScreenshot) "ENABLED" else "DISABLED"}
                
                PHYSICS CONTEXT:
                • 9.81 m/s² = Earth gravity
                • 1-3 m/s² = Walking
                • 3-8 m/s² = Running
                • 12-30 m/s² = Intentional shake
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Configuration Details")
                .setMessage(info)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun showDiagnostics() {
        val message = "Diagnostics will be captured on next shake event.\n\nIncludes:\n" +
                "• Device info\n• Memory usage\n• Storage space\n• System logs"
        AlertDialog.Builder(this)
            .setTitle("Diagnostics")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun logEvent(message: String) {
        if (!::eventLogView.isInitialized) return
        
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val current = eventLogView.text.toString()
        val newLog = "[$timestamp] $message\n$current"
        val lines = newLog.split("\n").take(15)
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
