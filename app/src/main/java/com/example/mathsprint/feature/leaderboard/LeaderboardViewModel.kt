package com.example.mathsprint.feature.leaderboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mathsprint.data.repository.RankingManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class LeaderboardUser(
    val rank: Int,
    val uid: String,
    val name: String,
    val xp: Int,
    val streak: Int,
    val score: Int,
    val rankLevel: Int,
    val rankTitle: String,
    val winRate: Float
)

data class LeaderboardUiState(
    val users: List<LeaderboardUser> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    private val usersRef = database.reference.child("users")

    init {
        fetchLeaderboard()
    }

    /**
     * Fetch all users from Firebase and calculate rankings
     */
    private fun fetchLeaderboard() {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val usersList = mutableListOf<Pair<String, Map<String, Any>>>()

                    for (userSnapshot in snapshot.children) {
                        val userData = userSnapshot.value as? Map<String, Any>
                        if (userData != null) {
                            usersList.add(userSnapshot.key!! to userData)
                        }
                    }

                    val leaderboardUsers = calculateLeaderboard(usersList)
                    _uiState.value = LeaderboardUiState(
                        users = leaderboardUsers,
                        isLoading = false,
                        error = null
                    )

                    Log.d("LeaderboardViewModel", "Leaderboard updated: ${leaderboardUsers.size} users")
                } catch (e: Exception) {
                    Log.e("LeaderboardViewModel", "Error fetching leaderboard", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load leaderboard: ${e.message}"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LeaderboardViewModel", "Firebase error: ${error.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Database error: ${error.message}"
                )
            }
        })
    }

    /**
     * Calculate leaderboard rankings based on user performance
     */
    private fun calculateLeaderboard(
        usersList: List<Pair<String, Map<String, Any>>>
    ): List<LeaderboardUser> {
        // Convert to scoreable format
        val userScores = usersList.map { (uid, userData) ->
            val xp = (userData["xp"] as? Number)?.toInt() ?: 0
            val wins = (userData["wins"] as? Number)?.toInt() ?: 0
            val totalGamesPlayed = (userData["totalGamesPlayed"] as? Number)?.toInt() ?: 0
            val winRate = RankingManager.updateWinRate(wins, totalGamesPlayed)
            val streak = (userData["streak"] as? Number)?.toInt() ?: 0
            val dailyImprovement = (userData["dailyImprovement"] as? Number)?.toInt() ?: 0

            val score = RankingManager.calculateUserScore(xp, winRate, streak, dailyImprovement)

            Triple(uid, score, userData)
        }

        // Sort by score descending
        val sortedUsers = userScores.sortedByDescending { it.second }

        // Create leaderboard entries with rank
        return sortedUsers.mapIndexed { index, (uid, score, userData) ->
            val name = (userData["name"] as? String) ?: "Anonymous"
            val xp = (userData["xp"] as? Number)?.toInt() ?: 0
            val streak = (userData["streak"] as? Number)?.toInt() ?: 0
            val wins = (userData["wins"] as? Number)?.toInt() ?: 0
            val totalGamesPlayed = (userData["totalGamesPlayed"] as? Number)?.toInt() ?: 0
            val winRate = RankingManager.updateWinRate(wins, totalGamesPlayed)

            val rankLevel = RankingManager.getRankLevel(xp)
            val rankTitle = RankingManager.getRankTitle(rankLevel)

            LeaderboardUser(
                rank = index + 1,
                uid = uid,
                name = name,
                xp = xp,
                streak = streak,
                score = score,
                rankLevel = rankLevel,
                rankTitle = rankTitle,
                winRate = winRate
            )
        }
    }

    /**
     * Refresh leaderboard manually
     */
    fun refreshLeaderboard() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        fetchLeaderboard()
    }
}
