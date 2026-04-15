package com.example.mathsprint.data.repository

import android.util.Log
import com.example.mathsprint.core.generator.QuestionGenerator
import com.example.mathsprint.data.local.dao.QuestionDao
import com.example.mathsprint.data.local.dao.GameSessionDao
import com.example.mathsprint.data.local.dao.GameResultDao
import com.example.mathsprint.data.local.dao.UserStatsDao
import com.example.mathsprint.data.local.dao.ActivityLogDao
import com.example.mathsprint.data.local.entity.QuestionEntity
import com.example.mathsprint.data.local.entity.GameSessionEntity
import com.example.mathsprint.data.local.entity.GameResultEntity
import com.example.mathsprint.data.local.entity.UserStatsEntity
import com.example.mathsprint.data.local.entity.ActivityLogEntity
import com.example.mathsprint.domain.model.Difficulty
import com.example.mathsprint.domain.model.GameMode
import com.example.mathsprint.domain.model.GameResult
import com.example.mathsprint.domain.model.GameSession
import com.example.mathsprint.domain.model.MathQuestion
import com.example.mathsprint.domain.model.Operation
import com.example.mathsprint.domain.model.UserStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.abs

/**
 * Repository for managing game questions, sessions, and results
 * Handles both offline (Room) and online (Firebase) operations
 */
