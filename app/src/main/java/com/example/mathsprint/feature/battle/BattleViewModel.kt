package com.example.mathsprint.feature.battle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.data.repository.GameRepository
import com.example.mathsprint.data.repository.UserRepository
import com.example.mathsprint.domain.model.Difficulty
import com.example.mathsprint.domain.model.GameMode
import com.example.mathsprint.domain.model.GameSession
import com.example.mathsprint.domain.model.MathQuestion
import com.example.mathsprint.domain.model.Operation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// ============== LEGACY MODELS (FOR COMPATIBILITY) ==============
data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

// ============== NEW UI STATE ==============
data class BattleUiState(
    // Legacy fields
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val lives: Int = 3,
    val selectedOption: Int? = null,
    val isAnswered: Boolean = false,
    val isFinished: Boolean = false,
    val timeLeftSeconds: Int = 30,

    // New game fields
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSession: GameSession? = null,
    val currentQuestion: MathQuestion? = null,
    val questionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val timeElapsedSeconds: Int = 0,
    val selectedAnswer: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val isAnswerCorrect: Boolean? = null,
    val showResult: Boolean = false,
    val resultMessage: String = "",
    val gameComplete: Boolean = false,
    val finalScore: Int = 0,
    val finalAccuracy: Float = 0f
)

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val TAG = "BattleViewModel"

    private val _uiState = MutableStateFlow(BattleUiState())
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private var sessionTimer: Long = 0

    // ============== LEGACY METHODS ==============

    fun loadLesson(chapterId: Int, lessonId: Int) {
        val questions = generateQuestions(chapterId, lessonId)
        _uiState.update { it.copy(questions = questions) }
    }

    fun selectOption(index: Int) {
        val state = _uiState.value
        if (state.isAnswered) return
        val correct = state.questions[state.currentIndex].correctIndex
        val isCorrect = index == correct
        _uiState.update {
            it.copy(
                selectedOption = index,
                isAnswered = true,
                score = if (isCorrect) it.score + 1 else it.score,
                lives = if (!isCorrect) it.lives - 1 else it.lives
            )
        }
    }

    fun nextQuestion(chapterId: Int, lessonId: Int, uid: String) {
        val state = _uiState.value
        if (state.currentIndex + 1 >= state.questions.size || state.lives <= 0) {
            _uiState.update { it.copy(isFinished = true) }
            viewModelScope.launch {
                userRepository.completeLesson(chapterId, lessonId, state.score, uid)
            }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOption = null,
                    isAnswered = false,
                    timeLeftSeconds = 30
                )
            }
        }
    }

    private fun generateQuestions(chapterId: Int, lessonId: Int): List<Question> {
        return (1..5).map {
            val a = Random.nextInt(2, 15)
            val b = Random.nextInt(2, 10)
            val answer = a * b
            val wrong1 = answer + Random.nextInt(1, 5)
            val wrong2 = answer - Random.nextInt(1, 5)
            val wrong3 = a + b
            val options = listOf(answer, wrong1, wrong2, wrong3).shuffled()
            val correctIndex = options.indexOf(answer)
            Question(
                text = "What is $a × $b?",
                options = options.map { it.toString() },
                correctIndex = correctIndex
            )
        }
    }

    // ============== NEW GAME METHODS ==============

    /**
     * Start a new game/battle
     */
    fun startGame(
        userId: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        operation: Operation,
        questionCount: Int = 10
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Create game session
                val session = gameRepository.createSession(
                    userId = userId,
                    gameMode = gameMode,
                    difficulty = difficulty,
                    operation = operation,
                    totalQuestions = questionCount
                )

                // Generate questions
                val questions = when (gameMode) {
                    GameMode.DAILY_CHALLENGE -> gameRepository.generateDailyChallenge()
                    else -> gameRepository.generateQuiz(difficulty, operation, questionCount)
                }

                // Save questions to database
                gameRepository.saveSessionQuestions(
                    sessionId = session.sessionId,
                    userId = userId,
                    questions = questions
                )

                // Initialize state
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        currentSession = session,
                        totalQuestions = questions.size,
                        currentQuestion = questions.firstOrNull()
                    )
                }

                sessionTimer = System.currentTimeMillis()
                startTimer()

                Log.d(TAG, "Game started: ${questions.size} questions, Mode: $gameMode, Difficulty: $difficulty")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting game", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to start game: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Select an answer option
     */
    fun selectAnswer(answer: Int) {
        _uiState.update { it.copy(selectedAnswer = answer) }
    }

    /**
     * Submit the selected answer
     */
    fun submitAnswer() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedAnswer = state.selectedAnswer
            val currentQuestion = state.currentQuestion

            if (selectedAnswer == null || currentQuestion == null) {
                return@launch
            }

            val isCorrect = selectedAnswer == currentQuestion.correctAnswer
            val newCorrect = if (isCorrect) state.correctAnswers + 1 else state.correctAnswers
            val newIncorrect = if (!isCorrect) state.incorrectAnswers + 1 else state.incorrectAnswers

            // Submit to database
            try {
                gameRepository.submitAnswer(
                    questionId = currentQuestion.id,
                    userAnswer = selectedAnswer,
                    timeSpentSeconds = 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting answer", e)
            }

            _uiState.update {
                it.copy(
                    isAnswerSubmitted = true,
                    isAnswerCorrect = isCorrect,
                    correctAnswers = newCorrect,
                    incorrectAnswers = newIncorrect,
                    showResult = true,
                    resultMessage = if (isCorrect) "✓ Correct!" else "✗ Incorrect! Answer: ${currentQuestion.correctAnswer}"
                )
            }

            Log.d(TAG, "Answer submitted: Correct=$isCorrect, Score=$newCorrect/${state.totalQuestions}")
        }
    }

    /**
     * Skip current question (shows answer and moves on)
     */
    fun skipQuestion() {
        val state = _uiState.value
        val currentQuestion = state.currentQuestion ?: return

        // Mark as skipped but not answered
        _uiState.update {
            it.copy(
                isAnswerSubmitted = true,
                isAnswerCorrect = false,  // Treated as incorrect
                showResult = true,
                resultMessage = "❌ Skipped! Answer: ${currentQuestion.correctAnswer}",
                incorrectAnswers = it.incorrectAnswers + 1
            )
        }

        Log.d(TAG, "Question skipped: ${currentQuestion.questionText}")
    }

    /**
     * Move to next question
     */
    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.questionIndex + 1

        if (nextIndex < state.totalQuestions) {
            // Update session progress
            viewModelScope.launch {
                val session = state.currentSession ?: return@launch
                gameRepository.updateSessionProgress(
                    sessionId = session.sessionId,
                    questionsAnswered = nextIndex,
                    correctAnswers = state.correctAnswers,
                    timeSpentSeconds = getElapsedSeconds()
                )
            }

            _uiState.update {
                it.copy(
                    questionIndex = nextIndex,
                    selectedAnswer = null,
                    isAnswerSubmitted = false,
                    isAnswerCorrect = null,
                    showResult = false,
                    resultMessage = ""
                )
            }
        } else {
            // Game complete
            completeGame()
        }
    }

    /**
     * Complete the game session
     */
    private fun completeGame() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = state.currentSession ?: return@launch

            try {
                // Mark session as complete
                gameRepository.completeSession(session.sessionId)

                // Calculate metrics
                val accuracy = if (state.totalQuestions > 0) {
                    (state.correctAnswers.toFloat() / state.totalQuestions) * 100f
                } else {
                    0f
                }

                val timeSpent = getElapsedSeconds()

                // Calculate rewards
                val xpEarned = calculateXP(state.correctAnswers, state.totalQuestions, accuracy)
                val coinsEarned = calculateCoins(accuracy)
                val gemsEarned = calculateGems(state.correctAnswers, state.totalQuestions)

                // Save result
                gameRepository.saveGameResult(
                    userId = session.userId,
                    sessionId = session.sessionId,
                    gameMode = session.gameMode,
                    difficulty = session.difficulty,
                    operation = session.operation,
                    score = state.correctAnswers,
                    totalQuestions = state.totalQuestions,
                    accuracy = accuracy,
                    timeSpentSeconds = timeSpent,
                    xpEarned = xpEarned,
                    coinsEarned = coinsEarned,
                    gemsEarned = gemsEarned
                )

                _uiState.update {
                    it.copy(
                        gameComplete = true,
                        finalScore = state.correctAnswers,
                        finalAccuracy = accuracy,
                        resultMessage = "Game Complete! Score: ${state.correctAnswers}/${state.totalQuestions}"
                    )
                }

                Log.d(
                    TAG,
                    "Game completed: Score=${state.correctAnswers}, Accuracy=$accuracy%, XP=$xpEarned, Coins=$coinsEarned, Gems=$gemsEarned"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error completing game", e)
                _uiState.update {
                    it.copy(
                        error = "Failed to save game result: ${e.message}"
                    )
                }
            }
        }
    }

    // ============== UTILITY FUNCTIONS ==============

    /**
     * Start automatic timer
     */
    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(1000)
                    _uiState.update {
                        it.copy(timeElapsedSeconds = getElapsedSeconds())
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    /**
     * Get elapsed time in seconds
     */
    private fun getElapsedSeconds(): Int {
        return if (sessionTimer > 0) {
            ((System.currentTimeMillis() - sessionTimer) / 1000).toInt()
        } else {
            0
        }
    }

    /**
     * Calculate XP earned based on performance
     */
    private fun calculateXP(correct: Int, total: Int, accuracy: Float): Int {
        val baseXP = correct * 10
        val accuracyBonus = (accuracy / 100f * 50).toInt()
        return baseXP + accuracyBonus
    }

    /**
     * Calculate coins earned
     */
    private fun calculateCoins(accuracy: Float): Int {
        return when {
            accuracy >= 90f -> 50
            accuracy >= 70f -> 30
            accuracy >= 50f -> 15
            else -> 5
        }
    }

    /**
     * Calculate gems earned (premium currency)
     */
    private fun calculateGems(correct: Int, total: Int): Int {
        return if (correct == total) 5 else 0
    }

    /**
     * Reset game state
     */
    fun resetGame() {
        _uiState.update {
            BattleUiState()
        }
        gameRepository.clearSessionCache()
    }

    /**
     * Format time for display (MM:SS)
     */
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
}

