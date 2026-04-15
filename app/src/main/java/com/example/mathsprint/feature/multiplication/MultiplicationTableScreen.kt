package com.example.mathsprint.feature.multiplication

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mathsprint.core.theme.MathSprintTheme
import com.example.mathsprint.domain.model.Difficulty
import com.example.mathsprint.feature.battle.GameScreen

/**
 * Customizable Multiplication Table Selection Screen
 */
@Composable
fun MultiplicationTableScreen(
    userId: String,
    viewModel: MultiplicationTableViewModel = hiltViewModel(),
    onGameStart: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var startGame by remember { mutableStateOf(false) }

    if (startGame) {
        // Launch game with selected multiplication table
        GameScreen(
            userId = userId,
            gameMode = com.example.mathsprint.domain.model.GameMode.PRACTICE,
            difficulty = uiState.selectedDifficulty,
            operation = com.example.mathsprint.domain.model.Operation.MULTIPLICATION_TABLE,
            questionCount = 15,
            onGameComplete = { score, accuracy ->
                startGame = false
                onGameStart()
            }
        )
        return
    }

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
            "Multiplication Tables",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MathSprintTheme.textWhite,
                fontSize = 32.sp
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Select a table to practice",
            color = MathSprintTheme.textGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(32.dp))

        // Difficulty Selection
        Text(
            "Select Difficulty",
            color = MathSprintTheme.textWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Difficulty.values().forEach { difficulty ->
                val isSelected = difficulty == uiState.selectedDifficulty
                Button(
                    onClick = { viewModel.selectDifficulty(difficulty) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected)
                            MathSprintTheme.accentColor
                        else
                            MathSprintTheme.cardBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        difficulty.name,
                        color = if (isSelected) Color.Black else MathSprintTheme.textGray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Table Selection Grid
        Text(
            "Select Table (${uiState.selectedTable}x)",
            color = MathSprintTheme.textWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Grid of multiplication tables
        repeat((uiState.availableTables.size + 4) / 5) { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { col ->
                    val index = row * 5 + col
                    if (index < uiState.availableTables.size) {
                        val table = uiState.availableTables[index]
                        val isSelected = table == uiState.selectedTable

                        TableButton(
                            number = table,
                            isSelected = isSelected,
                            onClick = { viewModel.selectTable(table) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MathSprintTheme.cardBackground
            )
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Preview",
                    color = MathSprintTheme.accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "${uiState.selectedTable} × 1 = ${uiState.selectedTable * 1}",
                    color = MathSprintTheme.textWhite,
                    fontSize = 18.sp
                )

                Text(
                    "${uiState.selectedTable} × 2 = ${uiState.selectedTable * 2}",
                    color = MathSprintTheme.textWhite,
                    fontSize = 18.sp
                )

                Text(
                    "${uiState.selectedTable} × 3 = ${uiState.selectedTable * 3}",
                    color = MathSprintTheme.textWhite,
                    fontSize = 18.sp
                )

                Text(
                    "...",
                    color = MathSprintTheme.textGray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Start Button
        Button(
            onClick = { startGame = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MathSprintTheme.accentColor
            )
        ) {
            Text(
                "START PRACTICE",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun TableButton(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(),
        label = "tableButtonScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MathSprintTheme.accentColor
        else
            MathSprintTheme.cardBackground,
        label = "tableButtonBackground"
    )

    val textColor = if (isSelected) Color.Black else MathSprintTheme.textWhite

    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .border(
                2.dp,
                if (isSelected) MathSprintTheme.accentColor else MathSprintTheme.borderColor,
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$number×",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            Text(
                "Table",
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}

