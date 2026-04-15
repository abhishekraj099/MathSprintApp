# Ranking Integration Guide - GameScreen Implementation

## Overview
This guide shows how to integrate the dynamic ranking system into your GameScreen after quiz completion.

## Step-by-Step Implementation

### Step 1: Calculate Performance Metrics After Quiz Completion

```kotlin
val correctAnswers = calculateCorrectAnswers(userAnswers)
val totalQuestions = userAnswers.size
val accuracy = (correctAnswers.toFloat() / totalQuestions) * 100
val earnedXP = correctAnswers * 10  // 10 XP per correct answer
val earnedCoins = correctAnswers * 2  // 2 coins per correct answer
```

### Step 2: Update User Performance in Firebase

```kotlin
val currentUser = getCurrentUser() // Get logged-in user

firebaseDataSource.updateUserPerformance(
    uid = currentUser.uid,
    xp = currentUser.xp + earnedXP,
    wins = if (accuracy > 70) currentUser.wins + 1 else currentUser.wins,
    totalGamesPlayed = currentUser.totalGamesPlayed + 1,
    score = (accuracy * totalQuestions).toInt(),
    accuracy = accuracy
)
```

### Step 3: Calculate and Update Ranking

```kotlin
val newXP = currentUser.xp + earnedXP
val newWins = if (accuracy > 70) currentUser.wins + 1 else currentUser.wins
val newWinRate = RankingManager.updateWinRate(
    newWins,
    currentUser.totalGamesPlayed + 1
)
val rankLevel = RankingManager.getRankLevel(newXP)
val totalScore = RankingManager.calculateUserScore(
    xp = newXP,
    winRate = newWinRate,
    streak = currentUser.streak,
    dailyImprovement = calculateDailyImprovement(totalScore)
)
```

### Step 4: Sync Ranking to Firebase

```kotlin
firebaseDataSource.updateUserRanking(
    uid = currentUser.uid,
    rankLevel = rankLevel,
    totalScore = totalScore,
    winRate = newWinRate,
    dailyImprovement = calculateDailyImprovement(totalScore)
)
```

### Step 5: Update Local Room Database

```kotlin
userDao.insertOrUpdateUser(
    currentUser.copy(
        xp = newXP,
        wins = newWins,
        totalGamesPlayed = currentUser.totalGamesPlayed + 1,
        coins = currentUser.coins + earnedCoins,
        rankLevel = rankLevel,
        winRate = newWinRate
    )
)
```

### Step 6: Update Daily Streak

```kotlin
if (earnedXP > 0) {  // User completed the quiz
    firebaseDataSource.updateUserStreak(
        uid = currentUser.uid,
        streak = currentUser.streak + 1,
        bestStreak = maxOf(currentUser.streak + 1, currentUser.bestStreak)
    )
}
```

## Helper Functions Reference

### Calculate Daily Improvement

```kotlin
fun calculateDailyImprovement(currentScore: Int): Int {
    val yesterday = getYesterdayScore()
    return if (currentScore > yesterday) {
        currentScore - yesterday
    } else {
        0
    }
}

fun getYesterdayScore(): Int {
    // Retrieve yesterday's score from local database or cache
    // If not found, return 0
    return 0
}
```

### Calculate Correct Answers

```kotlin
fun calculateCorrectAnswers(userAnswers: List<Answer>): Int {
    return userAnswers.count { it.isCorrect }
}
```

## Ranking Formula Reference

```
Total Score = (XP × 0.4) + (Win Rate × 1000 × 0.3) + (Streak × 50 × 0.2) + (Daily Improvement × 100 × 0.1)
```

**Weights:**
- **40%** - XP (Consistency and learning)
- **30%** - Win Rate (Accuracy percentage)
- **20%** - Streak (Daily habits)
- **10%** - Daily Improvement (Progress tracking)

## Rank Levels (Based on XP)

| Level | Title | XP Range |
|-------|-------|----------|
| 1 | Number Rookie | 0-2,500 |
| 2 | Math Explorer | 2,500-5,000 |
| 3 | Calculation Cadet | 5,000-7,500 |
| 4 | Logic Warrior | 7,500-10,000 |
| 5 | Mathlete | 10,000-15,000 |
| 6 | Quant Champion | 15,000-20,000 |
| 7 | Neural Ninja | 20,000-25,000 |
| 8 | Algebra Ace | 25,000-30,000 |
| 9 | Quantum Master | 30,000-35,000 |
| 10 | MathSprint Legend | 35,000+ |

