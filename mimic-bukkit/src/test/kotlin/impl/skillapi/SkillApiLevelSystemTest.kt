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

import com.sucy.skill.api.enums.ExpSource
import com.sucy.skill.api.player.PlayerClass
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import ru.endlesscode.mimic.level.LevelSystem
import kotlin.test.Test

@Suppress("DEPRECATION")
class SkillApiLevelSystemTest : SkillApiTestBase() {

    // SUT
    private val levelSystem: LevelSystem = SkillApiLevelSystem(player, skillApi)

    private val playerClass: PlayerClass

    init {
        prepareClasses("Primary")
        playerClass = data.mainClass
    }

    @Test
    fun `when decrease level - should set right level`() {
        set(level = 3, exp = 0.0)
        levelSystem.takeLevels(1)

        verify { playerClass.level = 2 }
    }

    @Test
    fun `when increase level - should call giveLevels`() {
        levelSystem.giveLevels(10)
        verify { playerClass.giveLevels(10) }
    }

    @Test
    fun `when get level - should get class level`() {
        set(level = 10)
        playerClass.level shouldBe 10
    }

    @Test
    fun `when set level - should change level of class`() {
        levelSystem.level = 10
        verify { playerClass.level = 10 }
    }

    @Test
    fun `when take exp - should call loseExp with percents`() {
        set(requiredExp = 10)
        levelSystem.takeExp(5.0)

        verify { playerClass.loseExp(0.5) }
    }

    @Test
    fun `when give exp - should call give exp with right exp and special source`() {
        levelSystem.giveExp(50.0)
        verify { playerClass.giveExp(50.0, ExpSource.SPECIAL) }
    }

    @Test
    fun `when get exp - should return exp of class`() {
        set(exp = 42.0)
        levelSystem.exp shouldBe 42.0
    }

    @Test
    fun `when set exp - should set class exp`() {
        set(level = 2)

        levelSystem.exp = 10.0
        verify { playerClass.exp = 10.0 }
    }

    @Test
    fun `when get exp to next level - should return right exp`() {
        set(level = 10, exp = 70.0)
        levelSystem.expToNextLevel shouldBe 30.0
    }

    private fun set(level: Int? = null, exp: Double? = null, requiredExp: Int? = null) {
        if (level != null) every { playerClass.level } returns level
        if (exp != null) every { playerClass.exp } returns exp
        if (requiredExp != null) every { playerClass.requiredExp } returns requiredExp
    }
}
