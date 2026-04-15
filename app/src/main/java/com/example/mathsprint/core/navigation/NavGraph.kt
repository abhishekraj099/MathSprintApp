package com.example.mathsprint.core.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mathsprint.feature.auth.LoginScreen
import com.example.mathsprint.feature.auth.RegisterScreenOtp
import com.example.mathsprint.feature.battle.BattleScreen
import com.example.mathsprint.feature.daily.DailyChallengeScreen
import com.example.mathsprint.feature.guest.GuestQuizScreen
import com.example.mathsprint.feature.home.HomeScreen
import com.example.mathsprint.feature.leaderboard.LeaderboardScreen
import com.example.mathsprint.feature.onboarding.OnboardingScreen
import com.example.mathsprint.feature.profile.ProfileScreen
import com.example.mathsprint.feature.splash.SplashScreen
import com.example.mathsprint.feature.welcome.WelcomeScreen

@Composable
fun MathSprintNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn() + slideInHorizontally { it / 4 } },
        exitTransition = { fadeOut() + slideOutHorizontally { -it / 4 } },
        popEnterTransition = { fadeIn() + slideInHorizontally { -it / 4 } },
        popExitTransition = { fadeOut() + slideOutHorizontally { it / 4 } }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreenOtp(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(navController = navController)
        }
        composable(Screen.DailyChallenge.route) {
            DailyChallengeScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.GuestQuiz.route) {
            GuestQuizScreen(navController = navController)
        }
        composable(
            route = "battle/{chapterId}/{lessonId}",
            arguments = listOf(
                navArgument("chapterId") { type = NavType.IntType },
                navArgument("lessonId") { type = NavType.IntType }
            )
        ) { backStack ->
            val chapterId = backStack.arguments?.getInt("chapterId") ?: 0
            val lessonId = backStack.arguments?.getInt("lessonId") ?: 0
            BattleScreen(
                navController = navController,
                chapterId = chapterId,
                lessonId = lessonId
            )
        }
    }
}

