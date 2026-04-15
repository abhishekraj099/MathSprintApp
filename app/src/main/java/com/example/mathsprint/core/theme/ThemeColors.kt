package com.example.mathsprint.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized theme colors for the entire app
 */
object MathSprintTheme {

    // Background colors
    val darkBackground = Color(0xFF0A0A0A)
    val darkBackgroundAlt = Color(0xFF121212)

    // Card and surface colors
    val cardBackground = Color(0xFF1A1A1A)
    val cardBackgroundAlt = Color(0xFF252525)

    // Text colors
    val textWhite = Color.White
    val textGray = Color(0xFF888888)
    val textGrayLight = Color(0xFFAAAAAA)
    val textDark = Color(0xFF333333)

    // Accent colors
    val accentColor = Color(0xFF00D9FF)        // Cyan
    val accentColorDim = Color(0xFF00D9FF).copy(alpha = 0.15f)
    val accentGlow = Color(0xFF00D9FF).copy(alpha = 0.3f)

    // Status colors
    val successColor = Color(0xFF4CAF50)        // Green
    val successDim = Color(0xFF4CAF50).copy(alpha = 0.2f)
    val errorColor = Color(0xFFFF6B6B)          // Red
    val errorDim = Color(0xFFFF6B6B).copy(alpha = 0.2f)
    val warningColor = Color(0xFFFFB800)        // Gold/Warning
    val warningDim = Color(0xFFFFB800).copy(alpha = 0.15f)

    // Border colors
    val borderColor = Color(0xFF333333)
    val borderColorAlt = Color(0xFF555555)
    val accentBorder = Color(0xFF00D9FF)

    // Highlight colors
    val highlightColor = Color(0xFF00D9FF).copy(alpha = 0.15f)
    val highlightDark = Color(0xFF00D9FF).copy(alpha = 0.08f)

    // Special colors
    val goldColor = Color(0xFFFFD700)
    val purpleColor = Color(0xFF9C27B0)
    val blueColor = Color(0xFF2196F3)
    val tealColor = Color(0xFF009688)

    /**
     * Get color based on correctness
     */
    fun getAnswerColor(isCorrect: Boolean, isDim: Boolean = false): Color {
        return if (isCorrect) {
            if (isDim) successDim else successColor
        } else {
            if (isDim) errorDim else errorColor
        }
    }

    /**
     * Get border color based on state
     */
    fun getBorderColor(
        isSelected: Boolean = false,
        isCorrect: Boolean? = null,
        isSubmitted: Boolean = false
    ): Color {
        return when {
            !isSubmitted && isSelected -> accentColor
            isSubmitted && isCorrect == true -> successColor
            isSubmitted && isCorrect == false -> errorColor
            else -> borderColor
        }
    }
}

