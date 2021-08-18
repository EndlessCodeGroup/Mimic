/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.test.assertEqualsDoubles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("DEPRECATION")
class MinecraftLevelSystemTest : BukkitTestBase() {

    // SUT
    private lateinit var levelSystem: BukkitLevelSystem

    @BeforeTest
    override fun setUp() {
        super.setUp()
        levelSystem = MinecraftLevelSystem.Provider().getSystem(player)
    }

    @Test
    fun `when get level - should return player level`() {
        // Given
        val expectedLevel = 10
        whenever(player.level) doReturn expectedLevel

        // Then
        assertEquals(expectedLevel, levelSystem.level)
    }

    @Test
    fun `when set level - should set right level to player`() {
        // Given
        val newLevel = 10

        // Then
        levelSystem.level = newLevel

        // Then
        verify(player).level = newLevel
    }

    @Test
    fun `when set negative level - should set zero level to player`() {
        // When
        levelSystem.level = -10

        // Then
        verify(player).level = 0
    }

    @Test
    fun `when get exp - should return right player exp`() {
        // Given
        whenever(player.level) doReturn 16
        whenever(player.exp) doReturn 0.5f

        // When
        val exp = levelSystem.exp

        // Then
        assertEquals(21.0, exp)
    }

    @Test
    fun `when set half level exp - should set right exp value`() {
        // Given
        whenever(player.level) doReturn 20
        val expTo21Level = 62

        // When
        levelSystem.exp = expTo21Level / 2.toDouble()

        // Then
        verify(player).exp = .5f
    }

    @Test
    fun `when set negative exp - should set zero exp`() {
        // When
        levelSystem.exp = -1.0

        // Then
        verify(player).exp = 0f
    }

    @Test
    fun `when get fractional exp - should return player exp`() {
        // Given
        whenever(player.exp) doReturn 0.4f

        // When
        val exp = levelSystem.fractionalExp

        // Then
        assertEqualsDoubles(0.4, exp)
    }

    @Test
    fun `when set fractional exp - should set player exp`() {
        // When
        levelSystem.fractionalExp = 0.97

        // Then
        verify(player).exp = 0.97f
    }

    @Test
    fun `when set fractional exp more than one - should set exp to one`() {
        // When
        levelSystem.fractionalExp = 1.2

        // Then
        verify(player).exp = 1f
    }

    @Test
    fun `when set negative fractional exp - should set zero exp`() {
        // When
        levelSystem.fractionalExp = -1.0

        // Then
        verify(player).exp = 0f
    }

    @Test
    fun `when get total exp to nex level - should return player exp to level`() {
        // Given
        whenever(player.expToLevel) doReturn 7

        // When
        val expToNex = levelSystem.totalExpToNextLevel

        // Then
        assertEquals(7.0, expToNex)
    }
}
