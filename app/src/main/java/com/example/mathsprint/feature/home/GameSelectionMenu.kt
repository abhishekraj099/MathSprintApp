package com.example.mathsprint.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mathsprint.core.theme.MathSprintTheme
import com.example.mathsprint.domain.model.Difficulty
import com.example.mathsprint.domain.model.GameMode
import com.example.mathsprint.domain.model.Operation
import com.example.mathsprint.feature.battle.GameScreen
import com.example.mathsprint.feature.memory.ArithmeticMemoryGameScreen
import com.example.mathsprint.feature.multiplication.MultiplicationTableScreen

/**
 * Game Selection Menu - Shows all available game modes
 * User can select from different game types:
 * 1. Regular Math Game
 * 2. Multiplication Tables
 * 3. Arithmetic Memory
 */
@Composable
fun GameSelectionMenu(
    userId: String,
    navController: NavController,
    onGameSelected: (gameType: String) -> Unit = {}
) {
    var selectedGameMode by remember { mutableStateOf<String?>(null) }

    // If a game mode is selected, show the appropriate screen
    when (selectedGameMode) {
        "REGULAR_GAME" -> {
            GameScreen(
                userId = userId,
                gameMode = GameMode.PRACTICE,
                difficulty = Difficulty.MEDIUM,
                operation = Operation.MIXED,
                questionCount = 10,
                onGameComplete = { score, accuracy ->
                    selectedGameMode = null
                    onGameSelected("REGULAR_GAME")
                }
            )
            return
        }
        "MULTIPLICATION" -> {
            MultiplicationTableScreen(
                userId = userId,
                onGameStart = {
                    selectedGameMode = null
                    onGameSelected("MULTIPLICATION")
                }
            )
            return
        }
        "MEMORY_GAME" -> {
            ArithmeticMemoryGameScreen(
                userId = userId,
                onGameComplete = { score ->
                    selectedGameMode = null
                    onGameSelected("MEMORY_GAME")
                }
            )
            return
        }
    }

    // Show menu
    Column(
        Modifier
            .fillMaxSize()
            .background(MathSprintTheme.darkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            "Choose Your Game",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MathSprintTheme.textWhite,
                fontSize = 32.sp
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Select a game mode to start playing",
            color = MathSprintTheme.textGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(32.dp))

        // Regular Math Game
        GameModeCard(
            title = "Quick Math",
            subtitle = "Mixed Operations",
            description = "Practice addition, subtraction, multiplication, division and more",
            icon = "🎮",
            onClick = { selectedGameMode = "REGULAR_GAME" }
        )

        Spacer(Modifier.height(16.dp))

        // Multiplication Tables
        GameModeCard(
            title = "Multiplication Master",
            subtitle = "Customizable Tables",
            description = "Master any multiplication table from 1x to 20x",
            icon = "📊",
            onClick = { selectedGameMode = "MULTIPLICATION" }
        )

        Spacer(Modifier.height(16.dp))

        // Arithmetic Memory Game
        GameModeCard(
            title = "Memory Challenge",
            subtitle = "Remember & Solve",
            description = "Remember math problems and test your memory skills",
            icon = "🧠",
            onClick = { selectedGameMode = "MEMORY_GAME" }
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MathSprintTheme.cardBackground
            ),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                MathSprintTheme.accentColor
            )
        ) {
            Text(
                "BACK",
                color = MathSprintTheme.accentColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun GameModeCard(
    title: String,
    subtitle: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MathSprintTheme.cardBackground
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MathSprintTheme.borderColor
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        color = MathSprintTheme.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitle,
                        color = MathSprintTheme.textGray,
                        fontSize = 12.sp
                    )
                }
                Text(
                    icon,
                    fontSize = 40.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                description,
                color = MathSprintTheme.textGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TAP TO PLAY",
                    color = MathSprintTheme.accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MathSprintTheme.accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

