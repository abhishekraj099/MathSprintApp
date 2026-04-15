package com.example.mathsprint.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.core.navigation.Screen
import com.example.mathsprint.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _destination = MutableStateFlow<String?>(null)
    val destination = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                delay(1800)
                val nextScreen = try {
                    if (authRepository.isLoggedIn) {
                        Screen.Home.route
                    } else {
                        Screen.Welcome.route
                    }
                } catch (e: Exception) {
                    // If any error occurs, default to Welcome
                    Screen.Welcome.route
                }
                _destination.value = nextScreen
            } catch (e: Exception) {
                // Fallback to Welcome screen on any error
                _destination.value = Screen.Welcome.route
            }
        }
    }
}

