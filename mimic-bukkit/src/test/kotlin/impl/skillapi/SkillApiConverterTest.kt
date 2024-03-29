/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

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
