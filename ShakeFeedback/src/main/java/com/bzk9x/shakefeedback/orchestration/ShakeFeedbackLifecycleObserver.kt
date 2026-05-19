package com.bzk9x.shakefeedback.orchestration

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Lifecycle observer that automatically manages the shake detection system.
 * - Activates sensor on onStart()
 * - Deactivates sensor on onStop()
 * This ensures zero background battery drain without developer intervention.
 */
internal class ShakeFeedbackLifecycleObserver(
    private val onStartListener: () -> Unit,
    private val onStopListener: () -> Unit
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        onStartListener()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        onStopListener()
    }
}
