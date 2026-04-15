package com.example.mathsprint.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.mathsprint.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return try {
            Log.d("DatabaseModule", "Initializing Room Database")
            Room.databaseBuilder(context, AppDatabase::class.java, "mathsprint.db")
                .fallbackToDestructiveMigration()
                .build()
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Error initializing Room Database", e)
            Room.databaseBuilder(context, AppDatabase::class.java, "mathsprint.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    @Provides
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    fun provideChapterDao(db: AppDatabase) = db.chapterDao()

    @Provides
    fun provideQuestionDao(db: AppDatabase) = db.questionDao()

    @Provides
    fun provideGameSessionDao(db: AppDatabase) = db.gameSessionDao()

    @Provides
    fun provideGameResultDao(db: AppDatabase) = db.gameResultDao()

    @Provides
    fun provideUserStatsDao(db: AppDatabase) = db.userStatsDao()

    @Provides
    fun provideActivityLogDao(db: AppDatabase) = db.activityLogDao()
}

