
package com.example.shortslock

import android.os.CountDownTimer

class TimerManager(private val sharedPreferencesManager: SharedPreferencesManager) {

    private var timer: CountDownTimer? = null

    fun startTimer(millisInFuture: Long, onFinish: () -> Unit) {
        timer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sharedPreferencesManager.setRemainingTime(millisUntilFinished)
            }

            override fun onFinish() {
                sharedPreferencesManager.setRemainingTime(0)
                onFinish()
            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel()
    }

    fun cancelTimer() {
        timer?.cancel()
        sharedPreferencesManager.setRemainingTime(0)
    }
}
