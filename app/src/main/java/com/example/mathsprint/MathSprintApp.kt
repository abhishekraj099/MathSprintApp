package com.example.mathsprint

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MathSprintApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MathSprintApp", "App initialized successfully")
    }
}

