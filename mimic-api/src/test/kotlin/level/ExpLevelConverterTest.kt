// Copyright (c) 2019 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

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
    private val converter: ExpLevelConverter = DummyExpLevelConverter()

    @ParameterizedTest
    @MethodSource("expLevel")
    fun `when expToLevel - should return right level`(exp: Double, expectedLevel: Double) {
        converter.expToLevel(exp) shouldBe expectedLevel
    }

    @ParameterizedTest
    @MethodSource("expFullLevel")
    fun `when expToFullLevel - should return right level`(exp: Double, fullLevel: Int) {
        converter.expToFullLevel(exp) shouldBe fullLevel
    }

    @ParameterizedTest
    @MethodSource("fullLevelExp")
    fun `when levelToExp - should return right exp`(fullLevel: Int, fullLevelExp: Double) {
        converter.levelToExp(fullLevel) shouldBe fullLevelExp
    }
}
