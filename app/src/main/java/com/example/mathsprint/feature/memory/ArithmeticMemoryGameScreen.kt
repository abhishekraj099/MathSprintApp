package com.example.mathsprint.feature.memory

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mathsprint.core.theme.MathSprintTheme
import com.example.mathsprint.domain.model.Difficulty
import kotlinx.coroutines.delay

/**
 * Arithmetic Memory Game - Remember and solve math problems
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArithmeticMemoryGameScreen(
    userId: String,
    viewModel: ArithmeticMemoryViewModel = hiltViewModel(),
    onGameComplete: (score: Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Memory state
    var currentProblem by remember { mutableStateOf("5 + 3") }
    var memorizedAnswer by remember { mutableStateOf(8) }
    var showAnswer by remember { mutableStateOf(false) }
    var roundScore by remember { mutableStateOf(0) }
    var gameEnded by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var gameStarted by remember { mutableStateOf(false) }

    if (!gameStarted) {
        // Difficulty Selection Screen
        DifficultySelectionScreen(
            onStartGame = { difficulty ->
                selectedDifficulty = difficulty
                gameStarted = true
                viewModel.startMemoryGame(userId, difficulty)
            }
        )
        return
    }

    if (gameEnded) {
        // Game Over Screen
        GameOverScreen(
            score = uiState.score,
            streak = uiState.streak,
            level = uiState.level,
            onPlayAgain = {
                gameEnded = false
                gameStarted = false
                roundScore = 0
                viewModel.resetStreak()
            },
            onExit = {
                onGameComplete(uiState.score)
            }
        )
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MathSprintTheme.darkBackground)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Memory Game",
                        color = MathSprintTheme.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Text(
                        "Level: ${uiState.level}",
                        color = MathSprintTheme.textGray,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Score: ${uiState.score}",
                        color = MathSprintTheme.textWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Streak: ${uiState.streak}",
                        color = MathSprintTheme.successColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Problem Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MathSprintTheme.cardBackground
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Remember this:",
                        color = MathSprintTheme.textGray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Problem with animation
                    AnimatedContent(
                        targetState = currentProblem,
                        transitionSpec = {
                            slideInVertically() + fadeIn() with slideOutVertically() + fadeOut()
                        },
                        label = "Problem"
                    ) { problem ->
                        Text(
                            problem,
                            color = MathSprintTheme.textWhite,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Show/Hide Answer Button
                    Button(
                        onClick = { showAnswer = !showAnswer },
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showAnswer)
                                MathSprintTheme.errorColor
                            else
                                MathSprintTheme.accentColor
                        )
                    ) {
                        Text(
                            if (showAnswer) "HIDE" else "SHOW",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Answer (shown or hidden)
                    if (showAnswer) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Answer: $memorizedAnswer",
                            color = MathSprintTheme.successColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Test Area - Generate new problems
            Text(
                "Solve the problems below to verify your memory!",
                color = MathSprintTheme.textGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.incrementStreak()
                        roundScore = uiState.score
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MathSprintTheme.successColor
                    )
                ) {
                    Text(
                        "✓ CORRECT",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        viewModel.resetStreak()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MathSprintTheme.errorColor
                    )
                ) {
                    Text(
                        "✗ WRONG",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { gameEnded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MathSprintTheme.cardBackground
                ),
                border = BorderStroke(2.dp, MathSprintTheme.accentColor)
            ) {
                Text(
                    "END GAME",
                    color = MathSprintTheme.accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DifficultySelectionScreen(
    onStartGame: (Difficulty) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MathSprintTheme.darkBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Arithmetic Memory",
            color = MathSprintTheme.accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Remember the problem and solve it",
            color = MathSprintTheme.textGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(48.dp))

        Text(
            "Select Difficulty",
            color = MathSprintTheme.textWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Difficulty.values().forEach { difficulty ->
            Button(
                onClick = { onStartGame(difficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MathSprintTheme.cardBackground
                ),
                border = BorderStroke(2.dp, MathSprintTheme.accentColor)
            ) {
                Text(
                    difficulty.name,
                    color = MathSprintTheme.accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    streak: Int,
    level: Int,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MathSprintTheme.darkBackground)
            .padding(16.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "🎉",
            fontSize = 64.sp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Game Over!",
            color = MathSprintTheme.accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MathSprintTheme.cardBackground
            )
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Final Score", color = MathSprintTheme.textGray, fontSize = 14.sp)
                Text(
                    score.toString(),
                    color = MathSprintTheme.accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Streak", color = MathSprintTheme.textGray, fontSize = 12.sp)
                        Text(
                            streak.toString(),
                            color = MathSprintTheme.successColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Level", color = MathSprintTheme.textGray, fontSize = 12.sp)
                        Text(
                            level.toString(),
                            color = MathSprintTheme.warningColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MathSprintTheme.accentColor
            )
        ) {
            Text("PLAY AGAIN", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onExit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, MathSprintTheme.accentColor)
        ) {
            Text("EXIT", color = MathSprintTheme.accentColor, fontWeight = FontWeight.Bold)
        }
    }
}

