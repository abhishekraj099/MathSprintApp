# Math Sprint - Question Generation Engine Documentation

## Overview

The Math Sprint Question Generation Engine is a production-ready, offline-first system for generating random math questions across multiple difficulty levels and operations. It integrates seamlessly with the MVVM architecture using Kotlin, Jetpack Compose, Room, and Firebase.

## Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  BattleScreen / GameScreen (Compose)    │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         ViewModel Layer                 │
│     BattleViewModel                     │
│  - Game flow management                 │
│  - State management                     │
│  - UI interaction handling              │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│        Repository Layer                 │
│    GameRepository                       │
│  - Session management                   │
│  - Result persistence                   │
│  - User stats tracking                  │
└──────────────────┬──────────────────────┘
         ┌─────────┴──────────┐
         │                    │
    ┌────▼────┐        ┌──────▼──────┐
    │  Room   │        │  Question   │
    │Database │        │ Generator   │
    └─────────┘        └─────────────┘
         │
    ┌────▼────────┐
    │  Firebase   │
    │  Firestore  │
    └─────────────┘
```

## Core Components

### 1. QuestionGenerator (`core/generator/QuestionGenerator.kt`)

The heart of the system - generates random math questions with full control over:

**Supported Operations:**
- `ADD`: Basic addition (e.g., "12 + 8")
- `SUBTRACT`: Subtraction with non-negative results
- `MULTIPLY`: Multiplication with operation-appropriate ranges
- `DIVIDE`: Integer division (dividend = divisor × quotient)
- `MIXED`: Multiple operations combined

**Difficulty Levels:**
- `EASY`: Numbers 1-10
- `MEDIUM`: Numbers 10-50
- `HARD`: Numbers 50-100
- `EXPERT`: Numbers 100-500

**Key Methods:**
```kotlin
// Generate single question
fun generateQuestion(
    difficulty: Difficulty,
    operation: Operation
): MathQuestion

// Generate quiz (multiple questions)
fun generateQuiz(
    difficulty: Difficulty,
    operation: Operation,
    count: Int
): List<MathQuestion>

// Generate daily challenge (seeded for reproducibility)
fun generateDailyChallenge(seed: Long = getDayBasedSeed()): List<MathQuestion>

// Get adaptive difficulty based on accuracy
fun getAdaptiveDifficulty(userAccuracy: Float, currentDifficulty: Difficulty): Difficulty
```

### 2. GameRepository (`data/repository/GameRepository.kt`)

Manages all game-related operations:

**Session Management:**
- Create new game sessions
- Track active sessions
- Update session progress
- Complete sessions

**Question Storage:**
- Save generated questions
- Submit answers
- Track question results

**Result Management:**
- Save game results
- Calculate rewards (XP, coins, gems)
- Update user statistics

**Key Methods:**
```kotlin
// Session operations
suspend fun createSession(userId, gameMode, difficulty, operation, totalQuestions)
suspend fun getActiveSession(userId): GameSession?
suspend fun updateSessionProgress(sessionId, questionsAnswered, correctAnswers, timeSpent)
suspend fun completeSession(sessionId)

// Question operations
fun generateQuestion(difficulty, operation): MathQuestion
fun generateQuiz(difficulty, operation, count): List<MathQuestion>
fun generateDailyChallenge(seed): List<MathQuestion>

// Result operations
suspend fun saveGameResult(userId, sessionId, gameMode, difficulty, operation, score, total, accuracy, timeSpent, xpEarned, coinsEarned, gemsEarned)
fun getUserResults(userId, limit): Flow<List<GameResult>>

