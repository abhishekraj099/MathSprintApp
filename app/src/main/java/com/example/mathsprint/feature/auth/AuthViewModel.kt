package com.example.mathsprint.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.data.repository.AuthRepository
import com.example.mathsprint.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    // LOGIN with Email & Password
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email.trim(), password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, success = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    // LOGIN with OTP (Email only)
    fun loginWithEmail(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please provide an email") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = authRepository.loginWithOtp(email.trim())) {
                    is AuthResult.Success -> {
                        Log.d("AuthViewModel", "OTP Login successful for $email")
                        _uiState.update { it.copy(isLoading = false, success = true) }
                    }
                    is AuthResult.Error -> {
                        Log.e("AuthViewModel", "OTP Login error: ${result.message}")
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception during OTP login", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

     // REGISTER with OTP (Email + Name, no password)
    fun registerWithOtp(name: String, email: String) {
        if (name.isBlank() || email.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = authRepository.registerWithOtp(name.trim(), email.trim())) {
                    is AuthResult.Success -> {
                        Log.d("AuthViewModel", "OTP Registration successful for $email")
                        _uiState.update { it.copy(isLoading = false, success = true) }
                    }
                    is AuthResult.Error -> {
                        Log.e("AuthViewModel", "OTP Registration error: ${result.message}")
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception during OTP registration", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    // Check if user exists in database
    suspend fun checkUserExists(email: String): Boolean {
        return try {
            authRepository.userExists(email)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error checking if user exists", e)
            false
        }
    }

    // REGISTER with Email, Password & Name
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        when {
            name.isBlank() || email.isBlank() || password.isBlank() -> {
                _uiState.update { it.copy(error = "Please fill in all fields") }
            }
            password != confirmPassword -> {
                _uiState.update { it.copy(error = "Passwords do not match") }
            }
            password.length < 6 -> {
                _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            }
            else -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                    when (val result = authRepository.register(name.trim(), email.trim(), password)) {
                        is AuthResult.Success -> {
                            _uiState.update { it.copy(isLoading = false, success = true) }
                        }
                        is AuthResult.Error -> {
                            _uiState.update { it.copy(isLoading = false, error = result.message) }
                        }
                    }
                }
            }
        }
    }
}

