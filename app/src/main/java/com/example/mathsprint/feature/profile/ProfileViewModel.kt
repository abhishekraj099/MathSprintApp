package com.example.mathsprint.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.data.local.LoginManager
import com.example.mathsprint.data.local.entity.UserEntity
import com.example.mathsprint.data.repository.AuthRepository
import com.example.mathsprint.data.repository.AuthResult
import com.example.mathsprint.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val loginManager = LoginManager(context)

    val user: StateFlow<UserEntity?> = userRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut = _loggedOut.asStateFlow()

    private val _deleteProgress = MutableStateFlow(0f)
    val deleteProgress = _deleteProgress.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                loginManager.clearLoginState()
                _loggedOut.value = true
            } catch (e: Exception) {
                _error.value = "Logout failed: ${e.message}"
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _deleteProgress.value = 0.2f
                
                // Step 1: Delete from local Room DB
                loginManager.clearLoginState()
                _deleteProgress.value = 0.5f
                
                // Step 2: Delete from Firebase and Auth
                when (val result = authRepository.deleteAccount()) {
                    is AuthResult.Success -> {
                        _deleteProgress.value = 1f
                        _loggedOut.value = true
                    }
                    is AuthResult.Error -> {
                        _deleteProgress.value = 0f
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _deleteProgress.value = 0f
                _error.value = "Delete failed: ${e.message}"
            }
        }
    }

    fun updateUserActivity() {
        viewModelScope.launch {
            user.value?.let { currentUser ->
                loginManager.saveLoginState(currentUser.email)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun changeEmail(newEmail: String) {
        viewModelScope.launch {
            try {
                user.value?.let { currentUser ->
                    // Update email in local database and Firebase
                    val updated = currentUser.copy(email = newEmail)
                    userRepository.updateUserEmail(newEmail)
                    _error.value = "Email updated successfully!"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update email: ${e.message}"
            }
        }
    }
}

