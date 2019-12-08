/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit.system.skillapi

import com.nhaarman.mockitokotlin2.*
import com.sucy.skill.api.classes.RPGClass
import com.sucy.skill.api.player.PlayerClass
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.data.Settings
import ru.endlesscode.mimic.bukkit.BukkitTestBase
import kotlin.test.BeforeTest

open class SkillApiTestBase : BukkitTestBase() {

    protected lateinit var data: PlayerData
    internal lateinit var skillApi: SkillApiWrapper

    @BeforeTest
    override fun setUp() {
        super.setUp()

        data = mock()
        skillApi = mock {
            val settingsMock = mockSettings()
            on { settings } doReturn settingsMock
            on { getPlayerData(player) } doReturn data
        }
    }

    private fun mockSettings(): Settings = mock {
        // Our formula exp to next level is: lvl * 10
        on { getRequiredExp(any()) } doAnswer { it.getArgument<Int>(0) * 10 }
    }

    protected fun prepareClasses(vararg classNames: String) {
        prepareClasses(classNames.toList())
    }

    protected fun prepareClasses(classNames: List<String>) {
        val playerClasses = classNames.map(::mockNamedPlayerClass)
        whenever(data.classes) doReturn playerClasses
        whenever(data.mainClass) doReturn playerClasses.firstOrNull()
    }

    private fun mockNamedPlayerClass(className: String): PlayerClass {
        val rpgClass = mock<RPGClass> {
            on { name } doReturn className
        }

        return mock {
            on { data } doReturn rpgClass
        }
    }
}
