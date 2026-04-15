package com.example.mathsprint.core.generator

import android.util.Log
import com.example.mathsprint.domain.model.Difficulty
import com.example.mathsprint.domain.model.MathQuestion
import com.example.mathsprint.domain.model.Operation
import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random

/**
 * Core engine for generating random math questions
 * Supports multiple operations, difficulty levels, and game modes
 */
class QuestionGenerator {

    private val sessionCache = mutableSetOf<String>() // Track generated questions in session

    /**
     * Get number range based on difficulty level
     */
    private fun getNumberRange(difficulty: Difficulty): IntRange {
        return when (difficulty) {
            Difficulty.EASY -> 1..10
            Difficulty.MEDIUM -> 10..50
            Difficulty.HARD -> 50..100
            Difficulty.EXPERT -> 100..500
        }
    }

    /**
     * Generate a single question with specified parameters
     */
    fun generateQuestion(
        difficulty: Difficulty,
        operation: Operation,
        random: Random = Random(System.currentTimeMillis()),
        multiplicationTableNumber: Int? = null  // For MULTIPLICATION_TABLE
    ): MathQuestion {
        val range = getNumberRange(difficulty)
        var question: MathQuestion
        var attempts = 0
        val maxAttempts = 100

        // Retry until we get a unique question (not in session cache)
        do {
            question = when (operation) {
                Operation.ADD -> generateAddition(range, random)
                Operation.SUBTRACT -> generateSubtraction(range, random)
                Operation.MULTIPLY -> generateMultiplication(range, difficulty, random)
                Operation.DIVIDE -> generateDivision(range, random)
                Operation.MIXED -> generateMixed(range, difficulty, random)
                Operation.SQUARE_ROOT -> generateSquareRoot(difficulty, random)
                Operation.EXPONENT -> generateExponent(difficulty, random)
                Operation.MULTIPLICATION_TABLE -> generateMultiplicationTable(multiplicationTableNumber ?: 7, difficulty, random)
                Operation.MEMORY_GAME -> generateMemoryGame(difficulty, random)
            }
            attempts++
        } while (sessionCache.contains(question.questionText) && attempts < maxAttempts)

        sessionCache.add(question.questionText)
        return question
    }

    /**
     * Generate a quiz with multiple questions
     */
    fun generateQuiz(
        difficulty: Difficulty,
        operation: Operation,
        count: Int,
        random: Random = Random(System.currentTimeMillis())
    ): List<MathQuestion> {
        sessionCache.clear() // Start fresh for a new quiz
        return (0 until count).map {
            generateQuestion(difficulty, operation, random)
        }
    }

    /**
     * Generate daily challenge questions using a seed for reproducibility
     * Same seed = same questions for all users on same day
     */
    fun generateDailyChallenge(seed: Long = getDayBasedSeed()): List<MathQuestion> {
        val random = Random(seed)
        sessionCache.clear()

        // Daily challenge: 5 questions from EASY to HARD
        val difficulties = listOf(
            Difficulty.EASY,
            Difficulty.EASY,
            Difficulty.MEDIUM,
            Difficulty.HARD,
            Difficulty.EXPERT
        )

        val operations = listOf(
            Operation.ADD,
            Operation.SUBTRACT,
            Operation.MULTIPLY,
            Operation.DIVIDE,
            Operation.MIXED
        )

        return difficulties.zip(operations).map { (diff, op) ->
            generateQuestion(diff, op, random)
        }
    }

    /**
     * Get a seed based on current day for reproducibility
     */
    private fun getDayBasedSeed(): Long {
        return System.currentTimeMillis() / (24 * 60 * 60 * 1000) // Changes once per day
    }

    // ============== OPERATION-SPECIFIC GENERATORS ==============

