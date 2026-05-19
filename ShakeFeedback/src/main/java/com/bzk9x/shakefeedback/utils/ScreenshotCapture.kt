package com.bzk9x.shakefeedback.utils

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for capturing screenshots of the current app state.
 * Used when a shake is detected to capture the visual context of the bug.
 */
object ScreenshotCapture {
    /**
     * Capture a screenshot of the given activity's root view.
     * This is done asynchronously to avoid blocking the UI thread.
     */
    suspend fun captureScreenshot(activity: Activity): Bitmap? = withContext(Dispatchers.Default) {
        try {
            val rootView = activity.window.decorView.rootView
            rootView.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(rootView.drawingCache)
            rootView.isDrawingCacheEnabled = false
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Capture a screenshot of a specific view.
     * Measures and draws the view into a bitmap.
     */
    suspend fun captureViewScreenshot(view: View): Bitmap? = withContext(Dispatchers.Default) {
        try {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
            )
            view.layout(view.left, view.top, view.right, view.bottom)

            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
