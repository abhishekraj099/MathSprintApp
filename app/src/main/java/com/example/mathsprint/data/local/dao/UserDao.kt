package com.example.mathsprint.data.local.dao

import androidx.room.*
import com.example.mathsprint.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("UPDATE users SET lastActiveAt = :time WHERE uid = :uid")
    suspend fun updateLastActive(uid: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE users SET lives = :lives, nextLifeAt = :nextLifeAt WHERE uid = :uid")
    suspend fun updateLives(uid: String, lives: Int, nextLifeAt: Long)

    @Query("UPDATE users SET streak = :streak WHERE uid = :uid")
    suspend fun updateStreak(uid: String, streak: Int)

    @Query("UPDATE users SET xp = xp + :xp, coins = coins + :coins WHERE uid = :uid")
    suspend fun addRewards(uid: String, xp: Int, coins: Int)
}

