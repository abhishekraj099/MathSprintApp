package com.example.mathsprint.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val avatarIndex: Int = 0,
    val skillLevel: String = "beginner",
    val goal: String = "",
    val coins: Int = 0,
    val gems: Int = 0,
    val xp: Int = 0,
    val streak: Int = 0,
    val lives: Int = 5,
    val lastActiveAt: Long = System.currentTimeMillis(),
    val nextLifeAt: Long = 0L,
    val rankLevel: Int = 1,
    val winRate: Float = 0f,
    val isGuest: Boolean = false
)

