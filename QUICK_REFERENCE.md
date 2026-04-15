# Math Sprint - Quick Reference Guide

## 🚀 Getting Started (5 minutes)

### 1. Import ViewModel
```kotlin
import com.example.mathsprint.feature.battle.BattleViewModel
import androidx.hilt.navigation.compose.hiltViewModel
```

### 2. Use in Composable
```kotlin
@Composable
fun YourGameScreen(userId: String) {
    val viewModel: BattleViewModel = hiltViewModel()
    
    GameScreen(
        userId = userId,
        gameMode = GameMode.PRACTICE,
        difficulty = Difficulty.MEDIUM,
        operation = Operation.MULTIPLY,
        questionCount = 10,
        onGameComplete = { score, accuracy ->
            println("Score: $score, Accuracy: $accuracy%")
        }
    )
}
```

### 3. That's it! 🎉

---

## 📚 Complete API Reference

### GameMode Options
```kotlin
GameMode.PRACTICE          // Unlimited questions
GameMode.QUIZ              // 10, 20, or 40 questions
GameMode.TIMED             // Time-based challenge
GameMode.DAILY_CHALLENGE   // Same for all users daily
```

### Difficulty Options
```kotlin
Difficulty.EASY      // Numbers 1-10
Difficulty.MEDIUM    // Numbers 10-50
Difficulty.HARD      // Numbers 50-100
Difficulty.EXPERT    // Numbers 100-500
```

### Operation Options
```kotlin
Operation.ADD        // "12 + 8 = 20"
Operation.SUBTRACT   // "15 - 7 = 8"
Operation.MULTIPLY   // "6 × 7 = 42"
Operation.DIVIDE     // "56 ÷ 8 = 7"
Operation.MIXED      // "5 + 3 × 2 = 11"
```

---

## 🎮 Common Scenarios

### Scenario 1: Practice Mode (Easy Math)
```kotlin
GameScreen(
    userId = userId,
    gameMode = GameMode.PRACTICE,
    difficulty = Difficulty.EASY,
    operation = Operation.ADD,
    questionCount = 5
)
```

### Scenario 2: Quiz Challenge (Hard)
```kotlin
GameScreen(
    userId = userId,
    gameMode = GameMode.QUIZ,
    difficulty = Difficulty.HARD,
    operation = Operation.MULTIPLY,
    questionCount = 20
)
```

### Scenario 3: Daily Challenge
```kotlin
GameScreen(
    userId = userId,
    gameMode = GameMode.DAILY_CHALLENGE,
    difficulty = Difficulty.MEDIUM,
    operation = Operation.MIXED,
    questionCount = 5  // Fixed
)
```

### Scenario 4: Mixed Operations (Expert)
```kotlin
GameScreen(
    userId = userId,
    gameMode = GameMode.PRACTICE,
    difficulty = Difficulty.EXPERT,
    operation = Operation.MIXED,
    questionCount = 15
)
```

---

## 💰 Rewards Calculation

### Example Game Result
```
Questions: 10
Correct: 8
Accuracy: 80%

XP Earned:
  baseXP = 8 × 10 = 80
  accuracyBonus = (80/100) × 50 = 40
  Total = 80 + 40 = 120 XP ✅

Coins Earned:
  Accuracy 80% ≥ 70% → 30 coins ✅

Gems Earned:
  Accuracy 80% < 100% → 0 gems ❌
```

---

## 📊 Accessing User Stats

```kotlin
// Inject repository
@Inject lateinit var gameRepository: GameRepository

// Get stats
val stats = gameRepository.getUserStats(userId)

// Use stats
stats?.let {
    val gamesPlayed = it.totalGamesPlayed
    val accuracy = it.averageAccuracy
    val currentStreak = it.currentStreak
    val adaptiveDifficulty = it.adaptiveDifficulty
    
    println("Games: $gamesPlayed")
    println("Accuracy: $accuracy%")
    println("Streak: $currentStreak days")
    println("Current Difficulty: $adaptiveDifficulty")
}
```

---

## 🔄 User Journey

