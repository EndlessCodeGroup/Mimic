/*
 * This file is part of Mimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
 *
 * Mimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.level

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
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
