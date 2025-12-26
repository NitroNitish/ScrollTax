package com.shortslock.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent storage for app state using SharedPreferences
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ShortsLockPrefs"
        private const val KEY_TIMER_DURATION = "timer_duration"
        private const val KEY_REMAINING_TIME = "remaining_time"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_IS_TEMP_UNLOCKED = "is_temp_unlocked"
        private const val KEY_TEMP_UNLOCK_END_TIME = "temp_unlock_end_time"
        private const val KEY_LAST_UPDATE_TIME = "last_update_time"
        private const val KEY_IS_MONITORING = "is_monitoring"
    }

    // Timer duration in minutes (0-60)
    var timerDuration: Int
        get() = prefs.getInt(KEY_TIMER_DURATION, 30)
        set(value) = prefs.edit().putInt(KEY_TIMER_DURATION, value).apply()

    // Remaining time in seconds
    var remainingTime: Long
        get() = prefs.getLong(KEY_REMAINING_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_REMAINING_TIME, value).apply()

    // Is Shorts currently locked
    var isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOCKED, value).apply()

    // Is temporarily unlocked (10 min paid unlock)
    var isTempUnlocked: Boolean
        get() = prefs.getBoolean(KEY_IS_TEMP_UNLOCKED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_TEMP_UNLOCKED, value).apply()

    // Timestamp when temp unlock expires
    var tempUnlockEndTime: Long
        get() = prefs.getLong(KEY_TEMP_UNLOCK_END_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_TEMP_UNLOCK_END_TIME, value).apply()

    // Last time the timer was updated (for calculating elapsed time)
    var lastUpdateTime: Long
        get() = prefs.getLong(KEY_LAST_UPDATE_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_UPDATE_TIME, value).apply()

    // Is monitoring currently active
    var isMonitoring: Boolean
        get() = prefs.getBoolean(KEY_IS_MONITORING, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_MONITORING, value).apply()

    /**
     * Reset all state to defaults
     */
    fun reset() {
        prefs.edit().clear().apply()
    }

    /**
     * Start a new monitoring session
     */
    fun startMonitoring(durationMinutes: Int) {
        timerDuration = durationMinutes
        remainingTime = durationMinutes * 60L
        isLocked = false
        isTempUnlocked = false
        tempUnlockEndTime = 0L
        lastUpdateTime = System.currentTimeMillis()
        isMonitoring = true
    }

    /**
     * Stop monitoring session
     */
    fun stopMonitoring() {
        isMonitoring = false
        isLocked = false
        isTempUnlocked = false
    }
}
