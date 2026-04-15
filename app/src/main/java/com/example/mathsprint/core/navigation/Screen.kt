package com.example.mathsprint.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Leaderboard : Screen("leaderboard")
    data object DailyChallenge : Screen("daily_challenge")
    data object Profile : Screen("profile")
    data class Battle(val chapterId: Int, val lessonId: Int) : Screen("battle/$chapterId/$lessonId") {
        companion object {
            fun createRoute(chapterId: Int, lessonId: Int) = "battle/$chapterId/$lessonId"
        }
    }
    data class LearningPath(val chapterId: Int) : Screen("learning_path/$chapterId") {
        companion object {
            fun createRoute(chapterId: Int) = "learning_path/$chapterId"
        }
    }
}