```
User Clicks "Start Battle"
         ↓
Select Game Mode & Difficulty
         ↓
GameScreen Composable Launched
         ↓
BattleViewModel.startGame() Called
         ↓
QuestionGenerator Creates Questions
         ↓
First Question Displayed
         ↓
User Selects Answer → viewModel.selectAnswer(42)
         ↓
User Clicks Submit → viewModel.submitAnswer()
         ↓
Result Shown → "✓ Correct!" or "✗ Incorrect!"
         ↓
User Clicks Next → viewModel.nextQuestion()
         ↓
Repeat for All Questions
         ↓
Game Complete!
         ↓
Show Score & Accuracy
         ↓
Rewards Added to User Account
         ↓
onGameComplete() Callback Triggered
```

---

## 🐛 Troubleshooting

### Problem: "Unable to create instance of BattleViewModel"
```
✅ Solution: Make sure GameModule is properly set up
  Check: di/GameModule.kt exists
  Check: @HiltViewModel on BattleViewModel
  Check: MathSprintApp has @HiltAndroidApp
```

### Problem: Questions not generating
```
✅ Solution: Check QuestionGenerator initialization
  Make sure: GameModule provides it
  Check: @Inject on GameRepository
  Verify: No database errors in Logcat
```

### Problem: No questions displayed
```
✅ Solution: Check GameScreen is receiving props
  Verify: userId is not empty
  Check: gameMode/difficulty are valid
  Ensure: questionCount > 0
```

### Problem: Results not saving
```
✅ Solution: Check database setup
  Verify: Room database initialized
  Check: DAOs properly provided
  Ensure: User has write permissions
```

---

## 📱 UI States

### Loading State
```
Show: CircularProgressIndicator + "Loading battle..."
Duration: While questions are being generated
```

### Question State
```
Show: Question Card + 4 Answer Buttons + Submit Button
User Action: Select answer → Click Submit
```

### Result State
```
Show: Result feedback (✓ or ✗) + "Correct/Incorrect"
User Action: Click "Next" to continue
```

### Complete State
```
Show: Trophy emoji + Score + Accuracy + "Play Again" button
Data: Final score, accuracy percentage
```

---

## ⚙️ Configuration

### Adjust Question Count
```kotlin
GameScreen(
    // ... other params ...
    questionCount = 20  // Change this
)
```

### Change Difficulty Progression
```kotlin
// Current adaptive system:
// accuracy > 85% → harder
// accuracy 60-85% → same
// accuracy < 60% → easier

// To modify, edit getAdaptiveDifficulty() in GameRepository.kt
```

### Modify Rewards
```kotlin
// Edit these functions in GameRepository.kt:
// - calculateXP()     → Change XP formula
// - calculateCoins()  → Change coin rewards
// - calculateGems()   → Change gem rewards
```

---

## 📈 Analytics Integration

### Track Game Completion
```kotlin
onGameComplete = { score, accuracy ->
    logEvent("game_completed", bundleOf(
        "score" to score,
        "accuracy" to accuracy,
        "gameMode" to gameMode.name
    ))
}
```

### Track Leaderboard Position
```kotlin
val stats = gameRepository.getUserStats(userId)
val rank = stats?.totalXpEarned?.let { xp ->
    // Compare with other users
    calculateRank(xp)
}
```

---

## 🎨 Customization

### Change Colors
```kotlin
// In BattleScreen/GameScreen:
val accentColor = Color(0xFF00D9FF)  // Cyan
val successColor = Color(0xFF4CAF50) // Green
val errorColor = Color(0xFFFF6B6B)   // Red

// Edit these to match your theme
```

### Change Animations
```kotlin
// In GameScreen.kt, edit:
// - scale animations (Button animations)
// - fade animations (State transitions)
// - spring parameters (Bounce effect)
```

---

## 🔐 Security Checklist

- ✅ User ID validated
- ✅ Results stored per user
- ✅ Firebase rules needed for sync
- ✅ Offline data encrypted by Android
- ✅ No sensitive math operations exposed

---

## 📚 Further Reading

```
Complete Documentation:
  → docs/QUESTION_ENGINE_DOCUMENTATION.md

README with Examples:
  → README_QUESTION_ENGINE.md

Implementation Details:
  → IMPLEMENTATION_SUMMARY.md

Full Source Code:
  → core/generator/QuestionGenerator.kt
  → data/repository/GameRepository.kt
  → feature/battle/BattleViewModel.kt
  → feature/battle/GameScreen.kt

Unit Tests:
  → core/generator/QuestionGeneratorTest.kt
```

---

## ✨ That's All!

You now have everything needed to integrate Math Sprint's question generation engine.

**Happy coding! 🚀**

