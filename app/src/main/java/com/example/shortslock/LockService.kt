
package com.example.shortslock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class LockService : Service() {

    private lateinit var timerManager: TimerManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "UNLOCK_ACTION" -> {
                    overlayManager.hideOverlay()
                    sharedPreferencesManager.setStatus(LockStatus.TEMP_UNLOCK)
                    startTempUnlockTimer()
                }
                "PAUSE_TIMER" -> timerManager.pauseTimer()
                "RESUME_TIMER" -> {
                    val remainingTime = sharedPreferencesManager.getRemainingTime()
                    if (remainingTime > 0) {
                        timerManager.startTimer(remainingTime) {
                            overlayManager.showOverlay()
                            sharedPreferencesManager.setStatus(LockStatus.LOCKED)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferencesManager = SharedPreferencesManager(this)
        timerManager = TimerManager(sharedPreferencesManager)
        overlayManager = OverlayManager(this)
        val filter = IntentFilter().apply {
            addAction("UNLOCK_ACTION")
            addAction("PAUSE_TIMER")
            addAction("RESUME_TIMER")
        }
        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timerValue = sharedPreferencesManager.getTimerValue()
        val remainingTime = sharedPreferencesManager.getRemainingTime()
        val startTime = if (remainingTime > 0) remainingTime else timerValue * 60 * 1000

        val notification = createNotification()
        startForeground(1, notification)

        if (sharedPreferencesManager.getStatus() != LockStatus.LOCKED) {
            timerManager.startTimer(startTime) {
                overlayManager.showOverlay()
                sharedPreferencesManager.setStatus(LockStatus.LOCKED)
            }
        }

        return START_STICKY
    }

    fun startTempUnlockTimer() {
        timerManager.startTimer(10 * 60 * 1000) {
            overlayManager.showOverlay()
            sharedPreferencesManager.setStatus(LockStatus.LOCKED)
        }
    }

    private fun createNotification(): android.app.Notification {
        val notificationChannelId = "LOCK_SERVICE_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId, 
                "Lock Service", 
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("ShortsLock is active")
            .setContentText("Monitoring YouTube usage.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        timerManager.cancelTimer()
        unregisterReceiver(receiver)
    }
}
