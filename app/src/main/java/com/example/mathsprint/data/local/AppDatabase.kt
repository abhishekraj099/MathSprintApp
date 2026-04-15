package com.example.mathsprint.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mathsprint.data.local.converter.GameConverter
import com.example.mathsprint.data.local.dao.ChapterDao
import com.example.mathsprint.data.local.dao.UserDao
import com.example.mathsprint.data.local.dao.QuestionDao
import com.example.mathsprint.data.local.dao.GameSessionDao
import com.example.mathsprint.data.local.dao.GameResultDao
import com.example.mathsprint.data.local.dao.UserStatsDao
import com.example.mathsprint.data.local.dao.ActivityLogDao
import com.example.mathsprint.data.local.entity.ChapterEntity
import com.example.mathsprint.data.local.entity.LessonProgressEntity
import com.example.mathsprint.data.local.entity.UserEntity
import com.example.mathsprint.data.local.entity.QuestionEntity
import com.example.mathsprint.data.local.entity.GameSessionEntity
import com.example.mathsprint.data.local.entity.GameResultEntity
import com.example.mathsprint.data.local.entity.UserStatsEntity
import com.example.mathsprint.data.local.entity.ActivityLogEntity
import com.example.mathsprint.data.local.entity.DailyChallengeEntity
import com.example.mathsprint.data.local.entity.LifelineEntity
import com.example.mathsprint.data.local.entity.UserActivityEntity

@Database(
    entities = [
        UserEntity::class,
        ChapterEntity::class,
        LessonProgressEntity::class,
        QuestionEntity::class,
        GameSessionEntity::class,
        GameResultEntity::class,
        UserStatsEntity::class,
        ActivityLogEntity::class,
        DailyChallengeEntity::class,
        LifelineEntity::class,
        UserActivityEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(GameConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chapterDao(): ChapterDao
    abstract fun questionDao(): QuestionDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun gameResultDao(): GameResultDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun activityLogDao(): ActivityLogDao
}

