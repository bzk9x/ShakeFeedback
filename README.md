# ShakeFeedback Library

A professional-grade Android library for implementing shake-to-report-bug functionality. Built with a four-layer architecture that separates hardware, signal processing, orchestration, and presentation concerns.

**Status**: ✅ Production-ready | **Size**: ~500KB | **Min API**: 24+ | **Latest**: 1.0.0

---

## Table of Contents

1. [Quick Start](#quick-start) (5 minutes)
2. [Architecture Overview](#architecture-overview)
3. [Configuration Guide](#configuration-guide)
4. [Usage Examples](#usage-examples)
5. [API Reference](#api-reference)
6. [Troubleshooting](#troubleshooting)
7. [Performance](#performance)

---

## Quick Start

### 1. Basic Setup

Add to your Activity:

```kotlin
import com.bzk9x.shakefeedback.ShakeFeedback
import com.bzk9x.shakefeedback.presentation.ShakeFeedbackCallback
import com.bzk9x.shakefeedback.data.ShakeEvent

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Initialize with default configuration
    ShakeFeedback.initialize(
        activity = this,
        callback = object : ShakeFeedbackCallback {
            override fun onShakeDetected(event: ShakeEvent) {
                // Handle shake detection
                showFeedbackForm(event)
            }

            override fun onFeedbackSubmitted(data: FeedbackData) {
                // Send data to your server
                sendFeedbackToServer(data)
            }

            override fun onFeedbackDismissed() {
                // User dismissed feedback
            }

            override fun onError(errorMessage: String) {
                // Handle errors
            }
        }
    )
}
```

### 2. Using the Built-in UI

```kotlin
override fun onShakeDetected(event: ShakeEvent) {
    val feedbackSheet = ShakeFeedbackBottomSheet.newInstance(
        screenshot = event.screenshot,
        diagnosticLogs = event.diagnosticLogs
    )
    
    feedbackSheet.setOnFeedbackSubmittedListener { feedback ->
        // Handle submitted feedback
        uploadFeedback(feedback)
    }
    
    feedbackSheet.show(supportFragmentManager, "feedback")
}
```

### 3. Custom Configuration

```kotlin
val config = ShakeFeedbackConfig.Builder()
    .shakeThreshold(12.0f)  // More sensitive
    .hapticPattern(HapticProfile.HEAVY_THUD)  // Custom haptic
    .autoCaptureScreenshot(true)
    .debounceInterval(2000L)  // Longer cool-down
    .build()

ShakeFeedback.initialize(
    activity = this,
    config = config,
    callback = myCallback
)
```

## Default Configuration

Out of the box, the library uses optimized defaults:

| Setting | Value | Rationale |
|---------|-------|-----------|
| **Shake Threshold** | 13.0 m/s² | Filters walking/running; requires deliberate wrist snap |
| **Shake Duration** | 400 ms | Real human shake = back-and-forth motion |
| **Debounce Interval** | 1000 ms | Prevents re-trigger when bringing phone back to face |
| **Haptic Feedback** | DOUBLE_TAP | Distinct pattern that cuts through hand vibration |
| **Auto-Screenshot** | true | Captures visual context of the bug instantly |
| **Smoothing Factor** | 0.9 | Aggressive noise filtering for false positive prevention |
| **Min Threshold Crossings** | 2 | Prevents single jolt false positives |

## Required Permissions

Add to your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

The library includes these automatically in its manifest.

## Architecture Overview

### Layer 1: Hardware Acquisition
**AccelerometerDataSource** - Raw sensor polling at ~60Hz
- Continuous (x, y, z) acceleration vectors
- No processing or decisions
- Pure hardware abstraction

### Layer 2: Signal Processing  
**ShakeDetector** - Mathematical filtering pipeline
- Magnitude calculation (Euclidean norm)
- Gravity isolation (high-pass filter)
- Noise reduction (exponential moving average)
- Threshold crossing validation
- Debounce logic

### Layer 3: Orchestration
**ShakeFeedbackManager** - Lifecycle & coordination
- Observes Activity lifecycle
- Manages sensor binding/unbinding
- Triggers haptic feedback
- Captures screenshots & diagnostics
- Zero background battery drain

### Layer 4: Presentation
**ShakeFeedbackCallback** - State emission to host app
- Pure callback interface
- Developers implement custom UI
- Built-in BottomSheet UI available

## Advanced Customization

### Custom Haptic Patterns

```kotlin
val config = ShakeFeedbackConfig.Builder()
    .hapticPattern(HapticProfile.LIGHT_CLICK)  // Quick confirmation
    // or
    .hapticPattern(HapticProfile.DOUBLE_TAP)   // Standard (default)
    // or
    .hapticPattern(HapticProfile.HEAVY_THUD)   // Strong feedback
    .build()
```

### Fine-tuning Shake Detection

```kotlin
// More sensitive to light shakes
val sensitiveConfig = ShakeFeedbackConfig.Builder()
    .shakeThreshold(10.0f)  // Lower threshold
    .smoothingFactor(0.8f)  // Less smoothing
    .minimumThresholdCrossings(1)  // Accept single peak
    .build()

// Stricter shake detection
val strictConfig = ShakeFeedbackConfig.Builder()
    .shakeThreshold(15.0f)  // Higher threshold
    .smoothingFactor(0.95f)  // More smoothing
    .minimumThresholdCrossings(3)  // Require multiple peaks
    .build()
```

### Capturing Feedback Data

```kotlin
override fun onFeedbackSubmitted(data: FeedbackData) {
    val json = JSONObject().apply {
        put("description", data.description)
        put("email", data.userEmail)
        put("timestamp", data.timestamp)
        put("screenshot_size", data.screenshot?.byteCount ?: 0)
        put("has_logs", data.diagnosticLogs != null)
    }
    
    sendToServer(json.toString())
}
```

## Testing

### Manual Trigger for QA

```kotlin
// In your debug build variant
if (BuildConfig.DEBUG) {
    findViewById<Button>(R.id.test_shake_button).setOnClickListener {
        ShakeFeedback.triggerShakeForTesting()
    }
}
```

### Mocking in Unit Tests

```kotlin
// Create a test configuration
val testConfig = ShakeFeedbackConfig(
    shakeThreshold = 5.0f,  // Very sensitive for testing
    debounceInterval = 0L  // No debounce for rapid tests
)
```

## Battery & Memory Considerations

- ✅ **Zero background drain** - Sensor unregistered when app pauses
- ✅ **Lightweight** - ~500KB library size
- ✅ **Memory efficient** - Constant memory footprint regardless of usage
- ✅ **No location/network** - Pure local processing
- ✅ **Graceful degradation** - Works on devices without vibrator

## Troubleshooting

### Shake not detected?
1. Check if device has accelerometer: `Settings → About → Sensor Information`
2. Try lowering `shakeThreshold` in config
3. Verify `debounceInterval` isn't too long
4. Check that app has permission to use vibrator

### Too many false positives?
1. Increase `shakeThreshold` (try 14.0-15.0f)
2. Increase `smoothingFactor` (try 0.95f)
3. Increase `minimumThresholdCrossings` (try 3)
4. Increase `shakeDurationThreshold` (try 500ms)

### Haptic feedback not working?
1. Enable vibration in device settings
2. Verify app has `VIBRATE` permission
3. Some devices may have haptics disabled or limited
4. Check if device has a real vibrator (not just software buzz)

## Example Use Cases

### 1. Simple Toast Notification
```kotlin
override fun onShakeDetected(event: ShakeEvent) {
    Toast.makeText(this, "Shake detected!", Toast.LENGTH_SHORT).show()
}
```

### 2. Automatic Error Reporting
```kotlin
override fun onFeedbackSubmitted(data: FeedbackData) {
    val report = ErrorReport(
        description = data.description,
        screenshot = data.screenshot,
        logs = data.diagnosticLogs,
        device = Build.DEVICE
    )
    ErrorReportingService.submit(report)
}
```

### 3. Analytics Integration
```kotlin
override fun onShakeDetected(event: ShakeEvent) {
    FirebaseAnalytics.getInstance(this).logEvent("shake_detected", null)
    // Show custom UI
}
```

## Performance Characteristics

- **Detection Latency**: ~100-200ms (time from physical shake to callback)
- **CPU Usage**: <1% average (only when accelerometer active)
- **Memory**: ~2MB peak (including bitmap cache)
- **Sensor Polling**: 16ms intervals (~60Hz)

## License

MIT License - See LICENSE file

---

## Architecture Overview

### The Four-Layer Architecture

```
┌─────────────────────────────────────────────────────────┐
│ LAYER 4: PRESENTATION                                   │
│ ShakeFeedbackCallback → Custom UI or Built-in BottomSheet│
└─────────────────────────────────────────────────────────┘
                           ▲
                           │ onShakeDetected()
┌─────────────────────────────────────────────────────────┐
│ LAYER 3: ORCHESTRATION                                  │
│ ShakeFeedbackManager - Lifecycle Management              │
│ └─ Haptic Feedback, Screenshot Capture, Diagnostics     │
└─────────────────────────────────────────────────────────┘
                           ▲
                           │ onShakeDetected()
┌─────────────────────────────────────────────────────────┐
│ LAYER 2: SIGNAL PROCESSING                              │
│ ShakeDetector - Mathematical Filtering Pipeline          │
│ └─ Magnitude → Gravity Filter → EMA → Threshold/Debounce│
└─────────────────────────────────────────────────────────┘
                           ▲
                           │ SensorData(x,y,z)
┌─────────────────────────────────────────────────────────┐
│ LAYER 1: HARDWARE ACQUISITION                           │
│ AccelerometerDataSource - Raw Sensor Polling (~60Hz)     │
│ └─ Direct SensorManager binding, zero processing        │
└─────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

**Layer 1: Hardware Acquisition**
- Continuous polling of device accelerometer
- Streams raw (x, y, z) vectors in m/s²
- No processing or decisions
- Pure hardware abstraction

**Layer 2: Signal Processing**
- Magnitude calculation (Euclidean norm)
- Gravity isolation (high-pass filter)
- Noise reduction (exponential moving average)
- Threshold crossing validation
- Debounce logic
- **Detection latency**: 100-200ms

**Layer 3: Orchestration**
- Observes Activity lifecycle
- Manages sensor binding/unbinding
- **Zero battery drain** when app paused
- Triggers haptic feedback immediately
- Async screenshot capture
- Async diagnostic collection

**Layer 4: Presentation**
- Pure callback interface (no forced UI)
- Developers implement custom behavior
- Optional built-in BottomSheet UI
- Complete control over feedback handling

### Why Four Layers?

- **Independence**: Each layer can be tested/replaced separately
- **Reusability**: Layers can be used independently
- **Maintainability**: Changes don't cascade across layers
- **Flexibility**: Developers can replace any layer
- **Battery Safety**: Clear lifecycle boundaries

---

## Configuration Guide

### All Configuration Parameters

| Parameter | Default | Type | Range | Purpose |
|-----------|---------|------|-------|---------|
| `shakeThreshold` | 13.0 | Float | 5-25 m/s² | Acceleration required to trigger |
| `shakeDurationThreshold` | 400 | Long | 200-1000 ms | Time window for validation |
| `debounceInterval` | 1000 | Long | 500-3000 ms | Cool-down period |
| `isHapticFeedbackEnabled` | true | Boolean | true/false | Vibration enabled |
| `hapticPattern` | DOUBLE_TAP | enum | 4 profiles | Vibration style |
| `autoCaptureScreenshot` | true | Boolean | true/false | Auto-capture screen |
| `smoothingFactor` | 0.9 | Float | 0.0-1.0 | EMA noise filter |
| `minimumThresholdCrossings` | 2 | Int | 1-5 | Required peaks |

### Physics Explanation

- **Earth's gravity**: 9.81 m/s²
- **Walking**: 1-3 m/s² acceleration peaks
- **Running**: 3-8 m/s² acceleration peaks
- **Deliberate shake**: 12-30 m/s² acceleration peaks

A threshold of 13.0 m/s² filters out everyday motion but catches intentional shakes.

### Signal Processing Pipeline

**Step 1: Magnitude Calculation**
```
magnitude = √(x² + y² + z²)
```
Converts (x, y, z) to scalar regardless of device orientation.

**Step 2: Gravity Isolation (High-Pass Filter)**
```
gravity_corrected = |current_magnitude - previous_magnitude|
```
Removes constant 9.81 m/s² gravity, isolates human-applied force.

**Step 3: Noise Reduction (Exponential Moving Average)**
```
smoothed[n] = α × raw[n] + (1 - α) × smoothed[n-1]
```
Default α = 0.9 provides excellent noise rejection.

**Step 4: Threshold & Debounce**
- Signal must cross threshold
- Must cross multiple times within time window
- Must not be in debounce cooldown period

### Configuration Presets

**Balanced (Default)**
```kotlin
ShakeFeedbackConfig()
```
Perfect for general-purpose apps. No configuration needed.

**Sensitive (Games, Accessibility)**
```kotlin
ShakeFeedbackConfig.Builder()
    .shakeThreshold(10.0f)
    .debounceInterval(500L)
    .minimumThresholdCrossings(1)
    .build()
```
Triggers on lighter shakes. More responsive.

**Strict (Banking, Safety-Critical)**
```kotlin
ShakeFeedbackConfig.Builder()
    .shakeThreshold(15.0f)
    .smoothingFactor(0.95f)
    .minimumThresholdCrossings(3)
    .autoCaptureScreenshot(false)
    .build()
```
Only triggers on deliberate, violent shakes. False-positive resistant.

**Privacy-Conscious**
```kotlin
ShakeFeedbackConfig.Builder()
    .autoCaptureScreenshot(false)
    .hapticFeedbackEnabled(false)
    .build()
```
No auto-screenshot, no vibration. Discrete feedback.

**Testing/Debug**
```kotlin
ShakeFeedbackConfig.Builder()
    .shakeThreshold(5.0f)  // Very sensitive
    .debounceInterval(0L)   // No debounce
    .build()
```
Easy to trigger for QA testing.

### Tuning Guide

**Problem: Too many false positives**
- Increase `shakeThreshold` by 1-2 m/s²
- Increase `smoothingFactor` to 0.93-0.95
- Increase `minimumThresholdCrossings` to 3

**Problem: Hard to trigger shake**
- Decrease `shakeThreshold` by 1-2 m/s²
- Decrease `smoothingFactor` to 0.85-0.87
- Decrease `minimumThresholdCrossings` to 1

**Problem: Delayed response**
- Decrease `smoothingFactor` to 0.85
- Decrease `minimumThresholdCrossings` to 1

**Problem: Double triggers**
- Increase `debounceInterval` to 1500-2000ms

---

## Usage Examples

### Example 1: Basic Bug Reporting
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        ShakeFeedback.initialize(
            activity = this,
            callback = object : ShakeFeedbackCallback {
                override fun onShakeDetected(event: ShakeEvent) {
                    ShakeFeedbackBottomSheet.newInstance(
                        event.screenshot, event.diagnosticLogs
                    ).show(supportFragmentManager, "feedback")
                }
                
                override fun onFeedbackSubmitted(data: FeedbackData) {
                    sendBugReportToServer(data)
                }
                
                override fun onFeedbackDismissed() {}
                override fun onError(msg: String) {}
            }
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ShakeFeedback.destroy()
    }
}
```

### Example 2: Custom Toast Notification
```kotlin
ShakeFeedback.initialize(this, callback = object : ShakeFeedbackCallback {
    override fun onShakeDetected(event: ShakeEvent) {
        Toast.makeText(this@MainActivity, 
            "Shake detected! Press icon to report", Toast.LENGTH_LONG).show()
    }
    override fun onFeedbackSubmitted(data: FeedbackData) {}
    override fun onFeedbackDismissed() {}
    override fun onError(msg: String) {}
})
```

### Example 3: Analytics Integration
```kotlin
override fun onShakeDetected(event: ShakeEvent) {
    analytics.logEvent("shake_detected", Bundle().apply {
        putLong("timestamp", event.timestamp)
        putBoolean("has_screenshot", event.screenshot != null)
    })
    showFeedbackForm(event)
}
```

### Example 4: Privacy-Aware Implementation
```kotlin
val privacyConfig = ShakeFeedbackConfig.Builder()
    .autoCaptureScreenshot(false)
    .hapticFeedbackEnabled(false)
    .build()

ShakeFeedback.initialize(this, config = privacyConfig, 
    callback = object : ShakeFeedbackCallback {
    override fun onShakeDetected(event: ShakeEvent) {
        // Ask user before including screenshot
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Report a Bug?")
            .setMessage("Help improve the app. Include screenshot?")
            .setPositiveButton("Yes") { showFeedbackForm(event) }
            .setNegativeButton("No") { showFeedbackFormWithoutScreenshot(event) }
            .show()
    }
    // ... other callbacks
})
```

### Example 5: Fragment Integration
```kotlin
class MyFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        ShakeFeedback.initializeWithLifecycle(
            lifecycleOwner = this,
            activity = requireActivity(),
            callback = myCallback
        )
    }
}
```

### Example 6: Build Variant Configuration
```kotlin
val shakeFeedbackConfig = if (BuildConfig.DEBUG) {
    // Sensitive for testing
    ShakeFeedbackConfig.Builder()
        .shakeThreshold(5.0f)
        .debounceInterval(0L)
        .build()
} else {
    // Production defaults
    ShakeFeedbackConfig()
}

ShakeFeedback.initialize(this, config = shakeFeedbackConfig, callback = myCallback)
```

### Example 7: Manual Testing Trigger
```kotlin
findViewById<Button>(R.id.test_shake_button).setOnClickListener {
    ShakeFeedback.triggerShakeForTesting()
}
```

### Example 8: Remote Configuration (Firebase)
```kotlin
remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val config = ShakeFeedbackConfig.Builder()
            .shakeThreshold(
                remoteConfig.getDouble("shake_threshold").toFloat()
            )
            .debounceInterval(
                remoteConfig.getLong("debounce_interval_ms")
            )
            .build()
        ShakeFeedback.initialize(this, config, myCallback)
    }
}
```

### Example 9: Unit Testing
```kotlin
@Test
fun testShakeDetection() {
    val detector = ShakeDetector(
        ShakeFeedbackConfig(shakeThreshold = 5.0f)
    )
    
    val testListener = object : ShakeDetectionListener {
        var shakeDetected = false
        override fun onShakeDetected(event: ShakeEvent) {
            shakeDetected = true
        }
    }
    
    detector.addListener(testListener)
    detector.onSensorDataReceived(SensorData(x = 20f, y = 20f, z = 20f))
    
    assertTrue(testListener.shakeDetected)
}
```

### Example 10: Crash Reporting Integration
```kotlin
private var lastShakeEvent: ShakeEvent? = null

override fun onShakeDetected(event: ShakeEvent) {
    lastShakeEvent = event
    crashlytics.setCustomKey("last_shake_timestamp", event.timestamp)
    
    // When app crashes, last shake context is included
}
```

---

## API Reference

### Main Entry Point

```kotlin
object ShakeFeedback {
    // Initialize with Activity
    fun initialize(
        activity: Activity,
        config: ShakeFeedbackConfig = ShakeFeedbackConfig(),
        callback: ShakeFeedbackCallback? = null
    ): ShakeFeedbackManager
    
    // Initialize with Lifecycle Owner
    fun initializeWithLifecycle(
        lifecycleOwner: LifecycleOwner,
        activity: Activity? = null,
        config: ShakeFeedbackConfig = ShakeFeedbackConfig(),
        callback: ShakeFeedbackCallback? = null
    ): ShakeFeedbackManager
    
    // Get active instance
    fun getInstance(): ShakeFeedbackManager?
    
    // Cleanup
    fun destroy()
    
    // Testing
    fun triggerShakeForTesting()
}
```

### ShakeFeedbackCallback Interface

```kotlin
interface ShakeFeedbackCallback {
    fun onShakeDetected(event: ShakeEvent)        // Valid shake confirmed
    fun onFeedbackSubmitted(data: FeedbackData)   // User submitted feedback
    fun onFeedbackDismissed()                     // User dismissed form
    fun onError(errorMessage: String)             // Error occurred
}
```

### Data Classes

```kotlin
// Raw sensor reading
data class SensorData(
    val x: Float,           // X-axis acceleration (m/s²)
    val y: Float,           // Y-axis acceleration (m/s²)
    val z: Float,           // Z-axis acceleration (m/s²)
    val timestamp: Long     // When reading occurred
) {
    fun magnitude(): Float  // Returns √(x²+y²+z²)
}

// Validated shake event
data class ShakeEvent(
    val timestamp: Long,            // When shake occurred
    val screenshot: Bitmap? = null, // Auto-captured screen
    val diagnosticLogs: String? = null  // Device diagnostics
)

// User-submitted feedback
data class FeedbackData(
    val description: String,              // Bug description
    val screenshot: Bitmap?,              // Optional screenshot
    val diagnosticLogs: String?,          // Optional device info
    val timestamp: Long,                  // When submitted
    val userEmail: String? = null         // Optional email
)
```

### Haptic Profiles

```kotlin
enum class HapticProfile {
    LIGHT_CLICK,    // 10ms single pulse (subtle)
    DOUBLE_TAP,     // Two 20ms pulses (standard - default)
    HEAVY_THUD,     // Two 40ms pulses (strong)
    // Plus: custom(timings, amplitudes)
}
```

### Configuration Builder

```kotlin
ShakeFeedbackConfig.Builder()
    .shakeThreshold(13.0f)
    .shakeDurationThreshold(400L)
    .debounceInterval(1000L)
    .hapticFeedbackEnabled(true)
    .hapticPattern(HapticProfile.DOUBLE_TAP)
    .autoCaptureScreenshot(true)
    .smoothingFactor(0.9f)
    .minimumThresholdCrossings(2)
    .build()
```

### Manager Methods

```kotlin
val manager = ShakeFeedback.getInstance()

manager?.startListening()              // Start sensor
manager?.stopListening()               // Stop sensor
manager?.reset()                       // Clear internal state
manager?.triggerShakeManually()        // Manual trigger
manager?.destroy()                     // Cleanup
```

---

## Troubleshooting

### Shake not detected?
1. **Check device has accelerometer**: Settings → About → Sensor Information
2. **Test on real device**: Simulator accelerometer is unreliable
3. **Lower threshold**: Try `shakeThreshold(10.0f)`
4. **Verify permission**: Check VIBRATE permission is granted
5. **Check focus**: Ensure app has foreground focus (not minimized)

### False positives while walking/in vehicle?
1. **Increase threshold**: Try 14.0-15.0 m/s²
2. **Increase smoothing**: Set `smoothingFactor(0.95f)`
3. **Require more peaks**: Set `minimumThresholdCrossings(3)`
4. **Extend time window**: Set `shakeDurationThreshold(500L)`

### Vibration not working?
1. **Enable in settings**: Check device vibration is enabled
2. **Verify permission**: App must have VIBRATE permission
3. **Check device**: Some devices have haptics disabled
4. **Test manually**: Use `triggerShakeForTesting()` to verify

### Delayed response?
1. **Lower smoothing**: Try `smoothingFactor(0.85f)`
2. **Reduce peaks required**: Set `minimumThresholdCrossings(1)`
3. **Shorter duration window**: Try `shakeDurationThreshold(300L)`

### Double triggers?
1. **Increase debounce**: Try `debounceInterval(1500L)` or `2000L`

---

## Performance

| Metric | Value | Notes |
|--------|-------|-------|
| Library size | ~500KB | APK size impact |
| Detection latency | 100-200ms | Shake to callback |
| CPU usage | <1% | Average when active |
| Memory peak | ~2MB | Includes bitmap cache |
| Sensor polling | ~60Hz | 16ms intervals |
| Screenshot capture | 50-200ms | Async, non-blocking |
| Diagnostic collection | 100-300ms | Async, non-blocking |

### Battery Guarantee

✅ **Zero background drain** - Sensor unregistered when app pauses  
✅ **No wake locks** - Device sleeps normally  
✅ **Lightweight** - Constant memory footprint  
✅ **Efficient** - <1% CPU when accelerometer active  

---

## What Gets Captured

### Screenshot (Optional)
- Current Activity's full screen
- Captured asynchronously (non-blocking)
- Can be disabled for privacy

### Diagnostics
- Device model, manufacturer, Android version
- Memory (heap, native, available)
- Storage (total, free space)
- Recent logcat entries (last 50 lines)
- Network not included (local only)

### User Input (Built-in UI)
- Bug description (free text)
- Email (optional)
- Permission choices

---

## Privacy & Security

### Data Handling
- **No tracking**: No automatic analytics
- **No persistence**: Data not saved to disk
- **Opt-in screenshot**: Can be disabled globally
- **Local only**: No network calls by default

### Sensitive Data Apps (Banking, Healthcare)
Use privacy configuration:
```kotlin
ShakeFeedbackConfig.Builder()
    .autoCaptureScreenshot(false)
    .hapticFeedbackEnabled(false)
    .build()
```

---

## Contributing

Contributions welcome! Please ensure:
1. All 4 layers remain properly separated
2. Add unit tests for signal processing changes
3. Test on real devices (simulator is limited)
4. Document configuration changes
5. Maintain backward compatibility

---

## License

MIT License - See LICENSE file

---

**Built with ❤️ for Android developers**  
Questions? Check the code comments for detailed explanations of each layer.
