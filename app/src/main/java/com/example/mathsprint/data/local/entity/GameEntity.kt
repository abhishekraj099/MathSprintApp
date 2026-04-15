package com.example.mathsprint.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ============== GAME QUESTION ENTITIES ==============

/**
 * Room entity for storing game questions
 */
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val questionId: String,
    val sessionId: String,
    val userId: String,
    val questionText: String,
    val correctAnswer: Int,
    val userAnswer: Int? = null,
    val difficulty: String,  // EASY, MEDIUM, HARD, EXPERT
    val operation: String,   // ADD, SUBTRACT, MULTIPLY, DIVIDE, MIXED
    val isCorrect: Boolean? = null,
    val timeSpentSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for storing game sessions
 */
@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey val sessionId: String,
    val userId: String,
    val gameMode: String,        // PRACTICE, QUIZ, TIMED, DAILY_CHALLENGE
    val difficulty: String,      // EASY, MEDIUM, HARD, EXPERT
    val operation: String,       // ADD, SUBTRACT, MULTIPLY, DIVIDE, MIXED
    val totalQuestions: Int,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val timeSpentSeconds: Int = 0,
    val isCompleted: Boolean = false
)

/**
 * Room entity for storing game results
 */
@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey val resultId: String,
    val userId: String,
    val sessionId: String,
    val gameMode: String,        // PRACTICE, QUIZ, TIMED, DAILY_CHALLENGE
    val difficulty: String,
    val operation: String,
    val score: Int,              // Total correct answers
    val totalQuestions: Int,
    val accuracy: Float,         // Percentage
    val timeSpentSeconds: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val gemsEarned: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false  // For offline sync tracking
)

/**
 * Room entity for user statistics
 */
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userId: String,
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
    val adaptiveDifficulty: String = "EASY",  // Current adaptive difficulty
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for activity log (for streak tracking)
 */
@Entity(tableName = "activity_log")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val activityDate: Long,  // Date in milliseconds
    val gamesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val streakContinued: Boolean = false
)

// ============== LEGACY ENTITIES (KEEP FOR COMPATIBILITY) ==============

@Entity(tableName = "daily_challenge")
data class DailyChallengeEntity(
    @PrimaryKey val id: Int = 1,
    val date: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val score: Int = 0,
    val reward: Int = 50
)

@Entity(tableName = "lifeline_log")
data class LifelineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val currentLives: Int = 5,
    val lastClaimedAt: Long = System.currentTimeMillis(),
    val nextClaimAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
)

@Entity(tableName = "user_activity")
data class UserActivityEntity(
    @PrimaryKey val userId: String,
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val inactiveCount: Int = 0
)

