# Dynamic Ranking System Documentation

## Overview
The MathSprint app now features a **dynamic, real-time ranking system** that calculates user ranks based on performance metrics synced with Firebase Realtime Database.

## Ranking Algorithm

### Score Calculation
Each user's rank is determined by their **Total Score**, calculated using:

```
Total Score = (XP × 0.4) + (Win Rate × 1000 × 0.3) + (Streak × 50 × 0.2) + (Daily Improvement × 100 × 0.1)
```

**Weighting:**
- **40%** - XP (Experience Points): Rewards consistent learning
- **30%** - Win Rate: Rewards accuracy and correct answers
- **20%** - Streak: Rewards daily consistency
- **10%** - Daily Improvement: Rewards daily growth

### Rank Levels (Based on Total XP)

| Level | Title | XP Range | Badge |
|-------|-------|----------|-------|
| 1 | Number Rookie | 0-2,500 | 🟢 |
| 2 | Math Explorer | 2,500-5,000 | 🟢 |
| 3 | Calculation Cadet | 5,000-7,500 | 🟢 |
| 4 | Logic Warrior | 7,500-10,000 | 🔵 |
| 5 | Mathlete | 10,000-15,000 | 🔵 |
| 6 | Quant Champion | 15,000-20,000 | 🟡 |
| 7 | Neural Ninja | 20,000-25,000 | 🟡 |
| 8 | Algebra Ace | 25,000-30,000 | 🔴 |
| 9 | Quantum Master | 30,000-35,000 | 🔴 |
| 10 | MathSprint Legend | 35,000+ | ⭐ |

### Leaderboard Display

The leaderboard shows:
1. **Rank Position** - User's global ranking (#1, #2, etc.) with medals for top 3
2. **User Name** - Player's username
3. **Rank Title** - Current rank level title (e.g., "Mathlete")
4. **XP** - Total experience points
5. **Total Score** - Calculated ranking score
6. **Streak** - Current day streak (🔥)
7. **Win Rate** - Percentage of correct answers (%)

## Performance Tracking

### User Performance Metrics
- **XP**: Earned per quiz completion (10 XP per question correct)
- **Coins**: Earned per correct answer (2 coins per point)
- **Wins**: Incremented when quiz accuracy > 70%
- **Total Games Played**: Incremented with each quiz completion
- **Streak**: Incremented for daily activity, reset after 24 hours of inactivity
- **Average Accuracy**: Calculated as (correct answers / total questions) × 100

### Daily Improvement Tracking
Calculates how much the user has improved compared to yesterday:
```
Daily Improvement = Today's Score - Yesterday's Score (if positive)
```

## Firebase Realtime Database Structure

```json
{
  "users": {
    "user_id_1": {
      "name": "Abhishek",
      "email": "abhisraj099@gmail.com",
      "xp": 5500,
      "wins": 45,
      "totalGamesPlayed": 62,
      "totalScore": 3850,
      "streak": 14,
      "bestStreak": 25,
      "winRate": 72.5,
      "averageAccuracy": 78.3,
      "rankLevel": 3,
      "dailyImprovement": 150,
      "lastScoreUpdate": 1702486200000,
      "lastActiveAt": 1702486200000
    },
    "user_id_2": {
      "name": "John Doe",
      "email": "john@gmail.com",
      "xp": 8900,
      "wins": 68,
      "totalGamesPlayed": 95,
      "totalScore": 5620,
      "streak": 8,
      "bestStreak": 30,
      "winRate": 71.6,
      "averageAccuracy": 75.8,
      "rankLevel": 4,
      "dailyImprovement": 200,
      "lastScoreUpdate": 1702486100000,
      "lastActiveAt": 1702486100000
    }
  }
}
```

## Updating User Rankings

When a user completes a quiz:

1. **Calculate Performance**
   - Award XP based on correct answers
   - Calculate accuracy percentage
   - Increment games played and wins

2. **Update Firebase**
   ```kotlin
   firebaseDataSource.updateUserPerformance(
       uid = userId,
       xp = newXP,
       wins = newWins,
       totalGamesPlayed = newTotal,
       score = quizScore,
       accuracy = accuracyPercentage
   )
   ```

3. **Sync Ranking**
   ```kotlin
   val rankLevel = RankingManager.getRankLevel(newXP)
   val totalScore = RankingManager.calculateUserScore(
       xp = newXP,
       winRate = newWinRate,
       streak = currentStreak,
       dailyImprovement = dailyImprovement
   )
   
   firebaseDataSource.updateUserRanking(
       uid = userId,
       rankLevel = rankLevel,
       totalScore = totalScore,
       winRate = newWinRate,
       dailyImprovement = dailyImprovement
   )
   ```

4. **Real-time Leaderboard Update**
   - LeaderboardViewModel listens to Firebase changes
   - Automatically recalculates rankings
   - Updates UI in real-time

## Key Features

✅ **Real-time Updates**: Leaderboard updates instantly as users complete quizzes  
✅ **Multiple Metrics**: Combines XP, accuracy, and consistency  
✅ **Daily Tracking**: Monitors daily improvements and streaks  
✅ **Fair Ranking**: Weighted algorithm prevents XP farming  
✅ **Firebase Sync**: All data persisted and synced across devices  
✅ **Rank Progression**: 10 distinct rank levels to achieve  
✅ **Visual Indicators**: Medals for top 3, color-coded ranks  

## Implementation in GameScreen

After each quiz completion, update rankings:

```kotlin
// In GameScreen or BattleViewModel
val accuracy = (correctAnswers.toFloat() / totalQuestions) * 100
firebaseDataSource.updateUserPerformance(
    uid = userId,
    xp = currentUser.xp + earnedXP,
    wins = if (accuracy > 70) currentUser.wins + 1 else currentUser.wins,
    totalGamesPlayed = currentUser.totalGamesPlayed + 1,
    score = (accuracy * totalQuestions).toInt(),
    accuracy = accuracy
)
```

## Future Enhancements

- Weekly/Monthly leaderboards
- Friend-only leaderboards
- Country-based rankings
- Seasonal rankings with rewards
- Leaderboard badges for streaks
- Tournament mode with special rankings

