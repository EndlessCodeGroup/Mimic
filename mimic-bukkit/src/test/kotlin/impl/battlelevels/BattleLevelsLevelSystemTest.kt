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

package ru.endlesscode.mimic.impl.battlelevels

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

class BattleLevelsLevelSystemTest : BukkitTestBase() {

    private val battleLevelsApi: BattleLevelsApiWrapper = mockk(relaxUnitFun = true) {
        // Our formula exp to next level is static = 10
        every { getNeededFor(any()) } answers { arg<Int>(0) * 10.0 }
    }

    // SUT
    private val levelSystem: BattleLevelsLevelSystem = BattleLevelsLevelSystem(player, battleLevelsApi)

    @Test
    fun `when set level lower than current - should remove level`() {
        set(level = 10)
        levelSystem.level = 6

        verify { battleLevelsApi.removeLevel(any(), eq(4)) }
    }

    @Test
    fun `when set level higher than current - should add level`() {
        set(level = 10)
        levelSystem.level = 16

        verify { battleLevelsApi.addLevel(any(), eq(6)) }
    }

    @Test
    fun `when set level equal to current - should not change level`() {
        set(level = 10)
        levelSystem.level = 10

        verify(exactly = 0) { battleLevelsApi.removeLevel(any(), any()) }
        verify(exactly = 0) { battleLevelsApi.addLevel(any(), any()) }
    }

    @Test
    fun `when set total exp lower than current - should remove score`() {
        set(level = 1, totalExp = 100.0)
        levelSystem.totalExp = 60.0

        verify { battleLevelsApi.removeScore(any(), eq(40.0)) }
    }

    @Test
    fun `when set total exp higher than current - should add score`() {
        set(level = 10, totalExp = 100.0)
        levelSystem.totalExp = 160.0

        verify { battleLevelsApi.addLevel(any(), eq(6)) }
    }

    @Test
    fun `when set total exp equal to current - should not change score`() {
        set(totalExp = 100.0)
        levelSystem.totalExp = 100.0

        verify(exactly = 0) { battleLevelsApi.removeScore(any(), any()) }
        verify(exactly = 0) { battleLevelsApi.addScore(any(), any()) }
    }

    @Test
    fun `when give exp more than one level - should add level and give extra exp`() {
        set(level = 1, totalExp = 16.0)
        levelSystem.giveExp(28.0)

        verify { battleLevelsApi.addLevel(any(), eq(3)) }
        verify { battleLevelsApi.addScore(any(), eq(4.0)) }
    }

    @Test
    fun `when give exp equal to one level - should add level`() {
        set(level = 1, totalExp = 16.0)
        levelSystem.giveExp(4.0)

        verify { battleLevelsApi.addLevel(any(), eq(1)) }
        verify(exactly = 0) { battleLevelsApi.addScore(any(), any()) }
    }

    @Test
    fun `when give exp not more than one level - should add exp`() {
        set(level = 1, totalExp = 16.0)
        levelSystem.giveExp(3.0)

        verify { battleLevelsApi.addScore(any(), eq(3.0)) }
        verify(exactly = 0) { battleLevelsApi.addLevel(any(), any()) }
    }

    @Test
    fun `when take exp more than current exp - should take level and give extra exp`() {
        set(level = 10, totalExp = 106.0)
        levelSystem.takeExp(28.0)

        verify { battleLevelsApi.removeLevel(any(), eq(3)) }
        verify { battleLevelsApi.addScore(any(), eq(8.0)) }
        verify(exactly = 0) { battleLevelsApi.removeScore(any(), any()) }
    }

    @Test
    fun `when take exp equal to two levels - should take level and not give extra exp`() {
        set(level = 10, totalExp = 106.0)
        levelSystem.takeExp(26.0)

        verify { battleLevelsApi.removeLevel(any(), eq(2)) }
        verify(exactly = 0) { battleLevelsApi.addScore(any(), any()) }
        verify(exactly = 0) { battleLevelsApi.removeScore(any(), any()) }
    }

    @Test
    fun `when take exp not more than current exp - should take only exp`() {
        set(level = 10, totalExp = 106.0)
        levelSystem.takeExp(6.0)

        verify { battleLevelsApi.removeScore(any(), eq(6.0)) }
        verify(exactly = 0) { battleLevelsApi.removeLevel(any(), any()) }
    }

    private fun set(level: Int? = null, totalExp: Double? = null) {
        if (level != null) every { battleLevelsApi.getLevel(any()) } returns level
        if (totalExp != null) every { battleLevelsApi.getScore(any()) } returns totalExp
    }
}
