package com.example.mathsprint.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val totalLessons: Int,
    val isUnlocked: Boolean = false,
    val completedLessons: Int = 0,
    val iconType: String = "book"
)

