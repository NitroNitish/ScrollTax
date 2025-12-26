
package com.example.shortslock

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class LockStatus { INACTIVE, ACTIVE, LOCKED, TEMP_UNLOCK }

class MainViewModel : ViewModel() {

    private val _status = MutableLiveData<LockStatus>(LockStatus.INACTIVE)
    val status: LiveData<LockStatus> = _status

    private val _timerValue = MutableLiveData<Long>(30)
    val timerValue: LiveData<Long> = _timerValue

    fun setTimerValue(minutes: Long) {
        _timerValue.value = minutes
    }

    fun setStatus(newStatus: LockStatus) {
        _status.value = newStatus
    }
}
