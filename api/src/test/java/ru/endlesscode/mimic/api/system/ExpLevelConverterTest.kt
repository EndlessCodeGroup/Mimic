package ru.endlesscode.mimic.api.system

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.endlesscode.mockito.MOCKS_ONLY_ABSTRACTS
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
        private const val EXP_IN_LEVEL = 10.0

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            data(exp = 0.0, level = 0.0),
            data(exp = 10.0, level = 1.0),
            data(exp = 15.0, level = 1.5),
            data(exp = -10.0, level = 0.0, levelToExp = 0.0),
            data(exp = 99.9, level = 9.99)
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
        converter = mock(defaultAnswer = MOCKS_ONLY_ABSTRACTS) {
            on(it.getExpToReachLevel(any())) doReturn EXP_IN_LEVEL
        }
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
