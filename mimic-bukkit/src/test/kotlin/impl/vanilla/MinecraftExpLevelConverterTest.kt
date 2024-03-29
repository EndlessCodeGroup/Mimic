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

package ru.endlesscode.mimic.impl.vanilla

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.endlesscode.mimic.level.ExpLevelConverter
import java.util.stream.Stream

class MinecraftExpLevelConverterTest {

    // SUT
    private val converter: ExpLevelConverter = MinecraftExpLevelConverter.instance

    @ParameterizedTest
    @MethodSource("levelExp")
    fun testExpToFullLevel(level: Int, exp: Int) {
        converter.expToFullLevel(exp.toDouble()) shouldBe level
    }

    @ParameterizedTest
    @MethodSource("levelExp")
    fun testLevelToExp(level: Int, exp: Int) {
        converter.levelToExp(level) shouldBe exp
    }

    @ParameterizedTest
    @MethodSource("levelExpToNext")
    fun testGetExpToReachNextLevel(level: Int, expToNext: Double) {
        converter.getExpToReachNextLevel(level) shouldBe expToNext
    }

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun levelExp(): Stream<Arguments> = Stream.of(
            arguments(0, 0),
            arguments(1, 7),
            arguments(15, 315),
            arguments(16, 352),
            arguments(30, 1395),
            arguments(31, 1507),
            arguments(39, 2727)
        )

        @JvmStatic
        fun levelExpToNext(): Stream<Arguments> = Stream.of(
            arguments(0, 7.0),
            arguments(1, 9.0),
            arguments(15, 37.0),
            arguments(16, 42.0),
            arguments(30, 112.0),
            arguments(31, 121.0),
            arguments(39, 193.0)
        )
    }
}
