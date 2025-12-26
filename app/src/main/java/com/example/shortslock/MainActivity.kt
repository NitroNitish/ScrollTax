
package com.example.shortslock

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.shortslock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferencesManager = SharedPreferencesManager(this)

        viewModel.status.observe(this) {
            binding.statusIndicator.text = "Status: ${it.name}"
            sharedPreferencesManager.setStatus(it)
        }

        viewModel.timerValue.observe(this) {
            binding.timerTextview.text = "$it minutes"
            sharedPreferencesManager.setTimerValue(it)
        }

        binding.timerSlider.value = sharedPreferencesManager.getTimerValue().toFloat()
        binding.timerSlider.addOnChangeListener { _, value, _ ->
            viewModel.setTimerValue(value.toLong())
        }

        binding.startButton.setOnClickListener {
            val intent = Intent(this, LockService::class.java)
            startService(intent)
            viewModel.setStatus(LockStatus.ACTIVE)
        }
    }
}
