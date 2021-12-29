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

import com.sucy.skill.api.classes.RPGClass
import com.sucy.skill.api.player.PlayerClass
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.data.Settings
import io.mockk.every
import io.mockk.mockk
import ru.endlesscode.mimic.BukkitTestBase

open class SkillApiTestBase : BukkitTestBase() {

    protected val data: PlayerData = mockk()

    internal val skillApi: SkillApiWrapper = mockk {
        every { settings } returns mockSettings()
        every { getPlayerData(player) } returns data
    }

    private fun mockSettings(): Settings = mockk {
        // Our formula exp to next level is: lvl * 10
        every { getRequiredExp(any()) } answers { firstArg<Int>() * 10 }
    }

    protected fun prepareClasses(vararg classNames: String) {
        prepareClasses(classNames.toSet())
    }

    protected fun prepareClasses(classNames: Set<String>) {
        val playerClasses = classNames.map(::mockNamedPlayerClass)
        every { data.classes } returns playerClasses
        every { data.mainClass } returns playerClasses.firstOrNull()
    }

    private fun mockNamedPlayerClass(className: String): PlayerClass {
        val rpgClass = mockk<RPGClass> {
            every { name } returns className
        }

        return mockk(relaxUnitFun = true) {
            every { data } returns rpgClass
        }
    }
}
