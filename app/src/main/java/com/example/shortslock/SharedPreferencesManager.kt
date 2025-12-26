
package com.example.shortslock

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ShortsLockPrefs", Context.MODE_PRIVATE)

    fun getTimerValue(): Long {
        return sharedPreferences.getLong("TIMER_VALUE", 60)
    }

    fun setTimerValue(minutes: Long) {
        sharedPreferences.edit().putLong("TIMER_VALUE", minutes).apply()
    }

    fun getStatus(): LockStatus {
        return LockStatus.valueOf(sharedPreferences.getString("STATUS", LockStatus.INACTIVE.name) ?: LockStatus.INACTIVE.name)
    }

    fun setStatus(status: LockStatus) {
        sharedPreferences.edit().putString("STATUS", status.name).apply()
    }
    
    fun getRemainingTime(): Long {
        return sharedPreferences.getLong("REMAINING_TIME", 0)
    }

    fun setRemainingTime(millis: Long) {
        sharedPreferences.edit().putLong("REMAINING_TIME", millis).apply()
    }
}
