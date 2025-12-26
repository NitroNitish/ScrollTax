package com.shortslock.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.shortslock.app.data.PreferencesManager
import com.shortslock.app.manager.OverlayManager
import com.shortslock.app.manager.PaymentManager
import com.shortslock.app.manager.TimerManager

/**
 * Accessibility Service that detects YouTube Shorts and enforces time limits
 */
class ShortsDetectionService : AccessibilityService() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var timerManager: TimerManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var paymentManager: PaymentManager

    private var isShortsActive = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        prefsManager = PreferencesManager(this)
        timerManager = TimerManager(prefsManager)
        overlayManager = OverlayManager(this)
        paymentManager = PaymentManager(this)

        // Start foreground service to keep process alive
        startForegroundService(Intent(this, MonitoringForegroundService::class.java))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Only process events from YouTube
        if (event.packageName != "com.google.android.youtube") return

        // Only process if monitoring is active
        if (!prefsManager.isMonitoring) return

        // Detect if Shorts is currently active
        val rootNode = rootInActiveWindow ?: return
        val shortsDetected = detectShorts(rootNode)
        rootNode.recycle()

        // Handle Shorts state changes
        if (shortsDetected && !isShortsActive) {
            // User just entered Shorts
            onShortsEntered()
        } else if (!shortsDetected && isShortsActive) {
            // User left Shorts
            onShortsExited()
        }

        // If Shorts is active, decrement timer
        if (shortsDetected && isShortsActive) {
            timerManager.decrementTimer()
            
            // Check if should show overlay
            if (prefsManager.isLocked && !prefsManager.isTempUnlocked) {
                if (!overlayManager.isShowing()) {
                    showLockOverlay()
                }
            } else {
                // Not locked, hide overlay if showing
                if (overlayManager.isShowing()) {
                    overlayManager.hideOverlay()
                }
            }
        }
    }

    /**
     * Detect if YouTube Shorts is currently active
     * Looks for "Shorts" text nodes and vertical pager layout
     */
    private fun detectShorts(node: AccessibilityNodeInfo): Boolean {
        // Strategy 1: Look for "Shorts" text
        if (node.text?.toString()?.contains("Shorts", ignoreCase = true) == true) {
            return true
        }

        // Strategy 2: Look for Shorts-specific view IDs or class names
        val viewId = node.viewIdResourceName
        if (viewId?.contains("shorts", ignoreCase = true) == true ||
            viewId?.contains("reel", ignoreCase = true) == true) {
            return true
        }

        // Strategy 3: Check for vertical scrolling container (Shorts uses vertical pager)
        val className = node.className?.toString()
        if (className?.contains("ViewPager", ignoreCase = true) == true ||
            className?.contains("RecyclerView", ignoreCase = true) == true) {
            // Additional check: if parent or sibling contains "Shorts"
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                if (detectShorts(child)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }

        // Recursively check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (detectShorts(child)) {
                child.recycle()
                return true
            }
            child.recycle()
        }

        return false
    }

    /**
     * Called when user enters Shorts
     */
    private fun onShortsEntered() {
        isShortsActive = true
        
        // Start timer if not already started
        if (!timerManager.getCurrentState().name.startsWith("ACTIVE")) {
            timerManager.startTimer(
                onExpired = {
                    // Timer expired, show lock overlay
                    showLockOverlay()
                },
                onTick = { remainingSeconds ->
                    // Timer tick - could broadcast to UI if needed
                }
            )
        }
    }

    /**
     * Called when user exits Shorts
     */
    private fun onShortsExited() {
        isShortsActive = false
        
        // Hide overlay if showing
        if (overlayManager.isShowing()) {
            overlayManager.hideOverlay()
        }
    }

    /**
     * Show the lock overlay
     */
    private fun showLockOverlay() {
        overlayManager.showOverlay {
            // User completed slide-to-unlock
            handleUnlockRequest()
        }
    }

    /**
     * Handle unlock request - launch payment
     */
    private fun handleUnlockRequest() {
        // Launch UPI payment
        val success = paymentManager.launchPayment()
        
        if (success) {
            // Assume payment success (trust-based)
            // Activate 10-minute temp unlock
            timerManager.activateTempUnlock()
            
            // Hide overlay
            overlayManager.hideOverlay()
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        timerManager.stopTimer()
        overlayManager.hideOverlay()
    }
}
