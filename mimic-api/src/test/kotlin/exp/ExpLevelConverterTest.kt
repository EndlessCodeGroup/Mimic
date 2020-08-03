package ru.endlesscode.mimic.exp

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.endlesscode.mimic.ExpLevelConverter
import ru.endlesscode.mimic.mockExpLevelConverter
import java.util.stream.Stream
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class ExpLevelConverterTest {

    @Suppress("unused") // Used in MethodSource
    companion object {
        @JvmStatic
        fun expLevel(): Stream<Arguments> = Stream.of(
            arguments(0.0, 0.0),
            arguments(10.0, 1.0),
            arguments(15.0, 1.25),
            arguments(-10.0, 0.0),
            arguments(105.0, 4.1)
        )

        @JvmStatic
        fun expFullLevel(): Stream<Arguments> = Stream.of(
            arguments(0.0, 0),
            arguments(10.0, 1),
            arguments(15.0, 1),
            arguments(-10.0, 0),
            arguments(105.0, 4)
        )

        @JvmStatic
        fun fullLevelExp(): Stream<Arguments> = Stream.of(
            arguments(0, 0.0),
            arguments(1, 10.0),
            arguments(4, 100.0)
        )
    }

    // SUT
    private lateinit var converter: ExpLevelConverter

    @BeforeTest
    fun setUp() {
        converter = mockExpLevelConverter()
    }

    @ParameterizedTest
    @MethodSource("expLevel")
    fun `when expToLevel - should return right level`(exp: Double, expectedLevel: Double) {
        // When
        val level = converter.expToLevel(exp)

        // Then
        assertEquals(expectedLevel, level)
    }

    @ParameterizedTest
    @MethodSource("expFullLevel")
    fun `when expToFullLevel - should return right level`(exp: Double, fullLevel: Int) {
        // When
        val level = converter.expToFullLevel(exp)

        // Then
        assertEquals(fullLevel, level)
    }

    @ParameterizedTest
    @MethodSource("fullLevelExp")
    fun `when levelToExp - should return right exp`(fullLevel: Int, fullLevelExp: Double) {
        // When
        val exp = converter.levelToExp(fullLevel)

        // Then
        assertEquals(fullLevelExp, exp)
    }
}
