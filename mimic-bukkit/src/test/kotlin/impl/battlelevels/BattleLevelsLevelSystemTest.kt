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

package ru.endlesscode.mimic.bukkit.impl.battlelevels

import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import ru.endlesscode.mimic.bukkit.BukkitTestBase
import kotlin.test.BeforeTest

class BattleLevelsLevelSystemTest : BukkitTestBase() {

    private lateinit var battleLevelsApi: BattleLevelsApiWrapper

    // SUT
    private lateinit var levelSystem: BattleLevelsLevelSystem

    @BeforeTest
    override fun setUp() {
        super.setUp()
        battleLevelsApi = mock {
            // Our formula exp to next level is static = 10
            on { getNeededFor(any()) } doAnswer { it.getArgument<Int>(0) * 10.0 }
        }
        levelSystem = BattleLevelsLevelSystem(player, battleLevelsApi)
    }

    @Test
    fun `when set level lower than current - should remove level`() {
        // Given
        set(level = 10)

        // When
        levelSystem.level = 6

        // Then
        verify(battleLevelsApi).removeLevel(any(), eq(4))
    }

    @Test
    fun `when set level higher than current - should add level`() {
        // Given
        set(level = 10)

        // When
        levelSystem.level = 16

        // Then
        verify(battleLevelsApi).addLevel(any(), eq(6))
    }

    @Test
    fun `when set level equal to current - should not change level`() {
        // Given
        set(level = 10)

        // When
        levelSystem.level = 10

        // Then
        verify(battleLevelsApi, never()).removeLevel(any(), any())
        verify(battleLevelsApi, never()).addLevel(any(), any())
    }

    @Test
    fun `when set total exp lower than current - should remove score`() {
        // Given
        set(totalExp = 100.0)

        // When
        levelSystem.totalExp = 60.0

        // Then
        verify(battleLevelsApi).removeScore(any(), eq(40.0))
    }

    @Test
    fun `when set total exp higher than current - should add score`() {
        // Given
        set(level = 10, totalExp = 100.0)

        // When
        levelSystem.totalExp = 160.0

        // Then
        verify(battleLevelsApi).addLevel(any(), eq(6))
    }

    @Test
    fun `when set total exp equal to current - should not change score`() {
        // Given
        set(totalExp = 100.0)

        // When
        levelSystem.totalExp = 100.0

        // Then
        verify(battleLevelsApi, never()).removeScore(any(), any())
        verify(battleLevelsApi, never()).addScore(any(), any())
    }

    @Test
    fun `when give exp more than one level - should add level and give extra exp`() {
        // Given
        set(level = 1, totalExp = 16.0)

        // When
        levelSystem.giveExp(28.0)

        // Then
        verify(battleLevelsApi).addLevel(any(), eq(3))
        verify(battleLevelsApi).addScore(any(), eq(4.0))
    }

    @Test
    fun `when give exp equal to one level - should add level`() {
        // Given
        set(level = 1, totalExp = 16.0)

        // When
        levelSystem.giveExp(4.0)

        // Then
        verify(battleLevelsApi).addLevel(any(), eq(1))
        verify(battleLevelsApi, never()).addScore(any(), any())
    }

    @Test
    fun `when give exp not more than one level - should add exp`() {
        // Given
        set(level = 1, totalExp = 16.0)

        // When
        levelSystem.giveExp(3.0)

        // Then
        verify(battleLevelsApi).addScore(any(), eq(3.0))
        verify(battleLevelsApi, never()).addLevel(any(), any())
    }

    @Test
    fun `when take exp more than current exp - should take level and give extra exp`() {
        // Given
        set(level = 10, totalExp = 106.0)

        // When
        levelSystem.takeExp(28.0)

        // Then
        verify(battleLevelsApi).removeLevel(any(), eq(3))
        verify(battleLevelsApi).addScore(any(), eq(8.0))
        verify(battleLevelsApi, never()).removeScore(any(), any())
    }

    @Test
    fun `when take exp equal to two levels - should take level and not give extra exp`() {
        // Given
        set(level = 10, totalExp = 106.0)

        // When
        levelSystem.takeExp(26.0)

        // Then
        verify(battleLevelsApi).removeLevel(any(), eq(2))
        verify(battleLevelsApi, never()).addScore(any(), any())
        verify(battleLevelsApi, never()).removeScore(any(), any())
    }

    @Test
    fun `when take exp not more than current exp - should take only exp`() {
        // Given
        set(level = 10, totalExp = 106.0)

        // When
        levelSystem.takeExp(6.0)

        // Then
        verify(battleLevelsApi).removeScore(any(), eq(6.0))
        verify(battleLevelsApi, never()).removeLevel(any(), any())
    }

    private fun set(level: Int? = null, totalExp: Double? = null) {
        if (level != null) whenever(battleLevelsApi.getLevel(any())) doReturn level
        if (totalExp != null) whenever(battleLevelsApi.getScore(any())) doReturn totalExp
    }
}
