package com.example.mathsprint.domain.model

/**
 * Enum for supported arithmetic operations
 */
enum class Operation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    MIXED,
    SQUARE_ROOT,
    EXPONENT,
    MULTIPLICATION_TABLE,  // Customizable times table
    MEMORY_GAME           // Arithmetic memory challenge
}

/**
 * Enum for difficulty levels
 */
enum class Difficulty {
    EASY,      // Numbers 1-10
    MEDIUM,    // Numbers 10-50
    HARD,      // Numbers 50-100
    EXPERT     // Numbers 100-500
}

/**
 * Enum for game modes
 */
enum class GameMode {
    PRACTICE,          // Unlimited questions
    QUIZ,              // Fixed: 10, 20, or 40 questions
    TIMED,             // Time-based challenge
    DAILY_CHALLENGE    // Seed-based, same for all users daily
}

/**
 * Core data class representing a math question
 */
data class MathQuestion(
    val id: String,                    // Unique identifier
    val questionText: String,          // e.g., "12 + 8"
    val correctAnswer: Int,            // Correct answer
    val options: List<Int>,            // 4 multiple choice options
    val difficulty: Difficulty,        // EASY, MEDIUM, HARD, EXPERT
    val operation: Operation,          // Operation type
    val timestamp: Long = System.currentTimeMillis(), // When generated
    val numOperands: Int = 2           // Number of operands (for mixed mode)
)

/**
 * Data class for game session tracking
 */
data class GameSession(
    val sessionId: String,
    val userId: String,
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val operation: Operation,
    val totalQuestions: Int,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val timeSpentSeconds: Int = 0
) {
    val accuracy: Float
        get() = if (questionsAnswered > 0) (correctAnswers.toFloat() / questionsAnswered) * 100f else 0f

    val isCompleted: Boolean
        get() = questionsAnswered >= totalQuestions
}

/**
 * Data class for storing game results
 */
data class GameResult(
    val resultId: String,
    val userId: String,
    val sessionId: String,
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val operation: Operation,
    val score: Int,                    // Total correct answers
    val totalQuestions: Int,
    val accuracy: Float,               // Percentage
    val timeSpentSeconds: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val gemsEarned: Int,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data class for user statistics
 */
data class UserStats(
    val userId: String,
    val totalGamesPlayed: Int = 0,
    val totalCorrect: Int = 0,
    val totalQuestions: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageAccuracy: Float = 0f,
    val totalXpEarned: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalGemsEarned: Int = 0,
    val bestScore: Int = 0,
    val lastPlayedAt: Long = 0,
    val adaptiveDifficulty: Difficulty = Difficulty.EASY  // Current adaptive difficulty
)

