package com.example.mathsprint.feature.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathsprint.core.theme.*
import com.example.mathsprint.data.local.entity.ChapterEntity

private val zigzagOffsets = listOf(0.18f, 0.5f, 0.82f, 0.5f, 0.18f, 0.5f, 0.82f, 0.5f)

@Composable
fun LearningPathTree(
    chapters: List<ChapterEntity>,
    onLessonClick: (chapterId: Int, lessonId: Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        chapters.forEachIndexed { chapterIndex, chapter ->
            ChapterSection(
                chapter = chapter,
                chapterIndex = chapterIndex,
                onLessonClick = onLessonClick
            )
        }
    }
}

@Composable
private fun ChapterSection(
    chapter: ChapterEntity,
    chapterIndex: Int,
    onLessonClick: (chapterId: Int, lessonId: Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (chapter.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = if (chapter.isUnlocked) 4.dp else 0.dp
            ) {
                Text(
                    chapter.title,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (chapter.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        val lessonCount = chapter.totalLessons
        repeat(lessonCount) { lessonIndex ->
            val lessonId = lessonIndex + 1
            val xFraction = zigzagOffsets[lessonIndex % zigzagOffsets.size]
            val isCompleted = lessonIndex < chapter.completedLessons
            val isActive = lessonIndex == chapter.completedLessons && chapter.isUnlocked
            val isLocked = !chapter.isUnlocked || lessonIndex > chapter.completedLessons

            LessonNode(
                lessonId = lessonId,
                xFraction = xFraction,
                isCompleted = isCompleted,
                isActive = isActive,
                isLocked = isLocked,
                iconType = chapter.iconType,
                onClick = { if (!isLocked) onLessonClick(chapter.id, lessonId) }
            )

            if (lessonIndex < lessonCount - 1) {
                val nextXFraction = zigzagOffsets[(lessonIndex + 1) % zigzagOffsets.size]
                NodeConnector(fromXFraction = xFraction, toXFraction = nextXFraction, isCompleted = isCompleted)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LessonNode(
    lessonId: Int,
    xFraction: Float,
    isCompleted: Boolean,
    isActive: Boolean,
    isLocked: Boolean,
    iconType: String,
    onClick: () -> Unit
) {
    val nodeColor = when {
        isCompleted -> NodeCompleted
        isActive -> NodeActive
        else -> NodeLocked
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        val offsetX = (maxWidth * xFraction) - 40.dp

        Box(modifier = Modifier.offset(x = offsetX).align(Alignment.CenterStart)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .then(if (isActive) Modifier.shadow(12.dp, CircleShape, ambientColor = LimeGreen, spotColor = LimeGreen) else Modifier)
                        .clip(CircleShape)
                        .background(nodeColor)
                        .clickable(enabled = !isLocked, onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLocked) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                    } else {
                        Text(
                            text = when (iconType) {
                                "book" -> "📖"
                                "chat" -> "💬"
                                "translate" -> "🔤"
                                "star" -> "⭐"
                                else -> "📖"
                            },
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isActive) "Active" else "Lesson $lessonId",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) LimeGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NodeConnector(fromXFraction: Float, toXFraction: Float, isCompleted: Boolean) {
    val lineColor = if (isCompleted) LimeGreenLight else Color(0xFFCFD8DC)
    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
        val fromX = size.width * fromXFraction
        val toX = size.width * toXFraction
        val fromY = 0f
        val toY = size.height

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(fromX, fromY)
            cubicTo(fromX, fromY + toY * 0.5f, toX, toY * 0.5f, toX, toY)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f)
        )
    }
}

