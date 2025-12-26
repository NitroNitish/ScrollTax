package com.shortslock.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.shortslock.app.R
import com.shortslock.app.data.PreferencesManager
import com.shortslock.app.manager.TimerManager
import com.shortslock.app.service.MonitoringForegroundService

/**
 * Main Activity - User interface for timer configuration and monitoring control
 */
class MainActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var timerManager: TimerManager

    private lateinit var statusText: TextView
    private lateinit var timerValueText: TextView
    private lateinit var timerSeekBar: SeekBar
    private lateinit var startButton: Button
    private lateinit var remainingTimeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize managers
        prefsManager = PreferencesManager(this)
        timerManager = TimerManager(prefsManager)

        // Initialize views
        statusText = findViewById(R.id.statusText)
        timerValueText = findViewById(R.id.timerValueText)
        timerSeekBar = findViewById(R.id.timerSeekBar)
        startButton = findViewById(R.id.startButton)
        remainingTimeText = findViewById(R.id.remainingTimeText)

        // Setup UI
        setupSeekBar()
        setupStartButton()
        updateUI()

        // Check permissions on start
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    /**
     * Setup timer seekbar
     */
    private fun setupSeekBar() {
        timerSeekBar.max = 60
        timerSeekBar.progress = prefsManager.timerDuration

        timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timerValueText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefsManager.timerDuration = seekBar?.progress ?: 30
            }
        })

        timerValueText.text = timerSeekBar.progress.toString()
    }

    /**
     * Setup start/stop button
     */
    private fun setupStartButton() {
        startButton.setOnClickListener {
            if (prefsManager.isMonitoring) {
                // Stop monitoring
                stopMonitoring()
            } else {
                // Start monitoring
                startMonitoring()
            }
        }
    }

    /**
     * Start monitoring session
     */
    private fun startMonitoring() {
        val duration = timerSeekBar.progress
        if (duration == 0) {
            showMessage("Please set a timer duration")
            return
        }

        // Check permissions
        if (!hasRequiredPermissions()) {
            checkPermissions()
            return
        }

        // Start monitoring
        prefsManager.startMonitoring(duration)

        // Start foreground service
        startForegroundService(Intent(this, MonitoringForegroundService::class.java))

        updateUI()
        showMessage("Monitoring started - ${duration} minutes")
    }

    /**
     * Stop monitoring session
     */
    private fun stopMonitoring() {
        prefsManager.stopMonitoring()
        
        // Stop foreground service
        stopService(Intent(this, MonitoringForegroundService::class.java))

        updateUI()
        showMessage("Monitoring stopped")
    }

    /**
     * Update UI based on current state
     */
    private fun updateUI() {
        val state = timerManager.getCurrentState()

        when (state) {
            TimerManager.TimerState.INACTIVE -> {
                statusText.text = "INACTIVE"
                statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                startButton.text = "START MONITORING"
                startButton.backgroundTintList = getColorStateList(android.R.color.holo_green_dark)
                timerSeekBar.isEnabled = true
                remainingTimeText.visibility = TextView.GONE
            }
            TimerManager.TimerState.ACTIVE -> {
                statusText.text = "ACTIVE"
                statusText.setTextColor(getColor(android.R.color.holo_green_dark))
                startButton.text = "STOP MONITORING"
                startButton.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                timerSeekBar.isEnabled = false
                
                val remaining = prefsManager.remainingTime
                val minutes = remaining / 60
                val seconds = remaining % 60
                remainingTimeText.text = "Time Remaining: ${minutes}m ${seconds}s"
                remainingTimeText.visibility = TextView.VISIBLE
            }
            TimerManager.TimerState.LOCKED -> {
                statusText.text = "LOCKED"
                statusText.setTextColor(getColor(android.R.color.holo_red_dark))
                startButton.text = "STOP MONITORING"
                startButton.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                timerSeekBar.isEnabled = false
                remainingTimeText.text = "Shorts Locked - Pay ₹49 to unlock"
                remainingTimeText.visibility = TextView.VISIBLE
            }
            TimerManager.TimerState.TEMP_UNLOCK -> {
                statusText.text = "TEMP UNLOCK"
                statusText.setTextColor(getColor(android.R.color.holo_orange_dark))
                startButton.text = "STOP MONITORING"
                startButton.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                timerSeekBar.isEnabled = false
                
                val remaining = (prefsManager.tempUnlockEndTime - System.currentTimeMillis()) / 1000
                val minutes = remaining / 60
                val seconds = remaining % 60
                remainingTimeText.text = "Temp Unlock: ${minutes}m ${seconds}s"
                remainingTimeText.visibility = TextView.VISIBLE
            }
        }
    }

    /**
     * Check if required permissions are granted
     */
    private fun hasRequiredPermissions(): Boolean {
        val overlayPermission = Settings.canDrawOverlays(this)
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        return overlayPermission && accessibilityEnabled
    }

    /**
     * Check if accessibility service is enabled
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/.service.ShortsDetectionService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }

    /**
     * Prompt user to grant required permissions
     */
    private fun checkPermissions() {
        val overlayPermission = Settings.canDrawOverlays(this)
        val accessibilityEnabled = isAccessibilityServiceEnabled()

        if (!overlayPermission || !accessibilityEnabled) {
            val message = buildString {
                append("ShortsLock requires the following permissions:\n\n")
                if (!overlayPermission) append("• Display over other apps\n")
                if (!accessibilityEnabled) append("• Accessibility Service\n")
                append("\nPlease grant these permissions to continue.")
            }

            AlertDialog.Builder(this)
                .setTitle("Permissions Required")
                .setMessage(message)
                .setPositiveButton("Grant") { _, _ ->
                    if (!overlayPermission) {
                        openOverlaySettings()
                    } else if (!accessibilityEnabled) {
                        openAccessibilitySettings()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    /**
     * Open overlay permission settings
     */
    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    /**
     * Open accessibility settings
     */
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    /**
     * Show toast message
     */
    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
