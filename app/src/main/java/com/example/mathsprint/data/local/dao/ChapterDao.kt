package com.example.mathsprint.data.local.dao

import androidx.room.*
import com.example.mathsprint.data.local.entity.ChapterEntity
import com.example.mathsprint.data.local.entity.LessonProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun observeAllChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("UPDATE chapters SET isUnlocked = 1 WHERE id = :chapterId")
    suspend fun unlockChapter(chapterId: Int)

    @Query("UPDATE chapters SET completedLessons = completedLessons + 1 WHERE id = :chapterId")
    suspend fun incrementCompletedLessons(chapterId: Int)

    @Query("SELECT * FROM lesson_progress WHERE chapterId = :chapterId ORDER BY lessonId ASC")
    fun observeLessonsForChapter(chapterId: Int): Flow<List<LessonProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonProgress(progress: LessonProgressEntity)

    @Query("UPDATE lesson_progress SET isCompleted = 1, score = :score, completedAt = :time WHERE chapterId = :chapterId AND lessonId = :lessonId")
    suspend fun markLessonCompleted(chapterId: Int, lessonId: Int, score: Int, time: Long = System.currentTimeMillis())

    @Query("UPDATE lesson_progress SET isActive = 1, isLocked = 0 WHERE chapterId = :chapterId AND lessonId = :lessonId")
    suspend fun activateLesson(chapterId: Int, lessonId: Int)
}

