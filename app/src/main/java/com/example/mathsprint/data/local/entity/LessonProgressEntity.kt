package com.example.mathsprint.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "lesson_progress",
    primaryKeys = ["chapterId", "lessonId"]
)
data class LessonProgressEntity(
    val chapterId: Int,
    val lessonId: Int,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
    val isLocked: Boolean = true,
    val score: Int = 0,
    val completedAt: Long = 0L
)