// Stats operations
suspend fun getUserStats(userId): UserStats?
suspend fun updateStreak(userId)
```

### 3. BattleViewModel (`feature/battle/BattleViewModel.kt`)

Manages game UI state and flow:

**State Management:**
- `isLoading`: Loading state
- `error`: Error messages
- `currentSession`: Active game session
- `currentQuestion`: Current question being answered
- `questionIndex`: Current question position
- `correctAnswers`: Count of correct answers
- `selectedAnswer`: User's selected answer
- `showResult`: Whether to show result feedback
- `gameComplete`: Game completion state

**Key Methods:**
```kotlin
fun startGame(userId, gameMode, difficulty, operation, questionCount)
fun selectAnswer(answer: Int)
fun submitAnswer()
fun nextQuestion()
fun resetGame()
fun formatTime(seconds: Int): String
```

### 4. Database Layer

**Entities:**
- `QuestionEntity`: Stores individual questions
- `GameSessionEntity`: Stores game sessions
- `GameResultEntity`: Stores game results
- `UserStatsEntity`: Stores user statistics
- `ActivityLogEntity`: Tracks daily activity for streaks

**DAOs:**
- `QuestionDao`: CRUD for questions
- `GameSessionDao`: CRUD for sessions
- `GameResultDao`: CRUD for results
- `UserStatsDao`: User stats operations
- `ActivityLogDao`: Activity log operations

## Data Models

### MathQuestion
```kotlin
data class MathQuestion(
    val id: String,                    // Unique ID
    val questionText: String,          // "12 + 8"
    val correctAnswer: Int,            // 20
    val options: List<Int>,            // [20, 21, 19, 22]
    val difficulty: Difficulty,        // EASY, MEDIUM, HARD, EXPERT
    val operation: Operation,          // ADD, SUBTRACT, MULTIPLY, DIVIDE, MIXED
    val timestamp: Long,               // When generated
    val numOperands: Int = 2           // For mixed operations
)
```

### GameSession
```kotlin
data class GameSession(
    val sessionId: String,
    val userId: String,
    val gameMode: GameMode,            // PRACTICE, QUIZ, TIMED, DAILY_CHALLENGE
    val difficulty: Difficulty,
    val operation: Operation,
    val totalQuestions: Int,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val timeSpentSeconds: Int = 0
)
```

### GameResult
```kotlin
data class GameResult(
    val resultId: String,
    val userId: String,
    val sessionId: String,
    val gameMode: GameMode,
    val difficulty: Difficulty,
    val operation: Operation,
    val score: Int,                    // Correct answers
    val totalQuestions: Int,
    val accuracy: Float,               // Percentage
    val timeSpentSeconds: Int,
    val xpEarned: Int,
    val coinsEarned: Int,
    val gemsEarned: Int,
    val createdAt: Long
)
```

## Game Flow

### Starting a Game
```
User clicks "Start Battle"
       ↓
BattleViewModel.startGame() called
       ↓
GameRepository.createSession() - Creates new session
       ↓
QuestionGenerator.generateQuiz() - Generates questions
       ↓
GameRepository.saveSessionQuestions() - Saves to Room DB
       ↓
UI displays first question
```

### Answering a Question
```
User selects answer
       ↓
BattleViewModel.selectAnswer() - Updates selected answer
       ↓
User clicks "Submit"
       ↓
BattleViewModel.submitAnswer()
       ↓
GameRepository.submitAnswer() - Records answer in DB
       ↓
Calculate if correct/incorrect
       ↓
Show result feedback
       ↓
User clicks "Next"
```

### Completing Game
```
Last question answered
       ↓
BattleViewModel.completeGame()
       ↓
Calculate metrics (accuracy, time, rewards)
       ↓
GameRepository.saveGameResult() - Saves result
       ↓
Update user statistics
       ↓
Show completion screen with score/accuracy
```

## Reward Calculation

### XP Earned
```
baseXP = correctAnswers * 10
accuracyBonus = (accuracy / 100) * 50
totalXP = baseXP + accuracyBonus
```

### Coins Earned
```
accuracy >= 90%  → 50 coins
accuracy >= 70%  → 30 coins
accuracy >= 50%  → 15 coins
else            → 5 coins
```

### Gems Earned
```
Perfect score (100%) → 5 gems
else                → 0 gems
```

## Adaptive Difficulty

The system adjusts difficulty based on user accuracy:

```kotlin
Accuracy > 85%   → Increase difficulty
Accuracy 60-85%  → Maintain difficulty
Accuracy < 60%   → Decrease difficulty
```

**Difficulty Progression:**
```
EASY → MEDIUM → HARD → EXPERT → (stays at EXPERT)
EASY ← MEDIUM ← HARD ← EXPERT ← (goes back)
```

## Distractor (Wrong Answer) Generation

The generator creates plausible incorrect options using these strategies:

1. **Off-by-one errors**: ±1 from correct answer
2. **Halving/Doubling**: answer / 2 or answer * 2
3. **Percentage changes**: 90% or 110% of correct answer
4. **Operation mistakes**: e.g., for "a + b", include "a * b"

This ensures wrong answers are plausible but clearly incorrect.

## Division Handling

To ensure integer results:

```kotlin
// Generate quotient (result)
val quotient = random.nextInt(1, rangeMax/2)

