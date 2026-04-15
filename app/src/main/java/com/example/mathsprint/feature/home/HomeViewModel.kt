package com.example.mathsprint.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.data.local.entity.ChapterEntity
import com.example.mathsprint.data.local.entity.UserEntity
import com.example.mathsprint.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: UserEntity? = null,
    val chapters: List<ChapterEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        userRepository.observeCurrentUser(),
        userRepository.observeChapters()
    ) { user, chapters ->
        HomeUiState(user = user, chapters = chapters, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch {
            userRepository.seedDefaultChapters()
        }
    }

    fun updateUserActivity() {
        viewModelScope.launch {
            uiState.value.user?.let { user ->
                userRepository.updateUserActivity(user.uid)
            }
        }
    }
}

