package com.example.mathsprint.feature.battle

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.mathsprint.domain.model.GameMode
import com.example.mathsprint.domain.model.MathQuestion
import com.example.mathsprint.domain.model.Operation

/**
 * New Game Screen using the Question Generation Engine
 */
@Composable
fun GameScreen(
    userId: String,
    gameMode: GameMode = GameMode.PRACTICE,
    difficulty: Difficulty = Difficulty.EASY,
    operation: Operation = Operation.MIXED,
    questionCount: Int = 10,
    viewModel: BattleViewModel = hiltViewModel(),
    onGameComplete: (score: Int, accuracy: Float) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    // Start game on first composition
    LaunchedEffect(Unit) {
        viewModel.startGame(userId, gameMode, difficulty, operation, questionCount)
    }

    // Use centralized theme colors
    val darkBackground = MathSprintTheme.darkBackground
    val cardBackground = MathSprintTheme.cardBackground
    val accentColor = MathSprintTheme.accentColor
    val textWhite = MathSprintTheme.textWhite
    val textGray = MathSprintTheme.textGray
    val successColor = MathSprintTheme.successColor
    val errorColor = MathSprintTheme.errorColor

    Box(
        Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        when {
            uiState.isLoading -> {
                LoadingGameState(accentColor)
            }

            uiState.error != null -> {
                ErrorGameState(
                    error = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.resetGame() },
                    errorColor = errorColor,
                    accentColor = accentColor,
                    textWhite = textWhite
                )
            }

            uiState.gameComplete -> {
                GameCompletionScreen(
                    score = uiState.finalScore,
                    totalQuestions = uiState.totalQuestions,
                    accuracy = uiState.finalAccuracy,
                    onPlayAgain = { 
                        viewModel.resetGame()
                        onGameComplete(uiState.finalScore, uiState.finalAccuracy)
                    },
                    accentColor = accentColor,
                    darkBackground = darkBackground,
                    cardBackground = cardBackground,
                    textWhite = textWhite,
                    textGray = textGray,
                    successColor = successColor
                )
            }

            uiState.currentQuestion != null -> {
                ActiveGameState(
                    uiState = uiState,
                    viewModel = viewModel,
                    accentColor = accentColor,
                    cardBackground = cardBackground,
                    textWhite = textWhite,
                    textGray = textGray,
                    successColor = successColor,
                    errorColor = errorColor
                )
            }
        }
    }
}


// ============== STATE COMPONENTS ==============

@Composable
fun LoadingGameState(accentColor: Color) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = accentColor)
            Spacer(Modifier.height(16.dp))
            Text("Loading battle...", color = Color.White)
        }
    }
}

@Composable
fun ErrorGameState(
    error: String,
    onRetry: () -> Unit,
    errorColor: Color,
    accentColor: Color,
    textWhite: Color
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Error",
                tint = errorColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                error,
                color = textWhite,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Try Again", color = Color.Black)
            }
        }
    }
}

