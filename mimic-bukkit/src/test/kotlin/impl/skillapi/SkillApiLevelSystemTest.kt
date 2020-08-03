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

package ru.endlesscode.mimic.bukkit.impl.skillapi

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.sucy.skill.api.enums.ExpSource
import com.sucy.skill.api.player.PlayerClass
import ru.endlesscode.mimic.LevelSystem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("DEPRECATION")
class SkillApiLevelSystemTest : SkillApiTestBase() {

    // SUT
    private lateinit var levelSystem: LevelSystem

    private lateinit var playerClass: PlayerClass

    @BeforeTest
    override fun setUp() {
        super.setUp()
        levelSystem = SkillApiLevelSystem(player, skillApi)
    }

    @Test
    fun `when decrease level - should set right level`() {
        // Given
        prepareMainClass()
        whenever(levelSystem.level) doReturn 3

        // When
        levelSystem.takeLevel(1)

        // Then
        verify(playerClass).level = 2
    }

    @Test
    fun `when increase level - should call giveLevels`() {
        // Given
        prepareMainClass()
        val lvlAmount = 10

        // When
        levelSystem.giveLevel(lvlAmount)

        // Then
        verify(playerClass).giveLevels(lvlAmount)
    }

    @Test
    fun `when get level - should get class level`() {
        // Given
        prepareMainClass()

        // When
        levelSystem.level

        // Then
        verify(playerClass).level
    }

    @Test
    fun `when set level - should change level of class`() {
        // Given
        prepareMainClass()
        val newLevel = 10

        // When
        levelSystem.level = newLevel

        // Then
        verify(playerClass).level = newLevel
    }

    @Test
    fun `when take exp - should call loseExp with percents`() {
        // Given
        prepareMainClass()
        whenever(playerClass.requiredExp) doReturn 10

        // When
        levelSystem.takeExp(5.0)

        // Then
        verify(playerClass).loseExp(0.5)
    }

    @Test
    fun `when give exp - should call give exp with right exp and special source`() {
        // Given
        prepareMainClass()
        val expAmount = 50.0

        // When
        levelSystem.giveExp(expAmount)

        // Then
        verify(playerClass).giveExp(expAmount, ExpSource.SPECIAL)
    }

    @Test
    fun `when get exp - should return exp of class`() {
        // Given
        prepareMainClass()

        // When
        levelSystem.exp

        // Then
        verify(playerClass).exp
    }

    @Test
    fun `when set exp - should set class exp`() {
        // Given
        prepareMainClass()
        whenever(playerClass.level) doReturn 2
        val expAmount = 10.0

        // When
        levelSystem.exp = expAmount

        // Then
        verify(playerClass).exp = expAmount
    }

    @Test
    fun `when get exp to next level - should return right exp`() {
        // Given
        prepareMainClass()
        whenever(playerClass.exp) doReturn 70.0
        whenever(playerClass.level) doReturn 10

        // When
        val actual = levelSystem.expToNextLevel

        // Then
        assertEquals(30.0, actual)
    }

    private fun prepareMainClass() {
        prepareClasses("Primary")
        playerClass = data.mainClass
    }
}
