package com.example.mathsprint.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mathsprint.data.local.entity.QuestionEntity
import com.example.mathsprint.data.local.entity.GameSessionEntity
import com.example.mathsprint.data.local.entity.GameResultEntity
import com.example.mathsprint.data.local.entity.UserStatsEntity
import com.example.mathsprint.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Question operations
 */
@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("SELECT * FROM questions WHERE questionId = :questionId")
    suspend fun getQuestion(questionId: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    suspend fun getSessionQuestions(sessionId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE userId = :userId AND createdAt > :since ORDER BY createdAt DESC")
    fun getUserQuestions(userId: String, since: Long): Flow<List<QuestionEntity>>

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE createdAt < :beforeTime")
    suspend fun deleteOldQuestions(beforeTime: Long)
}

/**
 * DAO for GameSession operations
 */
@Dao
interface GameSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity)

    @Update
    suspend fun updateSession(session: GameSessionEntity)

    @Query("SELECT * FROM game_sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: String): GameSessionEntity?

    @Query("SELECT * FROM game_sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getUserSessions(userId: String, limit: Int = 10): List<GameSessionEntity>

    @Query("SELECT * FROM game_sessions WHERE userId = :userId AND isCompleted = 0 LIMIT 1")
    suspend fun getActiveSession(userId: String): GameSessionEntity?

    @Query("DELETE FROM game_sessions WHERE endTime < :beforeTime")
    suspend fun deleteOldSessions(beforeTime: Long)
}

/**
 * DAO for GameResult operations
 */
@Dao
interface GameResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: GameResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<GameResultEntity>)

    @Query("SELECT * FROM game_results WHERE resultId = :resultId")
    suspend fun getResult(resultId: String): GameResultEntity?

    @Query("SELECT * FROM game_results WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getUserResults(userId: String, limit: Int = 20): Flow<List<GameResultEntity>>

    @Query("SELECT AVG(accuracy) FROM game_results WHERE userId = :userId")
    suspend fun getUserAverageAccuracy(userId: String): Float?

    @Query("SELECT SUM(score) FROM game_results WHERE userId = :userId")
    suspend fun getUserTotalScore(userId: String): Int?

    @Query("SELECT * FROM game_results WHERE userId = :userId AND synced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedResults(userId: String): List<GameResultEntity>

    @Query("UPDATE game_results SET synced = 1 WHERE resultId = :resultId")
    suspend fun markResultAsSynced(resultId: String)

    @Query("DELETE FROM game_results WHERE createdAt < :beforeTime")
    suspend fun deleteOldResults(beforeTime: Long)
}

/**
 * DAO for UserStats operations
 */
@Dao
interface UserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: UserStatsEntity)

    @Update
    suspend fun updateStats(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    suspend fun getUserStats(userId: String): UserStatsEntity?

    @Query("SELECT * FROM user_stats ORDER BY totalXpEarned DESC LIMIT :limit")
    suspend fun getLeaderboard(limit: Int = 100): List<UserStatsEntity>

    @Query("UPDATE user_stats SET currentStreak = currentStreak + 1 WHERE userId = :userId")
    suspend fun incrementStreak(userId: String)

    @Query("UPDATE user_stats SET currentStreak = 0 WHERE userId = :userId")
    suspend fun resetStreak(userId: String)
}

/**
 * DAO for ActivityLog operations
 */
@Dao
interface ActivityLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityLogEntity)

    @Query("SELECT * FROM activity_log WHERE userId = :userId AND activityDate = :date")
    suspend fun getActivity(userId: String, date: Long): ActivityLogEntity?

    @Query("SELECT * FROM activity_log WHERE userId = :userId ORDER BY activityDate DESC LIMIT :limit")
    suspend fun getUserActivityLog(userId: String, limit: Int = 30): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_log WHERE userId = :userId AND activityDate BETWEEN :startDate AND :endDate ORDER BY activityDate ASC")
    suspend fun getActivityRange(userId: String, startDate: Long, endDate: Long): List<ActivityLogEntity>
}

