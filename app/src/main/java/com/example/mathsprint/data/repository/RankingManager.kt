package com.example.mathsprint.data.repository

import com.example.mathsprint.data.local.entity.UserEntity

/**
 * Manages ranking calculation based on user performance metrics
 * Ranks are calculated using:
 * - XP (experience points)
 * - Win Rate
 * - Streak
 * - Daily performance improvements
 */
object RankingManager {

    /**
     * Calculate user score for ranking
     * Score = (XP * 0.4) + (WinRate * 1000 * 0.3) + (Streak * 50 * 0.2) + (DailyImprovement * 100 * 0.1)
     */
    fun calculateUserScore(
        xp: Int,
        winRate: Float,
        streak: Int,
        dailyImprovement: Int = 0
    ): Int {
        val xpScore = xp * 0.4
        val winRateScore = (winRate * 1000) * 0.3
        val streakScore = (streak * 50) * 0.2
        val dailyScore = (dailyImprovement * 100) * 0.1

        return (xpScore + winRateScore + streakScore + dailyScore).toInt()
    }

    /**
     * Determine rank level based on total XP
     * Tier 1: 0-2500 XP (Number Rookie)
     * Tier 2: 2500-5000 XP (Math Explorer)
     * Tier 3: 5000-7500 XP (Calculation Cadet)
     * Tier 4: 7500-10000 XP (Logic Warrior)
     * Tier 5: 10000-15000 XP (Mathlete)
     * Tier 6: 15000-20000 XP (Quant Champion)
     * Tier 7: 20000-25000 XP (Neural Ninja)
     * Tier 8: 25000-30000 XP (Algebra Ace)
     * Tier 9: 30000-35000 XP (Quantum Master)
     * Tier 10: 35000+ XP (MathSprint Legend)
     */
    fun getRankLevel(xp: Int): Int {
        return when {
            xp < 2500 -> 1
            xp < 5000 -> 2
            xp < 7500 -> 3
            xp < 10000 -> 4
            xp < 15000 -> 5
            xp < 20000 -> 6
            xp < 25000 -> 7
            xp < 30000 -> 8
            xp < 35000 -> 9
            else -> 10
        }
    }

    /**
     * Get rank title based on level
     */
    fun getRankTitle(level: Int): String {
        return when (level) {
            1 -> "Number Rookie"
            2 -> "Math Explorer"
            3 -> "Calculation Cadet"
            4 -> "Logic Warrior"
            5 -> "Mathlete"
            6 -> "Quant Champion"
            7 -> "Neural Ninja"
            8 -> "Algebra Ace"
            9 -> "Quantum Master"
            10 -> "MathSprint Legend"
            else -> "Number Rookie"
        }
    }

    /**
     * Calculate rank position based on user scores
     * Returns the rank position (1st, 2nd, 3rd, etc.)
     */
    fun calculateRankPosition(userScores: List<Pair<String, Int>>): Map<String, Int> {
        return userScores
            .sortedByDescending { it.second }
            .mapIndexed { index, (userId, _) -> userId to (index + 1) }
            .toMap()
    }

    /**
     * Update user win rate
     * Win Rate = (wins / total games played) * 100
     */
    fun updateWinRate(wins: Int, totalGamesPlayed: Int): Float {
        return if (totalGamesPlayed > 0) {
            (wins.toFloat() / totalGamesPlayed) * 100f
        } else {
            0f
        }
    }

    /**
     * Calculate daily performance improvement
     * This tracks how much the user has improved today
     */
    fun calculateDailyImprovement(
        todayScore: Int,
        yesterdayScore: Int
    ): Int {
        return if (todayScore > yesterdayScore) {
            todayScore - yesterdayScore
        } else {
            0
        }
    }
}

