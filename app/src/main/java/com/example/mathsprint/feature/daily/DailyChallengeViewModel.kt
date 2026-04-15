package com.example.mathsprint.feature.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.concurrent.TimeUnit

data class DailyChallengeUiState(
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val reward: Int = 50,
    val timeUntilNextChallenge: String = "00h 00m",
    val isLoading: Boolean = false,
    val canTakeChallengeToday: Boolean = true
)

@HiltViewModel
class DailyChallengeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DailyChallengeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDailyChallenge()
    }

    private fun loadDailyChallenge() {
        viewModelScope.launch {
            // TODO: Load from database
            updateTimeUntilNextChallenge()
        }
    }

    fun completedDaily(score: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCompleted = true,
                score = score,
                canTakeChallengeToday = false
            )
            // TODO: Save to database and Firebase
        }
    }

    private fun updateTimeUntilNextChallenge() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val nextDay = now + TimeUnit.DAYS.toMillis(1)
            val remaining = nextDay - now

            val hours = TimeUnit.MILLISECONDS.toHours(remaining)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60

            _uiState.value = _uiState.value.copy(
                timeUntilNextChallenge = String.format("%02dh %02dm", hours, minutes)
            )
        }
    }
}

