package com.shortslock.app.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.shortslock.app.R

/**
 * Manages the full-screen overlay that blocks YouTube Shorts
 */
class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var onUnlockRequested: (() -> Unit)? = null

    /**
     * Show the blocking overlay
     */
    @SuppressLint("ClickableViewAccessibility")
    fun showOverlay(onUnlock: () -> Unit) {
        if (overlayView != null) return // Already showing

        this.onUnlockRequested = onUnlock

        // Inflate overlay layout
        overlayView = LayoutInflater.from(context).inflate(R.layout.overlay_lock_screen, null)

        // Configure window parameters for system overlay
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        // Setup slide-to-unlock functionality
        setupSlideToUnlock(overlayView!!)

        // Add overlay to window
        windowManager.addView(overlayView, params)
    }

    /**
     * Hide the overlay
     */
    fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View already removed
            }
            overlayView = null
        }
    }

    /**
     * Setup slide-to-unlock interaction
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideToUnlock(view: View) {
        val sliderButton = view.findViewById<FrameLayout>(R.id.sliderButton)
        val container = sliderButton.parent as FrameLayout

        var initialX = 0f
        var initialButtonX = 0f

        sliderButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    initialButtonX = v.x
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialX
                    val newX = initialButtonX + deltaX

                    // Constrain movement within container
                    val maxX = container.width - sliderButton.width - 8f // 8dp margin
                    v.x = newX.coerceIn(0f, maxX)

                    // Check if slid to end (90% of max)
                    if (v.x >= maxX * 0.9f) {
                        // Unlock triggered
                        onUnlockRequested?.invoke()
                        return@setOnTouchListener true
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Snap back to start if not completed
                    v.animate().x(4f).setDuration(200).start()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Check if overlay is currently showing
     */
    fun isShowing(): Boolean = overlayView != null
}