## Complete Example Integration

```kotlin
// In BattleViewModel.kt or GameScreen.kt
fun completeQuiz(userAnswers: List<Answer>) {
    viewModelScope.launch {
        try {
            // Step 1: Calculate metrics
            val correctAnswers = userAnswers.count { it.isCorrect }
            val totalQuestions = userAnswers.size
            val accuracy = (correctAnswers.toFloat() / totalQuestions) * 100
            val earnedXP = correctAnswers * 10
            val earnedCoins = correctAnswers * 2
            
            val currentUser = userRepository.getCurrentUser()
            
            // Step 2: Update performance
            firebaseDataSource.updateUserPerformance(
                uid = currentUser.uid,
                xp = currentUser.xp + earnedXP,
                wins = if (accuracy > 70) currentUser.wins + 1 else currentUser.wins,
                totalGamesPlayed = currentUser.totalGamesPlayed + 1,
                score = (accuracy * totalQuestions).toInt(),
                accuracy = accuracy
            )
            
            // Step 3 & 4: Calculate and update ranking
            val newXP = currentUser.xp + earnedXP
            val rankLevel = RankingManager.getRankLevel(newXP)
            val totalScore = RankingManager.calculateUserScore(
                xp = newXP,
                winRate = RankingManager.updateWinRate(
                    if (accuracy > 70) currentUser.wins + 1 else currentUser.wins,
                    currentUser.totalGamesPlayed + 1
                ),
                streak = currentUser.streak,
                dailyImprovement = 0
            )
            
            firebaseDataSource.updateUserRanking(
                uid = currentUser.uid,
                rankLevel = rankLevel,
                totalScore = totalScore,
                winRate = RankingManager.updateWinRate(
                    if (accuracy > 70) currentUser.wins + 1 else currentUser.wins,
                    currentUser.totalGamesPlayed + 1
                ),
                dailyImprovement = 0
            )
            
            // Step 5: Update local DB
            userDao.insertOrUpdateUser(
                currentUser.copy(
                    xp = newXP,
                    coins = currentUser.coins + earnedCoins,
                    rankLevel = rankLevel
                )
            )
            
            // Show results
            _uiState.value = _uiState.value.copy(
                showResults = true,
                earnedXP = earnedXP,
                earnedCoins = earnedCoins,
                newRankLevel = rankLevel
            )
            
        } catch (e: Exception) {
            Log.e("BattleViewModel", "Error completing quiz", e)
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }
}
```

## Real-Time Leaderboard Updates

✅ **The LeaderboardScreen automatically:**
- Listens to Firebase changes
- Updates the leaderboard in real-time
- Recalculates rankings when users complete quizzes
- Displays sorted user positions

**No additional setup needed!** Just call the update methods and the leaderboard will sync automatically.

## Key Points

- ✅ Only call `updateUserPerformance()` after quiz completion
- ✅ Rankings update automatically in real-time
- ✅ All data synced across devices via Firebase
- ✅ Local Room database kept in sync
- ✅ Leaderboard refreshes automatically

## Troubleshooting

**Q: Leaderboard not updating?**
- A: Check Firebase rules allow read/write access
- A: Verify FirebaseDataSource methods are being called
- A: Check Logcat for any Firebase errors

**Q: User rank not changing?**
- A: Verify XP values are correct (10 per correct answer)
- A: Check if Win Rate calculation is correct (> 70% accuracy)
- A: Ensure `updateUserRanking()` is called after each quiz

**Q: Local data out of sync?**
- A: Make sure `userDao.insertOrUpdateUser()` is called
- A: Verify Room database schema matches UserEntity

## Production Checklist

- [ ] Integration code added to GameScreen/BattleViewModel
- [ ] Quiz completion triggers all 6 steps
- [ ] Firebase Realtime Database rules verified
- [ ] Leaderboard displays correct rankings
- [ ] Real-time updates working in testing
- [ ] Performance metrics being tracked
- [ ] Local Room database synced

