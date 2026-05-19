package com.bzk9x.shakefeedback.signal_processing

import com.bzk9x.shakefeedback.data.ShakeEvent

/**
 * Callback interface for shake detection events.
 * Implementations receive validated shake events from the Signal Processing Layer.
 */
interface ShakeDetectionListener {
    /**
     * Called when a valid shake has been detected and all filtering has been applied.
     */
    fun onShakeDetected(event: ShakeEvent)
}
