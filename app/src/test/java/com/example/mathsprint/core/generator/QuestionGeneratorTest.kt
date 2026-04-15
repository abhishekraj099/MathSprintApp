package com.example.mathsprint.core.generator

import com.example.mathsprint.domain.model.Difficulty
import com.example.mathsprint.domain.model.Operation
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the QuestionGenerator
 */
class QuestionGeneratorTest {

    private lateinit var generator: QuestionGenerator

    @Before
    fun setUp() {
        generator = QuestionGenerator()
    }

    // ============== BASIC GENERATION TESTS ==============

    @Test
    fun testGenerateAdditionQuestion() {
        val question = generator.generateQuestion(Difficulty.EASY, Operation.ADD)
        assertEquals(Operation.ADD, question.operation)
        assertEquals(Difficulty.EASY, question.difficulty)
        assertNotNull(question.options)
        assertEquals(4, question.options.size)
        assertTrue(question.options.contains(question.correctAnswer))
    }

    @Test
    fun testGenerateSubtractionQuestion() {
        val question = generator.generateQuestion(Difficulty.EASY, Operation.SUBTRACT)
        assertEquals(Operation.SUBTRACT, question.operation)
        assertTrue(question.correctAnswer >= 0)
    }

    @Test
    fun testGenerateMultiplicationQuestion() {
        val question = generator.generateQuestion(Difficulty.MEDIUM, Operation.MULTIPLY)
        assertEquals(Operation.MULTIPLY, question.operation)
        assertNotNull(question.correctAnswer)
    }

    @Test
    fun testGenerateDivisionQuestion() {
        val question = generator.generateQuestion(Difficulty.EASY, Operation.DIVIDE)
        assertEquals(Operation.DIVIDE, question.operation)
        assertTrue(question.correctAnswer > 0)
        assertTrue(question.questionText.contains("÷"))
    }

    // ============== DIFFICULTY TESTS ==============

    @Test
    fun testEasyDifficultyRange() {
        repeat(10) {
            val question = generator.generateQuestion(Difficulty.EASY, Operation.ADD)
            val numbers = extractNumbers(question.questionText)
            numbers.forEach { num ->
                assertTrue("Easy difficulty should have numbers 1-10", num in 1..10)
            }
        }
    }

    @Test
    fun testMediumDifficultyRange() {
        repeat(10) {
            val question = generator.generateQuestion(Difficulty.MEDIUM, Operation.ADD)
            val numbers = extractNumbers(question.questionText)
            numbers.forEach { num ->
                assertTrue("Medium difficulty should have numbers 1-50", num in 1..50)
            }
        }
    }

    @Test
    fun testHardDifficultyRange() {
        repeat(10) {
            val question = generator.generateQuestion(Difficulty.HARD, Operation.ADD)
            val numbers = extractNumbers(question.questionText)
            numbers.forEach { num ->
                assertTrue("Hard difficulty should have numbers 1-100", num in 1..100)
            }
        }
    }

    // ============== OPTIONS TESTS ==============

    @Test
    fun testCorrectAnswerInOptions() {
        repeat(20) {
            val question = generator.generateQuestion(Difficulty.EASY, Operation.ADD)
            assertTrue(
                "Correct answer must be in options",
                question.options.contains(question.correctAnswer)
            )
        }
    }

    @Test
    fun testOptionsAreUnique() {
        repeat(20) {
            val question = generator.generateQuestion(Difficulty.MEDIUM, Operation.MULTIPLY)
            assertEquals("Options should be unique", question.options.size, question.options.toSet().size)
        }
    }

    @Test
    fun testOptionsCount() {
        repeat(20) {
            val question = generator.generateQuestion(Difficulty.EASY, Operation.ADD)
            assertEquals("Should have exactly 4 options", 4, question.options.size)
        }
    }

    // ============== QUIZ GENERATION TESTS ==============

    @Test
    fun testGenerateQuiz() {
        val questions = generator.generateQuiz(Difficulty.EASY, Operation.ADD, 10)
        assertEquals(10, questions.size)
        questions.forEach { question ->
            assertEquals(Difficulty.EASY, question.difficulty)
            assertEquals(Operation.ADD, question.operation)
        }
    }

    @Test
    fun testGenerateDailyChallenge() {
        val questions = generator.generateDailyChallenge(1000L)
        assertEquals(5, questions.size)

        // Should have different difficulties
        val difficulties = questions.map { it.difficulty }.toSet()
        assertTrue("Daily challenge should have multiple difficulties", difficulties.size > 1)
    }

    @Test
    fun testDailyChallengeReproducibility() {
        val seed = System.currentTimeMillis()
        val questions1 = generator.generateDailyChallenge(seed)
        val questions2 = generator.generateDailyChallenge(seed)

        assertEquals("Same seed should produce same questions", questions1.size, questions2.size)
        for (i in questions1.indices) {
            assertEquals(
                "Questions should be identical with same seed",
                questions1[i].questionText,
                questions2[i].questionText
            )
        }
    }

    // ============== ACCURACY AND CALCULATION TESTS ==============

    @Test
    fun testAdditionCorrectness() {
        repeat(20) {
            val question = generator.generateQuestion(Difficulty.EASY, Operation.ADD)
            val parts = question.questionText.split(" + ")
            val a = parts[0].toInt()
            val b = parts[1].toInt()
            assertEquals("Addition calculation must be correct", a + b, question.correctAnswer)
        }
    }

    @Test
    fun testMultiplicationCorrectness() {
        repeat(20) {
            val question = generator.generateQuestion(Difficulty.EASY, Operation.MULTIPLY)
            val parts = question.questionText.split(" × ")
            val a = parts[0].toInt()
            val b = parts[1].toInt()
            assertEquals("Multiplication calculation must be correct", a * b, question.correctAnswer)
        }
    }

    @Test
    fun testDivisionIntegerResult() {
        repeat(20) {
            val question = generator.generateQuestion(Difficulty.EASY, Operation.DIVIDE)
            val parts = question.questionText.split(" ÷ ")
            val dividend = parts[0].toInt()
            val divisor = parts[1].toInt()
            assertTrue("Division result should be integer", dividend % divisor == 0)
            assertEquals("Division calculation must be correct", dividend / divisor, question.correctAnswer)
        }
    }

    // ============== ADAPTIVE DIFFICULTY TESTS ==============

    @Test
    fun testAdaptiveDifficultyIncrease() {
        val newDifficulty = generator.getAdaptiveDifficulty(90f, Difficulty.EASY)
        assertEquals("High accuracy should increase difficulty", Difficulty.MEDIUM, newDifficulty)
    }

    @Test
    fun testAdaptiveDifficultyDecrease() {
        val newDifficulty = generator.getAdaptiveDifficulty(50f, Difficulty.HARD)
        assertEquals("Low accuracy should decrease difficulty", Difficulty.MEDIUM, newDifficulty)
    }

    @Test
    fun testAdaptiveDifficultyMaintain() {
        val newDifficulty = generator.getAdaptiveDifficulty(75f, Difficulty.MEDIUM)
        assertEquals("Medium accuracy should maintain difficulty", Difficulty.MEDIUM, newDifficulty)
    }

    // ============== UTILITY FUNCTIONS ==============

    private fun extractNumbers(text: String): List<Int> {
        val numbers = mutableListOf<Int>()
        val regex = """\d+""".toRegex()
        regex.findAll(text).forEach { matchResult ->
            numbers.add(matchResult.value.toInt())
        }
        return numbers
    }
}

