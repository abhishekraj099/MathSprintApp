package com.example.mathsprint.feature.guest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mathsprint.core.navigation.Screen
import com.example.mathsprint.core.theme.LimeGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GuestQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int
)

@Composable
fun GuestQuizScreen(navController: NavController) {
    // Simple mixed quiz questions
    val questions = listOf(
        GuestQuestion(1, "What is 5 + 3?", listOf("8", "9", "7", "6"), 0),
        GuestQuestion(2, "What is 12 ÷ 4?", listOf("4", "3", "2", "5"), 1),
        GuestQuestion(3, "What is 7 × 6?", listOf("42", "40", "44", "48"), 0),
        GuestQuestion(4, "What is 100 - 25?", listOf("75", "70", "80", "85"), 0),
        GuestQuestion(5, "What is the square root of 16?", listOf("4", "3", "5", "2"), 0),
    )

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(listOf<Int?>()) }
    var showResults by remember { mutableStateOf(false) }
    var isAnswered by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    if (showResults) {
        // Results Screen
        val correctCount = selectedAnswers.filterIndexed { index, answer ->
            answer == questions[index].correctAnswer
        }.size
        val accuracy = (correctCount * 100) / questions.size

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Spacer(Modifier.height(40.dp))

                Text(
                    "Quiz Complete! 🎉",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LimeGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            "$correctCount/${questions.size}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = LimeGreen
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "$accuracy% Accuracy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "Create an account to save your progress and compete on the leaderboard!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = false }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                ) {
                    Text("Create Account", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = false }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Login", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
        return
    }

    // Quiz Questions Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1f) / questions.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = LimeGreen,
                trackColor = MaterialTheme.colorScheme.surface
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Question ${currentQuestionIndex + 1}/${questions.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            // Question Text
            Text(
                questions[currentQuestionIndex].question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // Answer Options
            questions[currentQuestionIndex].options.forEachIndexed { index, option ->
                Button(
                    onClick = {
                        val newAnswers = selectedAnswers.toMutableList()
                        newAnswers.add(index)
                        selectedAnswers = newAnswers
                        isAnswered = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAnswered && selectedAnswers.size > currentQuestionIndex && selectedAnswers[currentQuestionIndex] == index) {
                            LimeGreen
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    enabled = !isAnswered || selectedAnswers.size <= currentQuestionIndex
                ) {
                    Text(
                        option,
                        fontWeight = FontWeight.Bold,
                        color = if (isAnswered && selectedAnswers.size > currentQuestionIndex && selectedAnswers[currentQuestionIndex] == index) {
                            Color.Black
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Next Button
            if (isAnswered) {
                Button(
                    onClick = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            isAnswered = false
                        } else {
                            showResults = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                ) {
                    Text(
                        if (currentQuestionIndex < questions.size - 1) "Next" else "Finish",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