class GameRepository(
    private val questionDao: QuestionDao,
    private val gameSessionDao: GameSessionDao,
    private val gameResultDao: GameResultDao,
    private val userStatsDao: UserStatsDao,
    private val activityLogDao: ActivityLogDao,
    private val questionGenerator: QuestionGenerator
) {

    private val TAG = "GameRepository"

    // ============== QUESTION GENERATION ==============

    /**
     * Generate a single question
     */
    fun generateQuestion(
        difficulty: Difficulty,
        operation: Operation
    ): MathQuestion {
        return questionGenerator.generateQuestion(difficulty, operation)
    }

    /**
     * Generate a quiz session
     */
    fun generateQuiz(
        difficulty: Difficulty,
        operation: Operation,
        count: Int
    ): List<MathQuestion> {
        return questionGenerator.generateQuiz(difficulty, operation, count)
    }

    /**
     * Generate daily challenge questions (seeded for reproducibility)
     */
    fun generateDailyChallenge(seed: Long = 0L): List<MathQuestion> {
        return if (seed > 0) {
            questionGenerator.generateDailyChallenge(seed)
        } else {
            questionGenerator.generateDailyChallenge()
        }
    }

    // ============== SESSION MANAGEMENT ==============

    /**
     * Create a new game session
     */
    suspend fun createSession(
        userId: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        operation: Operation,
        totalQuestions: Int
    ): GameSession = withContext(Dispatchers.IO) {
        val sessionId = UUID.randomUUID().toString()
        val session = GameSessionEntity(
            sessionId = sessionId,
            userId = userId,
            gameMode = gameMode.name,
            difficulty = difficulty.name,
            operation = operation.name,
            totalQuestions = totalQuestions
        )
        gameSessionDao.insertSession(session)

        GameSession(
            sessionId = sessionId,
            userId = userId,
            gameMode = gameMode,
            difficulty = difficulty,
            operation = operation,
            totalQuestions = totalQuestions
        )
    }

    /**
     * Get active session for user
     */
    suspend fun getActiveSession(userId: String): GameSession? = withContext(Dispatchers.IO) {
        gameSessionDao.getActiveSession(userId)?.let { entity ->
            GameSession(
                sessionId = entity.sessionId,
                userId = entity.userId,
                gameMode = GameMode.valueOf(entity.gameMode),
                difficulty = Difficulty.valueOf(entity.difficulty),
                operation = Operation.valueOf(entity.operation),
                totalQuestions = entity.totalQuestions,
                questionsAnswered = entity.questionsAnswered,
                correctAnswers = entity.correctAnswers,
                startTime = entity.startTime,
                endTime = entity.endTime,
                timeSpentSeconds = entity.timeSpentSeconds
            )
        }
    }

    /**
     * Update session progress
     */
    suspend fun updateSessionProgress(
        sessionId: String,
        questionsAnswered: Int,
        correctAnswers: Int,
        timeSpentSeconds: Int
    ) = withContext(Dispatchers.IO) {
        val session = gameSessionDao.getSession(sessionId) ?: return@withContext
        gameSessionDao.updateSession(
            session.copy(
                questionsAnswered = questionsAnswered,
                correctAnswers = correctAnswers,
                timeSpentSeconds = timeSpentSeconds
            )
        )
    }

    /**
     * Complete a session
     */
    suspend fun completeSession(sessionId: String) = withContext(Dispatchers.IO) {
        val session = gameSessionDao.getSession(sessionId) ?: return@withContext
        gameSessionDao.updateSession(
            session.copy(
                isCompleted = true,
                endTime = System.currentTimeMillis()
            )
        )
    }

    // ============== QUESTION STORAGE ==============

    /**
     * Save questions for a session
     */
    suspend fun saveSessionQuestions(
        sessionId: String,
        userId: String,
        questions: List<MathQuestion>
    ) = withContext(Dispatchers.IO) {
        val questionEntities = questions.map { q ->
            QuestionEntity(
                questionId = q.id,
                sessionId = sessionId,
                userId = userId,
                questionText = q.questionText,
                correctAnswer = q.correctAnswer,
                difficulty = q.difficulty.name,
                operation = q.operation.name
            )
        }
        questionDao.insertQuestions(questionEntities)
    }

    /**
     * Submit answer for a question
     */
    suspend fun submitAnswer(
        questionId: String,
        userAnswer: Int,
        timeSpentSeconds: Int
    ) = withContext(Dispatchers.IO) {
        val question = questionDao.getQuestion(questionId) ?: return@withContext
        val isCorrect = userAnswer == question.correctAnswer

        questionDao.updateQuestion(
            question.copy(
                userAnswer = userAnswer,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpentSeconds
            )
        )
    }

    // ============== RESULT MANAGEMENT ==============

    /**
     * Save game result
     */
    suspend fun saveGameResult(
        userId: String,
        sessionId: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        operation: Operation,
        score: Int,
        totalQuestions: Int,
        accuracy: Float,
        timeSpentSeconds: Int,
        xpEarned: Int = 0,
        coinsEarned: Int = 0,
        gemsEarned: Int = 0
    ) = withContext(Dispatchers.IO) {
        val resultId = UUID.randomUUID().toString()
        val result = GameResultEntity(
            resultId = resultId,
            userId = userId,
            sessionId = sessionId,
            gameMode = gameMode.name,
            difficulty = difficulty.name,
            operation = operation.name,
            score = score,
            totalQuestions = totalQuestions,
            accuracy = accuracy,
            timeSpentSeconds = timeSpentSeconds,
            xpEarned = xpEarned,
            coinsEarned = coinsEarned,
            gemsEarned = gemsEarned
        )
        gameResultDao.insertResult(result)

        // Update user stats
        updateUserStats(userId, score, totalQuestions, accuracy, xpEarned, coinsEarned, gemsEarned)
    }

    /**
     * Get user game results
     */
    fun getUserResults(userId: String, limit: Int = 20): Flow<List<GameResult>> {
        return kotlinx.coroutines.flow.flow {
            // Emit results from local database
            gameResultDao.getUserResults(userId, limit).collect { entities ->
                emit(entities.map { e ->
                    GameResult(
                        resultId = e.resultId,
                        userId = e.userId,
                        sessionId = e.sessionId,
                        gameMode = GameMode.valueOf(e.gameMode),
                        difficulty = Difficulty.valueOf(e.difficulty),
                        operation = Operation.valueOf(e.operation),
                        score = e.score,
                        totalQuestions = e.totalQuestions,
                        accuracy = e.accuracy,
                        timeSpentSeconds = e.timeSpentSeconds,
                        xpEarned = e.xpEarned,
                        coinsEarned = e.coinsEarned,
                        gemsEarned = e.gemsEarned,
                        createdAt = e.createdAt
                    )
                })
            }
        }
    }

    // ============== USER STATS ==============

    /**
     * Update user statistics after game completion
     */
    private suspend fun updateUserStats(
        userId: String,
        score: Int,
        totalQuestions: Int,
        accuracy: Float,
        xpEarned: Int,
        coinsEarned: Int,
        gemsEarned: Int
    ) = withContext(Dispatchers.IO) {
        try {
            var stats = userStatsDao.getUserStats(userId) ?: UserStatsEntity(userId)

            val newTotalGames = stats.totalGamesPlayed + 1
            val newTotalCorrect = stats.totalCorrect + score
            val newTotalQuestions = stats.totalQuestions + totalQuestions
            val newAverageAccuracy = (newTotalCorrect.toFloat() / newTotalQuestions) * 100f

            stats = stats.copy(
                totalGamesPlayed = newTotalGames,
                totalCorrect = newTotalCorrect,
                totalQuestions = newTotalQuestions,
                averageAccuracy = newAverageAccuracy,
                totalXpEarned = stats.totalXpEarned + xpEarned,
                totalCoinsEarned = stats.totalCoinsEarned + coinsEarned,
                totalGemsEarned = stats.totalGemsEarned + gemsEarned,
                bestScore = maxOf(stats.bestScore, score),
                lastPlayedAt = System.currentTimeMillis(),
                adaptiveDifficulty = getAdaptiveDifficulty(stats.adaptiveDifficulty, newAverageAccuracy).name
            )

            userStatsDao.updateStats(stats)
            Log.d(TAG, "Updated stats for $userId: Games=$newTotalGames, Accuracy=$newAverageAccuracy%")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user stats", e)
        }
    }

    /**
     * Get user statistics
     */
    suspend fun getUserStats(userId: String): UserStats? = withContext(Dispatchers.IO) {
        userStatsDao.getUserStats(userId)?.let { entity ->
            UserStats(
                userId = entity.userId,
                totalGamesPlayed = entity.totalGamesPlayed,
                totalCorrect = entity.totalCorrect,
                totalQuestions = entity.totalQuestions,
                currentStreak = entity.currentStreak,
                longestStreak = entity.longestStreak,
                averageAccuracy = entity.averageAccuracy,
                totalXpEarned = entity.totalXpEarned,
                totalCoinsEarned = entity.totalCoinsEarned,
                totalGemsEarned = entity.totalGemsEarned,
                bestScore = entity.bestScore,
                lastPlayedAt = entity.lastPlayedAt,
                adaptiveDifficulty = Difficulty.valueOf(entity.adaptiveDifficulty)
            )
        }
    }

    // ============== STREAK TRACKING ==============

    /**
     * Update streak based on daily activity
     */
    suspend fun updateStreak(userId: String) = withContext(Dispatchers.IO) {
        val today = getTodayDateMillis()
        val yesterday = today - (24 * 60 * 60 * 1000)

        val todayActivity = activityLogDao.getActivity(userId, today)
        val yesterdayActivity = activityLogDao.getActivity(userId, yesterday)

        if (todayActivity != null && todayActivity.gamesPlayed > 0) {
            // User played today
            if (yesterdayActivity != null && yesterdayActivity.streakContinued) {
                // Continue streak
                userStatsDao.incrementStreak(userId)
                activityLogDao.insertActivity(
                    ActivityLogEntity(
                        userId = userId,
                        activityDate = today,
                        streakContinued = true
                    )
                )
            } else {
                // Start new streak
                activityLogDao.insertActivity(
                    ActivityLogEntity(
                        userId = userId,
                        activityDate = today,
                        streakContinued = true,
                        gamesPlayed = todayActivity?.gamesPlayed ?: 1
                    )
                )
            }
        } else {
            // Streak broken
            userStatsDao.resetStreak(userId)
            activityLogDao.insertActivity(
                ActivityLogEntity(
                    userId = userId,
                    activityDate = today,
                    streakContinued = false
                )
            )
        }
    }

    // ============== ADAPTIVE DIFFICULTY ==============

    /**
     * Get adaptive difficulty based on user performance
     */
    private fun getAdaptiveDifficulty(current: String, accuracy: Float): Difficulty {
        return questionGenerator.getAdaptiveDifficulty(
            accuracy,
            Difficulty.valueOf(current)
        )
    }

    // ============== UTILITY ==============

    /**
     * Get today's date in milliseconds (at midnight)
     */
    private fun getTodayDateMillis(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Clear session cache
     */
    fun clearSessionCache() {
        questionGenerator.clearSessionCache()
    }
}