// Generate divisor
val divisor = random.nextInt(1, divisorMax)

// Calculate dividend
val dividend = divisor * quotient

// Question: "dividend ÷ divisor = quotient"
```

This guarantees clean integer division without remainders.

## Offline-First Architecture

**Offline Capabilities:**
- ✅ Generate questions without internet
- ✅ Play games completely offline
- ✅ Store results locally in Room DB
- ✅ Track progress without connectivity

**Online Synchronization:**
When user reconnects:
- Upload game results to Firebase
- Update global leaderboards
- Sync user statistics
- Award achievements
- Check for new daily challenges

## Usage Examples

### Start a Practice Game
```kotlin
viewModel.startGame(
    userId = "user123",
    gameMode = GameMode.PRACTICE,
    difficulty = Difficulty.MEDIUM,
    operation = Operation.MULTIPLY,
    questionCount = 20
)
```

### Start Daily Challenge
```kotlin
viewModel.startGame(
    userId = "user123",
    gameMode = GameMode.DAILY_CHALLENGE,
    difficulty = Difficulty.EASY,
    operation = Operation.MIXED,
    questionCount = 5
)
```

### Get User Statistics
```kotlin
val stats = gameRepository.getUserStats("user123")
println("Games Played: ${stats?.totalGamesPlayed}")
println("Average Accuracy: ${stats?.averageAccuracy}%")
println("Current Streak: ${stats?.currentStreak}")
println("Adaptive Difficulty: ${stats?.adaptiveDifficulty}")
```

## Testing

Run unit tests:
```bash
./gradlew test
```

Tests cover:
- ✅ Basic question generation for each operation
- ✅ Difficulty level ranges
- ✅ Correct answer in options
- ✅ Unique options validation
- ✅ Quiz generation
- ✅ Daily challenge reproducibility
- ✅ Calculation correctness
- ✅ Adaptive difficulty logic
- ✅ Integer division constraints

## Performance Optimization

**Session Caching:**
- Questions cached during session to prevent duplicates
- Cleared on session end

**Database Indexing:**
- Indexed queries on userId, sessionId, createdAt
- Optimized for quick lookups

**Memory Management:**
- Lazy initialization of generators
- Proper coroutine scope management
- Auto-cleanup of old records

## Security & Privacy

**Data Protection:**
- User data stored locally and in Firebase with proper rules
- Questions don't contain sensitive information
- Scores/results properly scoped to user

**Offline Storage:**
- No network dependency for local games
- Results synced securely when online

## Future Enhancements

1. **AI Tutoring**: Explain step-by-step solutions
2. **Custom Questions**: User-created question sets
3. **Time-Attack Mode**: Timed challenges with leaderboard
4. **Multiplayer**: Real-time competitive battles
5. **Analytics**: Detailed performance insights
6. **Achievements**: Badges and milestones
7. **Localization**: Multiple languages
8. **Accessibility**: Screen reader support

## Troubleshooting

### Questions not generating
- Check `Difficulty` and `Operation` enums
- Verify `QuestionGenerator` is properly injected
- Check database permissions

### Results not saving
- Verify user ID is correct
- Check Room database is initialized
- Confirm DAO queries are valid

### Offline sync issues
- Enable Firebase Firestore offline persistence
- Check network connectivity status
- Verify Firebase rules allow write operations

## Support

For issues or questions:
1. Check unit tests for usage examples
2. Review GameRepository documentation
3. Check BattleViewModel implementation
4. Verify Room database setup

