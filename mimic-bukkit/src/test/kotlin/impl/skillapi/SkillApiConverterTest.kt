// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.skillapi

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.endlesscode.mimic.level.ExpLevelConverter
import java.util.stream.Stream

class SkillApiConverterTest : SkillApiTestBase() {

    // SUT
    private val converter: ExpLevelConverter = SkillApiConverter.getInstance(skillApi)

    @ParameterizedTest
    @MethodSource("expLevel")
    fun testExpToLevel(exp: Double, level: Double) {
        converter.expToLevel(exp) shouldBe level
    }

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun expLevel(): Stream<Arguments> = Stream.of(
            arguments(0.0, 1.0),
            arguments(-1.0, 0.0),
            arguments(100.0, 5.0),
            arguments(140.0, 5.8)
        )
    }
}