@Composable
fun ActiveGameState(
    uiState: BattleUiState,
    viewModel: BattleViewModel,
    accentColor: Color,
    cardBackground: Color,
    textWhite: Color,
    textGray: Color,
    successColor: Color,
    errorColor: Color
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .systemBarsPadding()
    ) {
        GameHeader(
            questionIndex = uiState.questionIndex + 1,
            totalQuestions = uiState.totalQuestions,
            correctAnswers = uiState.correctAnswers,
            timeElapsed = viewModel.formatTime(uiState.timeElapsedSeconds),
            accentColor = accentColor,
            textGray = textGray,
            textWhite = textWhite
        )

        Spacer(Modifier.height(24.dp))

        QuestionCard(
            question = uiState.currentQuestion!!,
            cardBackground = cardBackground,
            accentColor = accentColor,
            textWhite = textWhite
        )

        Spacer(Modifier.height(24.dp))

        if (!uiState.showResult) {
            AnswerOptions(
                options = uiState.currentQuestion!!.options,
                selectedAnswer = uiState.selectedAnswer,
                isSubmitted = false,
                correctAnswer = uiState.currentQuestion!!.correctAnswer,
                onSelectAnswer = { viewModel.selectAnswer(it) },
                cardBackground = cardBackground,
                accentColor = accentColor,
                textWhite = textWhite,
                textGray = textGray,
                successColor = successColor,
                errorColor = errorColor
            )

            Spacer(Modifier.height(16.dp))

            // Submit and Skip buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Submit Button
                Button(
                    onClick = { viewModel.submitAnswer() },
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    enabled = uiState.selectedAnswer != null && !uiState.isAnswerSubmitted,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        disabledContainerColor = accentColor.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        "SUBMIT",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Skip Button
                OutlinedButton(
                    onClick = { viewModel.skipQuestion() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, accentColor)
                ) {
                    Text(
                        "SKIP",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Show answer options with correct/wrong feedback
            AnswerOptions(
                options = uiState.currentQuestion!!.options,
                selectedAnswer = uiState.selectedAnswer,
                isSubmitted = true,
                correctAnswer = uiState.currentQuestion!!.correctAnswer,
                onSelectAnswer = { },
                cardBackground = cardBackground,
                accentColor = accentColor,
                textWhite = textWhite,
                textGray = textGray,
                successColor = successColor,
                errorColor = errorColor
            )

            Spacer(Modifier.height(24.dp))

            // Result feedback card
            ResultFeedback(
                isCorrect = uiState.isAnswerCorrect ?: false,
                message = uiState.resultMessage,
                correctAnswer = uiState.currentQuestion!!.correctAnswer,
                userAnswer = uiState.selectedAnswer!!,
                cardBackground = cardBackground,
                successColor = successColor,
                errorColor = errorColor,
                accentColor = accentColor,
                textWhite = textWhite
            )

            Spacer(Modifier.height(24.dp))

            // Next button
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    if (uiState.questionIndex + 1 >= uiState.totalQuestions) "FINISH" else "NEXT",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ============== COMPOSABLE COMPONENTS ==============

@Composable
fun GameHeader(
    questionIndex: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    timeElapsed: String,
    accentColor: Color,
    textGray: Color,
    textWhite: Color
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Question $questionIndex/$totalQuestions",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            LinearProgressIndicator(
                progress = { questionIndex.toFloat() / totalQuestions },
                modifier = Modifier
                    .width(150.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = textGray.copy(alpha = 0.2f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatItem("Score", correctAnswers.toString(), accentColor, textWhite)
            StatItem("Time", timeElapsed, accentColor, textWhite)
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    accentColor: Color,
    textWhite: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFF888888), fontSize = 12.sp)
        Text(value, color = textWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun QuestionCard(
    question: MathQuestion,
    cardBackground: Color,
    accentColor: Color,
    textWhite: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Solve this:",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                question.questionText,
                color = textWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnswerOptions(
    options: List<Int>,
    selectedAnswer: Int?,
    isSubmitted: Boolean,
    correctAnswer: Int,
    onSelectAnswer: (Int) -> Unit,
    cardBackground: Color,
    accentColor: Color,
    textWhite: Color,
    textGray: Color,
    successColor: Color,
    errorColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { option ->
            AnswerButton(
                option = option,
                isSelected = option == selectedAnswer,
                isSubmitted = isSubmitted,
                correctAnswer = correctAnswer,
                onClick = { onSelectAnswer(option) },
                cardBackground = cardBackground,
                accentColor = accentColor,
                textWhite = textWhite,
                textGray = textGray,
                successColor = successColor,
                errorColor = errorColor
            )
        }
    }
}

@Composable
fun AnswerButton(
    option: Int,
    isSelected: Boolean,
    isSubmitted: Boolean,
    correctAnswer: Int,
    onClick: () -> Unit,
    cardBackground: Color,
    accentColor: Color,
    textWhite: Color,
    textGray: Color,
    successColor: Color,
    errorColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected && !isSubmitted) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Determine button color after submission
    val backgroundColor = when {
        !isSubmitted -> {
            // Before submission - normal style
            if (isSelected) accentColor.copy(alpha = 0.15f) else cardBackground
        }
        option == correctAnswer -> {
            // Correct answer - green
            successColor.copy(alpha = 0.2f)
        }
        isSelected && option != correctAnswer -> {
            // Selected wrong answer - red
            errorColor.copy(alpha = 0.2f)
        }
        else -> cardBackground
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            !isSubmitted && isSelected -> accentColor
            isSubmitted && option == correctAnswer -> successColor
            isSubmitted && isSelected && option != correctAnswer -> errorColor
            !isSubmitted -> Color(0xFF333333)
            else -> Color(0xFF333333)
        },
        animationSpec = tween(200)
    )

    Button(
        onClick = { if (!isSubmitted) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        enabled = !isSubmitted
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                option.toString(),
                color = when {
                    isSubmitted && option == correctAnswer -> successColor
                    isSubmitted && isSelected && option != correctAnswer -> errorColor
                    isSelected && !isSubmitted -> accentColor
                    else -> textGray
                },
                fontWeight = if (isSelected || isSubmitted) FontWeight.Bold else FontWeight.Normal,
                fontSize = 18.sp
            )

            // Show checkmark for correct, X for wrong
            if (isSubmitted) {
                if (option == correctAnswer) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Correct",
                        tint = successColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isSelected && option != correctAnswer) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Wrong",
                        tint = errorColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ResultFeedback(
    isCorrect: Boolean,
    message: String,
    correctAnswer: Int,
    userAnswer: Int,
    cardBackground: Color,
    successColor: Color,
    errorColor: Color,
    accentColor: Color,
    textWhite: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isCorrect) 1.1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) successColor.copy(alpha = 0.15f) else errorColor.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            2.dp,
            if (isCorrect) successColor else errorColor
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = if (isCorrect) "Correct" else "Incorrect",
                tint = if (isCorrect) successColor else errorColor,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                message,
                color = textWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            if (!isCorrect) {
                Text(
                    "Correct answer: $correctAnswer",
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun GameCompletionScreen(
    score: Int,
    totalQuestions: Int,
    accuracy: Float,
    onPlayAgain: () -> Unit,
    accentColor: Color,
    darkBackground: Color,
    cardBackground: Color,
    textWhite: Color,
    textGray: Color,
    successColor: Color
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(darkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                "🏆",
                fontSize = 80.sp
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Game Complete!",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Score", color = textGray, fontSize = 14.sp)
                    Text(
                        "$score / $totalQuestions",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { accuracy / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = textGray.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Accuracy: %.1f%%".format(accuracy),
                        color = textWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    "PLAY AGAIN",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

