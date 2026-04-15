package com.example.mathsprint.feature.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.data.repository.GameRepository
import com.example.mathsprint.domain.model.Difficulty
import com.example.mathsprint.domain.model.GameMode
import com.example.mathsprint.domain.model.Operation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryGameUIState(
    val score: Int = 0,
    val streak: Int = 0,
    val level: Int = 1,
    val isLoading: Boolean = false,
    val gameActive: Boolean = false,
    val difficulty: Difficulty = Difficulty.EASY
)

@HiltViewModel
class ArithmeticMemoryViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryGameUIState())
    val uiState: StateFlow<MemoryGameUIState> = _uiState.asStateFlow()

    fun startMemoryGame(userId: String, difficulty: Difficulty) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    gameActive = true,
                    difficulty = difficulty,
                    score = 0,
                    streak = 0,
                    level = 1
                )
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun incrementStreak() {
        _uiState.update {
            val newStreak = it.streak + 1
            val newLevel = (newStreak / 5) + 1
            it.copy(
                streak = newStreak,
                level = newLevel,
                score = it.score + (10 * newLevel)
            )
        }
    }

    fun resetStreak() {
        _uiState.update { it.copy(streak = 0) }
    }

    fun endGame() {
        _uiState.update { it.copy(gameActive = false) }
    }
}

