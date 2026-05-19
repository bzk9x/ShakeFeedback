package com.bzk9x.shakefeedback.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bzk9x.shakefeedback.presentation.FeedbackData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Built-in UI for shake feedback.
 * Presents a clean Bottom Sheet overlay for users to describe their bug
 * and preview the captured screenshot.
 *
 * Developers can choose to use this or implement their own custom UI
 * by listening to the ShakeFeedbackCallback.
 */
class ShakeFeedbackBottomSheet : BottomSheetDialogFragment() {

    private var screenshot: Bitmap? = null
    private var diagnosticLogs: String? = null
    private var onFeedbackSubmitted: ((FeedbackData) -> Unit)? = null
    private var onDismissed: (() -> Unit)? = null

    companion object {
        private const val ARG_SCREENSHOT = "screenshot"
        private const val ARG_LOGS = "logs"

        fun newInstance(
            screenshot: Bitmap? = null,
            diagnosticLogs: String? = null
        ): ShakeFeedbackBottomSheet {
            return ShakeFeedbackBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SCREENSHOT, screenshot)
                    putString(ARG_LOGS, diagnosticLogs)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenshot = arguments?.getParcelable(ARG_SCREENSHOT)
        diagnosticLogs = arguments?.getString(ARG_LOGS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createLayout(inflater.context)
    }

    @SuppressLint("SetTextI18n")
    private fun createLayout(context: Context): ScrollView {
        val scrollView = ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val containerLayout = ConstraintLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16.dpToPx(), 24.dpToPx(), 16.dpToPx(), 24.dpToPx())
        }

        // Title
        val titleView = TextView(context).apply {
            text = "Report a Bug"
            textSize = 20f
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }
        containerLayout.addView(titleView)

        // Description label
        val descriptionLabel = TextView(context).apply {
            text = "What went wrong?"
            textSize = 14f
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToBottom = titleView.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = 16.dpToPx()
            }
            id = View.generateViewId()
        }
        containerLayout.addView(descriptionLabel)

        // Description input
        val descriptionInput = EditText(context).apply {
            hint = "Describe the issue..."
            setLines(4)
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToBottom = descriptionLabel.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = 8.dpToPx()
            }
            id = View.generateViewId()
        }
        containerLayout.addView(descriptionInput)

        // Email label (optional)
        val emailLabel = TextView(context).apply {
            text = "Your email (optional)"
            textSize = 12f
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToBottom = descriptionInput.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = 12.dpToPx()
            }
            id = View.generateViewId()
        }
        containerLayout.addView(emailLabel)

        // Email input
        val emailInput = EditText(context).apply {
            hint = "your@email.com"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            layoutParams = ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                topToBottom = emailLabel.id
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topMargin = 4.dpToPx()
            }
            id = View.generateViewId()
        }
        containerLayout.addView(emailInput)

        // Screenshot preview label
        if (screenshot != null) {
            val screenshotLabel = TextView(context).apply {
                text = "Screenshot"
                textSize = 14f
                layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    topToBottom = emailInput.id
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    topMargin = 12.dpToPx()
                }
                id = View.generateViewId()
            }
            containerLayout.addView(screenshotLabel)

            // Screenshot image
            val screenshotImage = ImageView(context).apply {
                setImageBitmap(screenshot)
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    300.dpToPx()
                ).apply {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    topToBottom = screenshotLabel.id
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    topMargin = 8.dpToPx()
                }
                id = View.generateViewId()
            }
            containerLayout.addView(screenshotImage)

            // Submit button
            val submitButton = Button(context).apply {
                text = "Submit"
                layoutParams = ConstraintLayout.LayoutParams(
                    0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    topToBottom = screenshotImage.id
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    topMargin = 16.dpToPx()
                }
                setOnClickListener {
                    val feedback = FeedbackData(
                        description = descriptionInput.text.toString(),
                        screenshot = screenshot,
                        diagnosticLogs = diagnosticLogs,
                        userEmail = emailInput.text.toString().takeIf { it.isNotEmpty() }
                    )
                    onFeedbackSubmitted?.invoke(feedback)
                    dismiss()
                }
            }
            containerLayout.addView(submitButton)
        }

        scrollView.addView(containerLayout)
        return scrollView
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissed?.invoke()
    }

    fun setOnFeedbackSubmittedListener(listener: (FeedbackData) -> Unit) {
        onFeedbackSubmitted = listener
    }

    fun setOnDismissedListener(listener: () -> Unit) {
        onDismissed = listener
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
