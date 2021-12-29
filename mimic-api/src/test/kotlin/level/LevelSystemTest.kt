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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test

@Suppress("DEPRECATION")
class LevelSystemTest {

    class DummyLevelSystem : LevelSystem {
        override val converter: ExpLevelConverter = DummyExpLevelConverter()
        override var level: Int = 0
        override var exp: Double = 0.0
    }

    // SUT
    private val levelSystem: LevelSystem = DummyLevelSystem()

    @Test
    fun `when increase level - should set right level`() {
        levelSystem.level = 1
        levelSystem.giveLevels(4)
        levelSystem.level shouldBe 5
    }

    @ParameterizedTest
    @CsvSource(
        "4, 2",
        "10, 0",
    )
    fun `test takeLevels`(lvlAmount: Int, expectedLevel: Int) {
        levelSystem.level = 6
        levelSystem.takeLevels(lvlAmount)
        levelSystem.level shouldBe expectedLevel
    }

    @ParameterizedTest
    @CsvSource(
        "5,     true",
        "-1,    true",
        "10,    true",
        "11,    false",
    )
    fun `test didReachLevel when level is 10`(requiredLevel: Int, isReached: Boolean) {
        levelSystem.level = 10
        levelSystem.didReachLevel(requiredLevel) shouldBe isReached
    }

    @ParameterizedTest
    @CsvSource(
        "-1.0,  0.0",
        "0.0,   0.0",
        "5.12,  0.512",
        "11.0,  0.9"
    )
    fun `test exp to fractionalExp`(exp: Double, fractionalExp: Double) {
        levelSystem.exp = exp
        levelSystem.fractionalExp shouldBe fractionalExp
    }

    @ParameterizedTest
    @CsvSource(
        "-1.0,  0.0",
        "0.0,   0.0",
        "0.512, 5.12",
    )
    fun `test fractionalExp to exp`(fractionalExp: Double, exp: Double) {
        levelSystem.fractionalExp = fractionalExp
        levelSystem.exp shouldBe exp
    }

    @ParameterizedTest
    @CsvSource(
        "1.0,   1",
        "42.0,  1",
    )
    fun `test set fractionalExp for full level`(fractionalExp: Double, level: Int) {
        levelSystem.exp = 1.0
        levelSystem.fractionalExp = fractionalExp

        levelSystem.level shouldBe level
        levelSystem.exp shouldBe 0
    }

    @ParameterizedTest
    @CsvSource(
        "0, 0.0,    0.0",
        "2, 12.0,   42.0",
    )
    fun `test get totalExp`(level: Int, exp: Double, totalExp: Double) {
        levelSystem.level = level
        levelSystem.exp = exp

        levelSystem.totalExp shouldBe totalExp
    }

    @ParameterizedTest
    @CsvSource(
        "-10.0, 0,  0.0",
        "0.0,   0,  0.0",
        "42.0,  2,  12.0",
    )
    fun `test set totalExp`(totalExp: Double, level: Int, exp: Double) {
        levelSystem.totalExp = totalExp

        levelSystem.level shouldBe level
        levelSystem.exp shouldBe exp
    }

    @ParameterizedTest
    @CsvSource(
        "15.0,  true",
        "5.0,   true",
        "18.0,  false",
    )
    fun `test hasExpTotal`(requiredExp: Double, has: Boolean) {
        levelSystem.level = 1
        levelSystem.exp = 5.0
        levelSystem.hasExpTotal(requiredExp) shouldBe has
    }

    @ParameterizedTest
    @CsvSource(
        "3.0,    10,     8.0",
        "105.0,  11,     0.0",
        "113.0,  11,     8.0",
    )
    fun `test giveExp`(expAmount: Double, level: Int, exp: Double) {
        levelSystem.level = 10
        levelSystem.exp = 5.0
        levelSystem.giveExp(expAmount)

        levelSystem.level shouldBe level
        levelSystem.exp shouldBe exp
    }

    @ParameterizedTest
    @CsvSource(
        "5.0,       10,     0.0",
        "6.0,       9,      99.0",
        "550.0,     0,      5.0",
        "555.0,     0,      0.0",
        "1000.0,    0,      0.0",
    )
    fun `test takeExp`(expAmount: Double, level: Int, exp: Double) {
        levelSystem.level = 10
        levelSystem.exp = 5.0
        levelSystem.takeExp(expAmount)

        levelSystem.level shouldBe level
        levelSystem.exp shouldBe exp
    }
}