    /**
     * Generate addition question
     */
    private fun generateAddition(range: IntRange, random: Random): MathQuestion {
        val a = random.nextInt(range.first, range.last + 1)
        val b = random.nextInt(range.first, range.last + 1)
        val correctAnswer = a + b

        val options = generateOptions(correctAnswer, range)

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            questionText = "$a + $b",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = getDifficultyFromRange(range),
            operation = Operation.ADD
        )
    }

    /**
     * Generate subtraction question
     */
    private fun generateSubtraction(range: IntRange, random: Random): MathQuestion {
        val a = random.nextInt(range.first, range.last + 1)
        val b = random.nextInt(range.first, range.last + 1)
        // Ensure result is positive
        val correctAnswer = maxOf(a, b) - minOf(a, b)

        val options = generateOptions(correctAnswer, range)

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            questionText = "${maxOf(a, b)} - ${minOf(a, b)}",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = getDifficultyFromRange(range),
            operation = Operation.SUBTRACT
        )
    }

    /**
     * Generate multiplication question
     */
    private fun generateMultiplication(
        range: IntRange,
        difficulty: Difficulty,
        random: Random
    ): MathQuestion {
        val multiplier = when (difficulty) {
            Difficulty.EASY -> random.nextInt(1, 12)
            Difficulty.MEDIUM -> random.nextInt(1, 20)
            Difficulty.HARD -> random.nextInt(1, 50)
            Difficulty.EXPERT -> random.nextInt(10, 100)
        }

        val multiplicand = random.nextInt(1, when (difficulty) {
            Difficulty.EASY -> 10
            Difficulty.MEDIUM -> 20
            Difficulty.HARD -> 30
            Difficulty.EXPERT -> 50
        })

        val correctAnswer = multiplier * multiplicand
        val options = generateOptions(correctAnswer, range)

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            questionText = "$multiplier × $multiplicand",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = difficulty,
            operation = Operation.MULTIPLY
        )
    }

    /**
     * Generate division question ensuring integer results
     * Strategy: Generate divisor and quotient, then calculate dividend
     */
    private fun generateDivision(range: IntRange, random: Random): MathQuestion {
        // Generate quotient (result)
        val quotient = random.nextInt(1, range.last / 2)

        // Generate divisor (avoid zero)
        val divisor = random.nextInt(1, minOf(20, range.last / quotient + 1))

        // Calculate dividend (ensures integer division)
        val dividend = divisor * quotient
        val correctAnswer = quotient

        val options = generateOptions(correctAnswer, range)

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            questionText = "$dividend ÷ $divisor",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = getDifficultyFromRange(range),
            operation = Operation.DIVIDE
        )
    }

    /**
     * Generate mixed operation question (2-3 operands)
     */
    private fun generateMixed(
        range: IntRange,
        difficulty: Difficulty,
        random: Random
    ): MathQuestion {
        val numOperands = if (difficulty == Difficulty.EXPERT) 3 else 2
        val operations = mutableListOf<Operation>()
        val numbers = mutableListOf<Int>()

        // Generate first number
        numbers.add(random.nextInt(range.first, range.last + 1))

        // Generate operations and remaining numbers
        for (i in 0 until numOperands - 1) {
            val op = listOf(Operation.ADD, Operation.SUBTRACT, Operation.MULTIPLY).random(random)
            operations.add(op)
            numbers.add(random.nextInt(1, range.last / 2 + 1))
        }

        // Calculate result (left to right evaluation)
        var result = numbers[0]
        for (i in 0 until operations.size) {
            result = when (operations[i]) {
                Operation.ADD -> result + numbers[i + 1]
                Operation.SUBTRACT -> result - numbers[i + 1]
                Operation.MULTIPLY -> result * numbers[i + 1]
                else -> result
            }
        }

        val correctAnswer = maxOf(0, result) // Ensure non-negative

        // Build question text
        val questionText = StringBuilder()
        questionText.append(numbers[0])
        for (i in 0 until operations.size) {
            questionText.append(" ")
            questionText.append(when (operations[i]) {
                Operation.ADD -> "+"
                Operation.SUBTRACT -> "-"
                Operation.MULTIPLY -> "×"
                else -> "+"
            })
            questionText.append(" ")
            questionText.append(numbers[i + 1])
        }

        val options = generateOptions(correctAnswer, range)

        return MathQuestion(
            id = UUID.randomUUID().toString(),
            questionText = questionText.toString(),
            correctAnswer = correctAnswer,
            options = options,
            difficulty = difficulty,
            operation = Operation.MIXED,
            numOperands = numOperands
        )
    }

    // ============== DISTRACTOR GENERATION ==============

    /**
     * Generate 4 multiple choice options including the correct answer
     */
    private fun generateOptions(
        correctAnswer: Int,
        range: IntRange,
        random: Random = Random(System.currentTimeMillis())
    ): List<Int> {
        val options = mutableSetOf<Int>()
        options.add(correctAnswer)

        // Generate 3 plausible incorrect answers
        val distractors = generateDistracters(correctAnswer, range, random)
        options.addAll(distractors.take(3))

        // If we don't have enough unique options, add more
        while (options.size < 4) {
            val randomOption = random.nextInt(maxOf(0, correctAnswer - 20), correctAnswer + 20)
            if (randomOption !in options && randomOption >= 0) {
                options.add(randomOption)
            }
        }

        // Shuffle and return as list
        return options.toList().shuffled(random)
    }

    /**
     * Generate plausible distractors (incorrect options)
     * Strategy: Generate numbers close to the correct answer
     */
    private fun generateDistracters(
        correctAnswer: Int,
        range: IntRange,
        random: Random
    ): List<Int> {
        val distractors = mutableSetOf<Int>()

        // Common mistake patterns
        val patterns = listOf(
            correctAnswer + 1,           // Off by one
            correctAnswer - 1,           // Off by one (negative)
            correctAnswer + 2,           // Off by two
            correctAnswer - 2,           // Off by two (negative)
            correctAnswer / 2,           // Halved
            correctAnswer * 2,           // Doubled
            (correctAnswer * 1.1).toInt(), // 10% more
            (correctAnswer * 0.9).toInt()  // 10% less
        )

        for (pattern in patterns) {
            if (pattern != correctAnswer && pattern >= 0 && distractors.size < 3) {
                distractors.add(pattern)
            }
        }

        // Add random distractors if needed
        while (distractors.size < 3) {
            val random = random.nextInt(maxOf(0, correctAnswer - 20), correctAnswer + 20)
            if (random != correctAnswer && random >= 0) {
                distractors.add(random)
            }
        }

        return distractors.toList()
    }

    // ============== UTILITY METHODS ==============

    /**
     * Determine difficulty from number range
     */
    private fun getDifficultyFromRange(range: IntRange): Difficulty {
        return when (range) {
            1..10 -> Difficulty.EASY
            10..50 -> Difficulty.MEDIUM
            50..100 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }

    /**
     * Adjust difficulty based on user accuracy
     */
    fun getAdaptiveDifficulty(userAccuracy: Float, currentDifficulty: Difficulty): Difficulty {
        return when {
            userAccuracy > 85f -> {
                // Increase difficulty
                when (currentDifficulty) {
                    Difficulty.EASY -> Difficulty.MEDIUM
                    Difficulty.MEDIUM -> Difficulty.HARD
                    Difficulty.HARD -> Difficulty.EXPERT
                    Difficulty.EXPERT -> Difficulty.EXPERT
                }
            }
            userAccuracy < 60f -> {
                // Decrease difficulty
                when (currentDifficulty) {
                    Difficulty.EASY -> Difficulty.EASY
                    Difficulty.MEDIUM -> Difficulty.EASY
                    Difficulty.HARD -> Difficulty.MEDIUM
                    Difficulty.EXPERT -> Difficulty.HARD
                }
            }
            else -> currentDifficulty // Maintain
        }
    }

    /**
     * Generate square root question
     */
    private fun generateSquareRoot(difficulty: Difficulty, random: Random): MathQuestion {
        val baseNumber = when (difficulty) {
            Difficulty.EASY -> random.nextInt(2, 6)      // 2-5
            Difficulty.MEDIUM -> random.nextInt(6, 11)   // 6-10
            Difficulty.HARD -> random.nextInt(11, 16)    // 11-15
            Difficulty.EXPERT -> random.nextInt(16, 21)  // 16-20
        }
        
        val squared = baseNumber * baseNumber
        val correctAnswer = baseNumber
        val options = generateOptions(correctAnswer, 1..20, random)
        
        return MathQuestion(
            id = java.util.UUID.randomUUID().toString(),
            questionText = "√$squared = ?",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = difficulty,
            operation = Operation.SQUARE_ROOT
        )
    }

    /**
     * Generate exponent question
     */
    private fun generateExponent(difficulty: Difficulty, random: Random): MathQuestion {
        val base = when (difficulty) {
            Difficulty.EASY -> random.nextInt(2, 5)
            Difficulty.MEDIUM -> random.nextInt(2, 8)
            Difficulty.HARD -> random.nextInt(2, 10)
            Difficulty.EXPERT -> random.nextInt(2, 12)
        }
        
        val exponent = when (difficulty) {
            Difficulty.EASY -> 2
            Difficulty.MEDIUM -> random.nextInt(2, 4)
            Difficulty.HARD -> random.nextInt(2, 5)
            Difficulty.EXPERT -> random.nextInt(2, 6)
        }
        
        val correctAnswer = base.toDouble().pow(exponent.toDouble()).toInt()
        val options = generateOptions(correctAnswer, 1..1000, random)
        
        return MathQuestion(
            id = java.util.UUID.randomUUID().toString(),
            questionText = "$base^$exponent = ?",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = difficulty,
            operation = Operation.EXPONENT
        )
    }

    /**
     * Generate customizable multiplication table question
     */
    private fun generateMultiplicationTable(
        tableNumber: Int,
        difficulty: Difficulty,
        random: Random
    ): MathQuestion {
        val multiplier = when (difficulty) {
            Difficulty.EASY -> random.nextInt(1, 7)
            Difficulty.MEDIUM -> random.nextInt(1, 10)
            Difficulty.HARD -> random.nextInt(1, 13)
            Difficulty.EXPERT -> random.nextInt(1, 20)
        }
        
        val correctAnswer = tableNumber * multiplier
        val options = generateOptions(correctAnswer, 1..200, random)
        
        return MathQuestion(
            id = java.util.UUID.randomUUID().toString(),
            questionText = "$tableNumber × $multiplier = ?",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = difficulty,
            operation = Operation.MULTIPLICATION_TABLE
        )
    }

    /**
     * Generate memory game question (remember the answer from previous)
     */
    private fun generateMemoryGame(difficulty: Difficulty, random: Random): MathQuestion {
        val range = getNumberRange(difficulty)
        val a = random.nextInt(range.first, range.last + 1)
        val b = random.nextInt(range.first, range.last + 1)
        val correctAnswer = a + b
        
        val options = generateOptions(correctAnswer, range, random)
        
        return MathQuestion(
            id = java.util.UUID.randomUUID().toString(),
            questionText = "Remember: $a + $b = ?",
            correctAnswer = correctAnswer,
            options = options,
            difficulty = difficulty,
            operation = Operation.MEMORY_GAME
        )
    }

    /**
     * Clear session cache
     */
    fun clearSessionCache() {
        sessionCache.clear()
    }
}

