package com.example.mathsprint.feature.battle

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mathsprint.core.navigation.Screen
import com.example.mathsprint.core.theme.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Battle/Game Screen - Main gameplay interface
 */
@Composable
fun BattleScreen(
    navController: NavController,
    chapterId: Int,
    lessonId: Int,
    viewModel: BattleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(chapterId, lessonId) {
        viewModel.loadLesson(chapterId, lessonId)
    }

    // Legacy battle screen
    if (uiState.isFinished) {
        BattleResultScreen(
            score = uiState.score,
            total = uiState.questions.size,
            onContinue = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        )
        return
    }

    if (uiState.questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LimeGreen)
        }
        return
    }

    val question = uiState.questions[uiState.currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            LinearProgressIndicator(
                progress = { (uiState.currentIndex + 1).toFloat() / uiState.questions.size },
                modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape).padding(horizontal = 12.dp),
                color = LimeGreen,
                trackColor = LimeGreenLight
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) { i ->
                    Text(if (i < uiState.lives) "❤️" else "🖤", fontSize = 18.sp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Question ${uiState.currentIndex + 1} / ${uiState.questions.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    question.text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.chunked(2).forEachIndexed { rowIdx, rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowOptions.forEachIndexed { colIdx, option ->
                        val optionIndex = rowIdx * 2 + colIdx
                        val isSelected = uiState.selectedOption == optionIndex
                        val isCorrect = optionIndex == question.correctIndex

                        val bgColor = when {
                            !uiState.isAnswered -> MaterialTheme.colorScheme.surface
                            isCorrect -> LimeGreen.copy(alpha = 0.15f)
                            isSelected && !isCorrect -> CoralRed.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val borderColor = when {
                            !uiState.isAnswered -> MaterialTheme.colorScheme.outlineVariant
                            isCorrect -> LimeGreen
                            isSelected && !isCorrect -> CoralRed
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(bgColor)
                                .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                                .clickable(enabled = !uiState.isAnswered) {
                                    viewModel.selectOption(optionIndex)
                                }
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                option,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    uiState.isAnswered && isCorrect -> LimeGreenDark
                                    uiState.isAnswered && isSelected && !isCorrect -> CoralRed
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = uiState.isAnswered) {
            Button(
                onClick = { viewModel.nextQuestion(chapterId, lessonId, uid) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
            ) {
                Text(
                    if (uiState.currentIndex + 1 >= uiState.questions.size) "See Results" else "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun BattleResultScreen(score: Int, total: Int, onContinue: () -> Unit) {
    val percentage = (score.toFloat() / total * 100).toInt()
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (percentage >= 60) "🎉" else "😅", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            if (percentage >= 60) "Great Job!" else "Keep Practicing!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "$score / $total correct ($percentage%)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(12.dp))
        Text("+${score * 10} XP  •  +${score * 2} 🪙", style = MaterialTheme.typography.bodyLarge, color = LimeGreen)
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

