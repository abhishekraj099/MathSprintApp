package com.example.mathsprint.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mathsprint.core.navigation.Screen
import com.example.mathsprint.core.theme.LimeGreen
import com.example.mathsprint.feature.home.components.LearningPathTree
import com.example.mathsprint.feature.leaderboard.LeaderboardScreen
import com.example.mathsprint.feature.daily.DailyChallengeScreen
import com.example.mathsprint.feature.home.components.HeaderSection
import com.example.mathsprint.feature.home.components.StatsCards
import com.example.mathsprint.feature.profile.ProfileScreen

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Update user activity when screen is shown
    LaunchedEffect(Unit) {
        viewModel.updateUserActivity()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> HomeTabContent(navController, uiState)
            1 -> LeaderboardTabContent(navController)
            2 -> DailyChallengeTabContent(navController)
            3 -> ProfileTabContent(navController)
        }

        // Bottom Navigation
        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            listOf(
                Triple(Icons.Default.Home, "Home", 0),
                Triple(Icons.Default.EmojiEvents, "Leaderboard", 1),
                Triple(Icons.Default.Star, "Daily", 2),
                Triple(Icons.Default.Person, "Profile", 3)
            ).forEach { (icon, label, index) ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
private fun HomeTabContent(navController: NavController, uiState: HomeUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(user = uiState.user)
        Spacer(Modifier.height(16.dp))
        StatsCards(user = uiState.user)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val firstChapter = uiState.chapters.firstOrNull { it.isUnlocked }
                if (firstChapter != null) {
                    navController.navigate(Screen.Battle.createRoute(firstChapter.id, 1))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.FlashOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("⚔️ Start Battle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Learning Path",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        LearningPathTree(
            chapters = uiState.chapters,
            onLessonClick = { chapterId, lessonId ->
                navController.navigate(Screen.Battle.createRoute(chapterId, lessonId))
            }
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun LeaderboardTabContent(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        LeaderboardScreen(navController = navController)
    }
}

@Composable
private fun DailyChallengeTabContent(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        DailyChallengeScreen(navController = navController)
    }
}

@Composable
private fun ProfileTabContent(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        ProfileScreen(navController = navController)
    }
}

