package com.example.mathsprint.di

import android.util.Log
import com.example.mathsprint.core.generator.QuestionGenerator
import com.example.mathsprint.data.local.dao.QuestionDao
import com.example.mathsprint.data.local.dao.GameSessionDao
import com.example.mathsprint.data.local.dao.GameResultDao
import com.example.mathsprint.data.local.dao.UserStatsDao
import com.example.mathsprint.data.local.dao.ActivityLogDao
import com.example.mathsprint.data.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideQuestionGenerator(): QuestionGenerator {
        return try {
            Log.d("GameModule", "Initializing QuestionGenerator")
            QuestionGenerator()
        } catch (e: Exception) {
            Log.e("GameModule", "Error initializing QuestionGenerator", e)
            QuestionGenerator()
        }
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        questionDao: QuestionDao,
        gameSessionDao: GameSessionDao,
        gameResultDao: GameResultDao,
        userStatsDao: UserStatsDao,
        activityLogDao: ActivityLogDao,
        questionGenerator: QuestionGenerator
    ): GameRepository {
        return try {
            Log.d("GameModule", "Initializing GameRepository")
            GameRepository(
                questionDao = questionDao,
                gameSessionDao = gameSessionDao,
                gameResultDao = gameResultDao,
                userStatsDao = userStatsDao,
                activityLogDao = activityLogDao,
                questionGenerator = questionGenerator
            )
        } catch (e: Exception) {
            Log.e("GameModule", "Error initializing GameRepository", e)
            GameRepository(
                questionDao = questionDao,
                gameSessionDao = gameSessionDao,
                gameResultDao = gameResultDao,
                userStatsDao = userStatsDao,
                activityLogDao = activityLogDao,
                questionGenerator = questionGenerator
            )
        }
    }
}

