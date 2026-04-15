package com.example.mathsprint.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return try {
            Log.d("AppModule", "Initializing FirebaseAuth")
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("AppModule", "Error initializing FirebaseAuth", e)
            FirebaseAuth.getInstance()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return try {
            Log.d("AppModule", "Initializing FirebaseDatabase")
            FirebaseDatabase.getInstance().apply {
                setPersistenceEnabled(true)
            }
        } catch (e: Exception) {
            Log.e("AppModule", "Error initializing FirebaseDatabase", e)
            FirebaseDatabase.getInstance()
        }
    }
}

