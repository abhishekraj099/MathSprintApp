package com.example.mathsprint.feature.multiplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.core.theme.MathSprintTheme
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

data class MultiplicationTableUIState(
    val selectedTable: Int = 7,            // Default multiplication table (7x)
    val selectedDifficulty: Difficulty = Difficulty.MEDIUM,
    val isStarting: Boolean = false,
    val availableTables: List<Int> = (1..20).toList()
)

@HiltViewModel
class MultiplicationTableViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiplicationTableUIState())
    val uiState: StateFlow<MultiplicationTableUIState> = _uiState.asStateFlow()

    fun selectTable(tableNumber: Int) {
        _uiState.update { it.copy(selectedTable = tableNumber) }
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun startMultiplicationGame(userId: String, questionCount: Int = 10) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isStarting = true) }
            // Will trigger GameScreen with MULTIPLICATION_TABLE operation
            _uiState.update { it.copy(isStarting = false) }
        }
    }
}

