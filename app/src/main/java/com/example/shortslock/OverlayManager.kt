
package com.example.shortslock

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.shortslock.databinding.OverlayLayoutBinding

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null

    fun showOverlay() {
        if (overlayView == null) {
            val inflater = LayoutInflater.from(context)
            val binding = OverlayLayoutBinding.inflate(inflater)
            overlayView = binding.root

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            binding.unlockSlider.addOnChangeListener { slider, value, fromUser ->
                if (value == slider.valueTo) {
                    val paymentManager = PaymentManager(context)
                    paymentManager.launchUpiIntent()
                    context.sendBroadcast(Intent("UNLOCK_ACTION"))
                }
            }
            windowManager.addView(overlayView, params)
        }
    }

    fun hideOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }
}
