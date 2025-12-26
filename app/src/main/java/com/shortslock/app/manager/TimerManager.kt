package com.shortslock.app.manager

import android.os.Handler
import android.os.Looper
import com.shortslock.app.data.PreferencesManager

/**
 * Manages timer countdown and state transitions
 */
class TimerManager(private val prefsManager: PreferencesManager) {

    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var onTimerExpired: (() -> Unit)? = null
    private var onTimerTick: ((Long) -> Unit)? = null

    /**
     * Start counting down the timer
     * Only counts down when Shorts is actively being viewed
     */
    fun startTimer(onExpired: () -> Unit, onTick: (Long) -> Unit) {
        this.onTimerExpired = onExpired
        this.onTimerTick = onTick

        // Update last update time
        prefsManager.lastUpdateTime = System.currentTimeMillis()

        startTimerTick()
    }

    /**
     * Decrement timer by elapsed time since last update
     */
    fun decrementTimer() {
        val currentTime = System.currentTimeMillis()
        val lastUpdate = prefsManager.lastUpdateTime
        val elapsedSeconds = (currentTime - lastUpdate) / 1000

        if (elapsedSeconds > 0) {
            val remaining = prefsManager.remainingTime - elapsedSeconds
            prefsManager.remainingTime = maxOf(0L, remaining)
            prefsManager.lastUpdateTime = currentTime

            // Check if timer expired
            if (prefsManager.remainingTime <= 0 && !prefsManager.isLocked) {
                prefsManager.isLocked = true
                onTimerExpired?.invoke()
            }

            onTimerTick?.invoke(prefsManager.remainingTime)
        }
    }

    /**
     * Start periodic timer tick (every second)
     */
    private fun startTimerTick() {
        timerRunnable = object : Runnable {
            override fun run() {
                // Timer only decrements when actively viewing Shorts
                // The AccessibilityService will call decrementTimer() when Shorts is detected
                
                // Check temp unlock expiration
                if (prefsManager.isTempUnlocked) {
                    if (System.currentTimeMillis() >= prefsManager.tempUnlockEndTime) {
                        // Temp unlock expired, re-lock
                        prefsManager.isTempUnlocked = false
                        prefsManager.isLocked = true
                        onTimerExpired?.invoke()
                    }
                }

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    /**
     * Stop the timer
     */
    fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }

    /**
     * Activate temporary unlock (10 minutes)
     */
    fun activateTempUnlock() {
        val unlockDurationMs = 10 * 60 * 1000L // 10 minutes
        prefsManager.isTempUnlocked = true
        prefsManager.isLocked = false
        prefsManager.tempUnlockEndTime = System.currentTimeMillis() + unlockDurationMs
    }

    /**
     * Get current state
     */
    fun getCurrentState(): TimerState {
        return when {
            !prefsManager.isMonitoring -> TimerState.INACTIVE
            prefsManager.isTempUnlocked -> TimerState.TEMP_UNLOCK
            prefsManager.isLocked -> TimerState.LOCKED
            else -> TimerState.ACTIVE
        }
    }

    enum class TimerState {
        INACTIVE,
        ACTIVE,
        LOCKED,
        TEMP_UNLOCK
    }
}
