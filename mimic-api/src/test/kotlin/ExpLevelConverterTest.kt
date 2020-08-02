package ru.endlesscode.mimic

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class ExpLevelConverterTest(
    private val exp: Double,
    private val level: Double,
    private val fullLevelExp: Double,
    private val fullLevel: Int
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            data(exp = 0.0, level = 0.0),
            data(exp = 10.0, level = 1.0),
            data(exp = 15.0, level = 1.25),
            data(exp = -10.0, level = 0.0, levelToExp = 0.0),
            data(exp = 105.0, level = 4.1)
        )

        private fun data(
            exp: Double,
            level: Double,
            levelToExp: Double = (exp / EXP_IN_LEVEL).toInt() * EXP_IN_LEVEL,
            fullLevel: Int = level.toInt()
        ): Array<Any> {
            return arrayOf(exp, level, levelToExp, fullLevel)
        }
    }

    // SUT
    private lateinit var converter: ExpLevelConverter

    @BeforeTest
    fun setUp() {
        converter = mockExpLevelConverter()
    }

    @Test
    fun `when expToLevel - should return right level`() {
        // When
        val level = converter.expToLevel(exp)

        // Then
        assertEquals(this.level, level)
    }

    @Test
    fun `when expToFullLevel - should return right level`() {
        // When
        val level = converter.expToFullLevel(exp)

        // Then
        assertEquals(this.fullLevel, level)
    }

    @Test
    fun `when levelToExp - should return right exp`() {
        // When
        val exp = converter.levelToExp(fullLevel)

        // Then
        assertEquals(this.fullLevelExp, exp)
    }
}
