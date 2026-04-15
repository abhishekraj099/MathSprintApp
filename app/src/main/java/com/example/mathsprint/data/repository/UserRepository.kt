package com.example.mathsprint.data.repository

import com.example.mathsprint.data.local.dao.ChapterDao
import com.example.mathsprint.data.local.dao.UserDao
import com.example.mathsprint.data.local.entity.ChapterEntity
import com.example.mathsprint.data.local.entity.LessonProgressEntity
import com.example.mathsprint.data.local.entity.UserEntity
import com.example.mathsprint.data.remote.FirebaseDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val chapterDao: ChapterDao,
    private val firebaseDataSource: FirebaseDataSource
) {
    fun observeCurrentUser(): Flow<UserEntity?> = userDao.observeCurrentUser()
    fun observeChapters(): Flow<List<ChapterEntity>> = chapterDao.observeAllChapters()
    fun observeLessons(chapterId: Int): Flow<List<LessonProgressEntity>> =
        chapterDao.observeLessonsForChapter(chapterId)

    suspend fun seedDefaultChapters() {
        chapterDao.insertChapters(defaultChapters())
        chapterDao.unlockChapter(1)
        (1..2).forEach { lessonId ->
            chapterDao.insertLessonProgress(
                LessonProgressEntity(
                    chapterId = 1,
                    lessonId = lessonId,
                    isLocked = lessonId != 1,
                    isActive = lessonId == 1
                )
            )
        }
    }

    suspend fun completeLesson(chapterId: Int, lessonId: Int, score: Int, uid: String) {
        chapterDao.markLessonCompleted(chapterId, lessonId, score)
        chapterDao.incrementCompletedLessons(chapterId)
        userDao.addRewards(uid, xp = score * 10, coins = score * 2)
        firebaseDataSource.updateLastActive(uid)
        chapterDao.activateLesson(chapterId, lessonId + 1)
    }

    suspend fun updateUserActivity(uid: String) {
        userDao.updateLastActive(uid)
        firebaseDataSource.updateLastActive(uid)
    }

    suspend fun updateUserEmail(newEmail: String) {
        val currentUser = userDao.getCurrentUser()
        currentUser?.let { user ->
            val updatedUser = user.copy(email = newEmail)
            userDao.insertOrUpdateUser(updatedUser)
            firebaseDataSource.updateUserEmail(updatedUser.uid, newEmail)
        }
    }

    private fun defaultChapters() = listOf(
        ChapterEntity(1, "Basics: Roots", "Square roots & fundamentals", 5, isUnlocked = true, iconType = "book"),
        ChapterEntity(2, "Conversation", "Applied math in context", 4, iconType = "chat"),
        ChapterEntity(3, "Crystal Grammar", "Advanced patterns", 6, iconType = "star"),
        ChapterEntity(4, "Number Theory", "Primes, factors, multiples", 5, iconType = "translate"),
        ChapterEntity(5, "Algebra Ascent", "Variables & equations", 6, iconType = "star"),
    )
}

