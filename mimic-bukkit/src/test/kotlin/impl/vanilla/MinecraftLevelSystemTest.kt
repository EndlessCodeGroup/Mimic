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

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.test.nearly
import kotlin.test.Test

@Suppress("DEPRECATION")
class MinecraftLevelSystemTest : BukkitTestBase() {

    // SUT
    private val levelSystem: BukkitLevelSystem = MinecraftLevelSystem.Provider().getSystem(player)

    @Test
    fun `when get level - should return player level`() {
        set(level = 10)
        levelSystem.level shouldBe 10
    }

    @Test
    fun `when set level - should set right level to player`() {
        levelSystem.level = 10
        verify { player.level = 10 }
    }

    @Test
    fun `when set negative level - should set zero level to player`() {
        levelSystem.level = -10
        verify { player.level = 0 }
    }

    @Test
    fun `when get exp - should return right player exp`() {
        set(level = 16, exp = 0.5f)
        levelSystem.exp shouldBe 21.0
    }

    @Test
    fun `when set half level exp - should set right exp value`() {
        set(level = 20)
        val expTo21Level = 62.0

        levelSystem.exp = expTo21Level / 2
        verify { player.exp = .5f }
    }

    @Test
    fun `when set negative exp - should set zero exp`() {
        set(level = 1)
        levelSystem.exp = -1.0
        verify { player.exp = 0f }
    }

    @Test
    fun `when get fractional exp - should return player exp`() {
        every { player.exp } returns 0.4f
        levelSystem.fractionalExp shouldBe 0.4.nearly
    }

    @Test
    fun `when set fractional exp - should set player exp`() {
        levelSystem.fractionalExp = 0.97
        verify { player.exp = 0.97f }
    }

    @Test
    fun `when set fractional exp more than one - should set exp to one`() {
        levelSystem.fractionalExp = 1.2
        verify { player.exp = 1f }
    }

    @Test
    fun `when set negative fractional exp - should set zero exp`() {
        levelSystem.fractionalExp = -1.0
        verify { player.exp = 0f }
    }

    @Test
    fun `when get total exp to next level - should return player exp to level`() {
        every { player.expToLevel } returns 7
        levelSystem.totalExpToNextLevel shouldBe 7.0
    }

    private fun set(level: Int? = null, exp: Float? = null) {
        if (level != null) every { player.level } returns level
        if (exp != null) every { player.exp } returns exp
    }
}
